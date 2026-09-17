package com.aita.plagiarism.util;

import com.aita.plagiarism.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JWTSecurityRegressionTest {
    @Test void profileTextCannotOverwriteIdentity() {
        User user = new User(4, "student", "Tên,userId:1,role:ADMIN \"quoted\"\nline", "s@example.invalid", "STUDENT");
        String token = JWTUtil.generateToken(user);
        assertTrue(JWTUtil.validateToken(token));
        var claims = JWTUtil.extractClaims(token);
        assertEquals("4", claims.get("userId"));
        assertEquals("STUDENT", claims.get("role"));
        assertEquals(user.getFullName(), claims.get("fullName"));
    }
    @Test void expiredMissingExpiryAndWrongIdTypeAreRejected() {
        var key = Keys.hmacShaKeyFor(System.getenv("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder().issuer("aita").claim("userId",4)
                .expiration(new Date(1000)).signWith(key,Jwts.SIG.HS256).compact();
        String missing = Jwts.builder().issuer("aita").claim("userId",4).signWith(key,Jwts.SIG.HS256).compact();
        String wrongId = Jwts.builder().issuer("aita").claim("userId","4")
                .expiration(new Date(System.currentTimeMillis()+60000)).signWith(key,Jwts.SIG.HS256).compact();
        for (String token : new String[]{expired,missing,wrongId}) {
            assertFalse(JWTUtil.validateToken(token));
            assertTrue(JWTUtil.extractClaims(token).isEmpty());
        }
    }
    @Test void invalidSignatureNeverExposesClaims() {
        String token = JWTUtil.generateToken(new User(4,"s","S","s@example.invalid","STUDENT"));
        String tampered = token.substring(0,token.lastIndexOf('.')+1)+"invalid";
        assertTrue(JWTUtil.extractClaims(tampered).isEmpty());
    }
}
