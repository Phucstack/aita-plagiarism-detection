package com.aita.plagiarism.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tiện ích Băm Mật Khẩu (PasswordUtil)")
public class PasswordUtilTest {

    @Test
    @DisplayName("Băm SHA-256 sinh chuỗi hex 64 ký tự chuẩn")
    void testHashSHA256Standard() {
        String raw = "SecurePassword@2026";
        String hash = PasswordUtil.hashSHA256(raw);

        assertNotNull(hash, "Mã băm không được null");
        assertEquals(64, hash.length(), "Độ dài băm SHA-256 phải đúng 64 ký tự hex");
        // Tính bất biến: cùng một input phải ra cùng một hash
        assertEquals(hash, PasswordUtil.hashSHA256(raw), "SHA-256 phải mang tính tất định (deterministic)");
    }

    @Test
    @DisplayName("Băm SHA-256 với giá trị null trả về null an toàn")
    void testHashSHA256Null() {
        assertNull(PasswordUtil.hashSHA256(null), "Băm null phải trả về null an toàn");
    }

    @Test
    @DisplayName("Băm MD5 tương thích ngược chuẩn 32 ký tự hex")
    void testHashMD5Standard() {
        String raw = "123456";
        String expectedMD5 = "e10adc3949ba59abbe56e057f20f883e";
        String computedMD5 = PasswordUtil.hashMD5(raw);

        assertNotNull(computedMD5);
        assertEquals(32, computedMD5.length(), "Độ dài băm MD5 phải đúng 32 ký tự hex");
        assertEquals(expectedMD5, computedMD5, "Hash MD5 của '123456' phải khớp chuẩn RFC 1321");
    }

    @Test
    @DisplayName("Băm MD5 với giá trị null trả về null an toàn")
    void testHashMD5Null() {
        assertNull(PasswordUtil.hashMD5(null), "Băm MD5 null phải trả về null an toàn");
    }

    @Test
    @DisplayName("Xác thực mật khẩu đúng với mã băm SHA-256")
    void testVerifyPasswordSHA256Success() {
        String raw = "AitaSecure#789";
        String hash = PasswordUtil.hashSHA256(raw);

        assertTrue(PasswordUtil.verifyPassword(raw, hash), "Mật khẩu đúng phải xác thực thành công");
        assertTrue(PasswordUtil.verifyPassword(raw, hash.toUpperCase()), "Xác thực không phân biệt hoa thường của hash hex");
    }

    @Test
    @DisplayName("Xác thực mật khẩu đúng với mã băm MD5 seed mẫu")
    void testVerifyPasswordMD5Success() {
        String raw = "123456";
        String storedMD5 = "e10adc3949ba59abbe56e057f20f883e";

        assertTrue(PasswordUtil.verifyPassword(raw, storedMD5), "Tương thích ngược xác thực MD5 cho mật khẩu mẫu PRJ301");
    }

    @ParameterizedTest
    @ValueSource(strings = {"wrongpass", "12345", "1234567", "password", " "})
    @DisplayName("Từ chối mật khẩu sai khi so khớp hash")
    void testVerifyPasswordFail(String wrongPass) {
        String hash = PasswordUtil.hashSHA256("123456");
        assertFalse(PasswordUtil.verifyPassword(wrongPass, hash), "Mật khẩu sai không được vượt qua xác thực");
    }

    @Test
    @DisplayName("Xử lý an toàn khi đầu vào null hoặc rỗng")
    void testVerifyPasswordEdgeCases() {
        String validHash = PasswordUtil.hashSHA256("123456");
        assertFalse(PasswordUtil.verifyPassword(null, validHash), "Raw password null phải trả về false");
        assertFalse(PasswordUtil.verifyPassword("123456", null), "Stored hash null phải trả về false");
        assertFalse(PasswordUtil.verifyPassword(null, null), "Cả hai null phải trả về false");
        assertFalse(PasswordUtil.verifyPassword("", validHash), "Raw password rỗng phải trả về false");
    }
}
