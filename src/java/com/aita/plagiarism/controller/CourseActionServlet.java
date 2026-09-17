package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller xử lý các tác vụ CRUD Khóa học (Courses) cho Giảng viên & Admin
 * URL Pattern: /course-action
 */
@WebServlet("/course-action")
public class CourseActionServlet extends HttpServlet {

    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // Chỉ Giảng viên hoặc Admin mới có quyền thao tác
        if (currentUser == null || (!"INSTRUCTOR".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            response.sendRedirect(request.getContextPath() + "/dashboard?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "create": {
                String code = request.getParameter("courseCode");
                String name = request.getParameter("courseName");
                String semester = request.getParameter("semester");

                if (code != null && !code.trim().isEmpty() && name != null && !name.trim().isEmpty()) {
                    Course c = new Course();
                    c.setCourseCode(code.trim());
                    c.setCourseName(name.trim());
                    c.setInstructorId(currentUser.getUserId());
                    c.setSemester(semester != null ? semester.trim() : "Fall 2026");

                    int newId = courseDAO.createCourse(c);
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + newId + "&courseMsg=created");
                } else {
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseError=missing_fields");
                }
                break;
            }

            case "update": {
                String idStr = request.getParameter("courseId");
                String code = request.getParameter("courseCode");
                String name = request.getParameter("courseName");
                String semester = request.getParameter("semester");

                try {
                    int courseId = Integer.parseInt(idStr.trim());
                    Course c = new Course();
                    c.setCourseId(courseId);
                    c.setCourseCode(code.trim());
                    c.setCourseName(name.trim());
                    c.setSemester(semester.trim());

                    courseDAO.updateCourse(c);
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + courseId + "&courseMsg=updated");
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseError=invalid_id");
                }
                break;
            }

            case "delete": {
                String idStr = request.getParameter("courseId");
                try {
                    int courseId = Integer.parseInt(idStr.trim());
                    courseDAO.deleteCourse(courseId);
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseMsg=deleted");
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/dashboard?courseError=delete_failed");
                }
                break;
            }

            default:
                response.sendRedirect(request.getContextPath() + "/dashboard");
                break;
        }
    }
}
