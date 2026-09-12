package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Dữ liệu Khóa học & Bài tập (CourseDAO)")
public class CourseDAOTest {

    private CourseDAO courseDAO;

    @BeforeEach
    void setUp() {
        courseDAO = new CourseDAO();
    }

    @Test
    @DisplayName("Lấy danh sách tất cả các khóa học trong hệ thống")
    void testGetAllCourses() {
        List<Course> courses = courseDAO.getAllCourses();
        assertNotNull(courses, "Danh sách khóa học không được null");
        assertFalse(courses.isEmpty(), "Phải có ít nhất 1 khóa học trong hệ thống");
        
        boolean hasPRJ301 = courses.stream().anyMatch(c -> "PRJ301".equalsIgnoreCase(c.getCourseCode()));
        assertTrue(hasPRJ301, "Phải có môn học PRJ301 trong chương trình");
    }

    @Test
    @DisplayName("Lấy danh sách khóa học phụ trách bởi Giảng viên")
    void testGetCoursesByInstructor() {
        List<Course> courses = courseDAO.getCoursesByInstructor(1);
        assertNotNull(courses);
        assertFalse(courses.isEmpty(), "Giảng viên ID 1 phải phụ trách ít nhất 1 khóa học");
        
        for (Course c : courses) {
            assertNotNull(c.getCourseCode());
            assertNotNull(c.getCourseName());
        }
    }

    @Test
    @DisplayName("Lấy danh sách bài tập theo mã khóa học")
    void testGetAssignmentsByCourse() {
        List<Assignment> assignments = courseDAO.getAssignmentsByCourse(1);
        assertNotNull(assignments);
        assertFalse(assignments.isEmpty(), "Khóa học ID 1 phải có danh sách bài tập");

        Assignment a = assignments.get(0);
        assertNotNull(a.getTitle());
        assertTrue(a.getMaxScore() > 0, "Điểm tối đa bài tập phải lớn hơn 0");
    }

    @Test
    @DisplayName("Truy vấn chi tiết một bài tập theo ID hợp lệ")
    void testGetAssignmentByIdValid() {
        Assignment assignment = courseDAO.getAssignmentById(1);
        assertNotNull(assignment);
        assertEquals(1, assignment.getAssignmentId());
        assertNotNull(assignment.getTitle());
        assertNotNull(assignment.getDescription());
    }

    @Test
    @DisplayName("Truy vấn bài tập với ID không tồn tại trả về bài tập mặc định/fallback an toàn")
    void testGetAssignmentByIdNonExistent() {
        Assignment assignment = courseDAO.getAssignmentById(99999);
        assertNotNull(assignment, "Không được văng NullPointerException khi ID không tìm thấy");
    }
}
