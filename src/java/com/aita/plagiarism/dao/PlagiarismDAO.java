package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.MatchingBlock;
import com.aita.plagiarism.model.PlagiarismReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        if (r == null) return -1;
        String sql = "INSERT INTO PlagiarismReports (assignment_id, submission_a_id, submission_b_id, similarity_score, risk_level, ai_analysis_summary) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getAssignmentId());
            ps.setInt(2, r.getSubmissionAId());
            ps.setInt(3, r.getSubmissionBId());
            ps.setDouble(4, r.getSimilarityScore());
            ps.setString(5, r.getRiskLevel() != null ? r.getRiskLevel() : "SAFE");
            ps.setString(6, r.getAiAnalysisSummary());

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
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    public int createMatchingBlock(MatchingBlock b) {
        if (b == null) return -1;
        String sql = "INSERT INTO MatchingBlocks (report_id, function_name, student_a_start_line, student_a_end_line, student_b_start_line, student_b_end_line, matched_code_snippet, variable_renaming_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        // Xóa các MatchingBlocks liên quan trước nếu CSDL không cấu hình cascade
        String sqlBlocks = "DELETE FROM MatchingBlocks WHERE report_id IN (SELECT report_id FROM PlagiarismReports WHERE assignment_id = ?)";
        String sqlReports = "DELETE FROM PlagiarismReports WHERE assignment_id = ?";
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(sqlBlocks)) {
                ps1.setInt(1, assignmentId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(sqlReports)) {
                ps2.setInt(1, assignmentId);
                ps2.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public List<PlagiarismReport> getReportsByAssignment(int assignmentId) {
        List<PlagiarismReport> list = new ArrayList<>();
        String sql = "SELECT pr.*, u1.full_name AS student_a_name, u2.full_name AS student_b_name " +
                     "FROM PlagiarismReports pr " +
                     "JOIN Submissions s1 ON pr.submission_a_id = s1.submission_id " +
                     "JOIN Users u1 ON s1.student_id = u1.user_id " +
                     "JOIN Submissions s2 ON pr.submission_b_id = s2.submission_id " +
                     "JOIN Users u2 ON s2.student_id = u2.user_id " +
                     "WHERE pr.assignment_id = ? " +
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
        String sql = "SELECT pr.*, u1.full_name AS student_a_name, u2.full_name AS student_b_name " +
                     "FROM PlagiarismReports pr " +
                     "JOIN Submissions s1 ON pr.submission_a_id = s1.submission_id " +
                     "JOIN Users u1 ON s1.student_id = u1.user_id " +
                     "JOIN Submissions s2 ON pr.submission_b_id = s2.submission_id " +
                     "JOIN Users u2 ON s2.student_id = u2.user_id " +
                     "WHERE pr.report_id = ?";

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
