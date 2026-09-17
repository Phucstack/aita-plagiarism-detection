package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.AccessPolicy;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

@WebServlet("/assignment-action")
public class AssignmentActionServlet extends HttpServlet {
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        User actor = request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("currentUser");
        if (actor == null || !Set.of("ADMIN", "INSTRUCTOR").contains(actor.getRole())) { response.sendError(403); return; }
        String action = request.getParameter("action");
        if (action == null || !Set.of("create", "update", "delete").contains(action)) { response.sendError(400); return; }
        try {
            int courseId = positiveId(request.getParameter("courseId"));
            int assignmentId = 0;
            Assignment existing = null;
            if (!"create".equals(action)) {
                assignmentId = positiveId(request.getParameter("assignmentId"));
                existing = assignmentDAO.getAssignmentById(assignmentId);
                if (existing == null) { response.sendError(404); return; }
                if (existing.getCourseId() != courseId) { response.sendError(403); return; }
            }
            if (!AccessPolicy.canManageCourse(actor, courseId)) { response.sendError(403); return; }
            boolean saved;
            if ("delete".equals(action)) {
                saved = assignmentDAO.deleteAssignment(assignmentId, actor);
            } else {
                String title = request.getParameter("title");
                if (title == null || title.trim().isEmpty() || title.trim().length() > 150) { response.sendError(400); return; }
                double max = number(request.getParameter("maxScore"), 100);
                double threshold = number(request.getParameter("similarityThreshold"), 75);
                if (!Double.isFinite(max) || max <= 0 || max > 999.99 || !Double.isFinite(threshold) || threshold < 0 || threshold > 100) {
                    response.sendError(400); return;
                }
                Assignment a = new Assignment();
                a.setAssignmentId(assignmentId); a.setCourseId(courseId); a.setTitle(title.trim());
                a.setDescription(request.getParameter("description") == null ? "" : request.getParameter("description").trim());
                a.setMaxScore(max); a.setSimilarityThreshold(threshold);
                String deadline = request.getParameter("deadline");
                a.setDeadline(deadline == null || deadline.isBlank() ? (existing == null ? Timestamp.valueOf(LocalDateTime.now().plusDays(14)) : existing.getDeadline())
                        : Timestamp.valueOf(LocalDateTime.parse(deadline)));
                if ("create".equals(action)) { assignmentId = assignmentDAO.createAssignment(a, actor); saved = assignmentId > 0; }
                else saved = assignmentDAO.updateAssignment(a, actor);
            }
            if (!saved) { response.sendError(409); return; }
            response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId
                    + ("delete".equals(action) ? "" : "&assignmentId=" + assignmentId)
                    + "&assignmentMsg=" + action + "d");
        } catch (IllegalArgumentException | DateTimeParseException e) { response.sendError(400); }
    }
    private static int positiveId(String value) {
        int id = Integer.parseInt(value); if (id <= 0) throw new IllegalArgumentException(); return id;
    }
    private static double number(String value, double fallback) {
        return value == null || value.isBlank() ? fallback : Double.parseDouble(value);
    }
}
