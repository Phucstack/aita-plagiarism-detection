package com.aita.plagiarism.util;

import com.aita.plagiarism.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Signed tokens. Unverified payloads are never exposed as claims. */
public final class JWTUtil {
    private JWTUtil() {}

    private static SecretKey key() {
        // Đọc system property trước (nhất quán với DBContext), rồi mới tới biến môi trường.
        String secret = System.getProperty("JWT_SECRET", System.getenv("JWT_SECRET"));
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(User user) {
        if (user == null || user.getUserId() <= 0) throw new IllegalArgumentException("Invalid user");
        long now = System.currentTimeMillis();
        return Jwts.builder().issuer("aita")
                .claim("userId", user.getUserId()).claim("username", user.getUsername())
                .claim("fullName", user.getFullName()).claim("role", user.getRole())
                .issuedAt(new Date(now)).expiration(new Date(now + 86400000L))
                .signWith(key(), Jwts.SIG.HS256).compact();
    }

    public static boolean validateToken(String token) {
        return !extractClaims(token).isEmpty();
    }

    public static Map<String, String> extractClaims(String token) {
        Map<String, String> result = new HashMap<>();
        if (token == null || token.isBlank()) return result;
        try {
            Claims claims = Jwts.parser().verifyWith(key()).requireIssuer("aita")
                    .sig().clear().add(Jwts.SIG.HS256).and().build()
                    .parseSignedClaims(token).getPayload();
            if (claims.getExpiration() == null || !claims.getExpiration().after(new Date())) return result;
            Object id = claims.get("userId");
            if (!(id instanceof Integer) || (Integer) id <= 0) return result;
            claims.forEach((name, value) -> result.put(name, String.valueOf(value)));
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            return new HashMap<>();
        }
        return result;
    }
}
