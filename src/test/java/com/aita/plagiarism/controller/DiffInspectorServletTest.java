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
@DisplayName("Kiểm thử Trình so sánh mã nguồn (DiffInspectorServlet)")
public class DiffInspectorServletTest {

    private DiffInspectorServlet servlet;

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
        servlet = new DiffInspectorServlet();
        lenient().when(request.getRequestDispatcher("/diff-inspector.jsp")).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("GET: Tải báo cáo mặc định và các khối mã AST trùng lặp cho Giảng viên")
    void testDoGetInstructorView() throws ServletException, IOException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getParameter("reportId")).thenReturn("1");
        when(request.getParameter("role")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(request).setAttribute(eq("report"), any());
        verify(request).setAttribute(eq("matchingBlocks"), any());
        verify(request).setAttribute(eq("isStudent"), eq(false));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Nhận diện góc nhìn Sinh viên qua query param role=student")
    void testDoGetStudentViewViaParam() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("reportId")).thenReturn(null);
        when(request.getParameter("role")).thenReturn("student");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("isStudent"), eq(true));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Xử lý an toàn khi reportId không phải số hợp lệ")
    void testDoGetInvalidReportId() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("reportId")).thenReturn("not_a_number");
        when(request.getParameter("role")).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("report"), any());
        verify(dispatcher).forward(request, response);
    }
}
