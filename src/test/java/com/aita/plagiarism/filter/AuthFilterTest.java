package com.aita.plagiarism.filter;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
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
}
