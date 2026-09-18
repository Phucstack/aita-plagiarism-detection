package com.aita.plagiarism.service;

import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Lõi Đối soát Mã nguồn (PlagiarismEngineService)")
public class PlagiarismEngineServiceTest {

    private PlagiarismEngineService engineService;

    /** Actor ADMIN dùng để tạo/xóa bài tập tạm trong kiểm thử. */
    private static User admin() {
        User u = new User();
        u.setUserId(1);
        u.setUsername("admin");
        u.setFullName("Quản Trị Viên");
        u.setRole("ADMIN");
        return u;
    }

    @BeforeEach
    void setUp() {
        engineService = new PlagiarismEngineService();
    }

    @Test
    @DisplayName("Chuẩn hóa mã nguồn Java loại bỏ comments và thay thế định danh")
    void testNormalizeJavaCode() {
        String code = "// This is a comment\n" +
                      "public int calculateSum(int a, int b) {\n" +
                      "    /* block comment */\n" +
                      "    return a + b;\n" +
                      "}";
        String normalized = engineService.normalizeJavaCode(code);
        assertNotNull(normalized);
        assertFalse(normalized.contains("comment"));
        assertTrue(normalized.contains("public"));
        assertTrue(normalized.contains("int"));
        assertTrue(normalized.contains("return"));
        assertTrue(normalized.contains("$ID_"));
    }

    @Test
    @DisplayName("Hai đoạn mã giống hệt nhau phải có độ tương đồng 100%")
    void testIdenticalCodeSimilarity() {
        String code = "public class A { public int getVal() { return 42; } }";
        double sim = engineService.calculateOverallSimilarity(code, code);
        assertEquals(100.0, sim, 0.5, "Mã nguồn giống hệt nhau phải đạt xấp xỉ 100%");
    }

    @Test
    @DisplayName("Hai đoạn mã chỉ đổi tên biến phải bị phát hiện tương đồng rất cao (> 85%)")
    void testRenamedVariablesSimilarity() {
        String codeA = "public int process(int totalAmount) {\n" +
                       "    int taxRate = 10;\n" +
                       "    return totalAmount * taxRate / 100;\n" +
                       "}";
        String codeB = "public int process(int val) {\n" +
                       "    int rate = 10;\n" +
                       "    return val * rate / 100;\n" +
                       "}";
        double sim = engineService.calculateOverallSimilarity(codeA, codeB);
        assertTrue(sim >= 85.0, "Đổi tên biến đơn thuần phải bị hệ thống phát hiện độ tương đồng cao (Actual: " + sim + "%)");
    }

    @Test
    @DisplayName("Hai đoạn mã hoàn toàn khác nhau phải có độ tương đồng thấp (< 30%)")
    void testDifferentCodeSimilarity() {
        String codeA = "public void printHello() { System.out.println(\"Hello World\"); }";
        String codeB = "public double computeDistance(double x1, double y1, double x2, double y2) {\n" +
                       "    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));\n" +
                       "}";
        double sim = engineService.calculateOverallSimilarity(codeA, codeB);
        assertTrue(sim < 35.0, "Đoạn mã khác nhau về logic phải có độ tương đồng thấp (Actual: " + sim + "%)");
    }

    @Test
    @DisplayName("Kiểm thử quét toàn bộ bài tập (scanAssignment)")
    void testScanAssignment() {
        var assignments = new com.aita.plagiarism.dao.AssignmentDAO();
        var assignment = new com.aita.plagiarism.model.Assignment();
        assignment.setCourseId(1);
        assignment.setTitle("Isolated empty scan");
        assignment.setDeadline(new java.sql.Timestamp(System.currentTimeMillis() + 86400000L));
        assignment.setMaxScore(100);
        assignment.setSimilarityThreshold(75);
        int id = assignments.createAssignment(assignment, admin());
        try {
            assertEquals(0, engineService.scanAssignment(id).getReportsCreated());
            assertEquals(0, engineService.scanAssignment(id).getSkippedPairs());
            assertTrue(new com.aita.plagiarism.dao.PlagiarismDAO().getReportsByAssignment(id).isEmpty());
        } finally {
            assertTrue(assignments.deleteAssignment(id, admin()));
        }
    }
}
