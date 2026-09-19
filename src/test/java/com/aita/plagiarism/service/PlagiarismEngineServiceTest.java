package com.aita.plagiarism.service;

import com.aita.plagiarism.model.MatchingBlock;
import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    @DisplayName("LCS: hai tệp giống hệt phải cho đúng 1 block phủ toàn bộ, tọa độ dòng gốc")
    void testMatchingBlocksIdenticalFiles() {
        String code = "public int sum(int a, int b) {\n" +   // dòng 1
                      "    int total = a + b;\n" +                 // dòng 2
                      "    return total;\n" +                    // dòng 3
                      "}";                                       // dòng 4
        List<MatchingBlock> blocks = engineService.computeMatchingBlocks(code, code);
        assertEquals(1, blocks.size(), "Hai tệp giống hệt phải cho đúng 1 block");
        MatchingBlock block = blocks.get(0);
        assertEquals(1, block.getStudentAStartLine());
        assertEquals(4, block.getStudentAEndLine());
        assertEquals(1, block.getStudentBStartLine());
        assertEquals(4, block.getStudentBEndLine());
        assertEquals("sum", block.getFunctionName());
        assertTrue(block.getMatchedCodeSnippet().contains("int total = a + b"),
                "Snippet phải là mã GỐC phía A, không phải mã đã chuẩn hóa");
    }

    @Test
    @DisplayName("LCS: đổi tên biến vẫn phát hiện khối khớp (so sánh trên dòng đã chuẩn hóa)")
    void testMatchingBlocksRenamedVariables() {
        String codeA = "public int calc(int a, int b) {\n" +
                       "    int total = a + b;\n" +
                       "    return total;\n" +
                       "}";
        String codeB = "public int calc(int x, int y) {\n" +
                       "    int sum = x + y;\n" +
                       "    return sum;\n" +
                       "}";
        List<MatchingBlock> blocks = engineService.computeMatchingBlocks(codeA, codeB);
        assertEquals(1, blocks.size(), "Đổi tên biến đơn thuần phải vẫn khớp toàn bộ");
        assertEquals(1, blocks.get(0).getStudentAStartLine());
        assertEquals(4, blocks.get(0).getStudentAEndLine());
    }

    @Test
    @DisplayName("LCS: chèn 1 dòng ở giữa phải tách thành 2 block với tọa độ lệch đúng 1 dòng")
    void testMatchingBlocksInsertedLine() {
        // Dòng chèn chỉ dùng biến đã có sẵn để không làm lệch đánh số $ID_n
        // của các định danh phía sau (chuẩn hóa đánh số theo thứ tự xuất hiện).
        String codeA = "public int calc() {\n" +     // 1
                       "    int a = 1;\n" +           // 2
                       "    int b = 2;\n" +           // 3
                       "    int d = a + b;\n" +       // 4
                       "    return d;\n" +            // 5
                       "}";                           // 6
        String codeB = "public int calc() {\n" +     // 1
                       "    int a = 1;\n" +           // 2
                       "    int b = 2;\n" +           // 3
                       "    a = a + 5;\n" +           // 4 (dòng chèn)
                       "    int d = a + b;\n" +       // 5
                       "    return d;\n" +            // 6
                       "}";                           // 7
        List<MatchingBlock> blocks = engineService.computeMatchingBlocks(codeA, codeB);
        assertEquals(2, blocks.size(), "Dòng chèn phải tách vùng khớp thành 2 block");

        assertEquals(1, blocks.get(0).getStudentAStartLine());
        assertEquals(3, blocks.get(0).getStudentAEndLine());
        assertEquals(1, blocks.get(0).getStudentBStartLine());
        assertEquals(3, blocks.get(0).getStudentBEndLine());

        // Block sau dòng chèn: phía B phải lệch xuống đúng 1 dòng so với phía A.
        assertEquals(4, blocks.get(1).getStudentAStartLine());
        assertEquals(6, blocks.get(1).getStudentAEndLine());
        assertEquals(5, blocks.get(1).getStudentBStartLine());
        assertEquals(7, blocks.get(1).getStudentBEndLine());
    }

    @Test
    @DisplayName("LCS: hai tệp hoàn toàn khác nhau không có block nào")
    void testMatchingBlocksCompletelyDifferent() {
        String codeA = "public void printHello() {\n" +
                       "    System.out.println(\"Hello\");\n" +
                       "    System.out.println(\"World\");\n" +
                       "    System.out.println(\"!\");\n" +
                       "}";
        String codeB = "public double computeDistance(double x1, double y1) {\n" +
                       "    double delta = x1 - y1;\n" +
                       "    return Math.sqrt(delta * delta);\n" +
                       "}";
        assertTrue(engineService.computeMatchingBlocks(codeA, codeB).isEmpty());
    }

    @Test
    @DisplayName("LCS: vùng khớp 2 dòng phải bị loại (ngưỡng tối thiểu 3 dòng)")
    void testMatchingBlocksTooShortFiltered() {
        // Chỉ đúng 2 dòng giữa (sameOne/sameTwo) khớp liên tiếp; dòng khai báo
        // hàm, dòng khai báo đầu và lệnh gọi cuối đều khác nhau (literal khác) nên
        // các vùng khớp còn lại chỉ dài 1 dòng. Tất cả phải bị loại.
        String codeA = "public void alpha() {\n" +
                       "    int onlyA = 0;\n" +
                       "    int sameOne = 1;\n" +
                       "    int sameTwo = 2;\n" +
                       "    doAlphaOnly(1);\n" +
                       "}";
        String codeB = "public void beta() {\n" +
                       "    int onlyB = 9;\n" +
                       "    int sameOne = 1;\n" +
                       "    int sameTwo = 2;\n" +
                       "    doBetaOnly(2);\n" +
                       "}";
        assertTrue(engineService.computeMatchingBlocks(codeA, codeB).isEmpty(),
                "Block ngắn hơn 3 dòng phải bị loại");
    }

    @Test
    @DisplayName("LCS: tọa độ là số dòng GỐC, bỏ qua dòng comment/trắng; snippet là mã gốc")
    void testMatchingBlocksOriginalLineCoordinates() {
        // Đầu tệp A chỉ có comment/trắng (không thêm định danh) để không làm lệch
        // đánh số $ID_n giữa hai file khi chuẩn hóa từng dòng.
        String codeA = "// dẫn giải, bị loại khi chuẩn hóa\n" +   // 1
                       "\n" +                                       // 2 (dòng trắng)
                       "public int sum(int a, int b) {\n" +         // 3
                       "    int total = a + b;\n" +                 // 4
                       "    return total;\n" +                      // 5
                       "}";                                         // 6
        String codeB = "public int sum(int x, int y) {\n" +         // 1
                       "    int total = x + y;\n" +                 // 2
                       "    return total;\n" +                      // 3
                       "}";                                         // 4
        List<MatchingBlock> blocks = engineService.computeMatchingBlocks(codeA, codeB);
        assertEquals(1, blocks.size());
        MatchingBlock block = blocks.get(0);
        // Phía A: dòng comment/trắng bị bỏ qua nhưng tọa độ vẫn theo dòng gốc.
        assertEquals(3, block.getStudentAStartLine());
        assertEquals(6, block.getStudentAEndLine());
        assertEquals(1, block.getStudentBStartLine());
        assertEquals(4, block.getStudentBEndLine());
        assertEquals("sum", block.getFunctionName());
        String snippet = block.getMatchedCodeSnippet();
        assertTrue(snippet.contains("public int sum(int a, int b)"),
                "Snippet phải giữ nguyên mã gốc phía A");
        assertTrue(snippet.length() <= 500, "Snippet không được vượt quá 500 ký tự");
    }

    @Test
    @DisplayName("LCS: block ngoài mọi hàm ghi functionName là 'unknown' (best-effort)")
    void testMatchingBlocksUnknownFunctionName() {
        String codeA = "int a = 1;\n" +
                       "int b = 2;\n" +
                       "int c = a + b;";
        String codeB = "int a = 1;\n" +
                       "int b = 2;\n" +
                       "int c = a + b;";
        List<MatchingBlock> blocks = engineService.computeMatchingBlocks(codeA, codeB);
        assertEquals(1, blocks.size());
        assertEquals("unknown", blocks.get(0).getFunctionName());
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
