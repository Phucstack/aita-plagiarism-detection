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
}
