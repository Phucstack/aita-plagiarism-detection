package com.aita.plagiarism.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service tích hợp Google Gemini API để phân tích ngữ nghĩa và phát hiện đạo văn.
 * Không key hoặc lỗi mạng/quota thì trả fallback gắn nhãn rõ ràng; không bịa điểm AI.
 */
public class GeminiPlagiarismService {

    private static final Logger LOG = Logger.getLogger(GeminiPlagiarismService.class.getName());
    // gemini-2.0-flash đã bị Google khai tử (HTTP 404, khuyến nghị gemini-3.6-flash).
    // Thứ tự thử: model mới nhất trước, lùi dần về bản ổn định cũ hơn nếu tài khoản
    // chưa được cấp quyền. Có thể override hoàn toàn qua GEMINI_MODELS="a,b,c".
    private static final String DEFAULT_MODEL = "gemini-3.6-flash";
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + DEFAULT_MODEL + ":generateContent";
    private static final List<String> FALLBACK_MODELS = fallbackModels();

    private static List<String> fallbackModels() {
        String configured = System.getProperty("GEMINI_MODELS", System.getenv("GEMINI_MODELS"));
        if (configured != null && !configured.isBlank()) {
            List<String> models = new java.util.ArrayList<>();
            for (String m : configured.split(",")) {
                if (!m.isBlank()) models.add(m.trim());
            }
            if (!models.isEmpty()) return List.copyOf(models);
        }
        // Đã kiểm chứng với API key thật (19/09/2026): gemini-3.6-flash, gemini-3-flash-preview,
        // gemini-3.1-flash-lite-preview trả 200; gemini-2.0-flash và gemini-2.5-* đã bị khai tử (404).
        return List.of(DEFAULT_MODEL, "gemini-3-flash-preview", "gemini-3.1-flash-lite-preview");
    }
    private static final ObjectMapper JSON = new ObjectMapper();
    /** Giá trị verdict được chấp nhận trong structured output; giá trị khác coi như malformed. */
    private static final Set<String> VALID_VERDICTS = Set.of("PLAGIARIZED", "SUSPICIOUS", "ORIGINAL");
    private final HttpClient httpClient;
    private final String apiKey;

    public GeminiPlagiarismService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), resolveApiKey());
    }

    GeminiPlagiarismService(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }

    private static String resolveApiKey() {
        String key = System.getProperty("GEMINI_API_KEY", System.getenv("GEMINI_API_KEY"));
        if (key == null) return null;
        key = key.trim().replace("\"", "").replace("'", "");
        return (key.isEmpty() || key.startsWith("your_")) ? null : key;
    }

    /**
     * So sánh 2 đoạn mã nguồn Java và trả về báo cáo JSON phân tích ngữ nghĩa.
     */
    public String analyzeCodeSimilarity(String codeA, String codeB) {
        String safeA = truncate(codeA);
        String safeB = truncate(codeB);
        if (apiKey == null) {
            return fallback("missing_key", "Chưa cấu hình GEMINI_API_KEY. Đây là mẫu dự phòng, không phải kết quả AI.");
        }

        String prompt = "Bạn là chuyên gia phân tích mã nguồn và liêm chính học thuật. "
                + "So sánh 2 đoạn mã Java sau và xác định có sao chép logic hay không, kể cả khi đã qua biến đổi bề mặt. "
                + "Chỉ trả về MỘT đối tượng JSON thuần (không markdown, không ```json, không lời dẫn) đúng cấu trúc:\n"
                + "{\n"
                + "  \"verdict\": \"PLAGIARIZED\" | \"SUSPICIOUS\" | \"ORIGINAL\",\n"
                + "  \"evidence\": [string, ...],          // bằng chứng cụ thể: đoạn/kiểu logic trùng nhau\n"
                + "  \"techniques\": [string, ...],        // kỹ thuật né tránh thực sự quan sát được:\n"
                + "                                      // đổi tên biến nào (nêu tên cũ -> mới), tái cấu trúc khối nào\n"
                + "                                      // (đảo thứ tự hàm, tách/gộp hàm, đổi vòng lặp), dead code được thêm...\n"
                + "                                      // mảng rỗng nếu không phát hiện\n"
                + "  \"confidence\": 0.0-1.0               // độ tự tin của chính bạn vào verdict\n"
                + "}\n"
                + "Tuyệt đối không thêm trường khác, không suy diễn kỹ thuật không thấy trong mã.\n\n"
                + "CODE SINH VIÊN A:\n" + safeA + "\n\n"
                + "CODE SINH VIÊN B:\n" + safeB;

        String jsonPayload = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapeJson(prompt)
        );

        for (String model : FALLBACK_MODELS) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GEMINI_ENDPOINT.replace(DEFAULT_MODEL, model) + "?key=" + apiKey))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body() != null && response.body().contains("candidates")) {
                    return response.body();
                }
                LOG.log(Level.WARNING, "Gemini model {0} returned HTTP {1}", new Object[]{model, response.statusCode()});
                if (response.statusCode() == 400 || response.statusCode() == 401 || response.statusCode() == 403) {
                    return fallback("auth_error", "Gemini từ chối key hoặc quota (HTTP " + response.statusCode() + "). Mẫu dự phòng.");
                }
            } catch (java.net.http.HttpTimeoutException e) {
                LOG.log(Level.WARNING, "Gemini timeout for model " + model, e);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Gemini call failed for model " + model, e);
            }
        }
        return fallback("unavailable", "Không gọi được Gemini (timeout/mạng/quota). Mẫu dự phòng, không lưu làm kết luận AI.");
    }

    public boolean isConfigured() {
        return apiKey != null;
    }

    /**
     * Kết quả phân tích có cấu trúc của Gemini (structured output).
     * Mọi trường đã được validate kiểu dữ liệu; nội dung văn bản vẫn là lời của mô hình,
     * không được dùng để điều khiển luồng nghiệp vụ ngoài việc hiển thị.
     */
    public record GeminiAnalysis(String verdict, List<String> evidence,
                                 List<String> techniques, double confidence) {
    }

    /**
     * Parse phản hồi Gemini thành {@link GeminiAnalysis}.
     *
     * @return kết quả đã validate, hoặc {@code null} nếu phản hồi là fallback gắn nhãn,
     *         JSON malformed, hoặc thiếu/sai kiểu trường — khi đó người gọi giữ nguyên
     *         nhận định cục bộ (rule-based) thay vì lưu dữ liệu lỗi.
     */
    public static GeminiAnalysis parseAnalysis(String apiResponse) {
        if (apiResponse == null || apiResponse.isBlank()) return null;
        if (apiResponse.contains("\"fallback\"")) return null;

        String modelText;
        try {
            JsonNode root = JSON.readTree(apiResponse);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (!textNode.isTextual()) return null;
            modelText = textNode.asText();
        } catch (Exception e) {
            return null;
        }

        // Mô hình đôi khi bọc JSON trong ```json ... ``` — gỡ hàng rào trước khi parse.
        String cleaned = modelText.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-zA-Z]*\\s*", "");
            int fence = cleaned.lastIndexOf("```");
            if (fence > 0) cleaned = cleaned.substring(0, fence);
            cleaned = cleaned.trim();
        }

        try {
            JsonNode node = JSON.readTree(cleaned);
            if (!node.isObject()) return null;
            String verdict = node.path("verdict").asText(null);
            if (verdict == null || !VALID_VERDICTS.contains(verdict)) return null;
            JsonNode confidenceNode = node.path("confidence");
            if (!confidenceNode.isNumber()) return null;
            double confidence = confidenceNode.asDouble();
            if (confidence < 0.0 || confidence > 1.0) return null;
            List<String> evidence = readStringList(node.get("evidence"));
            if (evidence == null) return null;
            List<String> techniques = readStringList(node.get("techniques"));
            if (techniques == null) return null;
            return new GeminiAnalysis(verdict, evidence, techniques, confidence);
        } catch (Exception e) {
            return null;
        }
    }

    /** @return danh sách chuỗi, hoặc {@code null} nếu node thiếu/không phải mảng chuỗi. */
    private static List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) return null;
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isTextual()) return null;
            out.add(item.asText());
        }
        return out;
    }

    /**
     * Render {@link GeminiAnalysis} thành văn bản nhiều dòng để lưu vào ai_analysis_summary.
     * Không tự thêm tiền tố [Gemini] — người gọi chịu trách nhiệm gắn nhãn nguồn.
     */
    public static String renderAnalysis(GeminiAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kết luận: ").append(analysis.verdict())
                .append(String.format(" (độ tự tin của mô hình: %.0f%%)", analysis.confidence() * 100));
        if (!analysis.evidence().isEmpty()) {
            sb.append("\nBằng chứng:");
            for (String ev : analysis.evidence()) {
                sb.append("\n- ").append(ev);
            }
        }
        sb.append("\nKỹ thuật né tránh phát hiện được:");
        if (analysis.techniques().isEmpty()) {
            sb.append(" không ghi nhận kỹ thuật né tránh cụ thể.");
        } else {
            for (String tech : analysis.techniques()) {
                sb.append("\n- ").append(tech);
            }
        }
        return sb.toString();
    }

    /**
     * Trích phần tóm tắt từ phản hồi thô của Gemini.
     *
     * @return nội dung tóm tắt, hoặc {@code null} nếu không có — khi đó người gọi
     *         giữ nguyên nhận định cục bộ đã gắn nhãn thay vì lưu một chuỗi rỗng.
     *         Trích xuất không cần thêm thư viện: kết quả chỉ dùng để hiển thị,
     *         không dùng để điều khiển luồng nghiệp vụ.
     */
    public static String extractSummary(String apiResponse) {
        if (apiResponse == null || apiResponse.isBlank()) return null;
        if (apiResponse.contains("\"fallback\"")) return null;

        String text = firstJsonString(apiResponse, "text");
        String candidate = text != null ? unescape(text) : apiResponse;
        String summary = firstJsonString(candidate, "summary");
        if (summary == null) return null;
        String cleaned = unescape(summary).trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String firstJsonString(String json, String key) {
        String needle = "\"" + key + "\"";
        int at = json.indexOf(needle);
        if (at < 0) return null;
        int colon = json.indexOf(':', at + needle.length());
        if (colon < 0) return null;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length() || json.charAt(i) != '"') return null;
        int start = i + 1;
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '\\') { end += 2; continue; }
            if (c == '"') break;
            end++;
        }
        if (end > json.length()) return null;
        return json.substring(start, end);
    }

    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                sb.append(c);
                continue;
            }
            char n = value.charAt(++i);
            switch (n) {
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'u':
                    if (i + 4 < value.length()) {
                        try {
                            sb.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            sb.append(n);
                        }
                    } else {
                        sb.append(n);
                    }
                    break;
                default: sb.append(n);
            }
        }
        return sb.toString();
    }

    private String truncate(String code) {
        if (code == null) return "";
        return code.length() > 8000 ? code.substring(0, 8000) : code;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Dữ liệu dự phòng gắn nhãn rõ ràng khi chưa cấu hình API Key hoặc lỗi gọi.
     */
    private String fallback(String reason, String note) {
        return "{\n"
                + "  \"fallback\": true,\n"
                + "  \"reason\": \"" + escapeJson(reason) + "\",\n"
                + "  \"note\": \"" + escapeJson(note) + "\",\n"
                + "  \"similarityScore\": 0.0,\n"
                + "  \"riskLevel\": \"SAFE\",\n"
                + "  \"renamedVariables\": [],\n"
                + "  \"summary\": \"" + escapeJson(note) + "\"\n"
                + "}";
    }
}
