package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.*;
import com.aita.plagiarism.service.AccessPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/diff-inspector")
public class DiffInspectorServlet extends HttpServlet {
    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        User actor = request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("currentUser");
        if (actor == null) { response.sendError(403); return; }
        int id;
        try { id = Integer.parseInt(request.getParameter("reportId")); if (id <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { response.sendError(400); return; }
        PlagiarismReport report = plagiarismDAO.getReportById(id);
        if (report == null) { response.sendError(404); return; }
        if ("STUDENT".equals(actor.getRole())) {
            SubmissionDAO dao = new SubmissionDAO();
            Submission a = dao.getSubmissionById(report.getSubmissionAId());
            Submission b = dao.getSubmissionById(report.getSubmissionBId());
            boolean owns = (a != null && a.getStudentId() == actor.getUserId()) || (b != null && b.getStudentId() == actor.getUserId());
            if (!owns) { response.sendError(403); return; }
            // Only own result; do not disclose peer identity, source or matching blocks.
            request.setAttribute("reportId", id);
            request.setAttribute("score", report.getSimilarityScore());
            request.setAttribute("createdAt", report.getCreatedAt());
            request.getRequestDispatcher("/WEB-INF/views/student-result.jsp").forward(request, response);
            return;
        }
        if (!AccessPolicy.canManageAssignment(actor, report.getAssignmentId())) { response.sendError(403); return; }
        Assignment assignment = new com.aita.plagiarism.dao.AssignmentDAO().getAssignmentById(report.getAssignmentId());
        if (assignment == null) { response.sendError(404); return; }
        request.setAttribute("report", report);
        request.setAttribute("reportCourseId", assignment.getCourseId());
        request.setAttribute("matchingBlocks", plagiarismDAO.getMatchingBlocks(id));
        request.setAttribute("isStudent", false);
        request.getRequestDispatcher("/diff-inspector.jsp").forward(request, response);
    }
}
