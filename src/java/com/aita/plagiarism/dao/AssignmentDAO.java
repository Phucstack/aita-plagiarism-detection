package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu độc lập cho Bảng Assignments (Chuẩn 3NF)
 * Hỗ trợ Full CRUD: Create, Read, Update, Delete với PreparedStatement chống SQL Injection
 */
public class AssignmentDAO {

    /**
     * Tạo bài tập. Bắt buộc truyền {@code actor}: không có overload nào thiếu actor,
     * vì trước đây thiếu actor đồng nghĩa với việc bỏ qua toàn bộ kiểm tra sở hữu.
     */
    public int createAssignment(Assignment a, User actor) {
        if (a == null) return -1;
        requireActor(actor);
        String sql = "INSERT INTO Assignments (course_id, title, description, max_score, deadline, similarity_threshold) " +
                     "SELECT ?, ?, ?, ?, ?, ? FROM Courses WHERE course_id = ? " +
                     "AND (? = 1 OR instructor_id = ? OR ? = 'ADMIN')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCourseId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getDescription());
            ps.setDouble(4, a.getMaxScore() > 0 ? a.getMaxScore() : 100.0);
            ps.setTimestamp(5, a.getDeadline() != null ? a.getDeadline() : new Timestamp(System.currentTimeMillis() + 86400000L * 14));
            ps.setDouble(6, a.getSimilarityThreshold());
            ps.setInt(7, a.getCourseId());
            bindActor(ps, 8, actor);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        a.setAssignmentId(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    public boolean updateAssignment(Assignment a, User actor) {
        if (a == null || a.getAssignmentId() <= 0) return false;
        requireActor(actor);
        String sql = "UPDATE Assignments SET title = ?, description = ?, max_score = ?, deadline = ?, similarity_threshold = ? " +
                     "WHERE assignment_id = ? AND EXISTS (SELECT 1 FROM Courses c WHERE c.course_id = Assignments.course_id " +
                     "AND (? = 1 OR c.instructor_id = ? OR ? = 'ADMIN'))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setDouble(3, a.getMaxScore());
            ps.setTimestamp(4, a.getDeadline());
            ps.setDouble(5, a.getSimilarityThreshold());
            ps.setInt(6, a.getAssignmentId());
            bindActor(ps, 7, actor);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public boolean deleteAssignment(int assignmentId, User actor) {
        requireActor(actor);
        String sql = "DELETE FROM Assignments WHERE assignment_id = ? AND EXISTS " +
                "(SELECT 1 FROM Courses c WHERE c.course_id = Assignments.course_id " +
                "AND (? = 1 OR c.instructor_id = ? OR ? = 'ADMIN'))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            bindActor(ps, 2, actor);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public Assignment getAssignmentById(int assignmentId) {
        String sql = "SELECT * FROM Assignments WHERE assignment_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAssignment(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public List<Assignment> getAssignmentsByCourse(int courseId) {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT * FROM Assignments WHERE course_id = ? ORDER BY deadline ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAssignment(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    public List<Assignment> getAllAssignments() {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT * FROM Assignments ORDER BY created_at DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAssignment(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    /** Không bao giờ cho phép bỏ qua kiểm tra sở hữu: thiếu actor là lỗi lập trình. */
    private static void requireActor(User actor) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required: ownership checks must never be skipped");
        }
    }

    private void bindActor(PreparedStatement ps, int index, User actor) throws java.sql.SQLException {
        requireActor(actor);
        ps.setInt(index, 0);
        ps.setInt(index + 1, "INSTRUCTOR".equals(actor.getRole()) ? actor.getUserId() : -1);
        ps.setString(index + 2, actor.getRole());
    }

    private Assignment mapAssignment(ResultSet rs) throws Exception {
        Assignment a = new Assignment();
        a.setAssignmentId(rs.getInt("assignment_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setMaxScore(rs.getDouble("max_score"));
        a.setDeadline(rs.getTimestamp("deadline"));
        try {
            a.setSimilarityThreshold(rs.getDouble("similarity_threshold"));
        } catch (Exception ignored) {
            a.setSimilarityThreshold(75.0);
        }
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }

}
