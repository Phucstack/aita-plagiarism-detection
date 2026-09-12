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
    @DisplayName("Phân tích tương đồng mã nguồn trả về JSON có cấu trúc chuẩn")
    void testAnalyzeCodeSimilarityResponseFormat() {
        String codeA = "public void processCart() { int total = 0; for (Item i : items) total += i.price; }";
        String codeB = "public void processBasket() { int finalSum = 0; for (Item item : orderItems) finalSum += item.price; }";

        String jsonResult = service.analyzeCodeSimilarity(codeA, codeB);

        assertNotNull(jsonResult, "Kết quả phân tích không được null");
        assertTrue(jsonResult.contains("similarityScore"), "JSON phải chứa trường similarityScore");
        assertTrue(jsonResult.contains("riskLevel"), "JSON phải chứa trường riskLevel");
        assertTrue(jsonResult.contains("renamedVariables"), "JSON phải phân tích đổi tên biến");
        assertTrue(jsonResult.contains("summary"), "JSON phải có phần tóm tắt nhận định");
    }

    @Test
    @DisplayName("Xử lý an toàn khi mã nguồn rỗng hoặc ngắn")
    void testAnalyzeEmptyOrShortCode() {
        String result = service.analyzeCodeSimilarity("", "");
        assertNotNull(result);
        assertTrue(result.startsWith("{") && result.endsWith("}"));
    }
}
