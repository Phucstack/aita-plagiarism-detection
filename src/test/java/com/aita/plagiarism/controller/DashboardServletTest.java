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
@DisplayName("Kiểm thử Bảng điều khiển Giảng viên (DashboardServlet)")
public class DashboardServletTest {

    private DashboardServlet servlet;

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
        servlet = new DashboardServlet();
        lenient().when(request.getSession()).thenReturn(session);
        lenient().when(request.getRequestDispatcher("/dashboard.jsp")).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("GET: Giảng viên xem Dashboard mặc định -> Thiết lập đầy đủ Courses, Assignments, Reports")
    void testDoGetInstructorDefault() throws ServletException, IOException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getParameter("courseId")).thenReturn(null);
        when(request.getParameter("assignmentId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setContentType("text/html;charset=UTF-8");
        verify(request).setAttribute(eq("courses"), any());
        verify(request).setAttribute(eq("assignments"), any());
        verify(request).setAttribute(eq("selectedCourseId"), anyInt());
        verify(request).setAttribute(eq("selectedAssignmentId"), anyInt());
        verify(request).setAttribute(eq("currentAssignment"), any());
        verify(request).setAttribute(eq("reports"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Truy vấn Dashboard với tham số courseId và assignmentId chỉ định")
    void testDoGetWithCustomParameters() throws ServletException, IOException {
        User instructor = new User(1, "teacher_ha", "TS. Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(instructor);
        when(request.getParameter("courseId")).thenReturn("1");
        when(request.getParameter("assignmentId")).thenReturn("1");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("selectedCourseId"), eq(1));
        verify(request).setAttribute(eq("selectedAssignmentId"), eq(1));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Xử lý an toàn khi tham số query không phải số nguyên")
    void testDoGetWithInvalidNumberParameters() throws ServletException, IOException {
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getParameter("courseId")).thenReturn("invalid_id");
        when(request.getParameter("assignmentId")).thenReturn("invalid_assignment");

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }
}
