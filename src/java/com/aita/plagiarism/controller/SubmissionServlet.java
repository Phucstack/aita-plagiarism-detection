package com.aita.plagiarism.controller;

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
            int assignmentId = 1;
            String assignParam = request.getParameter("assignmentId");
            if (assignParam != null && !assignParam.trim().isEmpty()) {
                assignmentId = Integer.parseInt(assignParam.trim());
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

            // Tạo thư mục lưu trữ an toàn
            String uploadDir = getServletContext().getRealPath("/uploads");
            if (uploadDir == null) {
                uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "aita_uploads";
            }
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String safeFileName = "sub_" + currentUser.getUserId() + "_" + System.currentTimeMillis() + "_" + submittedFileName;
            File targetFile = new File(uploadFolder, safeFileName);

            String sha256Hash;
            try (InputStream is = filePart.getInputStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");

                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                    fos.write(buffer, 0, bytesRead);
                }
                
                StringBuilder hex = new StringBuilder();
                for (byte b : digest.digest()) {
                    String h = Integer.toHexString(0xff & b);
                    if (h.length() == 1) hex.append('0');
                    hex.append(h);
                }
                sha256Hash = hex.toString();
            } catch (Exception ex) {
                sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
            }

            // Ghi bản ghi vào CSDL
            Submission sub = new Submission();
            sub.setAssignmentId(assignmentId);
            sub.setStudentId(currentUser.getUserId());
            sub.setFileName(submittedFileName);
            sub.setFilePath(targetFile.getAbsolutePath());
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
                // Xóa file trên đĩa nếu tồn tại
                if (sub.getFilePath() != null) {
                    File f = new File(sub.getFilePath());
                    if (f.exists()) f.delete();
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
