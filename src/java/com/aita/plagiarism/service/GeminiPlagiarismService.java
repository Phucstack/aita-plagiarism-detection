package com.aita.plagiarism.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service tích hợp Google Gemini API để phân tích ngữ nghĩa và phát hiện đạo văn.
 * Không key hoặc lỗi mạng/quota thì trả fallback gắn nhãn rõ ràng; không bịa điểm AI.
 */
public class GeminiPlagiarismService {

    private static final Logger LOG = Logger.getLogger(GeminiPlagiarismService.class.getName());
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final List<String> FALLBACK_MODELS = List.of("gemini-2.0-flash");
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
                + "Hãy so sánh 2 đoạn mã Java sau xem có sao chép logic, đổi tên biến hay đảo thứ tự hàm không. "
                + "Trả về kết quả JSON gồm: {similarityScore: float, riskLevel: 'SAFE'|'MEDIUM'|'HIGH_RISK', renamedVariables: [string], summary: string}.\n\n"
                + "CODE SINH VIÊN A:\n" + safeA + "\n\n"
                + "CODE SINH VIÊN B:\n" + safeB;

        String jsonPayload = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapeJson(prompt)
        );

        for (String model : FALLBACK_MODELS) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GEMINI_ENDPOINT.replace("gemini-2.0-flash", model) + "?key=" + apiKey))
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
