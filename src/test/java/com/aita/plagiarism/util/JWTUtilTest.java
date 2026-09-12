package com.aita.plagiarism.util;

import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tiện ích JSON Web Token (JWTUtil - RFC 7519)")
public class JWTUtilTest {

    private User sampleInstructor;
    private User sampleStudent;

    @BeforeEach
    void setUp() {
        sampleInstructor = new User(1, "teacher_ha", "TS. Nguyễn Hoàng Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        sampleStudent = new User(102, "student_102", "Trần Văn Long", "longtvse1701@fpt.edu.vn", "STUDENT");
    }

    @Test
    @DisplayName("Sinh JWT Token đúng cấu trúc 3 phần Header.Payload.Signature")
    void testGenerateTokenStructure() {
        String token = JWTUtil.generateToken(sampleInstructor);

        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT phải gồm đúng 3 phần phân tách bởi dấu chấm");
        assertFalse(parts[0].isEmpty(), "Header không được rỗng");
        assertFalse(parts[1].isEmpty(), "Payload không được rỗng");
        assertFalse(parts[2].isEmpty(), "Signature không được rỗng");
    }

    @Test
    @DisplayName("Xác thực JWT Token vừa sinh thành công")
    void testValidateTokenSuccess() {
        String token = JWTUtil.generateToken(sampleStudent);
        assertTrue(JWTUtil.validateToken(token), "Token vừa tạo ra với secret key hợp lệ phải validate thành công");
    }

    @Test
    @DisplayName("Trích xuất chính xác tất cả các Claims trong Token")
    void testExtractClaimsAccurate() {
        String token = JWTUtil.generateToken(sampleInstructor);
        Map<String, String> claims = JWTUtil.extractClaims(token);

        assertNotNull(claims);
        assertEquals("1", claims.get("userId"));
        assertEquals("teacher_ha", claims.get("username"));
        assertEquals("TS. Nguyễn Hoàng Hà", claims.get("fullName"));
        assertEquals("INSTRUCTOR", claims.get("role"));
        assertTrue(claims.containsKey("iat"), "Claims phải chứa thời điểm tạo (iat)");
        assertTrue(claims.containsKey("exp"), "Claims phải chứa thời điểm hết hạn (exp)");

        long iat = Long.parseLong(claims.get("iat"));
        long exp = Long.parseLong(claims.get("exp"));
        assertEquals(86400, exp - iat, "Thời hạn hiệu lực mặc định phải là 24 giờ (86400s)");
    }

    @Test
    @DisplayName("Phát hiện và CHẶN token khi Payload bị can thiệp (Tampered Payload)")
    void testValidateTokenTamperedPayload() {
        String token = JWTUtil.generateToken(sampleStudent);
        String[] parts = token.split("\\.");

        // Giả mạo một ký tự trong payload
        char lastChar = parts[1].charAt(parts[1].length() - 1);
        char replacedChar = (lastChar == 'A') ? 'B' : 'A';
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 1) + replacedChar;
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertFalse(JWTUtil.validateToken(tamperedToken), "Token bị giả mạo payload phải bị từ chối");
    }

    @Test
    @DisplayName("Phát hiện và CHẶN token khi Chữ ký bị can thiệp (Tampered Signature)")
    void testValidateTokenTamperedSignature() {
        String token = JWTUtil.generateToken(sampleInstructor);
        String[] parts = token.split("\\.");

        String invalidSigToken = parts[0] + "." + parts[1] + ".invalidSignatureFakeHash123456";
        assertFalse(JWTUtil.validateToken(invalidSigToken), "Token có chữ ký sai phải bị từ chối");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abc", "abc.def", "a.b.c.d", "null"})
    @DisplayName("Xử lý an toàn khi token dị dạng hoặc rỗng")
    void testValidateTokenMalformed(String malformedToken) {
        assertFalse(JWTUtil.validateToken(malformedToken), "Token dị dạng phải trả về false an toàn");
    }

    @Test
    @DisplayName("Xử lý an toàn khi token là null")
    void testValidateTokenNull() {
        assertFalse(JWTUtil.validateToken(null), "Token null phải trả về false an toàn");
        Map<String, String> claims = JWTUtil.extractClaims(null);
        assertNotNull(claims);
        assertTrue(claims.isEmpty(), "Extract claims từ token null trả về map rỗng");
    }

    @Test
    @DisplayName("Bảo toàn thông tin người dùng chứa ký tự đặc biệt và Unicode Tiếng Việt")
    void testSpecialCharactersAndUnicode() {
        User userSpecial = new User(99, "user_đặc_biệt", "Nguyễn Văn Test - SE1701", "special@fpt.edu.vn", "ADMIN");
        String token = JWTUtil.generateToken(userSpecial);

        assertTrue(JWTUtil.validateToken(token));
        Map<String, String> claims = JWTUtil.extractClaims(token);
        assertEquals("user_đặc_biệt", claims.get("username"));
        assertEquals("Nguyễn Văn Test - SE1701", claims.get("fullName"));
        assertEquals("ADMIN", claims.get("role"));
    }
}
