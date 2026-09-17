package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.model.*;
import com.aita.plagiarism.service.AccessPolicy;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class DiffInspectorServletTest {
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse res = mock(HttpServletResponse.class);
    private void login() {
        var session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(new User(2,"teacher","Teacher","t@example.invalid","INSTRUCTOR"));
    }
    @Test void ownerCanReadAndOtherInstructorCannot() throws Exception {
        login(); when(req.getParameter("reportId")).thenReturn("20");
        var report = new PlagiarismReport(); report.setAssignmentId(7);
        var assignment = new Assignment(); assignment.setCourseId(1);
        var dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/diff-inspector.jsp")).thenReturn(dispatcher);
        try (var dao = mockConstruction(PlagiarismDAO.class, (m,c) -> when(m.getReportById(20)).thenReturn(report));
             var assignments = mockConstruction(com.aita.plagiarism.dao.AssignmentDAO.class, (m,c) -> when(m.getAssignmentById(7)).thenReturn(assignment));
             var policy = mockStatic(AccessPolicy.class)) {
            policy.when(() -> AccessPolicy.canManageAssignment(any(User.class),eq(7))).thenReturn(true);
            new DiffInspectorServlet().doGet(req,res);
            verify(req).setAttribute("report",report); verify(dispatcher).forward(req,res);
            policy.when(() -> AccessPolicy.canManageAssignment(any(User.class),eq(7))).thenReturn(false);
            new DiffInspectorServlet().doGet(req,res);
            verify(res).sendError(403);
            verify(dao.constructed().get(1),never()).getMatchingBlocks(anyInt());
        }
    }
    @Test void roleParameterCannotAuthenticateAnonymousCaller() throws Exception {
        when(req.getParameter("role")).thenReturn("student");
        new DiffInspectorServlet().doGet(req,res);
        verify(res).sendError(403);
        verify(req,never()).getRequestDispatcher(anyString());
    }
    @Test void invalidIdDoesNotSelectReportOne() throws Exception {
        login(); when(req.getParameter("reportId")).thenReturn("invalid");
        try (var dao = mockConstruction(PlagiarismDAO.class)) {
            new DiffInspectorServlet().doGet(req,res);
            verify(res).sendError(400); verifyNoInteractions(dao.constructed().get(0));
        }
    }
}
