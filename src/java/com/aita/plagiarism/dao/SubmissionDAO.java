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
import java.util.Set;

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

    public List<Submission> getSubmissionsByAssignmentAndStatus(int assignmentId, String status) {
        List<Submission> list = new ArrayList<>();
        String normalized = status == null ? "" : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!java.util.Set.of("PENDING", "PARSED", "ANALYZED", "FLAGGED").contains(normalized)) {
            throw new IllegalArgumentException("Unsupported submission status filter.");
        }
        String sql = "SELECT * FROM Submissions WHERE assignment_id = ? AND status = ? ORDER BY submitted_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
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

    /**
     * Xóa bài nộp. Bắt buộc truyền {@code actor}: không còn overload thiếu actor,
     * vì thiếu actor từng đồng nghĩa với việc bỏ qua toàn bộ kiểm tra sở hữu.
     */
    public boolean deleteSubmission(int submissionId, com.aita.plagiarism.model.User actor) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required: ownership checks must never be skipped");
        }
        String sql = "DELETE FROM Submissions WHERE submission_id = ? AND (? = 1 OR student_id = ? OR ? = 'ADMIN' " +
                "OR EXISTS (SELECT 1 FROM Assignments a JOIN Courses c ON c.course_id = a.course_id " +
                "WHERE a.assignment_id = Submissions.assignment_id AND c.instructor_id = ?))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            ps.setInt(2, 0);
            ps.setInt(3, actor.getUserId());
            ps.setString(4, actor.getRole());
            ps.setInt(5, "INSTRUCTOR".equals(actor.getRole()) ? actor.getUserId() : -1);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return false;
    }

    public boolean updateSubmissionStatus(int submissionId, String status) {
        try (Connection conn = DBContext.getConnection()) {
            return updateSubmissionStatus(submissionId, status, conn);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /** Cập nhật trạng thái trong cùng một giao dịch do người gọi quản lý. */
    public boolean updateSubmissionStatus(int submissionId, String status, Connection conn) {
        String sql = "UPDATE Submissions SET status = ? WHERE submission_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, submissionId);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return false;
    }

    /**
     * Gán trạng thái cho toàn bộ bài nộp của một bài tập sau khi quét: bài nằm trong ít
     * nhất một cặp HIGH_RISK thành {@code FLAGGED}, còn lại thành {@code ANALYZED}.
     *
     * Dùng 2 câu lệnh thay vì 2 câu cho mỗi cặp bài nộp.
     */
    public boolean markSubmissionsByOutcome(int assignmentId, Set<Integer> flaggedIds, Connection conn) {
        if (conn == null) throw new IllegalArgumentException("connection is required");
        Set<Integer> flagged = flaggedIds == null ? Set.of() : flaggedIds;
        try {
            if (!flagged.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        buildInClause("UPDATE Submissions SET status = 'FLAGGED' WHERE assignment_id = ? AND submission_id IN (", flagged.size()) + ")")) {
                    ps.setInt(1, assignmentId);
                    bindIds(ps, 2, flagged);
                    ps.executeUpdate();
                }
            }
            // Những bài không bị gắn cờ -> ANALYZED (câu lệnh này chạy cả khi flagged rỗng).
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Submissions SET status = 'ANALYZED' WHERE assignment_id = ?"
                            + (flagged.isEmpty() ? "" :
                            " AND submission_id NOT IN (" + placeholders(flagged.size()) + ")"))) {
                ps.setInt(1, assignmentId);
                bindIds(ps, 2, flagged);
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
        }
        return sb.toString();
    }

    private static String buildInClause(String prefix, int count) {
        return prefix + placeholders(count);
    }

    private static void bindIds(PreparedStatement ps, int startIndex, Set<Integer> ids) throws java.sql.SQLException {
        int i = startIndex;
        for (Integer id : ids) {
            ps.setInt(i++, id);
        }
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
