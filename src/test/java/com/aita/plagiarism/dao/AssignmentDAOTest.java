package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.Assignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Dữ liệu Bài tập (AssignmentDAO)")
public class AssignmentDAOTest {

    private AssignmentDAO assignmentDAO;

    @BeforeEach
    void setUp() {
        assignmentDAO = new AssignmentDAO();
    }

    @Test
    @DisplayName("Lấy danh sách toàn bộ bài tập")
    void testGetAllAssignments() {
        List<Assignment> list = assignmentDAO.getAllAssignments();
        assertNotNull(list, "Danh sách bài tập không được null");
        assertFalse(list.isEmpty(), "Phải có ít nhất 1 bài tập trong hệ thống");
    }

    @Test
    @DisplayName("Truy vấn bài tập theo ID hợp lệ")
    void testGetAssignmentById() {
        Assignment a = assignmentDAO.getAssignmentById(1);
        assertNotNull(a, "Bài tập ID 1 phải tồn tại");
        assertEquals(1, a.getAssignmentId());
        assertNotNull(a.getTitle());
    }

    @Test
    @DisplayName("Truy vấn bài tập theo Course ID")
    void testGetAssignmentsByCourse() {
        List<Assignment> list = assignmentDAO.getAssignmentsByCourse(1);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        for (Assignment a : list) {
            assertEquals(1, a.getCourseId());
        }
    }

    @Test
    @DisplayName("Chu trình Full CRUD Bài tập: Create -> Update -> Delete")
    void testAssignmentFullCRUD() {
        // 1. Create
        Assignment a = new Assignment();
        a.setCourseId(1);
        a.setTitle("Test Unit Assignment " + System.currentTimeMillis());
        a.setDescription("Unit test description");
        a.setMaxScore(100.0);
        a.setSimilarityThreshold(80.0);
        a.setDeadline(new Timestamp(System.currentTimeMillis() + 86400000L));

        int newId = assignmentDAO.createAssignment(a);
        assertTrue(newId > 0, "ID bài tập tạo mới phải lớn hơn 0");

        // 2. Read
        Assignment created = assignmentDAO.getAssignmentById(newId);
        assertNotNull(created, "Bài tập vừa tạo phải truy vấn được");
        assertEquals(a.getTitle(), created.getTitle());

        // 3. Update
        created.setTitle("Updated Title " + System.currentTimeMillis());
        created.setSimilarityThreshold(85.0);
        boolean updateOk = assignmentDAO.updateAssignment(created);
        assertTrue(updateOk, "Cập nhật bài tập phải trả về true");

        // 4. Delete
        boolean deleteOk = assignmentDAO.deleteAssignment(newId);
        assertTrue(deleteOk, "Xóa bài tập phải trả về true");
    }
}
