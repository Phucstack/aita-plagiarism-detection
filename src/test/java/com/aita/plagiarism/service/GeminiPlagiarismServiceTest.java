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
        String codeA = "public void processCart() { int total = 0; for (Item i : items) total += i.price; }";
        String codeB = "public void processBasket() { int finalSum = 0; for (Item item : orderItems) finalSum += item.price; }";

        String jsonResult = service.analyzeCodeSimilarity(codeA, codeB);

        assertNotNull(jsonResult, "Kết quả phân tích không được null");
        assertTrue(jsonResult.contains("similarityScore"), "JSON phải chứa trường similarityScore");
        assertTrue(jsonResult.contains("riskLevel"), "JSON phải chứa trường riskLevel");
        assertTrue(jsonResult.contains("renamedVariables"), "JSON phải phân tích đổi tên biến");
        assertTrue(jsonResult.contains("summary"), "JSON phải có phần tóm tắt nhận định");
        if (!service.isConfigured()) {
            assertTrue(jsonResult.contains("\"fallback\": true"), "Fallback phải gắn nhãn rõ ràng");
            assertFalse(jsonResult.contains("88.5"), "Fallback không được bịa điểm demo 88.5%");
        }
    }

    @Test
    @DisplayName("Xử lý an toàn khi mã nguồn rỗng hoặc ngắn")
    void testAnalyzeEmptyOrShortCode() {
        String result = service.analyzeCodeSimilarity("", "");
        assertNotNull(result);
        assertTrue(result.startsWith("{") && result.endsWith("}"));
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
}
