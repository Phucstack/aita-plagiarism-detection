package com.aita.plagiarism.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm và xác thực mật khẩu an toàn
 * Hỗ trợ đồng thời chuẩn hiện đại SHA-256 và tương thích ngược MD5 cho dữ liệu mẫu
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Băm chuỗi bằng thuật toán SHA-256
     */
    public static String hashSHA256(String rawPassword) {
        if (rawPassword == null) return null;
        return hashWithAlgorithm(rawPassword, "SHA-256");
    }

    /**
     * Băm chuỗi bằng thuật toán MD5 (tương thích dữ liệu seed mẫu của PRJ301)
     */
    public static String hashMD5(String rawPassword) {
        if (rawPassword == null) return null;
        return hashWithAlgorithm(rawPassword, "MD5");
    }

    /**
     * Xác thực mật khẩu nhập vào với mã băm trong cơ sở dữ liệu
     * Hỗ trợ tự động nhận diện SHA-256 (64 ký tự hex) hoặc MD5 (32 ký tự hex)
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String cleanHash = storedHash.trim().toLowerCase();
        
        // Kiểm tra tương thích với MD5 (32 ký tự)
        if (cleanHash.length() == 32) {
            String computedMD5 = hashMD5(rawPassword);
            return cleanHash.equalsIgnoreCase(computedMD5);
        }
        
        // Kiểm tra với SHA-256 (64 ký tự)
        if (cleanHash.length() == 64) {
            String computedSHA = hashSHA256(rawPassword);
            return cleanHash.equalsIgnoreCase(computedSHA);
        }

        // Fallback kiểm tra cả 2 trường hợp
        return cleanHash.equalsIgnoreCase(hashSHA256(rawPassword)) 
            || cleanHash.equalsIgnoreCase(hashMD5(rawPassword));
    }

    private static String hashWithAlgorithm(String input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán băm không khả dụng: " + algorithm, e);
        }
    }
}
