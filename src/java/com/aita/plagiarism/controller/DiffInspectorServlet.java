package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.model.MatchingBlock;
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

@WebServlet("/diff-inspector")
public class DiffInspectorServlet extends HttpServlet {

    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        String roleParam = request.getParameter("role");

        boolean isStudent = (currentUser != null && "STUDENT".equalsIgnoreCase(currentUser.getRole()))
                || "student".equalsIgnoreCase(roleParam);

        String reportIdParam = request.getParameter("reportId");
        int reportId = 1;
        if (reportIdParam != null && !reportIdParam.trim().isEmpty()) {
            try {
                reportId = Integer.parseInt(reportIdParam);
            } catch (NumberFormatException ignored) {}
        }

        PlagiarismReport report = plagiarismDAO.getReportById(reportId);
        List<MatchingBlock> matchingBlocks = plagiarismDAO.getMatchingBlocks(reportId);

        request.setAttribute("report", report);
        request.setAttribute("matchingBlocks", matchingBlocks);
        request.setAttribute("isStudent", isStudent);

        // Chuyển tiếp sang View JSP chuẩn MVC2
        request.getRequestDispatcher("/diff-inspector.jsp").forward(request, response);
    }
}
