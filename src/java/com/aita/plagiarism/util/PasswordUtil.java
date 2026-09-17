package com.aita.plagiarism.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Tiện ích băm và xác thực mật khẩu an toàn
 * PBKDF2 for new passwords; legacy digests are accepted only for migration.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    private static final int ITERATIONS = 600_000;

    public static String hashPassword(String password) {
        if (password == null || password.length() > 1024) throw new IllegalArgumentException("Invalid password length");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public static boolean needsUpgrade(String hash) {
        return hash != null && hash.trim().matches("(?i)([a-f0-9]{32}|[a-f0-9]{64})");
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

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
        if (rawPassword == null || storedHash == null || rawPassword.length() > 1024) {
            return false;
        }
        if (storedHash.startsWith("pbkdf2-sha256$")) {
            try {
                String[] parts = storedHash.split("\\$", -1);
                if (parts.length != 4) return false;
                int rounds = Integer.parseInt(parts[1]);
                if (rounds < ITERATIONS || rounds > 2_000_000) return false;
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                return salt.length == 16 && expected.length == 32
                        && MessageDigest.isEqual(expected, derive(rawPassword, salt, rounds));
            } catch (IllegalArgumentException e) { return false; }
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
