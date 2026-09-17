package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller xử lý các tác vụ CRUD Bài tập (Assignments) cho Giảng viên
 * URL Pattern: /assignment-action
 */
@WebServlet("/assignment-action")
public class AssignmentActionServlet extends HttpServlet {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null || (!"INSTRUCTOR".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "create": {
                try {
                    int courseId = Integer.parseInt(request.getParameter("courseId"));
                    String title = request.getParameter("title");
                    String description = request.getParameter("description");
                    double maxScore = parseDoubleSafe(request.getParameter("maxScore"), 100.0);
                    double threshold = parseDoubleSafe(request.getParameter("similarityThreshold"), 75.0);
                    Timestamp deadline = parseTimestampSafe(request.getParameter("deadline"));

                    if (title != null && !title.trim().isEmpty()) {
                        Assignment a = new Assignment();
                        a.setCourseId(courseId);
                        a.setTitle(title.trim());
                        a.setDescription(description != null ? description.trim() : "");
                        a.setMaxScore(maxScore);
                        a.setDeadline(deadline);
                        a.setSimilarityThreshold(threshold);

                        int newId = assignmentDAO.createAssignment(a);
                        response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId + "&assignmentId=" + newId + "&assignmentMsg=created");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId + "&assignmentError=missing_title");
                    }
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?assignmentError=create_failed");
                }
                break;
            }

            case "update": {
                try {
                    int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
                    int courseId = Integer.parseInt(request.getParameter("courseId"));
                    String title = request.getParameter("title");
                    String description = request.getParameter("description");
                    double maxScore = parseDoubleSafe(request.getParameter("maxScore"), 100.0);
                    double threshold = parseDoubleSafe(request.getParameter("similarityThreshold"), 75.0);
                    Timestamp deadline = parseTimestampSafe(request.getParameter("deadline"));

                    Assignment a = new Assignment();
                    a.setAssignmentId(assignmentId);
                    a.setCourseId(courseId);
                    a.setTitle(title.trim());
                    a.setDescription(description != null ? description.trim() : "");
                    a.setMaxScore(maxScore);
                    a.setDeadline(deadline);
                    a.setSimilarityThreshold(threshold);

                    assignmentDAO.updateAssignment(a);
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId + "&assignmentId=" + assignmentId + "&assignmentMsg=updated");
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?assignmentError=update_failed");
                }
                break;
            }

            case "delete": {
                try {
                    int assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
                    int courseId = Integer.parseInt(request.getParameter("courseId"));
                    assignmentDAO.deleteAssignment(assignmentId);
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId + "&assignmentMsg=deleted");
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?assignmentError=delete_failed");
                }
                break;
            }

            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }

    private double parseDoubleSafe(String val, double defaultVal) {
        if (val == null || val.trim().isEmpty()) return defaultVal;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Timestamp parseTimestampSafe(String val) {
        if (val == null || val.trim().isEmpty()) {
            return new Timestamp(System.currentTimeMillis() + 86400000L * 14); // 14 ngày tới
        }
        try {
            // Định dạng HTML5 datetime-local: yyyy-MM-dd'T'HH:mm
            String formatted = val.replace("T", " ");
            if (formatted.length() == 16) {
                formatted += ":00";
            }
            return Timestamp.valueOf(formatted);
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis() + 86400000L * 14);
        }
    }
}
