package com.smartexam.dto;

public class LeaderboardDto {

    private Integer rank;
    private String studentName;
    private String examTitle;
    private String subjectName;
    private Double score;
    private Double totalMarks;
    private Double percentage;
    private String timeTakenFormatted;

    public LeaderboardDto() {
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getTimeTakenFormatted() {
        return timeTakenFormatted;
    }

    public void setTimeTakenFormatted(String timeTakenFormatted) {
        this.timeTakenFormatted = timeTakenFormatted;
    }
}
