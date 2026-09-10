package com.aita.plagiarism.util;

import com.aita.plagiarism.model.User;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích xử lý JSON Web Token (JWT) theo chuẩn RFC 7519 HMAC-SHA256
 * Thuần Java Standard Library - Tương thích hoàn hảo môi trường PRJ301
 */
public class JWTUtil {

    private static final String SECRET_KEY = System.getenv("JWT_SECRET") != null 
            ? System.getenv("JWT_SECRET") 
            : "AITA_DEFENSE_SUITE_SECURE_JWT_KEY_2026_FPTU_PRJ301";
    private static final long EXPIRATION_SECONDS = 86400; // 24 giờ hiệu lực

    private JWTUtil() {}

    /**
     * Khởi tạo JWT Token từ thông tin người dùng
     */
    public static String generateToken(User user) {
        long now = System.currentTimeMillis() / 1000;
        long exp = now + EXPIRATION_SECONDS;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
            "{\"userId\":%d,\"username\":\"%s\",\"fullName\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
            user.getUserId(),
            escapeJson(user.getUsername()),
            escapeJson(user.getFullName()),
            escapeJson(user.getRole()),
            now,
            exp
        );

        String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String dataToSign = encodedHeader + "." + encodedPayload;
        String signature = signHMACSHA256(dataToSign, SECRET_KEY);

        return dataToSign + "." + signature;
    }

    /**
     * Xác thực tính hợp lệ của JWT Token (Chữ ký & Thời hạn)
     */
    public static boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;

        String dataToSign = parts[0] + "." + parts[1];
        String expectedSignature = signHMACSHA256(dataToSign, SECRET_KEY);

        if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), 
                                   expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        Map<String, String> claims = extractClaims(token);
        if (claims.containsKey("exp")) {
            long exp = Long.parseLong(claims.get("exp"));
            long now = System.currentTimeMillis() / 1000;
            return now <= exp;
        }
        return false;
    }

    /**
     * Trích xuất các Claims từ Payload của JWT
     */
    public static Map<String, String> extractClaims(String token) {
        Map<String, String> claims = new HashMap<>();
        if (token == null) return claims;
        String[] parts = token.split("\\.");
        if (parts.length < 2) return claims;

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
            parseSimpleJson(payloadJson, claims);
        } catch (Exception e) {
            // Không văng lỗi uncaught khi token bị giả mạo
        }
        return claims;
    }

    private static String signHMACSHA256(String data, String key) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] signedBytes = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(signedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính chữ ký số HMAC-SHA256: " + e.getMessage(), e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void parseSimpleJson(String json, Map<String, String> map) {
        String clean = json.replace("{", "").replace("}", "").trim();
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
    }
}
