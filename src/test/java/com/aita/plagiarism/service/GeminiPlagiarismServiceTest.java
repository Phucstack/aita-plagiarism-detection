package com.aita.plagiarism.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Dịch vụ Phân tích Đạo văn AI (GeminiPlagiarismService)")
public class GeminiPlagiarismServiceTest {

    private GeminiPlagiarismService service;

    @BeforeEach
    void setUp() {
        service = new GeminiPlagiarismService();
    }

    @Test
    @DisplayName("Thiếu API key phải trả fallback gắn nhãn, không bịa điểm HIGH_RISK")
    void testMissingKeyReturnsLabeledFallback() {
        // Ép service chạy ở chế độ không key để test không phụ thuộc việc môi trường
        // có GEMINI_API_KEY thật hay không (CI có thể có key, gây gọi mạng thật).
        GeminiPlagiarismService noKey = new GeminiPlagiarismService(null, null);
        String codeA = "public void processCart() { int total = 0; for (Item i : items) total += i.price; }";
        String codeB = "public void processBasket() { int finalSum = 0; for (Item item : orderItems) finalSum += item.price; }";

        String jsonResult = noKey.analyzeCodeSimilarity(codeA, codeB);

        assertNotNull(jsonResult, "Kết quả phân tích không được null");
        assertTrue(jsonResult.contains("similarityScore"), "JSON phải chứa trường similarityScore");
        assertTrue(jsonResult.contains("riskLevel"), "JSON phải chứa trường riskLevel");
        assertTrue(jsonResult.contains("\"fallback\": true"), "Fallback phải gắn nhãn rõ ràng");
        assertTrue(jsonResult.contains("missing_key"), "Phải ghi rõ lý do thiếu key");
        assertFalse(jsonResult.contains("88.5"), "Fallback không được bịa điểm demo 88.5%");
    }

    @Test
    @DisplayName("Xử lý an toàn khi mã nguồn rỗng hoặc ngắn")
    void testAnalyzeEmptyOrShortCode() {
        // Service không key: không gọi mạng, chỉ kiểm tra hợp đồng fallback ổn định.
        GeminiPlagiarismService noKey = new GeminiPlagiarismService(null, null);
        String result = noKey.analyzeCodeSimilarity("", "");
        assertNotNull(result);
        assertTrue(result.startsWith("{") && result.endsWith("}"));
        assertTrue(result.contains("\"fallback\": true"));
    }

    @Test
    @DisplayName("Trích được tóm tắt từ phản hồi hợp lệ của Gemini")
    void testExtractSummaryFromRealResponse() {
        // JSON mà mô hình trả về (nằm trong phần text).
        String innerJson = "{\n  \"summary\": \"Hai ham giong nhau 90 phan tram\",\n  \"riskLevel\": \"HIGH_RISK\"\n}";
        // Khi nằm trong trường "text" của phản hồi, JSON này đã được escape.
        String escaped = innerJson.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        String apiResponse = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + escaped
                + "\"}],\"role\":\"model\"}],\"modelVersion\":\"gemini-2.0-flash\"}";

        String summary = GeminiPlagiarismService.extractSummary(apiResponse);
        assertNotNull(summary, "Phải trích được tóm tắt từ phản hồi hợp lệ");
        assertTrue(summary.contains("Hai ham giong nhau"), "Nội dung tóm tắt: " + summary);
    }

    @Test
    @DisplayName("Phản hồi fallback hoặc rỗng phải trả null để giữ nguyên nhận định cục bộ")
    void testExtractSummaryReturnsNullForFallbackOrEmpty() {
        assertNull(GeminiPlagiarismService.extractSummary(null));
        assertNull(GeminiPlagiarismService.extractSummary(""));
        assertNull(GeminiPlagiarismService.extractSummary(
                "{\"fallback\": true, \"reason\": \"missing_key\", \"summary\": \"Chưa cấu hình\"}"));
        assertNull(GeminiPlagiarismService.extractSummary("{\"candidates\":[]}"));
    }

    /** Bọc JSON nội dung vào cấu trúc phản hồi Gemini (candidates[0].content.parts[0].text). */
    private static String wrapInApiResponse(String innerJson) {
        String escaped = innerJson.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + escaped
                + "\"}],\"role\":\"model\"}}]}";
    }

    @Test
    @DisplayName("Structured output hợp lệ: parse đúng verdict/evidence/techniques/confidence")
    void testParseAnalysisValidStructuredOutput() {
        String innerJson = "{\n"
                + "  \"verdict\": \"PLAGIARIZED\",\n"
                + "  \"evidence\": [\"Ham tinhTong giong het nhau ve luong xu ly\"],\n"
                + "  \"techniques\": [\"Doi ten bien total -> finalSum\", \"Dao thu tu hai ham\"],\n"
                + "  \"confidence\": 0.92\n"
                + "}";

        GeminiPlagiarismService.GeminiAnalysis analysis = assertDoesNotThrow(
                () -> GeminiPlagiarismService.parseAnalysis(wrapInApiResponse(innerJson)));

        assertNotNull(analysis, "JSON hợp lệ phải parse được");
        assertEquals("PLAGIARIZED", analysis.verdict());
        assertEquals(2, analysis.techniques().size());
        assertTrue(analysis.techniques().get(0).contains("total -> finalSum"));
        assertEquals(1, analysis.evidence().size());
        assertEquals(0.92, analysis.confidence(), 0.0001);

        String rendered = GeminiPlagiarismService.renderAnalysis(analysis);
        assertTrue(rendered.contains("PLAGIARIZED"), "Render phải chứa verdict");
        assertTrue(rendered.contains("total -> finalSum"), "Render phải liệt kê kỹ thuật né tránh");
        assertTrue(rendered.contains("Bằng chứng:"), "Render phải liệt kê bằng chứng");
    }

    @Test
    @DisplayName("Structured output bọc trong ```json fence vẫn parse được")
    void testParseAnalysisWithMarkdownFence() {
        String inner = "```json\n{\"verdict\": \"ORIGINAL\", \"evidence\": [], \"techniques\": [], \"confidence\": 0.8}\n```";
        GeminiPlagiarismService.GeminiAnalysis analysis =
                GeminiPlagiarismService.parseAnalysis(wrapInApiResponse(inner));
        assertNotNull(analysis);
        assertEquals("ORIGINAL", analysis.verdict());
        assertTrue(analysis.evidence().isEmpty());
    }

    @Test
    @DisplayName("JSON malformed hoặc sai schema trả null để fallback về nhận định cục bộ, không ném exception")
    void testParseAnalysisMalformedReturnsNull() {
        // JSON hỏng cú pháp
        assertNull(assertDoesNotThrow(() -> GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("{\"verdict\": \"PLAGIARIZED\", \"evidence\": ["))));
        // Verdict ngoài tập cho phép
        assertNull(GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("{\"verdict\": \"DEFINITELY_COPIED\", \"evidence\": [], \"techniques\": [], \"confidence\": 0.9}")));
        // Thiếu trường techniques
        assertNull(GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("{\"verdict\": \"SUSPICIOUS\", \"evidence\": [], \"confidence\": 0.5}")));
        // confidence vượt khoảng 0-1
        assertNull(GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("{\"verdict\": \"SUSPICIOUS\", \"evidence\": [], \"techniques\": [], \"confidence\": 1.5}")));
        // evidence không phải mảng chuỗi
        assertNull(GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("{\"verdict\": \"SUSPICIOUS\", \"evidence\": [1, 2], \"techniques\": [], \"confidence\": 0.5}")));
        // text không phải JSON
        assertNull(GeminiPlagiarismService.parseAnalysis(
                wrapInApiResponse("Xin loi, toi khong the phan tich doan ma nay.")));
        // Phản hồi fallback gắn nhãn của chính service
        assertNull(GeminiPlagiarismService.parseAnalysis(
                "{\"fallback\": true, \"reason\": \"unavailable\"}"));
        assertNull(GeminiPlagiarismService.parseAnalysis(null));
        assertNull(GeminiPlagiarismService.parseAnalysis("not json at all"));
    }
}
