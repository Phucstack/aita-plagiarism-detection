package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.AccessPolicy;
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

        User actor = (User) request.getSession().getAttribute("currentUser");
        List<Assignment> assignments = assignmentDAO.getAllAssignments().stream()
                .filter(a -> AccessPolicy.canManageCourse(actor, a.getCourseId())).toList();
        request.setAttribute("assignments", assignments);

        request.getRequestDispatcher("/batch-scanner.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        int assignmentId;
        try { assignmentId = Integer.parseInt(request.getParameter("assignmentId")); if (assignmentId <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { response.sendError(400); return; }
        User actor = (User) request.getSession().getAttribute("currentUser");
        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);
        if (assignment == null) { response.sendError(404); return; }
        if (!AccessPolicy.canManageAssignment(actor, assignmentId)) { response.sendError(403); return; }

        // Kích hoạt lõi đối soát thật (Deterministic Java Core: Lexer + Jaccard + Levenshtein + MatchingBlocks)
        PlagiarismEngineService.ScanResult result = engineService.scanAssignment(assignmentId);

        // Sau khi hoàn thành điều hướng về Dashboard để hiển thị báo cáo thật.
        // skipped > 0 nghĩa là có cặp không đọc được nội dung file và đã bị bỏ qua.
        response.sendRedirect(request.getContextPath() + "/dashboard?courseId=" + assignment.getCourseId()
                + "&assignmentId=" + assignmentId + "&scanSuccess=true&count=" + result.getReportsCreated()
                + "&skipped=" + result.getSkippedPairs());
    }
}
