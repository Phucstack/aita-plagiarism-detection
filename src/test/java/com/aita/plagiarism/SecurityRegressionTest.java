package com.aita.plagiarism;

import com.aita.plagiarism.controller.ExportReportServlet;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
import com.aita.plagiarism.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử bảo mật và hồi quy các lỗ hổng đã được sửa.
 * Phần lớn là kiểm thử hộp trắng (đọc mã nguồn/JSP) nên không cần CSDL.
 */
@DisplayName("Kiểm thử bảo mật & hồi quy")
class SecurityRegressionTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    /**
     * Các ca dưới đây thay đổi System property toàn cục. Phải chụp lại và khôi phục
     * sau mỗi ca, nếu không sẽ làm hỏng cấu hình của mọi kiểm thử chạy sau trong cùng JVM
     * (đặc biệt khi secret được truyền qua -D thay vì biến môi trường).
     */
    private String originalJwtSecret;

    @BeforeEach
    void snapshotJwtSecret() {
        originalJwtSecret = System.getProperty("JWT_SECRET");
    }

    @AfterEach
    void restoreJwtSecret() {
        if (originalJwtSecret == null) {
            System.clearProperty("JWT_SECRET");
        } else {
            System.setProperty("JWT_SECRET", originalJwtSecret);
        }
    }

    private static User user(int id, String role) {
        User u = new User();
        u.setUserId(id);
        u.setUsername("u" + id);
        u.setFullName("Người Dùng " + id);
        u.setEmail("u" + id + "@fpt.edu.vn");
        u.setRole(role);
        return u;
    }

    // ---------------------------------------------------------------- JWT

    @Test
    @DisplayName("Token hợp lệ giữ nguyên định danh và vai trò")
    void validTokenRoundTrip() {
        System.setProperty("JWT_SECRET", SECRET);
        try {
            User u = user(42, "INSTRUCTOR");
            Map<String, String> claims = JWTUtil.extractClaims(JWTUtil.generateToken(u));
            assertEquals("42", claims.get("userId"));
            assertEquals("INSTRUCTOR", claims.get("role"));
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    @Test
    @DisplayName("Token bị sửa chữ ký phải bị từ chối")
    void tamperedSignatureRejected() {
        System.setProperty("JWT_SECRET", SECRET);
        try {
            String token = JWTUtil.generateToken(user(7, "ADMIN"));
            String tampered = token.substring(0, token.length() - 2)
                    + (token.endsWith("AA") ? "BB" : "AA");
            assertTrue(JWTUtil.extractClaims(tampered).isEmpty(), "Token sửa chữ ký phải bị từ chối");
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    @Test
    @DisplayName("Token ký bằng khóa khác phải bị từ chối")
    void tokenSignedWithOtherSecretRejected() {
        System.setProperty("JWT_SECRET", SECRET);
        String token;
        try {
            token = JWTUtil.generateToken(user(9, "ADMIN"));
        } finally {
            System.clearProperty("JWT_SECRET");
        }
        System.setProperty("JWT_SECRET", "ffffffffffffffffffffffffffffffffffffffffffffffff");
        try {
            assertTrue(JWTUtil.extractClaims(token).isEmpty(), "Token ký bằng khóa khác phải bị từ chối");
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    @Test
    @DisplayName("Token giả mạo kiểu 'alg=none' (không chữ ký) phải bị từ chối")
    void unsignedAlgNoneTokenRejected() {
        System.setProperty("JWT_SECRET", SECRET);
        try {
            String header = base64Url("{\"alg\":\"none\"}");
            String payload = base64Url("{\"iss\":\"aita\",\"userId\":1,\"role\":\"ADMIN\"}");
            String forged = header + "." + payload + ".";
            assertTrue(JWTUtil.extractClaims(forged).isEmpty(), "Token alg=none phải bị từ chối");
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    @Test
    @DisplayName("Token rỗng, null hoặc rác phải trả về rỗng thay vì ném lỗi")
    void malformedTokensReturnEmpty() {
        System.setProperty("JWT_SECRET", SECRET);
        try {
            assertTrue(JWTUtil.extractClaims(null).isEmpty());
            assertTrue(JWTUtil.extractClaims("").isEmpty());
            assertTrue(JWTUtil.extractClaims("not-a-token").isEmpty());
            assertFalse(JWTUtil.validateToken("garbage"));
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    @Test
    @DisplayName("Secret ngắn hơn 32 byte bị từ chối; system property được ưu tiên hơn biến môi trường")
    void shortSecretRejectedAndPropertyWins() {
        // Đặt property ngắn: dù biến môi trường có thể đang chứa secret hợp lệ,
        // property phải được ưu tiên và bị từ chối vì không đủ dài.
        System.setProperty("JWT_SECRET", "quangan");
        try {
            assertThrows(IllegalStateException.class, () -> JWTUtil.generateToken(user(1, "ADMIN")),
                    "Secret ngắn hơn 32 byte phải bị từ chối");
        } finally {
            System.clearProperty("JWT_SECRET");
        }
        // Và với property hợp lệ thì sinh token thành công.
        System.setProperty("JWT_SECRET", SECRET);
        try {
            assertNotNull(JWTUtil.generateToken(user(1, "ADMIN")));
        } finally {
            System.clearProperty("JWT_SECRET");
        }
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------- Mật khẩu legacy

    @Test
    @DisplayName("Mật khẩu legacy MD5/SHA-256 vẫn đăng nhập được và được đánh dấu cần nâng cấp")
    void legacyHashAcceptedAndFlaggedForUpgrade() {
        String md5 = PasswordUtil.hashMD5("123456");
        String sha = PasswordUtil.hashSHA256("123456");
        assertTrue(PasswordUtil.verifyPassword("123456", md5));
        assertTrue(PasswordUtil.verifyPassword("123456", sha));
        assertTrue(PasswordUtil.needsUpgrade(md5));
        assertTrue(PasswordUtil.needsUpgrade(sha));
        assertFalse(PasswordUtil.needsUpgrade(PasswordUtil.hashPassword("123456")));
    }

    // ---------------------------------------------------------------- CSV injection

    @Test
    @DisplayName("Giá trị CSV bắt đầu bằng = + - @ phải được trung hòa (chống formula injection)")
    void csvFormulaInjectionNeutralized() throws Exception {
        Method csv = ExportReportServlet.class.getDeclaredMethod("csv", Object.class);
        csv.setAccessible(true);
        ExportReportServlet servlet = new ExportReportServlet();

        assertEquals("'=cmd|'/c calc'!A1", csv.invoke(servlet, "=cmd|'/c calc'!A1"));
        assertEquals("'+1+1", csv.invoke(servlet, "+1+1"));
        assertEquals("'-2+3", csv.invoke(servlet, "-2+3"));
        assertEquals("'@SUM(A1)", csv.invoke(servlet, "@SUM(A1)"));
        // Giá trị bình thường không bị thêm prefix
        assertEquals("Nguyễn Văn A", csv.invoke(servlet, "Nguyễn Văn A"));
        // Dấu phẩy và ngoặc kép vẫn được quote đúng
        assertEquals("\"a,b\"", csv.invoke(servlet, "a,b"));
        assertEquals("\"say \"\"hi\"\"\"", csv.invoke(servlet, "say \"hi\""));
        // Giá trị null được quy về chuỗi rỗng (không sinh ra token "null" trong file)
        assertEquals("", csv.invoke(servlet, (Object) null));
    }

    // ---------------------------------------------------------------- XSS / JSP

    @Test
    @DisplayName("Biến do người dùng kiểm soát trong JSP phải được escape")
    void userControlledOutputIsEscaped() throws Exception {
        String login = read("web/login.jsp");
        String index = read("web/index.jsp");
        String header = read("web/includes/header.jsp");

        assertTrue(login.contains("fn:escapeXml(lastEmail)"),
                "login.jsp phải escape email phản hồi khi đăng nhập sai");
        assertTrue(index.contains("fn:escapeXml(sessionScope.currentUser.fullName)"),
                "index.jsp phải escape họ tên người dùng");
        assertTrue(header.contains("fn:escapeXml(param.title)"),
                "header.jsp phải escape tham số title");
    }

    @Test
    @DisplayName("Không còn scriptlet trong các trang JSP (trừ redirect-old.jsp)")
    void noScriptletsInViews() throws Exception {
        List<Path> jsps = Files.walk(Paths.get("web"))
                .filter(p -> p.toString().endsWith(".jsp") || p.toString().endsWith(".jspf"))
                .toList();
        assertFalse(jsps.isEmpty(), "Phải tìm thấy các trang JSP");
        for (Path p : jsps) {
            if (p.getFileName().toString().equals("redirect-old.jsp")) continue;
            String content = read(p.toString());
            assertFalse(content.matches("(?s).*<%(?![@!#]).*"),
                    "Không được dùng scriptlet trong " + p);
        }
    }

    @Test
    @DisplayName("Cấu hình container: cookie phiên HttpOnly và có trang lỗi riêng")
    void containerHardeningPresent() throws Exception {
        String webXml = read("web/WEB-INF/web.xml");
        assertTrue(webXml.contains("<http-only>true</http-only>"), "Cookie phiên phải là HttpOnly");
        assertTrue(webXml.contains("error.jsp"), "Phải có trang lỗi riêng, không lộ stack trace");
        assertTrue(Files.exists(Paths.get("web/error.jsp")), "error.jsp phải tồn tại");
    }

    @Test
    @DisplayName("Không còn mã băm thay thế (hash của chuỗi rỗng) trong mã nguồn chính")
    void noPlaceholderHashInMainSource() throws Exception {
        List<Path> sources = Files.walk(Paths.get("src/java")).filter(p -> p.toString().endsWith(".java")).toList();
        for (Path p : sources) {
            String content = read(p.toString());
            assertFalse(content.contains("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                    "Không được dùng mã băm của chuỗi rỗng làm giá trị thay thế trong " + p);
        }
    }

    @Test
    @DisplayName("Không còn chuỗi nhận định mạo danh AI trong mã nguồn và seed")
    void noFakeAiClaimsInSource() throws Exception {
        for (Path p : Files.walk(Paths.get("src/java")).filter(x -> x.toString().endsWith(".java")).toList()) {
            assertFalse(read(p.toString()).contains("AITA AI phát hiện"), p.toString());
        }
        for (Path p : Files.walk(Paths.get("database")).filter(x -> x.toString().endsWith(".sql")).toList()) {
            assertFalse(read(p.toString()).contains("Gemini AI phát hiện"), p.toString());
        }
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
