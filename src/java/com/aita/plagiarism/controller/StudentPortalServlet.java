package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.Assignment;
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

/**
 * Controller phục vụ Cổng thông tin Sinh viên (Student Portal)
 * URL Mappings: /student-portal
 */
@WebServlet("/student-portal")
public class StudentPortalServlet extends HttpServlet {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
            return;
        }

        // Lấy danh sách bài tập hiện hành
        List<Assignment> assignments = assignmentDAO.getAllAssignments();
        
        // Lấy danh sách bài nộp thực tế của sinh viên này
        List<Submission> mySubmissions = submissionDAO.getSubmissionsByStudent(currentUser.getUserId());

        request.setAttribute("assignments", assignments);
        request.setAttribute("mySubmissions", mySubmissions);
        request.setAttribute("submissionCount", mySubmissions.size());
        request.setAttribute("myResults", new com.aita.plagiarism.dao.PlagiarismDAO().getResultsByStudent(currentUser.getUserId()));

        // Forward tới giao diện student-portal.jsp
        request.getRequestDispatcher("/student-portal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Xử lý nộp đơn giải trình / khiếu nại (Appeal) từ sinh viên
        response.sendError(501, "Chức năng gửi giải trình chưa được triển khai.");
    }
}
