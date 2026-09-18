package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.PlagiarismDAO;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.PlagiarismReport;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.AccessPolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet("/export-report")
public class ExportReportServlet extends HttpServlet {
    private final PlagiarismDAO plagiarismDAO = new PlagiarismDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        User actor = request.getSession(false) == null ? null : (User) request.getSession(false).getAttribute("currentUser");
        if (actor == null) { response.sendError(403); return; }
        int assignmentId;
        try {
            assignmentId = Integer.parseInt(request.getParameter("assignmentId"));
            if (assignmentId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) { response.sendError(400); return; }
        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);
        if (assignment == null) { response.sendError(404); return; }
        String format = request.getParameter("format");
        if (format != null && !"csv".equalsIgnoreCase(format.trim())) { response.sendError(400); return; }
        if ("STUDENT".equals(actor.getRole())) {
            boolean owns = submissionDAO.getSubmissionsByStudent(actor.getUserId()).stream()
                    .anyMatch(s -> s.getAssignmentId() == assignmentId);
            if (!owns) { response.sendError(403); return; }
        } else if (!AccessPolicy.canManageCourse(actor, assignment.getCourseId())) {
            response.sendError(403);
            return;
        }
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"assignment-" + assignmentId + "-reports.csv\"");
        response.setHeader("Cache-Control", "no-store");
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        response.getOutputStream().write(bom);
        try (PrintWriter writer = new PrintWriter(new java.io.OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.println("report_id,similarity_score,risk_level,created_at,student_a_name,student_b_name,ai_analysis_summary");
            for (PlagiarismReport report : plagiarismDAO.getReportsByAssignment(assignmentId)) {
                if ("STUDENT".equals(actor.getRole())) {
                    writer.println(csv(report.getReportId()) + "," + report.getSimilarityScore() + ",REDACTED,"
                            + csv(report.getCreatedAt()) + ",REDACTED,REDACTED,REDACTED");
                } else {
                    writer.println(csv(report.getReportId()) + "," + report.getSimilarityScore() + ","
                            + csv(report.getRiskLevel()) + "," + csv(report.getCreatedAt()) + ","
                            + csv(report.getStudentAName()) + "," + csv(report.getStudentBName()) + ","
                            + csv(report.getAiAnalysisSummary()));
                }
            }
            writer.flush();
        }
    }

    /**
     * Escape một ô CSV. Ngoài việc đóng ngoặc kép, các giá trị bắt đầu bằng
     * = + - @ được thêm prefix ' để trình bảng tính không hiểu chúng thành công thức
     * (CSV injection / formula injection).
     */
    private String csv(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).replace("\"", "\"\"");
        if (!text.isEmpty()) {
            char first = text.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
                text = "'" + text;
            }
        }
        return text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")
                ? "\"" + text + "\"" : text;
    }
}
