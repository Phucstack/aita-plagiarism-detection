package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.MatchingBlock;
import com.aita.plagiarism.model.PlagiarismReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlagiarismDAO {

    public List<PlagiarismReport> getResultsByStudent(int studentId) {
        String sql = "SELECT pr.report_id, pr.similarity_score, pr.created_at FROM PlagiarismReports pr "
                + "JOIN Submissions a ON a.submission_id = pr.submission_a_id "
                + "JOIN Submissions b ON b.submission_id = pr.submission_b_id "
                + "WHERE a.student_id = ? OR b.student_id = ? ORDER BY pr.created_at DESC";
        List<PlagiarismReport> results = new ArrayList<>();
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlagiarismReport report = new PlagiarismReport();
                    report.setReportId(rs.getInt(1)); report.setSimilarityScore(rs.getDouble(2));
                    report.setCreatedAt(rs.getTimestamp(3)); results.add(report);
                }
            }
        } catch (Exception e) { throw new DataAccessException(e); }
        return results;
    }

    public int createReport(PlagiarismReport r) {
        try (Connection conn = DBContext.getConnection()) {
            return createReport(r, conn);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /** Ghi báo cáo trong cùng một giao dịch do người gọi quản lý. */
    public int createReport(PlagiarismReport r, Connection conn) {
        if (r == null) return -1;
        if (r.getSubmissionAId() <= 0 || r.getSubmissionBId() <= 0
                || r.getSubmissionAId() == r.getSubmissionBId()) {
            throw new IllegalArgumentException("A plagiarism report requires two distinct submissions.");
        }
        // Chỉ truy vấn xác nhận khi người gọi CHƯA biết assignment. Lượt quét lấy bài nộp
        // bằng getSubmissionsByAssignment nên mọi cặp chắc chắn cùng assignment — việc
        // kiểm tra lại cho từng cặp tốn thêm ~1.225 SELECT mỗi lượt quét 50 bài.
        if (r.getAssignmentId() <= 0) {
            String assignmentSql = "SELECT sa.assignment_id FROM Submissions sa " +
                    "JOIN Submissions sb ON sb.submission_id = ? " +
                    "WHERE sa.submission_id = ? AND sa.assignment_id = sb.assignment_id";
            try (PreparedStatement assignmentPs = conn.prepareStatement(assignmentSql)) {
                assignmentPs.setInt(1, r.getSubmissionBId());
                assignmentPs.setInt(2, r.getSubmissionAId());
                try (ResultSet assignmentRs = assignmentPs.executeQuery()) {
                    if (!assignmentRs.next()) {
                        throw new IllegalArgumentException("Both submissions must exist and belong to the same assignment.");
                    }
                    r.setAssignmentId(assignmentRs.getInt(1));
                }
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }

        try {
            boolean legacySchema = hasLegacyAssignmentColumn(conn);
            String sql = legacySchema
                    ? "INSERT INTO PlagiarismReports (assignment_id, submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) VALUES (?, ?, ?, ?, ?, ?)"
                    : "INSERT INTO PlagiarismReports (submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int offset = 0;
                if (legacySchema) {
                    ps.setInt(1, r.getAssignmentId());
                    offset = 1;
                }
                ps.setInt(1 + offset, r.getSubmissionAId());
                ps.setInt(2 + offset, r.getSubmissionBId());
                ps.setDouble(3 + offset, r.getSimilarityScore());
                ps.setString(4 + offset, r.getRiskLevel() != null ? r.getRiskLevel() : "SAFE");
                ps.setString(5 + offset, r.getAiAnalysisSummary());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            r.setReportId(id);
                            return id;
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    private boolean hasLegacyAssignmentColumn(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT CASE WHEN COL_LENGTH('dbo.PlagiarismReports', 'assignment_id') IS NULL THEN 0 ELSE 1 END");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    public int createMatchingBlock(MatchingBlock b) {
        try (Connection conn = DBContext.getConnection()) {
            return createMatchingBlock(b, conn);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /** Ghi khối mã trong cùng một giao dịch do người gọi quản lý. */
    public int createMatchingBlock(MatchingBlock b, Connection conn) {
        if (b == null) return -1;
        String sql = "INSERT INTO MatchingBlocks (report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getReportId());
            ps.setString(2, b.getFunctionName());
            ps.setInt(3, b.getStudentAStartLine());
            ps.setInt(4, b.getStudentAEndLine());
            ps.setInt(5, b.getStudentBStartLine());
            ps.setInt(6, b.getStudentBEndLine());
            ps.setString(7, b.getMatchedCodeSnippet());
            ps.setString(8, b.getVariableRenamingNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        b.setBlockId(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    public boolean clearReportsByAssignment(int assignmentId) {
        try (Connection conn = DBContext.getConnection()) {
            return clearReportsByAssignment(assignmentId, conn);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /** Xóa báo cáo cũ trong cùng một giao dịch do người gọi quản lý. */
    public boolean clearReportsByAssignment(int assignmentId, Connection conn) {
        String sql = "DELETE pr FROM PlagiarismReports pr " +
                "JOIN Submissions sa ON sa.submission_id = pr.submission_a_id " +
                "JOIN Submissions sb ON sb.submission_id = pr.submission_b_id " +
                "WHERE sa.assignment_id = ? AND sb.assignment_id = sa.assignment_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Cập nhật nhận định phân tích cho một báo cáo đã lưu.
     * Dùng sau khi gọi Gemini thành công; bản ghi giữ nguyên nếu cập nhật thất bại.
     */
    public boolean updateAnalysisSummary(int reportId, String summary) {
        if (reportId <= 0 || summary == null) return false;
        String sql = "UPDATE PlagiarismReports SET ai_analysis_summary = ? WHERE report_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, summary);
            ps.setInt(2, reportId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public java.util.Map<Integer, java.util.Map<Integer, Double>> getSimilarityMatrix(int assignmentId) {
        String sql = "SELECT pr.submission_a_id, pr.submission_b_id, MAX(pr.similarity_score) "
                + "FROM PlagiarismReports pr "
                + "JOIN Submissions s1 ON pr.submission_a_id = s1.submission_id "
                + "JOIN Submissions s2 ON pr.submission_b_id = s2.submission_id "
                + "WHERE s1.assignment_id = ? AND s2.assignment_id = s1.assignment_id "
                + "GROUP BY pr.submission_a_id, pr.submission_b_id";
        java.util.Map<Integer, java.util.Map<Integer, Double>> matrix = new java.util.LinkedHashMap<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int a = rs.getInt(1);
                    int b = rs.getInt(2);
                    double score = rs.getDouble(3);
                    matrix.computeIfAbsent(a, key -> new java.util.LinkedHashMap<>()).put(b, score);
                    matrix.computeIfAbsent(b, key -> new java.util.LinkedHashMap<>()).put(a, score);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return matrix;
    }

    public List<PlagiarismReport> getReportsByAssignment(int assignmentId) {
        List<PlagiarismReport> list = new ArrayList<>();
        String sql = "SELECT pr.report_id, s1.assignment_id AS assignment_id, pr.submission_a_id, pr.submission_b_id, " +
                     "pr.similarity_score, pr.risk_level, pr.ai_analysis_summary, pr.created_at, " +
                     "u1.full_name AS student_a_name, u2.full_name AS student_b_name " +
                     "FROM PlagiarismReports pr " +
                     "JOIN Submissions s1 ON pr.submission_a_id = s1.submission_id " +
                     "JOIN Users u1 ON s1.student_id = u1.user_id " +
                     "JOIN Submissions s2 ON pr.submission_b_id = s2.submission_id " +
                     "JOIN Users u2 ON s2.student_id = u2.user_id " +
                     "WHERE s1.assignment_id = ? AND s2.assignment_id = s1.assignment_id " +
                     "ORDER BY pr.similarity_score DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReport(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    public PlagiarismReport getReportById(int reportId) {
        String sql = "SELECT pr.report_id, s1.assignment_id AS assignment_id, pr.submission_a_id, pr.submission_b_id, " +
                     "pr.similarity_score, pr.risk_level, pr.ai_analysis_summary, pr.created_at, " +
                     "u1.full_name AS student_a_name, u2.full_name AS student_b_name " +
                     "FROM PlagiarismReports pr " +
                     "JOIN Submissions s1 ON pr.submission_a_id = s1.submission_id " +
                     "JOIN Users u1 ON s1.student_id = u1.user_id " +
                     "JOIN Submissions s2 ON pr.submission_b_id = s2.submission_id " +
                     "JOIN Users u2 ON s2.student_id = u2.user_id " +
                     "WHERE pr.report_id = ? AND s2.assignment_id = s1.assignment_id";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReport(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public List<MatchingBlock> getMatchingBlocks(int reportId) {
        List<MatchingBlock> list = new ArrayList<>();
        String sql = "SELECT * FROM MatchingBlocks WHERE report_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchingBlock b = new MatchingBlock();
                    b.setBlockId(rs.getInt("block_id"));
                    b.setReportId(rs.getInt("report_id"));
                    b.setFunctionName(rs.getString("function_name"));
                    b.setStudentAStartLine(rs.getInt("student_a_start_line"));
                    b.setStudentAEndLine(rs.getInt("student_a_end_line"));
                    b.setStudentBStartLine(rs.getInt("student_b_start_line"));
                    b.setStudentBEndLine(rs.getInt("student_b_end_line"));
                    b.setMatchedCodeSnippet(rs.getString("matched_code_snippet"));
                    b.setVariableRenamingNotes(rs.getString("variable_renaming_notes"));
                    list.add(b);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    private PlagiarismReport mapReport(ResultSet rs) throws Exception {
        PlagiarismReport r = new PlagiarismReport();
        r.setReportId(rs.getInt("report_id"));
        r.setAssignmentId(rs.getInt("assignment_id"));
        r.setSubmissionAId(rs.getInt("submission_a_id"));
        r.setSubmissionBId(rs.getInt("submission_b_id"));
        r.setSimilarityScore(rs.getDouble("similarity_score"));
        r.setRiskLevel(rs.getString("risk_level"));
        r.setAiAnalysisSummary(rs.getString("ai_analysis_summary"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setStudentAName(rs.getString("student_a_name"));
        r.setStudentBName(rs.getString("student_b_name"));
        return r;
    }

}
