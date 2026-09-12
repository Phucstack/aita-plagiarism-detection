package com.aita.plagiarism.controller;

import com.aita.plagiarism.model.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Cổng thông tin Sinh viên (StudentPortalServlet)")
public class StudentPortalServletTest {

    private StudentPortalServlet servlet;

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
        servlet = new StudentPortalServlet();
        lenient().when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("GET: Chặn khi chưa đăng nhập và điều hướng về trang Login")
    void testDoGetUnauthenticated() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/aita/login?error=unauthorized");
    }

    @Test
    @DisplayName("GET: Sinh viên đã đăng nhập -> Chuyển tiếp tới student-portal.jsp")
    void testDoGetAuthenticated() throws ServletException, IOException {
        User student = new User(4, "phuctv", "Trần Văn Phúc", "phuctv@fpt.edu.vn", "STUDENT");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(student);
        when(request.getRequestDispatcher("/student-portal.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Xử lý nộp đơn giải trình (Appeal) -> Chuyển hướng kèm appealSuccess=true")
    void testDoPostAppeal() throws ServletException, IOException {
        when(request.getContextPath()).thenReturn("/aita");

        servlet.doPost(request, response);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/html;charset=UTF-8");
        verify(response).sendRedirect("/aita/student-portal?appealSuccess=true");
    }
}
