package com.aita.plagiarism.controller;

import com.aita.plagiarism.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller phục vụ Cổng thông tin Sinh viên (Student Portal)
 * URL Mappings: /student-portal
 */
@WebServlet("/student-portal")
public class StudentPortalServlet extends HttpServlet {

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

        // Forward tới giao diện student-portal.jsp
        request.getRequestDispatcher("/student-portal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Xử lý nộp đơn giải trình / khiếu nại (Appeal) từ sinh viên
        response.sendRedirect(request.getContextPath() + "/student-portal?appealSuccess=true");
    }
}
