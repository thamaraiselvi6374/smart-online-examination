package com.smartexam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExamDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Double passingMarks;

    private boolean questionRandomization = true;
    private boolean optionRandomization = true;
    private Integer maxWarnings = 3;
    private boolean published = false;

    public ExamDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getPassingMarks() {
        return passingMarks;
    }

    public void setPassingMarks(Double passingMarks) {
        this.passingMarks = passingMarks;
    }

    public boolean isQuestionRandomization() {
        return questionRandomization;
    }

    public void setQuestionRandomization(boolean questionRandomization) {
        this.questionRandomization = questionRandomization;
    }

    public boolean isOptionRandomization() {
        return optionRandomization;
    }

    public void setOptionRandomization(boolean optionRandomization) {
        this.optionRandomization = optionRandomization;
    }

    public Integer getMaxWarnings() {
        return maxWarnings;
    }

    public void setMaxWarnings(Integer maxWarnings) {
        this.maxWarnings = maxWarnings;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
