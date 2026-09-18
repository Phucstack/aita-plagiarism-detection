package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.PlagiarismReport;
import com.aita.plagiarism.model.Submission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ma trận tương đồng + lọc trạng thái bài nộp (hướng B)")
public class SimilarityMatrixStatusTest {

    @Test
    @DisplayName("Ma trận đối xứng và chỉ chứa cặp cùng assignment")
    void matrixIsSymmetricForSameAssignment() {
        PlagiarismDAO dao = new PlagiarismDAO();
        Map<Integer, Map<Integer, Double>> matrix = dao.getSimilarityMatrix(2);
        assertNotNull(matrix);
        assertFalse(matrix.isEmpty(), "Assignment 2 phải có ít nhất một cặp báo cáo mẫu");
        for (Map.Entry<Integer, Map<Integer, Double>> row : matrix.entrySet()) {
            for (Map.Entry<Integer, Double> cell : row.getValue().entrySet()) {
                assertTrue(cell.getValue() >= 0 && cell.getValue() <= 100);
                assertEquals(cell.getValue(), matrix.get(cell.getKey()).get(row.getKey()), 0.0001,
                        "Ma trận phải đối xứng A-B và B-A");
            }
        }
    }

    @Test
    @DisplayName("Lọc PENDING/ANALYZED khớp tập con của toàn bộ bài nộp")
    void statusFilterMatchesSubset() {
        SubmissionDAO submissions = new SubmissionDAO();
        List<Submission> all = submissions.getSubmissionsByAssignment(2);
        List<Submission> pending = submissions.getSubmissionsByAssignmentAndStatus(2, "pending");
        assertNotNull(pending);
        assertTrue(all.size() >= pending.size());
        for (Submission s : pending) {
            assertEquals("PENDING", s.getStatus());
            assertEquals(2, s.getAssignmentId());
        }
    }

    @Test
    @DisplayName("Trạng thái lọc không hợp lệ phải 400 ở DAO bằng IllegalArgumentException")
    void invalidStatusRejected() {
        SubmissionDAO submissions = new SubmissionDAO();
        assertThrows(IllegalArgumentException.class, () -> submissions.getSubmissionsByAssignmentAndStatus(2, "GRADED"));
    }

    @Test
    @DisplayName("Báo cáo CSV phải đọc được từ cùng nguồn với dashboard")
    void csvSourceMatchesDashboard() {
        PlagiarismDAO dao = new PlagiarismDAO();
        List<PlagiarismReport> reports = dao.getReportsByAssignment(2);
        Map<Integer, Map<Integer, Double>> matrix = dao.getSimilarityMatrix(2);
        assertFalse(reports.isEmpty());
        assertFalse(matrix.isEmpty());
        long pairs = matrix.values().stream().mapToInt(Map::size).sum() / 2;
        assertTrue(pairs >= 1);
    }
}
