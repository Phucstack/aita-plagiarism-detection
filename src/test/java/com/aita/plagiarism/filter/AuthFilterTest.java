package com.aita.plagiarism.filter;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.*;

class AuthFilterTest {
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private final HttpSession session = mock(HttpSession.class);
    private final FilterChain chain = mock(FilterChain.class);

    @Test void anonymousIsRejected() throws Exception {
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("/aita");
        new AuthFilter().doFilter(req, res, chain);
        verify(res).sendRedirect("/aita/login?error=unauthorized");
        verifyNoInteractions(chain);
    }

    @Test void sessionAloneCannotBypassExpiredOrMissingToken() throws Exception {
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("/aita");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(new User(1,"admin","Admin","a@example.invalid","ADMIN"));
        new AuthFilter().doFilter(req,res,chain);
        verify(session).invalidate();
        verifyNoInteractions(chain);
    }

    @ParameterizedTest
    @ValueSource(strings={"/dashboard", "/dashboard.jsp", "/batch-scanner", "/batch-scanner.jsp", "/course-action", "/assignment-action"})
    void studentCannotAccessStaffRoutes(String path) throws Exception {
        User user = new User(4,"student","Student","s@example.invalid","STUDENT");
        when(req.getServletPath()).thenReturn(path);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN",JWTUtil.generateToken(user))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(4)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(res).sendError(403);
            verifyNoInteractions(chain);
        }
    }

    @Test void studentCanReachOwnedExportEnforcementPoint() throws Exception {
        User user = new User(4,"student","Student","s@example.invalid","STUDENT");
        when(req.getServletPath()).thenReturn("/export-report");
        when(req.getMethod()).thenReturn("GET");
        when(req.getSession(true)).thenReturn(session);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN",JWTUtil.generateToken(user))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(4)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(session).setAttribute("currentUser",user);
            verify(chain).doFilter(req,res);
        }
    }

    // ------------------------------------------------------------------ CSRF

    private void givenAuthenticatedInstructor() {
        User user = new User(2, "teacher", "Teacher", "t@example.invalid", "INSTRUCTOR");
        when(req.getServletPath()).thenReturn("/profile-action");
        when(req.getMethod()).thenReturn("POST");
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getServerPort()).thenReturn(8080);
        when(req.getSession(true)).thenReturn(session);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", JWTUtil.generateToken(user))});
    }

    @Test
    void mutationWithoutAnyOriginEvidenceIsRejected() throws Exception {
        givenAuthenticatedInstructor();
        // Không có Origin, cũng không có Sec-Fetch-Site: không chứng minh được cùng nguồn.
        try (var dao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getUserById(2))
                        .thenReturn(new User(2, "teacher", "Teacher", "t@example.invalid", "INSTRUCTOR")))) {
            new AuthFilter().doFilter(req, res, chain);
            verify(res).sendError(403);
            verifyNoInteractions(chain);
        }
    }

    @Test
    void mutationFromForeignOriginIsRejected() throws Exception {
        givenAuthenticatedInstructor();
        when(req.getHeader("Origin")).thenReturn("https://attacker.invalid");
        try (var dao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getUserById(2))
                        .thenReturn(new User(2, "teacher", "Teacher", "t@example.invalid", "INSTRUCTOR")))) {
            new AuthFilter().doFilter(req, res, chain);
            verify(res).sendError(403);
            verifyNoInteractions(chain);
        }
    }

    @Test
    void mutationWithMatchingOriginIsAllowed() throws Exception {
        givenAuthenticatedInstructor();
        when(req.getHeader("Origin")).thenReturn("http://localhost:8080");
        try (var dao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getUserById(2))
                        .thenReturn(new User(2, "teacher", "Teacher", "t@example.invalid", "INSTRUCTOR")))) {
            new AuthFilter().doFilter(req, res, chain);
            verify(chain).doFilter(req, res);
            verify(res, never()).sendError(403);
        }
    }

    @Test
    void mutationWithSameOriginFetchMetadataIsAllowed() throws Exception {
        givenAuthenticatedInstructor();
        when(req.getHeader("Sec-Fetch-Site")).thenReturn("same-origin");
        try (var dao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getUserById(2))
                        .thenReturn(new User(2, "teacher", "Teacher", "t@example.invalid", "INSTRUCTOR")))) {
            new AuthFilter().doFilter(req, res, chain);
            verify(chain).doFilter(req, res);
        }
    }

    @Test void validTokenRestoresCurrentDatabaseIdentity() throws Exception {
        User user = new User(2,"teacher","Teacher","t@example.invalid","INSTRUCTOR");
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getMethod()).thenReturn("GET");
        when(req.getSession(true)).thenReturn(session);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN",JWTUtil.generateToken(user))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(2)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(session).setAttribute("currentUser",user);
            verify(chain).doFilter(req,res);
        }
    }

    // ------------------------------------------------- Session revocation (5c)

    /** Ký token bằng đúng secret mà JWTUtil dùng, với iat tùy chọn (epoch ms). */
    private static String tokenWithIssuedAt(User user, long issuedAtMs) {
        String secret = System.getProperty("JWT_SECRET", System.getenv("JWT_SECRET"));
        var key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Jwts.builder().issuer("aita")
                .claim("userId", user.getUserId()).claim("username", user.getUsername())
                .claim("fullName", user.getFullName()).claim("role", user.getRole())
                .issuedAt(new java.util.Date(issuedAtMs))
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000L))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    @Test void tokenIssuedBeforePasswordChangeIsRejected() throws Exception {
        long passwordChangedMs = System.currentTimeMillis();
        long oldIssuedAtMs = passwordChangedMs - 5000; // token cũ, phát hành trước khi đổi pass
        User user = new User(2,"teacher","Teacher","t@example.invalid","INSTRUCTOR");
        user.setPasswordChangedAt(new java.sql.Timestamp(passwordChangedMs));
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getContextPath()).thenReturn("/aita");
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", tokenWithIssuedAt(user, oldIssuedAtMs))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(2)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(res).sendRedirect("/aita/login?error=unauthorized");
            verifyNoInteractions(chain);
        }
    }

    @Test void tokenIssuedAfterPasswordChangeStillWorks() throws Exception {
        long passwordChangedMs = System.currentTimeMillis() - 60000; // đổi pass 1 phút trước
        long newIssuedAtMs = System.currentTimeMillis();             // token mới, phát hành sau
        User user = new User(2,"teacher","Teacher","t@example.invalid","INSTRUCTOR");
        user.setPasswordChangedAt(new java.sql.Timestamp(passwordChangedMs));
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getMethod()).thenReturn("GET");
        when(req.getSession(true)).thenReturn(session);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", tokenWithIssuedAt(user, newIssuedAtMs))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(2)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(session).setAttribute("currentUser",user);
            verify(chain).doFilter(req,res);
        }
    }

    @Test void userWithoutPasswordChangedAtKeepsExistingTokens() throws Exception {
        // Tài khoản chưa từng đổi mật khẩu (password_changed_at NULL) -> token cũ vẫn hợp lệ.
        User user = new User(2,"teacher","Teacher","t@example.invalid","INSTRUCTOR");
        long oldIssuedAtMs = System.currentTimeMillis() - 3600000;
        when(req.getServletPath()).thenReturn("/dashboard");
        when(req.getMethod()).thenReturn("GET");
        when(req.getSession(true)).thenReturn(session);
        when(req.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", tokenWithIssuedAt(user, oldIssuedAtMs))});
        try (var dao = mockConstruction(UserDAO.class, (mock, ctx) -> when(mock.getUserById(2)).thenReturn(user))) {
            new AuthFilter().doFilter(req,res,chain);
            verify(chain).doFilter(req,res);
        }
    }
}
