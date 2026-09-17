package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/course-action")
public class CourseActionServlet extends HttpServlet {
    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User actor = session == null ? null : (User) session.getAttribute("currentUser");
        if (actor == null || (!"INSTRUCTOR".equals(actor.getRole()) && !"ADMIN".equals(actor.getRole()))) {
            response.sendError(403);
            return;
        }
        String action = request.getParameter("action");
        if (!"create".equals(action) && !"update".equals(action) && !"delete".equals(action)) {
            response.sendError(400);
            return;
        }
        int id = 0;
        if (!"create".equals(action)) {
            try { id = Integer.parseInt(request.getParameter("courseId")); }
            catch (NumberFormatException e) { response.sendError(400); return; }
            Course existing = courseDAO.getCourseById(id);
            if (existing == null) { response.sendError(404); return; }
            if (!"ADMIN".equals(actor.getRole()) && existing.getInstructorId() != actor.getUserId()) {
                response.sendError(403);
                return;
            }
        }
        String result;
        if ("delete".equals(action)) {
            if (!courseDAO.deleteCourse(id, actor)) { response.sendError(409); return; }
            result = "deleted";
        } else {
            String code = request.getParameter("courseCode");
            String name = request.getParameter("courseName");
            String semester = request.getParameter("semester");
            if (!valid(code, 20) || !valid(name, 150) || !valid(semester, 20)) {
                response.sendError(400);
                return;
            }
            Course course = new Course();
            course.setCourseId(id);
            course.setCourseCode(code.trim());
            course.setCourseName(name.trim());
            course.setSemester(semester.trim());
            course.setInstructorId(actor.getUserId());
            if ("create".equals(action)) {
                id = courseDAO.createCourse(course);
                if (id <= 0) { response.sendError(503); return; }
                result = "created";
            } else {
                if (!courseDAO.updateCourse(course, actor)) { response.sendError(409); return; }
                result = "updated";
            }
        }
        response.sendRedirect(request.getContextPath() + "/dashboard?courseMsg=" + result
                + ("deleted".equals(result) ? "" : "&courseId=" + id));
    }

    private boolean valid(String value, int max) {
        return value != null && !value.isBlank() && value.trim().length() <= max;
    }
}
