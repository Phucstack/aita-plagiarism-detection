package com.aita.plagiarism.model;

import java.sql.Timestamp;

public class PlagiarismReport {
    private int reportId;
    private int assignmentId;
    private int submissionAId;
    private int submissionBId;
    private double similarityScore;
    private String riskLevel;
    private String aiAnalysisSummary;
    private Timestamp createdAt;

    // Tên sinh viên để hiển thị UI View
    private String studentAName;
    private String studentBName;

    public PlagiarismReport() {}

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getSubmissionAId() { return submissionAId; }
    public void setSubmissionAId(int submissionAId) { this.submissionAId = submissionAId; }

    public int getSubmissionBId() { return submissionBId; }
    public void setSubmissionBId(int submissionBId) { this.submissionBId = submissionBId; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getAiAnalysisSummary() { return aiAnalysisSummary; }
    public void setAiAnalysisSummary(String aiAnalysisSummary) { this.aiAnalysisSummary = aiAnalysisSummary; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStudentAName() { return studentAName; }
    public void setStudentAName(String studentAName) { this.studentAName = studentAName; }

    public String getStudentBName() { return studentBName; }
    public void setStudentBName(String studentBName) { this.studentBName = studentBName; }
}
