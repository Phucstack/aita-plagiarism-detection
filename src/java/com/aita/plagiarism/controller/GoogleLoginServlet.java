package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.GoogleIdentityVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet("/login-google")
public class GoogleLoginServlet extends HttpServlet {
    private final GoogleIdentityVerifier verifier = new GoogleIdentityVerifier();
    private final UserDAO userDAO = new UserDAO();
    private static final String CHALLENGE = "googleLoginChallenge";
    private static final String ISSUED = "googleLoginIssued";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String clientId = GoogleIdentityVerifier.clientId();
        if (clientId == null) { response.sendError(503, "Thiếu cấu hình GOOGLE_CLIENT_ID của ứng dụng."); return; }
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        HttpSession session = request.getSession(true);
        synchronized (session) {
            session.setAttribute(CHALLENGE, challenge);
            session.setAttribute(ISSUED, System.currentTimeMillis());
        }
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"clientId\":\"" + clientId + "\",\"nonce\":\"" + challenge + "\"}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        String state = request.getParameter("state");
        if (session == null || state == null) { response.sendError(403); return; }
        synchronized (session) {
            Object issued = session.getAttribute(ISSUED);
            if (!state.equals(session.getAttribute(CHALLENGE)) || !(issued instanceof Long)
                    || System.currentTimeMillis() - (Long) issued > 300_000) { response.sendError(403); return; }
            session.removeAttribute(CHALLENGE); session.removeAttribute(ISSUED);
        }
        GoogleIdToken.Payload identity;
        try { identity = verifier.verify(request.getParameter("credential"), state); }
        catch (IllegalStateException e) { response.sendError(503, "Thiếu cấu hình Google login."); return; }
        catch (java.security.GeneralSecurityException | IOException e) {
            response.sendError(503, "Chưa xác minh được với Google. Vui lòng thử lại."); return;
        }
        if (identity == null) { response.sendError(401, "Google ID token không hợp lệ hoặc đã hết hạn."); return; }
        boolean authoritative = identity.getEmail().toLowerCase(java.util.Locale.ROOT).endsWith("@gmail.com")
                || (identity.getHostedDomain() != null && !identity.getHostedDomain().isBlank());
        User user = userDAO.authenticateGoogle(identity.getSubject(), identity.getEmail(), authoritative);
        if (user == null) {
            response.sendError(403, "Email Google chưa được cấp tài khoản hoặc chưa đủ điều kiện liên kết. Liên hệ quản trị viên."); return;
        }
        LoginServlet.completeLogin(request, response, user);
    }
}
