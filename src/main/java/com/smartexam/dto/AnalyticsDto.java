package com.smartexam.dto;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsDto {

    private long totalStudents;
    private long totalFaculty;
    private long totalExams;
    private long totalSubjects;
    private long totalAttempts;
    private double averageScorePercentage;
    private double overallPassRate;

    private Map<String, Integer> subjectExamCounts = new HashMap<>();
    private Map<String, Double> subjectAverageScores = new HashMap<>();
    private Map<String, Integer> difficultyDistribution = new HashMap<>();

    public AnalyticsDto() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public long getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(long totalExams) {
        this.totalExams = totalExams;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getAverageScorePercentage() {
        return averageScorePercentage;
    }

    public void setAverageScorePercentage(double averageScorePercentage) {
        this.averageScorePercentage = averageScorePercentage;
    }

    public double getOverallPassRate() {
        return overallPassRate;
    }

    public void setOverallPassRate(double overallPassRate) {
        this.overallPassRate = overallPassRate;
    }

    public Map<String, Integer> getSubjectExamCounts() {
        return subjectExamCounts;
    }

    public void setSubjectExamCounts(Map<String, Integer> subjectExamCounts) {
        this.subjectExamCounts = subjectExamCounts;
    }

    public Map<String, Double> getSubjectAverageScores() {
        return subjectAverageScores;
    }

    public void setSubjectAverageScores(Map<String, Double> subjectAverageScores) {
        this.subjectAverageScores = subjectAverageScores;
    }

    public Map<String, Integer> getDifficultyDistribution() {
        return difficultyDistribution;
    }

    public void setDifficultyDistribution(Map<String, Integer> difficultyDistribution) {
        this.difficultyDistribution = difficultyDistribution;
    }
}
