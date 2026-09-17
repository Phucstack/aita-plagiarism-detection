package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.Course;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseFailureRegressionTest {
    @Test void outageNeverAuthenticatesOrPretendsToPersist() throws Exception {
        try (var db = mockStatic(DBContext.class)) {
            db.when(DBContext::getConnection).thenThrow(new SQLException("test outage"));
            UserDAO users = new UserDAO();
            assertThrows(DataAccessException.class, () -> users.authenticate("admin","123456"));
            assertThrows(DataAccessException.class, () -> users.getUserById(1));
            assertThrows(DataAccessException.class, () -> users.updateProfile(1,"changed",""));
            assertThrows(DataAccessException.class, () -> users.changePassword(1,"123456","new-password"));
            Course c = new Course(0,"AUDIT","Audit",2,"Test");
            assertThrows(DataAccessException.class, () -> new CourseDAO().createCourse(c));
            assertEquals(0,c.getCourseId());
            assertThrows(DataAccessException.class, () -> new CourseDAO().getAllCourses());
            assertThrows(DataAccessException.class, () -> new AssignmentDAO().getAllAssignments());
            assertThrows(DataAccessException.class, () -> new SubmissionDAO().getSubmissionsByStudent(4));
            assertThrows(DataAccessException.class, () -> new PlagiarismDAO().getReportsByAssignment(2));
        }
    }
}
