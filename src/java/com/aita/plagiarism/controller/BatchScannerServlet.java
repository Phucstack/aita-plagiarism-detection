package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.AccessPolicy;
import com.aita.plagiarism.service.PlagiarismEngineService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/batch-scanner")
public class BatchScannerServlet extends HttpServlet {

    private final PlagiarismEngineService engineService = new PlagiarismEngineService();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        User actor = (User) request.getSession().getAttribute("currentUser");
        List<Assignment> assignments = listAssignmentsFor(actor);
        request.setAttribute("assignments", assignments);

        request.getRequestDispatcher("/batch-scanner.jsp").forward(request, response);
    }

    /**
     * Lấy các bài tập mà người dùng được phép quét.
     *
     * Trước đây dùng {@code AccessPolicy.canManageCourse} cho từng bài tập, sinh ra
     * N+1 truy vấn (mỗi bài tập một lần xuống CSDL). Ở đây lấy danh sách khóa học được
     * quản lý một lần rồi lọc trong bộ nhớ.
     */
    private List<Assignment> listAssignmentsFor(User actor) {
        List<Assignment> all = assignmentDAO.getAllAssignments();
        if (actor == null) return List.of();
        if ("ADMIN".equals(actor.getRole())) return all;

        Set<Integer> managed = new HashSet<>();
        for (Course course : new CourseDAO().getCoursesByInstructor(actor.getUserId())) {
            managed.add(course.getCourseId());
        }
        return all.stream().filter(a -> managed.contains(a.getCourseId())).toList();
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
