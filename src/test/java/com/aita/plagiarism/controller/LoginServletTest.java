package com.aita.plagiarism.controller;

import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.JWTUtil;
import jakarta.servlet.RequestDispatcher;
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

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Điều hướng Đăng nhập (LoginServlet)")
public class LoginServletTest {

    private LoginServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new LoginServlet();
        lenient().when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("GET: Chuyển tiếp tới login.jsp khi chưa đăng nhập")
    void testDoGetNotLoggedIn() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Tự động chuyển hướng về Dashboard nếu đã có JWT Cookie hợp lệ")
    void testDoGetAlreadyLoggedIn() throws ServletException, IOException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        String validToken = JWTUtil.generateToken(instructor);
        Cookie authCookie = new Cookie(LoginServlet.AUTH_COOKIE_NAME, validToken);

        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/aita/dashboard");
    }

    @Test
    @DisplayName("POST: Báo lỗi khi bỏ trống email hoặc mật khẩu")
    void testDoPostMissingCredentials() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Vui lòng nhập đầy đủ"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Báo lỗi khi nhập sai thông tin tài khoản")
    void testDoPostInvalidCredentials() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("teacher_ha");
        when(request.getParameter("password")).thenReturn("wrongpassword");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("không chính xác"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Đăng nhập thành công với Giảng viên -> Cấp phát Cookie & chuyển hướng Dashboard")
    void testDoPostSuccessInstructor() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("teacher_ha");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals(LoginServlet.AUTH_COOKIE_NAME, capturedCookie.getName());
        assertTrue(capturedCookie.isHttpOnly());
        assertEquals("/", capturedCookie.getPath());
        assertTrue(JWTUtil.validateToken(capturedCookie.getValue()));

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(session).setAttribute(eq("jwtToken"), anyString());
        verify(response).sendRedirect("/aita/dashboard");
    }

    @Test
    @DisplayName("POST: Đăng nhập thành công với Sinh viên -> Chuyển hướng Student Portal")
    void testDoPostSuccessStudent() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("phuctv");
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(response).sendRedirect("/aita/student-portal");
    }
}
