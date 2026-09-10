package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.PlagiarismReport;
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
            if (courses.isEmpty()) {
                courses = courseDAO.getAllCourses();
            }
        } else {
            courses = courseDAO.getAllCourses();
        }

        int selectedCourseId = 1;
        String courseParam = request.getParameter("courseId");
        if (courseParam != null && !courseParam.trim().isEmpty()) {
            try {
                selectedCourseId = Integer.parseInt(courseParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (!courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        // Lấy danh sách bài tập của khóa học được chọn
        List<Assignment> assignments = courseDAO.getAssignmentsByCourse(selectedCourseId);
        int selectedAssignmentId = 1;
        String assignmentParam = request.getParameter("assignmentId");
        if (assignmentParam != null && !assignmentParam.trim().isEmpty()) {
            try {
                selectedAssignmentId = Integer.parseInt(assignmentParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (!assignments.isEmpty()) {
            selectedAssignmentId = assignments.get(0).getAssignmentId();
        }

        Assignment currentAssignment = courseDAO.getAssignmentById(selectedAssignmentId);
        List<PlagiarismReport> reports = plagiarismDAO.getReportsByAssignment(selectedAssignmentId);

        request.setAttribute("courses", courses);
        request.setAttribute("assignments", assignments);
        request.setAttribute("selectedCourseId", selectedCourseId);
        request.setAttribute("selectedAssignmentId", selectedAssignmentId);
        request.setAttribute("currentAssignment", currentAssignment);
        request.setAttribute("reports", reports);

        // Chuyển tiếp sang View JSP chuẩn mô hình MVC2
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
