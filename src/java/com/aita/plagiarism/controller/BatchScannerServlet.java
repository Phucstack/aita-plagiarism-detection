package com.aita.plagiarism.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/batch-scanner")
public class BatchScannerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/batch-scanner.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Mô phỏng nhận đợt nộp bài và kích hoạt quét AI
        // Sau khi hoàn thành điều hướng về Dashboard để hiển thị báo cáo
        response.sendRedirect(request.getContextPath() + "/dashboard?scanSuccess=true");
    }
}
