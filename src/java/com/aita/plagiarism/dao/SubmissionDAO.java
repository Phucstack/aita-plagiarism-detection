package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.Submission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu độc lập cho Bảng Submissions (Chuẩn 3NF)
 * Hỗ trợ Full CRUD cho bài nộp sinh viên và xác thực mã băm SHA-256
 */
public class SubmissionDAO {

    public int createSubmission(Submission sub) {
        if (sub == null) return -1;
        String sql = "INSERT INTO Submissions (assignment_id, student_id, file_name, file_path, file_type, sha256_hash, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getAssignmentId());
            ps.setInt(2, sub.getStudentId());
            ps.setString(3, sub.getFileName());
            ps.setString(4, sub.getFilePath());
            ps.setString(5, sub.getFileType() != null ? sub.getFileType() : "JAVA");
            ps.setString(6, sub.getSha256Hash() != null ? sub.getSha256Hash() : "");
            ps.setString(7, sub.getStatus() != null ? sub.getStatus() : "PENDING");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        sub.setSubmissionId(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    public List<Submission> getSubmissionsByAssignment(int assignmentId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM Submissions WHERE assignment_id = ? ORDER BY submitted_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    public List<Submission> getSubmissionsByStudent(int studentId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM Submissions WHERE student_id = ? ORDER BY submitted_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    public Submission getSubmissionById(int submissionId) {
        String sql = "SELECT * FROM Submissions WHERE submission_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSubmission(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public boolean deleteSubmission(int submissionId) { return deleteSubmission(submissionId, null); }

    public boolean deleteSubmission(int submissionId, com.aita.plagiarism.model.User actor) {
        String sql = "DELETE FROM Submissions WHERE submission_id = ? AND (? = 1 OR student_id = ? OR ? = 'ADMIN' " +
                "OR EXISTS (SELECT 1 FROM Assignments a JOIN Courses c ON c.course_id = a.course_id " +
                "WHERE a.assignment_id = Submissions.assignment_id AND c.instructor_id = ?))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            ps.setInt(2, actor == null ? 1 : 0);
            ps.setInt(3, actor == null ? -1 : actor.getUserId());
            ps.setString(4, actor == null ? "" : actor.getRole());
            ps.setInt(5, actor != null && "INSTRUCTOR".equals(actor.getRole()) ? actor.getUserId() : -1);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return false;
    }

    public boolean updateSubmissionStatus(int submissionId, String status) {
        String sql = "UPDATE Submissions SET status = ? WHERE submission_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, submissionId);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return false;
    }

    private Submission mapSubmission(ResultSet rs) throws Exception {
        Submission s = new Submission();
        s.setSubmissionId(rs.getInt("submission_id"));
        s.setAssignmentId(rs.getInt("assignment_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setFileName(rs.getString("file_name"));
        s.setFilePath(rs.getString("file_path"));
        s.setFileType(rs.getString("file_type"));
        s.setSha256Hash(rs.getString("sha256_hash"));
        s.setSubmittedAt(rs.getTimestamp("submitted_at"));
        s.setStatus(rs.getString("status"));
        return s;
    }

}
