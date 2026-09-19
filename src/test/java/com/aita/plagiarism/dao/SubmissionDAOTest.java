package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.Submission;
import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Dữ liệu Bài nộp (SubmissionDAO)")
public class SubmissionDAOTest {

    private SubmissionDAO submissionDAO;

    @BeforeEach
    void setUp() {
        submissionDAO = new SubmissionDAO();
    }

    @Test
    @DisplayName("Truy vấn danh sách bài nộp theo Assignment ID")
    void testGetSubmissionsByAssignment() {
        List<Submission> list = submissionDAO.getSubmissionsByAssignment(2);
        assertNotNull(list, "Danh sách bài nộp không được null");
        assertFalse(list.isEmpty(), "Phải có bài nộp mẫu cho Assignment 2");
    }

    @Test
    @DisplayName("Truy vấn bài nộp theo Student ID")
    void testGetSubmissionsByStudent() {
        // user_id=4 (phuctv) là STUDENT trong DB test — sau migration 5d, submission
        // chỉ hợp lệ khi student_id trỏ tới user có role STUDENT.
        List<Submission> list = submissionDAO.getSubmissionsByStudent(4);
        assertNotNull(list);
    }

    @Test
    @DisplayName("Chu trình Full CRUD Bài nộp: Create -> Read -> Update Status -> Delete")
    void testSubmissionFullCRUD() {
        // 1. Create
        Submission s = new Submission();
        s.setAssignmentId(1);
        // student_id=4 (phuctv) có role STUDENT — bắt buộc sau CHECK CK_Submissions_StudentRole.
        s.setStudentId(4);
        s.setFileName("TestSubmission_" + System.currentTimeMillis() + ".java");
        s.setFilePath("C:/uploads/test.java");
        s.setFileType("JAVA");
        s.setSha256Hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        s.setStatus("PENDING");

        int newId = submissionDAO.createSubmission(s);
        assertTrue(newId > 0, "Mã bài nộp tạo mới phải lớn hơn 0");

        // 2. Read
        Submission retrieved = submissionDAO.getSubmissionById(newId);
        assertNotNull(retrieved, "Bài nộp vừa tạo phải lấy lại được");
        assertEquals(s.getFileName(), retrieved.getFileName());
        assertEquals("PENDING", retrieved.getStatus());

        // 3. Update Status (Tuân thủ ràng buộc CHECK (status IN ('PENDING', 'PARSED', 'ANALYZED', 'FLAGGED')))
        boolean updateStatusOk = submissionDAO.updateSubmissionStatus(newId, "ANALYZED");
        assertTrue(updateStatusOk, "Cập nhật trạng thái bài nộp phải thành công");

        // 4. Delete — phải truyền actor (không còn overload bỏ qua kiểm tra sở hữu)
        User admin = new User();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setRole("ADMIN");
        boolean deleteOk = submissionDAO.deleteSubmission(newId, admin);
        assertTrue(deleteOk, "Xóa bài nộp phải thành công");
    }

    @Test
    @DisplayName("Thiếu actor khi xóa bài nộp phải bị từ chối (không bỏ qua kiểm tra sở hữu)")
    void testDeleteSubmissionWithoutActorRejected() {
        assertThrows(IllegalArgumentException.class, () -> submissionDAO.deleteSubmission(1, null));
    }
}
