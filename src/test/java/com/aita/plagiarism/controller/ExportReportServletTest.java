package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.AccessPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Unit test cho ExportReportServlet (CSV, không cần DB thật cho các nhánh 400/403/404).
 * Các nhánh 200/DB được kiểm chứng bằng verify_followup_http.py trên DB kiểm thử riêng.
 */
@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@org.junit.jupiter.api.DisplayName("Kiểm thử xuất báo cáo CSV (ExportReportServlet)")
public class ExportReportServletTest {

    private ExportReportServlet servlet;

    @org.mockito.Mock
    private HttpServletRequest request;

    @org.mockito.Mock
    private HttpServletResponse response;

    @org.mockito.Mock
    private HttpSession session;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        servlet = new ExportReportServlet();
        lenient().when(request.getSession(false)).thenReturn(session);
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Thiếu assignmentId trả 400")
    void missingIdReturns400() throws ServletException, IOException {
        User teacher = new User(2, "teacher_ha", "TS. Ha", "ha@example.invalid", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(teacher);
        when(request.getParameter("assignmentId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(400);
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Format khác CSV trả 400")
    void unsupportedFormatReturns400() throws ServletException, IOException {
        User teacher = new User(2, "teacher_ha", "TS. Ha", "ha@example.invalid", "INSTRUCTOR");
        when(session.getAttribute("currentUser")).thenReturn(teacher);
        when(request.getParameter("assignmentId")).thenReturn("1");
        when(request.getParameter("format")).thenReturn("pdf");
        try (var assignments = mockConstruction(AssignmentDAO.class,
                (mock, ctx) -> when(mock.getAssignmentById(1)).thenReturn(new Assignment(1, 1, "A", "D", 100, null)))) {
            servlet.doGet(request, response);
            verify(response).sendError(400);
        }
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Sinh viên ngoài bài tập bị 403 khi export")
    void outsiderStudentReturns403() throws ServletException, IOException {
        User outsider = new User(9, "outsider", "Outsider", "o@example.invalid", "STUDENT");
        when(session.getAttribute("currentUser")).thenReturn(outsider);
        when(request.getParameter("assignmentId")).thenReturn("1");
        try (var submissions = mockConstruction(com.aita.plagiarism.dao.SubmissionDAO.class,
                (mock, ctx) -> when(mock.getSubmissionsByStudent(9)).thenReturn(java.util.List.of()));
             var assignments = mockConstruction(AssignmentDAO.class,
                (mock, ctx) -> when(mock.getAssignmentById(1)).thenReturn(new Assignment(1, 1, "A", "D", 100, null)))) {
            servlet.doGet(request, response);
            verify(response).sendError(403);
        }
    }
}
