package com.aita.plagiarism.service;

import com.aita.plagiarism.dao.AssignmentDAO;
import com.aita.plagiarism.dao.CourseDAO;
import com.aita.plagiarism.model.Assignment;
import com.aita.plagiarism.model.Course;
import com.aita.plagiarism.model.User;

/** Ownership policy shared by assignment, scanner and report controllers. */
public final class AccessPolicy {
    private AccessPolicy() {}

    public static boolean canManageCourse(User user, int courseId) {
        if (user == null) return false;
        Course course = new CourseDAO().getCourseById(courseId);
        return course != null && ("ADMIN".equals(user.getRole())
                || ("INSTRUCTOR".equals(user.getRole()) && course.getInstructorId() == user.getUserId()));
    }

    public static boolean canManageAssignment(User user, int assignmentId) {
        Assignment assignment = new AssignmentDAO().getAssignmentById(assignmentId);
        return assignment != null && canManageCourse(user, assignment.getCourseId());
    }

    /**
     * Người dùng có được nộp bài vào bài tập này không.
     *
     * Hiện hệ thống chưa có quan hệ enrolment (sinh viên đăng ký môn), nên không thể
     * giới hạn theo "sinh viên thuộc lớp". Chính sách hiện tại kiểm tra những gì có thể
     * kiểm tra một cách trung thực: bài tập phải tồn tại và còn trong hạn nộp.
     * Khi quan hệ enrolment được bổ sung (dự kiến tuần 4–6), bổ sung thêm điều kiện
     * thành viên lớp tại đây.
     */
    public static boolean canSubmitTo(User user, int assignmentId) {
        if (user == null || assignmentId <= 0) return false;
        Assignment assignment = new AssignmentDAO().getAssignmentById(assignmentId);
        if (assignment == null) return false;
        if (assignment.getDeadline() == null) return true;
        return assignment.getDeadline().getTime() >= System.currentTimeMillis();
    }
}
