package com.aita.plagiarism.dao;

import com.aita.plagiarism.model.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CourseSecurityIntegrationTest {
    private final CourseDAO dao = new CourseDAO();
    private final User owner = new User(2,"teacher","Teacher","owner@example.invalid","INSTRUCTOR");
    private final User other = new User(3,"other","Other","other@example.invalid","INSTRUCTOR");
    private String code() { return "T" + UUID.randomUUID().toString().replace("-","").substring(0,15).toUpperCase(java.util.Locale.ROOT); }

    @Test void foreignOwnerCannotUpdateOrDeleteAndCommittedChangesAreReadable() {
        Course course = new Course(0,code(),"Scoped test",2,"Test");
        int id = dao.createCourse(course);
        try {
            course.setCourseName("Unauthorized");
            assertFalse(dao.updateCourse(course,other));
            assertEquals("Scoped test",new CourseDAO().getCourseById(id).getCourseName());
            assertFalse(dao.deleteCourse(id,other));
            assertNotNull(new CourseDAO().getCourseById(id));
            course.setCourseName("Authorized");
            assertTrue(dao.updateCourse(course,owner));
            assertEquals("Authorized",new CourseDAO().getCourseById(id).getCourseName());
            assertThrows(DataAccessException.class, () -> dao.createCourse(new Course(0,course.getCourseCode(),"Duplicate",2,"Test")));
        } finally {
            assertTrue(dao.deleteCourse(id,owner));
            assertNull(new CourseDAO().getCourseById(id));
        }
    }

    @Test void simultaneousDuplicateCreationCommitsExactlyOneRow() throws Exception {
        String code = code();
        var gate = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        Callable<Integer> insert = () -> {
            gate.await();
            try { return new CourseDAO().createCourse(new Course(0,code,"Concurrent",2,"Test")); }
            catch (DataAccessException e) { if (e.isConflict()) return -1; throw e; }
        };
        var first = pool.submit(insert);
        var second = pool.submit(insert);
        gate.countDown();
        int a = -1, b = -1;
        try {
            a = first.get(15,TimeUnit.SECONDS); b = second.get(15,TimeUnit.SECONDS);
            assertTrue((a > 0) ^ (b > 0));
            assertEquals(1,dao.getCoursesByInstructor(2).stream().filter(c -> code.equals(c.getCourseCode())).count());
        } finally {
            pool.shutdownNow();
            if (a > 0) dao.deleteCourse(a,owner);
            if (b > 0) dao.deleteCourse(b,owner);
        }
    }

    @Test void emptyAndMissingResultsStayEmpty() {
        assertTrue(dao.getCoursesByInstructor(999999).isEmpty());
        assertNull(dao.getCourseById(999999));
        assertNull(new AssignmentDAO().getAssignmentById(999999));
        assertTrue(new PlagiarismDAO().getReportsByAssignment(999999).isEmpty());
    }
}
