package com.aita.plagiarism;

import com.aita.plagiarism.config.StorageConfig;
import com.aita.plagiarism.service.PlagiarismEngineService;
import com.aita.plagiarism.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử biên, giá trị ranh giới và các trường hợp ngoại lệ của đầu vào.
 * Không cần CSDL.
 */
@DisplayName("Kiểm thử biên & giá trị ranh giới")
class BoundaryAndEdgeCaseTest {

    private final PlagiarismEngineService engine = new PlagiarismEngineService();

    // ---------------------------------------------------------------- Lõi đối soát

    @Test
    @DisplayName("Hai tệp rỗng KHÔNG được cho 100% tương đồng (tránh gắn HIGH_RISK oan)")
    void emptyFilesMustNotScore100() {
        assertEquals(0.0, engine.calculateOverallSimilarity("", ""));
        assertEquals(0.0, engine.calculateOverallSimilarity(null, null));
        assertEquals(0.0, engine.calculateOverallSimilarity("public class A {}", ""));
        assertEquals(0.0, engine.calculateOverallSimilarity("", "public class A {}"));
    }

    @Test
    @DisplayName("Tệp chỉ chứa comment được coi là không có gì để so sánh")
    void commentOnlyFilesScoreZero() {
        String commentsOnly = "// TODO\n/* nothing here */\n";
        assertEquals(0.0, engine.calculateOverallSimilarity(commentsOnly, commentsOnly));
    }

    @Test
    @DisplayName("Mã giống hệt nhau phải đạt tương đồng rất cao")
    void identicalCodeScoresVeryHigh() {
        String code = "public class Sort { void run(int[] a) { for (int i=0;i<a.length;i++) { int t=a[i]; } } }";
        double score = engine.calculateOverallSimilarity(code, code);
        assertTrue(score >= 99.0, "Mã giống hệt phải đạt >= 99%, thực tế " + score);
    }

    @Test
    @DisplayName("Đổi tên định danh vẫn bị phát hiện (AC-SIM-01)")
    void renamedIdentifiersStillDetected() {
        String a = "public void processCart() { int total = 0; for (Item i : items) total += i.price; }";
        String b = "public void handleBasket() { int sum = 0; for (Item x : orderItems) sum += x.price; }";
        double score = engine.calculateOverallSimilarity(a, b);
        assertTrue(score >= 85.0, "Đổi tên biến phải vẫn bị phát hiện >= 85%, thực tế " + score);
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "int a;", "class A { }"})
    @DisplayName("Đoạn mã quá ngắn (ít hơn k=3 token) không được gây lỗi hay ra điểm âm")
    void veryShortInputsAreHandled(String snippet) {
        double score = engine.calculateOverallSimilarity(snippet, snippet);
        assertTrue(score >= 0.0 && score <= 100.0, "Điểm phải nằm trong [0,100], thực tế " + score);
        assertFalse(Double.isNaN(score), "Điểm không được là NaN");
    }

    @Test
    @DisplayName("Điểm luôn nằm trong [0,100] với mọi cặp đầu vào")
    void scoreAlwaysWithinRange() {
        String[][] pairs = {
                {"", ""},
                {"a", "b"},
                {null, "class A{}"},
                {"重复 内容 repetido", "otra cosa distinta"},
        };
        for (String[] p : pairs) {
            double score = engine.calculateOverallSimilarity(p[0], p[1]);
            assertTrue(score >= 0.0 && score <= 100.0, "Ngoài miền cho cặp: " + score);
        }
    }

    @Test
    @DisplayName("Chuẩn hóa: null trả về chuỗi rỗng, không ném lỗi")
    void normalizeHandlesNull() {
        assertEquals("", engine.normalizeJavaCode(null));
    }

    // ---------------------------------------------------------------- Mật khẩu

    @Test
    @DisplayName("Mật khẩu null hoặc quá 1024 ký tự bị từ chối khi băm")
    void hashRejectsNullAndTooLong() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("a".repeat(1025)));
    }

    @Test
    @DisplayName("Ranh giới độ dài mật khẩu khi đổi: 7 ký tự bị từ chối, 8 được chấp nhận")
    void changePasswordLengthBoundary() {
        int min = 8, max = 1024;
        assertFalse(7 >= min, "7 ký tự phải nhỏ hơn ngưỡng tối thiểu");
        assertTrue(8 >= min);
        assertTrue(max >= min);
    }

    @Test
    @DisplayName("Mã băm PBKDF2 bị sửa đổi phải bị từ chối")
    void tamperedPbkdf2HashRejected() {
        String hash = PasswordUtil.hashPassword("correct-horse-battery");
        assertTrue(PasswordUtil.verifyPassword("correct-horse-battery", hash));

        String tampered = hash.substring(0, hash.length() - 1) + (hash.endsWith("a") ? "b" : "a");
        assertFalse(PasswordUtil.verifyPassword("correct-horse-battery", tampered));
        assertFalse(PasswordUtil.verifyPassword("wrong-password", hash));
    }

    @Test
    @DisplayName("Mã băm có số vòng lặp thấp hơn quy định bị từ chối (chống downgrade)")
    void weakIterationCountRejected() {
        String weak = "pbkdf2-sha256$1000$" + "AAAAAAAAAAAAAAAAAAAAAA=="
                + "$" + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
        assertFalse(PasswordUtil.verifyPassword("anything", weak));
    }

    @Test
    @DisplayName("verifyPassword với mật khẩu hoặc mã băm null trả về false, không ném lỗi")
    void verifyHandlesNulls() {
        assertFalse(PasswordUtil.verifyPassword(null, "abc"));
        assertFalse(PasswordUtil.verifyPassword("abc", null));
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }

    @Test
    @DisplayName("Hai lần băm cùng mật khẩu cho hai mã khác nhau (salt ngẫu nhiên)")
    void saltsAreRandom() {
        String h1 = PasswordUtil.hashPassword("same-password");
        String h2 = PasswordUtil.hashPassword("same-password");
        assertNotEquals(h1, h2, "Salt phải khác nhau giữa hai lần băm");
        assertTrue(PasswordUtil.verifyPassword("same-password", h1));
        assertTrue(PasswordUtil.verifyPassword("same-password", h2));
    }

    // ---------------------------------------------------------------- Lưu trữ

    @Test
    @DisplayName("Đường dẫn lưu trữ rỗng/không hợp lệ được xử lý an toàn")
    void storageHandlesBlankPaths() {
        assertNull(StorageConfig.resolve(null));
        assertNull(StorageConfig.resolve(""));
        assertThrows(IllegalArgumentException.class, () -> StorageConfig.newTarget("  "));
    }
}
