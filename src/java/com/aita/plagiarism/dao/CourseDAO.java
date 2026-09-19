package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy xuất dữ liệu cho Bảng Courses & Phục vụ Assignment
 * Hỗ trợ Full CRUD cho khóa học của Giảng viên và Admin
 */
public class CourseDAO {

    public int createCourse(Course c) {
        if (c == null) return -1;
        String sql = "INSERT INTO Courses (course_code, course_name, instructor_id, semester) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourseCode().trim().toUpperCase());
            ps.setString(2, c.getCourseName().trim());
            ps.setInt(3, c.getInstructorId());
            ps.setString(4, c.getSemester() != null ? c.getSemester().trim() : "Fall 2026");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        c.setCourseId(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return -1;
    }

    public boolean updateCourse(Course c, User actor) {
        if (c == null || c.getCourseId() <= 0) return false;
        String sql = "UPDATE Courses SET course_code = ?, course_name = ?, semester = ? WHERE course_id = ? AND (instructor_id = ? OR ? = 'ADMIN')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCourseCode().trim().toUpperCase());
            ps.setString(2, c.getCourseName().trim());
            ps.setString(3, c.getSemester().trim());
            ps.setInt(4, c.getCourseId());
            ps.setInt(5, actor.getUserId());
            ps.setString(6, actor.getRole());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public boolean deleteCourse(int courseId, User actor) {
        String sql = "DELETE FROM Courses WHERE course_id = ? AND (instructor_id = ? OR ? = 'ADMIN')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, actor.getUserId());
            ps.setString(3, actor.getRole());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM Courses WHERE course_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM Courses ORDER BY created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCourse(rs));
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    public List<Course> getCoursesByInstructor(int instructorId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM Courses WHERE instructor_id = ? ORDER BY course_code ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    /**
     * Phân trang + tìm kiếm khóa học của một giảng viên (SQL Server OFFSET/FETCH).
     * {@code page} bắt đầu từ 1; giá trị < 1 được clamp về 1. {@code size} được clamp vào [5..50].
     * {@code keyword} rỗng/null thì không lọc; ngược lại lọc theo course_code HOẶC course_name (LIKE).
     */
    public List<Course> getCoursesByInstructor(int instructorId, int page, int size, String keyword) {
        List<Course> list = new ArrayList<>();
        int safePage = Math.max(1, page);
        int safeSize = Math.min(50, Math.max(5, size));
        String kw = keyword == null ? "" : keyword.trim();
        String sql = "SELECT * FROM Courses WHERE instructor_id = ?"
                + (kw.isEmpty() ? "" : " AND (course_code LIKE ? ESCAPE '\\' OR course_name LIKE ? ESCAPE '\\')")
                + " ORDER BY course_code ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, instructorId);
            if (!kw.isEmpty()) {
                String pattern = "%" + escapeLike(kw) + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }
            ps.setInt(idx++, (safePage - 1) * safeSize);
            ps.setInt(idx, safeSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return list;
    }

    /** Tổng số khóa học của giảng viên khớp keyword (dùng để tính tổng trang). */
    public int countCoursesByInstructor(int instructorId, String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        String sql = "SELECT COUNT(*) FROM Courses WHERE instructor_id = ?"
                + (kw.isEmpty() ? "" : " AND (course_code LIKE ? ESCAPE '\\' OR course_name LIKE ? ESCAPE '\\')");
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            if (!kw.isEmpty()) {
                String pattern = "%" + escapeLike(kw) + "%";
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return 0;
    }

    /** Escape ký tự đặc biệt của LIKE ('%', '_', '[' và chính '\') với ESCAPE '\'. */
    private static String escapeLike(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '%' || c == '_' || c == '[' || c == '\\') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    public List<Assignment> getAssignmentsByCourse(int courseId) {
        AssignmentDAO assignmentDAO = new AssignmentDAO();
        return assignmentDAO.getAssignmentsByCourse(courseId);
    }

    public Assignment getAssignmentById(int assignmentId) {
        AssignmentDAO assignmentDAO = new AssignmentDAO();
        return assignmentDAO.getAssignmentById(assignmentId);
    }

    private Course mapCourse(ResultSet rs) throws Exception {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setInstructorId(rs.getInt("instructor_id"));
        c.setSemester(rs.getString("semester"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }

}
