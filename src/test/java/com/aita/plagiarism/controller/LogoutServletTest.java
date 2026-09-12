package com.aita.plagiarism.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Điều hướng Đăng xuất (LogoutServlet)")
public class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
    }

    @Test
    @DisplayName("Hủy session, xóa cookie AUTH_TOKEN và chuyển hướng về trang đăng nhập")
    void testDoGetLogout() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doGet(request, response);

        verify(session).invalidate();

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(LoginServlet.AUTH_COOKIE_NAME, cookie.getName());
        assertEquals(0, cookie.getMaxAge(), "Cookie maxAge phải được set về 0 để xóa");
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());

        verify(response).sendRedirect("/aita/login?message=logged_out");
    }

    @Test
    @DisplayName("Xử lý an toàn khi không tồn tại session trước đó")
    void testDoGetLogoutWithoutSession() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/aita/login?message=logged_out");
    }
}
