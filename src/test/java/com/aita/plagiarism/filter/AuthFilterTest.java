package com.aita.plagiarism.filter;

import com.aita.plagiarism.controller.LoginServlet;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Bộ lọc Xác thực & Phân quyền (AuthFilter - RBAC)")
public class AuthFilterTest {

    private AuthFilter authFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        lenient().when(request.getSession()).thenReturn(session);
    }

    @Test
    @DisplayName("Chặn người dùng chưa đăng nhập và điều hướng về trang Login")
    void testUnauthenticatedRedirectsToLogin() throws IOException, ServletException {
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getContextPath()).thenReturn("/aita");

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/aita/login?error=unauthorized");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Cho phép người dùng đã đăng nhập trong session đi tiếp")
    void testAuthenticatedSessionPasses() throws IOException, ServletException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getServletPath()).thenReturn("/dashboard");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Khôi phục phiên đăng nhập từ JWT Token hợp lệ trong Cookie")
    void testRestoreSessionFromValidJWTCookie() throws IOException, ServletException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        String validToken = JWTUtil.generateToken(instructor);

        when(session.getAttribute("currentUser")).thenReturn(null);
        Cookie authCookie = new Cookie(LoginServlet.AUTH_COOKIE_NAME, validToken);
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(request.getServletPath()).thenReturn("/dashboard");

        authFilter.doFilter(request, response, chain);

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(session).setAttribute(eq("jwtToken"), eq(validToken));
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("RBAC: Chặn Sinh viên truy cập Batch Scanner và chuyển hướng về Student Portal")
    void testStudentBlockedFromBatchScanner() throws IOException, ServletException {
        User student = new User(4, "phuctv", "Trần Văn Phúc", "phuctv@fpt.edu.vn", "STUDENT");
        when(session.getAttribute("currentUser")).thenReturn(student);
        when(request.getServletPath()).thenReturn("/batch-scanner");
        when(request.getContextPath()).thenReturn("/aita");

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/aita/student-portal?error=forbidden");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("RBAC: Cho phép Giảng viên truy cập Batch Scanner")
    void testInstructorAllowedOnBatchScanner() throws IOException, ServletException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getServletPath()).thenReturn("/batch-scanner");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
