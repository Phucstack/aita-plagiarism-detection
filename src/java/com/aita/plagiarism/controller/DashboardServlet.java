package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.PlagiarismReport;
import com.aita.plagiarism.model.Submission;
import com.aita.plagiarism.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // Lấy danh sách khóa học theo giảng viên hiện tại hoặc tất cả các môn
        List<Course> courses;
        if (currentUser != null && "INSTRUCTOR".equalsIgnoreCase(currentUser.getRole())) {
            courses = courseDAO.getCoursesByInstructor(currentUser.getUserId());

        } else {
            courses = courseDAO.getAllCourses();
        }

        int selectedCourseId = 0;
        String courseParam = request.getParameter("courseId");
        if (courseParam != null && !courseParam.trim().isEmpty()) {
            try {
                selectedCourseId = Integer.parseInt(courseParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (!courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        if (selectedCourseId != 0) {
            Course selected = courseDAO.getCourseById(selectedCourseId);
            if (selected == null) { response.sendError(404); return; }
            if (!"ADMIN".equals(currentUser.getRole()) && selected.getInstructorId() != currentUser.getUserId()) {
                response.sendError(403); return;
            }
        }

        // Lấy danh sách bài tập của khóa học được chọn
        List<Assignment> assignments = courseDAO.getAssignmentsByCourse(selectedCourseId);
        int selectedAssignmentId = 0;
        String assignmentParam = request.getParameter("assignmentId");
        if (assignmentParam != null && !assignmentParam.trim().isEmpty()) {
            try {
                selectedAssignmentId = Integer.parseInt(assignmentParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (!assignments.isEmpty()) {
            selectedAssignmentId = assignments.get(0).getAssignmentId();
        }

        Assignment currentAssignment = courseDAO.getAssignmentById(selectedAssignmentId);
        if (selectedAssignmentId != 0 && currentAssignment == null) { response.sendError(404); return; }
        if (currentAssignment != null && currentAssignment.getCourseId() != selectedCourseId) {
            response.sendError(403); return;
        }
        List<PlagiarismReport> reports = plagiarismDAO.getReportsByAssignment(selectedAssignmentId);
        List<Submission> submissions = submissionDAO.getSubmissionsByAssignment(selectedAssignmentId);
        String statusFilter = request.getParameter("status");
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            if (!java.util.Set.of("PENDING", "PARSED", "ANALYZED", "FLAGGED").contains(statusFilter.trim().toUpperCase(java.util.Locale.ROOT))) {
                response.sendError(400);
                return;
            }
            submissions = submissionDAO.getSubmissionsByAssignmentAndStatus(selectedAssignmentId, statusFilter);
        }

        double threshold = currentAssignment == null ? 75 : currentAssignment.getSimilarityThreshold();
        request.setAttribute("courseCount", courses.size());
        request.setAttribute("assignmentCount", assignments.size());
        request.setAttribute("submissionCount", submissions.size());
        request.setAttribute("reportCount", reports.size());
        request.setAttribute("reportThreshold", threshold);
        request.setAttribute("flaggedCount", reports.stream().filter(r -> r.getSimilarityScore() >= threshold).count());
        request.setAttribute("averageScore", String.format(java.util.Locale.ROOT, "%.2f",
                reports.stream().mapToDouble(PlagiarismReport::getSimilarityScore).average().orElse(0)));
        request.setAttribute("courses", courses);
        request.setAttribute("assignments", assignments);
        request.setAttribute("selectedCourseId", selectedCourseId);
        request.setAttribute("selectedAssignmentId", selectedAssignmentId);
        request.setAttribute("currentAssignment", currentAssignment);
        request.setAttribute("selectedDeadline", currentAssignment == null || currentAssignment.getDeadline() == null ? ""
                : currentAssignment.getDeadline().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        request.setAttribute("reports", reports);
        request.setAttribute("submissions", submissions);
        request.setAttribute("statusFilter", statusFilter == null ? "" : statusFilter.trim().toUpperCase(java.util.Locale.ROOT));
        request.setAttribute("similarityMatrix", plagiarismDAO.getSimilarityMatrix(selectedAssignmentId));

        // Chuyển tiếp sang View JSP chuẩn mô hình MVC2
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
