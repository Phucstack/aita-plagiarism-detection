package com.aita.plagiarism.controller;

import com.aita.plagiarism.config.StorageConfig;
import com.aita.plagiarism.dao.SubmissionDAO;
import com.aita.plagiarism.model.Submission;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.util.SHA256ChecksumUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Controller tiếp nhận nộp bài thực tế từ Sinh viên (File Ingestion Pipeline)
 * Hỗ trợ: Upload file thật, băm mã SHA-256 byte stream, lưu trữ đĩa an toàn, xóa bài nộp
 * URL Pattern: /submit, /submission-action
 */
@WebServlet({"/submit", "/submission-action"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 25 * 1024 * 1024,       // 25 MB
        maxRequestSize = 30 * 1024 * 1024     // 30 MB
)
public class SubmissionServlet extends HttpServlet {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            handleDeleteSubmission(request, response, currentUser);
            return;
        }

        // Xử lý nộp file bài tập
        try {
            // assignmentId là bắt buộc: không bao giờ ngầm gán một bài tập mặc định,
            // vì làm vậy có thể gán nhầm bài nộp vào bài tập của người khác.
            String assignParam = request.getParameter("assignmentId");
            if (assignParam == null || assignParam.trim().isEmpty()) {
                response.sendError(400, "Thiếu assignmentId. Không thể xác định bài tập cần nộp.");
                return;
            }
            int assignmentId;
            try {
                assignmentId = Integer.parseInt(assignParam.trim());
            } catch (NumberFormatException e) {
                response.sendError(400, "assignmentId không hợp lệ.");
                return;
            }
            if (assignmentId <= 0) {
                response.sendError(400, "assignmentId không hợp lệ.");
                return;
            }

            // Bài tập phải tồn tại. Nếu không, từ chối ngay thay vì để CSDL ném lỗi khoá ngoại.
            if (new com.aita.plagiarism.dao.AssignmentDAO().getAssignmentById(assignmentId) == null) {
                response.sendError(404, "Bài tập không tồn tại.");
                return;
            }
            // Còn hạn nộp? (Chính sách đầy đủ cần quan hệ enrolment — xem AccessPolicy.canSubmitTo)
            if (!com.aita.plagiarism.service.AccessPolicy.canSubmitTo(currentUser, assignmentId)) {
                response.sendError(403, "Bài tập đã hết hạn nộp.");
                return;
            }

            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                response.sendRedirect(request.getContextPath() + "/student-portal?submitError=empty_file");
                return;
            }

            String submittedFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String fileExt = getFileExtension(submittedFileName).toUpperCase();

            // Whitelist định dạng: JAVA, TEXT, DOCX, ZIP
            if (!fileExt.equals("JAVA") && !fileExt.equals("TXT") && !fileExt.equals("DOCX") && !fileExt.equals("ZIP")) {
                response.sendRedirect(request.getContextPath() + "/student-portal?submitError=invalid_format");
                return;
            }

            // Thư mục lưu trữ nằm NGOÀI web root (AITA_UPLOAD_DIR, mặc định
            // ${catalina.base}/aita-uploads) — không thể bị truy cập qua HTTP.
            String safeFileName = "sub_" + currentUser.getUserId() + "_" + System.currentTimeMillis() + "_" + submittedFileName;
            File targetFile = StorageConfig.newTarget(safeFileName);

            // Băm và ghi xuống đĩa trong một lượt đọc. Nếu băm lỗi, thao tác bị từ chối:
            // không bao giờ lưu mã băm thay thế.
            String sha256Hash;
            try (InputStream is = filePart.getInputStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {
                sha256Hash = SHA256ChecksumUtil.digestAndWrite(is, fos);
            } catch (Exception ex) {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                response.sendError(500, "Không tính được mã băm SHA-256 cho tệp tải lên. Bài nộp chưa được lưu.");
                return;
            }

            // Ghi bản ghi vào CSDL. Lưu đường dẫn tương đối để có thể di chuyển thư mục lưu trữ.
            Submission sub = new Submission();
            sub.setAssignmentId(assignmentId);
            sub.setStudentId(currentUser.getUserId());
            sub.setFileName(submittedFileName);
            sub.setFilePath(safeFileName);
            sub.setFileType(fileExt.equals("TXT") ? "TEXT" : fileExt);
            sub.setSha256Hash(sha256Hash);
            sub.setStatus("PENDING");

            int newSubId = submissionDAO.createSubmission(sub);

            response.sendRedirect(request.getContextPath() + "/student-portal?submitSuccess=true&subId=" + newSubId + "&hash=" + sha256Hash);

        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/student-portal?submitError=upload_failed");
        }
    }

    private void handleDeleteSubmission(HttpServletRequest request, HttpServletResponse response, User currentUser) 
            throws IOException {
        String subIdStr = request.getParameter("submissionId");
        try {
            int subId = Integer.parseInt(subIdStr.trim());
            Submission sub = submissionDAO.getSubmissionById(subId);

            // Kiểm tra quyền sở hữu bài nộp (chỉ chính sinh viên đó hoặc Giảng viên/Admin mới được xóa)
            if (sub != null && (sub.getStudentId() == currentUser.getUserId() || com.aita.plagiarism.service.AccessPolicy.canManageAssignment(currentUser, sub.getAssignmentId()))) {
                if (!submissionDAO.deleteSubmission(subId, currentUser)) { response.sendError(409); return; }
                // Xóa file trên đĩa nếu tồn tại (hỗ trợ cả đường dẫn tuyệt đối cũ)
                File f = StorageConfig.resolve(sub.getFilePath());
                if (f != null && f.exists()) {
                    f.delete();
                }
                response.sendRedirect(request.getContextPath() + "/student-portal?subMsg=deleted");
            } else {
                response.sendError(403);
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/student-portal?subError=delete_failed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? "" : filename.substring(dot + 1);
    }
}
