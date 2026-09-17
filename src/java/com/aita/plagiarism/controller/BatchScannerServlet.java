package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.service.PlagiarismEngineService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/batch-scanner")
public class BatchScannerServlet extends HttpServlet {

    private final PlagiarismEngineService engineService = new PlagiarismEngineService();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        List<Assignment> assignments = assignmentDAO.getAllAssignments();
        request.setAttribute("assignments", assignments);

        request.getRequestDispatcher("/batch-scanner.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        int assignmentId = 2; // Mặc định bài tập 2 nếu không truyền
        String assignParam = request.getParameter("assignmentId");
        if (assignParam != null && !assignParam.trim().isEmpty()) {
            try {
                assignmentId = Integer.parseInt(assignParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Kích hoạt lõi đối soát thật (Deterministic Java Core: Lexer + Jaccard + Levenshtein + MatchingBlocks)
        int reportsCount = engineService.scanAssignment(assignmentId);

        // Sau khi hoàn thành điều hướng về Dashboard để hiển thị báo cáo thật
        response.sendRedirect(request.getContextPath() + "/dashboard?assignmentId=" + assignmentId + "&scanSuccess=true&count=" + reportsCount);
    }
}
