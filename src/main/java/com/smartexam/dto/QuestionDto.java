package com.smartexam.dto;

import com.smartexam.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class QuestionDto {

    private Long id;

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private Question.QuestionType questionType = Question.QuestionType.MCQ_SINGLE;

    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

    @NotNull(message = "Marks is required")
    private Double marks = 1.0;

    private String explanation;

    private String blankAnswer;

    private List<QuestionOptionDto> options = new ArrayList<>();

    public QuestionDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Question.QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Question.QuestionType questionType) {
        this.questionType = questionType;
    }

    public Question.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Question.Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getBlankAnswer() {
        return blankAnswer;
    }

    public void setBlankAnswer(String blankAnswer) {
        this.blankAnswer = blankAnswer;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
