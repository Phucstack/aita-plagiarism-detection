package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.*;
import com.aita.plagiarism.service.AccessPolicy;
import com.aita.plagiarism.service.GeminiPlagiarismService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/diff-inspector")
public class DiffInspectorServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(DiffInspectorServlet.class.getName());
    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();
    private final GeminiPlagiarismService geminiService = new GeminiPlagiarismService();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        User actor = request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("currentUser");
        if (actor == null) { response.sendError(403); return; }
        int id;
        try { id = Integer.parseInt(request.getParameter("reportId")); if (id <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { response.sendError(400); return; }
        PlagiarismReport report = plagiarismDAO.getReportById(id);
        if (report == null) { response.sendError(404); return; }
        if ("STUDENT".equals(actor.getRole())) {
            SubmissionDAO dao = new SubmissionDAO();
            Submission a = dao.getSubmissionById(report.getSubmissionAId());
            Submission b = dao.getSubmissionById(report.getSubmissionBId());
            boolean owns = (a != null && a.getStudentId() == actor.getUserId()) || (b != null && b.getStudentId() == actor.getUserId());
            if (!owns) { response.sendError(403); return; }
            // Only own result; do not disclose peer identity, source or matching blocks.
            request.setAttribute("reportId", id);
            request.setAttribute("score", report.getSimilarityScore());
            request.setAttribute("createdAt", report.getCreatedAt());
            request.getRequestDispatcher("/WEB-INF/views/student-result.jsp").forward(request, response);
            return;
        }
        if (!AccessPolicy.canManageAssignment(actor, report.getAssignmentId())) { response.sendError(403); return; }
        Assignment assignment = new com.aita.plagiarism.dao.AssignmentDAO().getAssignmentById(report.getAssignmentId());
        if (assignment == null) { response.sendError(404); return; }
        request.setAttribute("report", report);
        request.setAttribute("reportCourseId", assignment.getCourseId());
        request.setAttribute("matchingBlocks", plagiarismDAO.getMatchingBlocks(id));
        request.setAttribute("isStudent", false);
        request.setAttribute("hasGeminiAnalysis", report.getAiAnalysisSummary() != null
                && report.getAiAnalysisSummary().startsWith("[Gemini]"));
        String flash = (String) request.getSession().getAttribute("aiFlash");
        if (flash != null) {
            request.getSession().removeAttribute("aiFlash");
            request.setAttribute("aiFlash", flash);
        }
        request.getRequestDispatcher("/diff-inspector.jsp").forward(request, response);
    }

    /**
     * Phân tích sâu bằng Gemini cho đúng một cặp trong báo cáo (on-demand).
     * Chỉ ADMIN/INSTRUCTOR quản lý được bài tập mới gọi được; rate limit đơn giản:
     * từ chối khi báo cáo đã có phân tích [Gemini]. Lỗi Gemini không làm gãy trang —
     * báo cáo giữ nguyên nhận định cục bộ và người dùng nhận thông báo thân thiện.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User actor = request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("currentUser");
        if (actor == null || !("ADMIN".equals(actor.getRole()) || "INSTRUCTOR".equals(actor.getRole()))) {
            response.sendError(403);
            return;
        }
        if (!"analyze-deep".equals(request.getParameter("action"))) {
            response.sendError(400);
            return;
        }
        int id;
        try {
            id = Integer.parseInt(request.getParameter("reportId"));
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.sendError(400);
            return;
        }
        PlagiarismReport report = plagiarismDAO.getReportById(id);
        if (report == null) {
            response.sendError(404);
            return;
        }
        if (!AccessPolicy.canManageAssignment(actor, report.getAssignmentId())) {
            response.sendError(403);
            return;
        }

        String notice;
        String current = report.getAiAnalysisSummary();
        if (current != null && current.startsWith("[Gemini]")) {
            // Rate limit: mỗi báo cáo chỉ nhận một phân tích Gemini.
            notice = "Báo cáo này đã có phân tích Gemini; không gọi lại để tránh lặp chi phí.";
        } else if (!geminiService.isConfigured()) {
            notice = "Chưa cấu hình GEMINI_API_KEY nên chưa thể phân tích sâu. Báo cáo giữ nguyên nhận định cục bộ (rule-based).";
        } else {
            SubmissionDAO submissionDAO = new SubmissionDAO();
            Submission a = submissionDAO.getSubmissionById(report.getSubmissionAId());
            Submission b = submissionDAO.getSubmissionById(report.getSubmissionBId());
            String codeA = a == null ? null : readSubmissionContent(a);
            String codeB = b == null ? null : readSubmissionContent(b);
            if (codeA == null || codeA.isBlank() || codeB == null || codeB.isBlank()) {
                notice = "Không đọc được nội dung bài nộp (tệp thiếu hoặc rỗng) nên không thể phân tích.";
            } else {
                try {
                    String apiResponse = geminiService.analyzeCodeSimilarity(codeA, codeB);
                    GeminiPlagiarismService.GeminiAnalysis analysis = GeminiPlagiarismService.parseAnalysis(apiResponse);
                    if (analysis != null) {
                        plagiarismDAO.updateAnalysisSummary(id,
                                "[Gemini] " + GeminiPlagiarismService.renderAnalysis(analysis));
                        notice = "Đã cập nhật phân tích sâu bằng Gemini (kết quả do mô hình sinh ra).";
                    } else {
                        notice = "Gemini không khả dụng hoặc trả về dữ liệu không hợp lệ; báo cáo giữ nguyên nhận định cục bộ (rule-based).";
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Phân tích Gemini on-demand thất bại cho report " + id, e);
                    notice = "Không gọi được Gemini (timeout/mạng/quota); báo cáo giữ nguyên nhận định cục bộ (rule-based).";
                }
            }
        }
        request.getSession().setAttribute("aiFlash", notice);
        response.sendRedirect(request.getContextPath() + "/diff-inspector?reportId=" + id);
    }

    /** @return nội dung tệp bài nộp, hoặc {@code null} nếu không đọc được. */
    private static String readSubmissionContent(Submission sub) {
        java.io.File file = com.aita.plagiarism.config.StorageConfig.resolve(sub.getFilePath());
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
