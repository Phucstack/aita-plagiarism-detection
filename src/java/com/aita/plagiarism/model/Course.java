package com.aita.plagiarism.model;

import java.sql.Timestamp;

public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int instructorId;
    private String semester;
    private Timestamp createdAt;

    public Course() {}

    public Course(int courseId, String courseCode, String courseName, int instructorId, String semester) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructorId = instructorId;
        this.semester = semester;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
