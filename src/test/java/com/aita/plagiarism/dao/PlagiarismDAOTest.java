package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.MatchingBlock;
import com.aita.plagiarism.model.PlagiarismReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Dữ liệu Báo cáo Đạo văn & Khối mã Trùng khớp (PlagiarismDAO)")
public class PlagiarismDAOTest {

    private PlagiarismDAO plagiarismDAO;

    @BeforeEach
    void setUp() {
        plagiarismDAO = new PlagiarismDAO();
    }

    @Test
    @DisplayName("Truy vấn danh sách báo cáo đạo văn theo ID bài tập")
    void testGetReportsByAssignment() {
        List<PlagiarismReport> reports = plagiarismDAO.getReportsByAssignment(1);
        assertNotNull(reports, "Danh sách báo cáo không được null");
        assertFalse(reports.isEmpty(), "Bài tập 1 phải có báo cáo đối soát đạo văn mẫu");

        for (PlagiarismReport r : reports) {
            assertTrue(r.getSimilarityScore() >= 0.0 && r.getSimilarityScore() <= 100.0,
                    "Tỷ lệ trùng lặp phải nằm trong khoảng 0 - 100%");
            assertNotNull(r.getRiskLevel(), "Mức độ rủi ro (risk level) không được null");
            assertNotNull(r.getStudentAName(), "Tên sinh viên A phải được ánh xạ");
            assertNotNull(r.getStudentBName(), "Tên sinh viên B phải được ánh xạ");
        }
    }

    @Test
    @DisplayName("Truy vấn chi tiết báo cáo đạo văn theo Report ID")
    void testGetReportById() {
        PlagiarismReport report = plagiarismDAO.getReportById(1);
        assertNotNull(report, "Báo cáo ID 1 phải tồn tại");
        assertTrue(report.getSimilarityScore() > 0);
        assertNotNull(report.getAiAnalysisSummary(), "Tóm tắt phân tích AI không được null");
    }

    @Test
    @DisplayName("Truy vấn các khối mã AST trùng lặp chi tiết của báo cáo")
    void testGetMatchingBlocks() {
        List<MatchingBlock> blocks = plagiarismDAO.getMatchingBlocks(1);
        assertNotNull(blocks, "Danh sách khối mã không được null");
        assertFalse(blocks.isEmpty(), "Báo cáo ID 1 phải chứa ít nhất 1 khối mã trùng lặp");

        for (MatchingBlock b : blocks) {
            assertNotNull(b.getFunctionName(), "Tên hàm trùng khớp không được null");
            assertTrue(b.getStudentAStartLine() > 0, "Dòng bắt đầu của sinh viên A phải > 0");
            assertTrue(b.getStudentAEndLine() >= b.getStudentAStartLine(), "Dòng kết thúc phải >= dòng bắt đầu (Sinh viên A)");
            assertTrue(b.getStudentBStartLine() > 0, "Dòng bắt đầu của sinh viên B phải > 0");
            assertTrue(b.getStudentBEndLine() >= b.getStudentBStartLine(), "Dòng kết thúc phải >= dòng bắt đầu (Sinh viên B)");
            assertNotNull(b.getMatchedCodeSnippet(), "Đoạn code trùng khớp không được null");
        }
    }
}
