package com.aita.plagiarism.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleIdentityVerifierTest {
    @Test void verifiesRealSignaturesAndRejectsWrongClaims() throws Exception {
        String client = "verification.apps.googleusercontent.com";
        KeyPair keys = Jwts.SIG.RS256.keyPair().build();
        GooglePublicKeysManager manager = mock(GooglePublicKeysManager.class);
        when(manager.getPublicKeys()).thenReturn(List.of(keys.getPublic()));
        when(manager.getJsonFactory()).thenReturn(GsonFactory.getDefaultInstance());
        GoogleIdTokenVerifier google = new GoogleIdTokenVerifier.Builder(manager).setAudience(List.of(client)).setAcceptableTimeSkewSeconds(0).build();
        GoogleIdentityVerifier service = new GoogleIdentityVerifier(google,client);
        Map<String,Object> claims = new HashMap<>();
        claims.put("iss","https://accounts.google.com");claims.put("aud",client);claims.put("sub","123456");
        claims.put("iat",System.currentTimeMillis()/1000);claims.put("exp",System.currentTimeMillis()/1000+300);
        claims.put("nonce","nonce");claims.put("email","student@gmail.com");claims.put("email_verified",true);
        assertNotNull(service.verify(token(claims,keys),"nonce"));
        assertNull(service.verify(token(claims,Jwts.SIG.RS256.keyPair().build()),"nonce"));
        for (String claim : List.of("iss","aud","nonce","email_verified","exp")) {
            var invalid=new HashMap<>(claims);
            invalid.put(claim,claim.equals("exp") ? 1L : claim.equals("email_verified") ? false : "wrong");
            assertNull(service.verify(token(invalid,keys),"nonce"),claim);
        }
        assertNull(service.verify("not-a-token","nonce"));
    }
    private String token(Map<String,Object> claims, KeyPair keys) {
        return Jwts.builder().claims(claims).signWith(keys.getPrivate(),Jwts.SIG.RS256).compact();
    }
}
