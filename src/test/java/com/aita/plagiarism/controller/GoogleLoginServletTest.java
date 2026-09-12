package com.aita.plagiarism.controller;

import com.aita.plagiarism.model.User;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Đăng nhập Google OAuth2 (GoogleLoginServlet)")
public class GoogleLoginServletTest {

    private GoogleLoginServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new GoogleLoginServlet();
        lenient().when(request.getSession(true)).thenReturn(session);
    }

    @Test
    @DisplayName("GET/POST: Đăng nhập Google một chạm cho Sinh viên (Fast-Login Demo)")
    void testProcessGoogleAuthStudentDefault() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("name")).thenReturn(null);
        when(request.getParameter("avatar")).thenReturn(null);
        when(request.getParameter("role")).thenReturn("student");
        when(request.getParameter("credential")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doGet(request, response);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals(LoginServlet.AUTH_COOKIE_NAME, cookie.getName());
        assertTrue(cookie.isHttpOnly());

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(response).sendRedirect("/aita/student-portal");
    }

    @Test
    @DisplayName("GET/POST: Đăng nhập Google một chạm cho Giảng viên")
    void testProcessGoogleAuthInstructor() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("name")).thenReturn(null);
        when(request.getParameter("avatar")).thenReturn(null);
        when(request.getParameter("role")).thenReturn("instructor");
        when(request.getParameter("credential")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/aita/dashboard");
    }

    @Test
    @DisplayName("Giải mã credential Google Identity Services ID Token (JWT)")
    void testProcessGoogleAuthWithCredential() throws ServletException, IOException {
        String dummyHeader = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
        String jsonPayload = "{\"email\":\"google_student@fpt.edu.vn\",\"name\":\"Google Student\",\"picture\":\"https://lh3.googleusercontent.com/a/avatar\"}";
        String dummyPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
        String dummyToken = dummyHeader + "." + dummyPayload + ".dummySignature";

        when(request.getParameter("credential")).thenReturn(dummyToken);
        when(request.getParameter("role")).thenReturn("STUDENT");
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(session).setAttribute(eq("currentUser"), userCaptor.capture());
        User authenticatedUser = userCaptor.getValue();
        assertEquals("google_student@fpt.edu.vn", authenticatedUser.getEmail());
        assertEquals("Google Student", authenticatedUser.getFullName());
        assertEquals("STUDENT", authenticatedUser.getRole());

        verify(response).sendRedirect("/aita/student-portal");
    }
}
