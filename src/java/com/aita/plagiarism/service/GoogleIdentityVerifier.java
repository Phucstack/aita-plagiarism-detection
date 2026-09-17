package com.aita.plagiarism.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/** Google library verifies signature, issuer, audience and expiry against Google's rotating keys. */
public class GoogleIdentityVerifier {
    private static volatile GoogleIdTokenVerifier cached;
    private static volatile String cachedClientId;
    private final GoogleIdTokenVerifier suppliedVerifier;
    private final String suppliedClientId;

    public GoogleIdentityVerifier() { this(null, null); }
    GoogleIdentityVerifier(GoogleIdTokenVerifier verifier, String clientId) {
        this.suppliedVerifier = verifier; this.suppliedClientId = clientId;
    }

    public static String clientId() {
        String id = System.getProperty("GOOGLE_CLIENT_ID", System.getenv("GOOGLE_CLIENT_ID"));
        return id != null && id.matches("[A-Za-z0-9_-]+\\.apps\\.googleusercontent\\.com") ? id : null;
    }

    private static synchronized GoogleIdTokenVerifier verifier(String clientId) {
        if (cached == null || !clientId.equals(cachedClientId)) {
            cached = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(clientId)).setAcceptableTimeSkewSeconds(30).build();
            cachedClientId = clientId;
        }
        return cached;
    }

    public GoogleIdToken.Payload verify(String token, String nonce) throws IOException, GeneralSecurityException {
        String id = suppliedClientId == null ? clientId() : suppliedClientId;
        if (id == null) throw new IllegalStateException("GOOGLE_CLIENT_ID is not configured");
        if (token == null || token.length() > 16000 || nonce == null) return null;
        GoogleIdToken verified;
        try { verified = (suppliedVerifier == null ? verifier(id) : suppliedVerifier).verify(token); }
        catch (IllegalArgumentException e) { return null; }
        if (verified == null) return null;
        GoogleIdToken.Payload payload = verified.getPayload();
        if (!nonce.equals(payload.get("nonce")) || !Boolean.TRUE.equals(payload.getEmailVerified())
                || payload.getSubject() == null || payload.getSubject().isBlank() || payload.getSubject().length() > 255
                || payload.getEmail() == null || payload.getEmail().length() > 100
                || (payload.getAuthorizedParty() != null && !id.equals(payload.getAuthorizedParty()))) return null;
        return payload;
    }
}
