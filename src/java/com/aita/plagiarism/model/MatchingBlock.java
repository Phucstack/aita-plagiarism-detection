package com.aita.plagiarism.model;

public class MatchingBlock {
    private int blockId;
    private int reportId;
    private String functionName;
    private int studentAStartLine;
    private int studentAEndLine;
    private int studentBStartLine;
    private int studentBEndLine;
    private String matchedCodeSnippet;
    private String variableRenamingNotes;

    public MatchingBlock() {}

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public int getStudentAStartLine() { return studentAStartLine; }
    public void setStudentAStartLine(int studentAStartLine) { this.studentAStartLine = studentAStartLine; }

    public int getStudentAEndLine() { return studentAEndLine; }
    public void setStudentAEndLine(int studentAEndLine) { this.studentAEndLine = studentAEndLine; }

    public int getStudentBStartLine() { return studentBStartLine; }
    public void setStudentBStartLine(int studentBStartLine) { this.studentBStartLine = studentBStartLine; }

    public int getStudentBEndLine() { return studentBEndLine; }
    public void setStudentBEndLine(int studentBEndLine) { this.studentBEndLine = studentBEndLine; }

    public String getMatchedCodeSnippet() { return matchedCodeSnippet; }
    public void setMatchedCodeSnippet(String matchedCodeSnippet) { this.matchedCodeSnippet = matchedCodeSnippet; }

    public String getVariableRenamingNotes() { return variableRenamingNotes; }
    public void setVariableRenamingNotes(String variableRenamingNotes) { this.variableRenamingNotes = variableRenamingNotes; }
}
