package com.aita.plagiarism.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Toàn diện Domain Models AITA")
public class ModelTest {

    @Test
    @DisplayName("Kiểm thử thực thể User - Constructors, Getters/Setters")
    void testUserModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        User user = new User(1, "teacher_ha", "TS. Nguyễn Hoàng Hà", "ha.nh@fpt.edu.vn", "INSTRUCTOR");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setCreatedAt(now);

        assertEquals(1, user.getUserId());
        assertEquals("teacher_ha", user.getUsername());
        assertEquals("TS. Nguyễn Hoàng Hà", user.getFullName());
        assertEquals("ha.nh@fpt.edu.vn", user.getEmail());
        assertEquals("INSTRUCTOR", user.getRole());
        assertEquals("https://example.com/avatar.jpg", user.getAvatarUrl());
        assertEquals(now, user.getCreatedAt());

        // Default constructor and setters
        User defaultUser = new User();
        defaultUser.setUserId(2);
        defaultUser.setUsername("student_test");
        defaultUser.setFullName("Lê Sinh Viên");
        defaultUser.setEmail("sv@fpt.edu.vn");
        defaultUser.setRole("STUDENT");

        assertEquals(2, defaultUser.getUserId());
        assertEquals("student_test", defaultUser.getUsername());
        assertEquals("Lê Sinh Viên", defaultUser.getFullName());
        assertEquals("sv@fpt.edu.vn", defaultUser.getEmail());
        assertEquals("STUDENT", defaultUser.getRole());
    }

    @Test
    @DisplayName("Kiểm thử thực thể Course - Constructors, Getters/Setters")
    void testCourseModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Course course = new Course(10, "PRJ301", "Java Web Application Development", 1, "Fall 2026");
        course.setCreatedAt(now);

        assertEquals(10, course.getCourseId());
        assertEquals("PRJ301", course.getCourseCode());
        assertEquals("Java Web Application Development", course.getCourseName());
        assertEquals(1, course.getInstructorId());
        assertEquals("Fall 2026", course.getSemester());
        assertEquals(now, course.getCreatedAt());

        Course defaultCourse = new Course();
        defaultCourse.setCourseId(20);
        defaultCourse.setCourseCode("CSD201");
        defaultCourse.setCourseName("Data Structures");
        defaultCourse.setInstructorId(2);
        defaultCourse.setSemester("Spring 2026");

        assertEquals(20, defaultCourse.getCourseId());
        assertEquals("CSD201", defaultCourse.getCourseCode());
        assertEquals("Data Structures", defaultCourse.getCourseName());
        assertEquals(2, defaultCourse.getInstructorId());
        assertEquals("Spring 2026", defaultCourse.getSemester());
    }

    @Test
    @DisplayName("Kiểm thử thực thể Assignment - Constructors, Getters/Setters")
    void testAssignmentModel() {
        Timestamp deadline = new Timestamp(System.currentTimeMillis() + 86400000L);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Assignment assignment = new Assignment(1, 10, "Lab 1 - Servlet Basics", "Lập trình Servlet cơ bản", 10.0, deadline);
        assignment.setCreatedAt(createdAt);

        assertEquals(1, assignment.getAssignmentId());
        assertEquals(10, assignment.getCourseId());
        assertEquals("Lab 1 - Servlet Basics", assignment.getTitle());
        assertEquals("Lập trình Servlet cơ bản", assignment.getDescription());
        assertEquals(10.0, assignment.getMaxScore());
        assertEquals(deadline, assignment.getDeadline());
        assertEquals(createdAt, assignment.getCreatedAt());

        Assignment defaultAssignment = new Assignment();
        defaultAssignment.setAssignmentId(2);
        defaultAssignment.setCourseId(10);
        defaultAssignment.setTitle("Assignment 2 - E-Commerce");
        defaultAssignment.setDescription("Xây dựng giỏ hàng");
        defaultAssignment.setMaxScore(100.0);

        assertEquals(2, defaultAssignment.getAssignmentId());
        assertEquals(10, defaultAssignment.getCourseId());
        assertEquals("Assignment 2 - E-Commerce", defaultAssignment.getTitle());
        assertEquals("Xây dựng giỏ hàng", defaultAssignment.getDescription());
        assertEquals(100.0, defaultAssignment.getMaxScore());
    }

    @Test
    @DisplayName("Kiểm thử thực thể Submission - Constructors, Getters/Setters")
    void testSubmissionModel() {
        Timestamp submittedAt = new Timestamp(System.currentTimeMillis());
        String sha256 = "d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2";
        Submission sub = new Submission(100, 1, 4, "CartServlet.java", sha256, "ANALYZED");
        sub.setFilePath("/uploads/submissions/CartServlet.java");
        sub.setSubmittedAt(submittedAt);

        assertEquals(100, sub.getSubmissionId());
        assertEquals(1, sub.getAssignmentId());
        assertEquals(4, sub.getStudentId());
        assertEquals("CartServlet.java", sub.getFileName());
        assertEquals("/uploads/submissions/CartServlet.java", sub.getFilePath());
        assertEquals(sha256, sub.getSha256Hash());
        assertEquals(submittedAt, sub.getSubmittedAt());
        assertEquals("ANALYZED", sub.getStatus());

        Submission defaultSub = new Submission();
        defaultSub.setSubmissionId(101);
        defaultSub.setAssignmentId(1);
        defaultSub.setStudentId(5);
        defaultSub.setFileName("PaymentController.java");
        defaultSub.setSha256Hash(sha256);
        defaultSub.setStatus("PENDING");

        assertEquals(101, defaultSub.getSubmissionId());
        assertEquals(1, defaultSub.getAssignmentId());
        assertEquals(5, defaultSub.getStudentId());
        assertEquals("PaymentController.java", defaultSub.getFileName());
        assertEquals("PENDING", defaultSub.getStatus());
    }

    @Test
    @DisplayName("Kiểm thử thực thể PlagiarismReport - Getters/Setters và UI Mapping")
    void testPlagiarismReportModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PlagiarismReport report = new PlagiarismReport();
        report.setReportId(501);
        report.setAssignmentId(1);
        report.setSubmissionAId(100);
        report.setSubmissionBId(101);
        report.setSimilarityScore(89.5);
        report.setRiskLevel("HIGH_RISK");
        report.setAiAnalysisSummary("Phát hiện 12 khối mã tương đồng cấu trúc AST.");
        report.setCreatedAt(now);
        report.setStudentAName("Trần Văn Phúc");
        report.setStudentBName("Đinh Vũ Phương Khánh");

        assertEquals(501, report.getReportId());
        assertEquals(1, report.getAssignmentId());
        assertEquals(100, report.getSubmissionAId());
        assertEquals(101, report.getSubmissionBId());
        assertEquals(89.5, report.getSimilarityScore());
        assertEquals("HIGH_RISK", report.getRiskLevel());
        assertEquals("Phát hiện 12 khối mã tương đồng cấu trúc AST.", report.getAiAnalysisSummary());
        assertEquals(now, report.getCreatedAt());
        assertEquals("Trần Văn Phúc", report.getStudentAName());
        assertEquals("Đinh Vũ Phương Khánh", report.getStudentBName());
    }

    @Test
    @DisplayName("Kiểm thử thực thể MatchingBlock - Chi tiết khối mã trùng lặp AST")
    void testMatchingBlockModel() {
        MatchingBlock block = new MatchingBlock();
        block.setBlockId(1001);
        block.setReportId(501);
        block.setFunctionName("doPost(HttpServletRequest, HttpServletResponse)");
        block.setStudentAStartLine(45);
        block.setStudentAEndLine(78);
        block.setStudentBStartLine(50);
        block.setStudentBEndLine(83);
        block.setMatchedCodeSnippet("for (Item item : cart.getItems()) { total += item.getPrice(); }");
        block.setVariableRenamingNotes("cart -> basket, item -> productItem");

        assertEquals(1001, block.getBlockId());
        assertEquals(501, block.getReportId());
        assertEquals("doPost(HttpServletRequest, HttpServletResponse)", block.getFunctionName());
        assertEquals(45, block.getStudentAStartLine());
        assertEquals(78, block.getStudentAEndLine());
        assertEquals(50, block.getStudentBStartLine());
        assertEquals(83, block.getStudentBEndLine());
        assertEquals("for (Item item : cart.getItems()) { total += item.getPrice(); }", block.getMatchedCodeSnippet());
        assertEquals("cart -> basket, item -> productItem", block.getVariableRenamingNotes());
    }
}
