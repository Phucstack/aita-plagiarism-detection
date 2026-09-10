package com.aita.plagiarism.model;

import java.sql.Timestamp;

public class Assignment {
    private int assignmentId;
    private int courseId;
    private String title;
    private String description;
    private double maxScore;
    private Timestamp deadline;
    private Timestamp createdAt;

    public Assignment() {}

    public Assignment(int assignmentId, int courseId, String title, String description, double maxScore, Timestamp deadline) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.maxScore = maxScore;
        this.deadline = deadline;
    }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
