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
        List<Course> courses = courseDAO.getCoursesByInstructor(2);
        assertNotNull(courses);
        assertFalse(courses.isEmpty(), "Giảng viên ID 2 phải phụ trách ít nhất 1 khóa học");
        
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
    @DisplayName("Truy vấn bài tập với ID không tồn tại trả về null")
    void testGetAssignmentByIdNonExistent() {
        Assignment assignment = courseDAO.getAssignmentById(99999);
        assertNull(assignment, "Unknown IDs must not return sample assignments");
    }

    // ------------------------------------------------------------------
    // CÁC TEST SAU CẦN DB SQL SERVER THẬT (seed instructor_id = 2 có PRJ301).
    // Không chạy trong CI không-DB: chỉ chạy khi có -DDB_* kết nối được.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("[DB] Keyword lọc đúng khóa học theo mã/tên (course_code LIKE hoặc course_name LIKE)")
    void testPagedCoursesKeywordFilter() {
        List<Course> all = courseDAO.getCoursesByInstructor(2, 1, 50, null);
        int total = courseDAO.countCoursesByInstructor(2, null);
        assertEquals(all.size(), total, "count phải khớp với danh sách đầy đủ");

        List<Course> filtered = courseDAO.getCoursesByInstructor(2, 1, 50, "PRJ");
        assertFalse(filtered.isEmpty(), "Keyword 'PRJ' phải khớp ít nhất PRJ301");
        for (Course c : filtered) {
            assertTrue(
                    c.getCourseCode().toUpperCase(java.util.Locale.ROOT).contains("PRJ")
                            || c.getCourseName().toUpperCase(java.util.Locale.ROOT).contains("PRJ"),
                    "Mọi kết quả phải khớp keyword: " + c.getCourseCode());
        }
        assertEquals(courseDAO.countCoursesByInstructor(2, "PRJ"), filtered.size());

        // Keyword không khớp gì phải trả rỗng, count = 0
        List<Course> none = courseDAO.getCoursesByInstructor(2, 1, 50, "ZZZ_KHONG_TON_TAI_999");
        assertTrue(none.isEmpty());
        assertEquals(0, courseDAO.countCoursesByInstructor(2, "ZZZ_KHONG_TON_TAI_999"));

        // Ký tự đặc biệt LIKE phải được escape, không ném lỗi cú pháp
        List<Course> special = courseDAO.getCoursesByInstructor(2, 1, 50, "%_[");
        assertTrue(special.isEmpty(), "Wildcard phải được coi là chuỗi thường");
    }

    @Test
    @DisplayName("[DB] Trang 2 trả đúng phần tử (OFFSET/FETCH khớp với cắt tay trên danh sách đầy đủ)")
    void testPagedCoursesSecondPage() {
        int size = 5;
        List<Course> all = courseDAO.getCoursesByInstructor(2, 1, 50, null);
        List<Course> page2 = courseDAO.getCoursesByInstructor(2, 2, size, null);
        int expectedCount = Math.max(0, Math.min(size, all.size() - size));
        assertEquals(expectedCount, page2.size(), "Trang 2 phải có đúng số phần tử còn lại");
        for (int i = 0; i < page2.size(); i++) {
            assertEquals(all.get(size + i).getCourseId(), page2.get(i).getCourseId(),
                    "Phần tử trang 2 phải trùng với cắt tay theo ORDER BY course_code ASC");
        }
    }

    @Test
    @DisplayName("[DB] Page âm/0 được clamp về trang 1; size được clamp vào [5..50]")
    void testPagedCoursesClampInvalidParams() {
        List<Course> page1 = courseDAO.getCoursesByInstructor(2, 1, 5, null);
        List<Course> pageZero = courseDAO.getCoursesByInstructor(2, 0, 5, null);
        List<Course> pageNegative = courseDAO.getCoursesByInstructor(2, -7, 5, null);
        assertEquals(page1.stream().map(Course::getCourseId).toList(),
                pageZero.stream().map(Course::getCourseId).toList(), "page=0 phải về trang 1");
        assertEquals(page1.stream().map(Course::getCourseId).toList(),
                pageNegative.stream().map(Course::getCourseId).toList(), "page âm phải về trang 1");

        List<Course> clampedSize = courseDAO.getCoursesByInstructor(2, 1, 1, null);
        assertTrue(clampedSize.size() <= 5, "size=1 phải được clamp lên 5");
        List<Course> hugeSize = courseDAO.getCoursesByInstructor(2, 1, 5000, null);
        assertTrue(hugeSize.size() <= 50, "size=5000 phải được clamp xuống 50");
    }
}
