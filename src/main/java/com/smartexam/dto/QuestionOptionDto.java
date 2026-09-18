package com.smartexam.dto;

public class QuestionOptionDto {

    private Long id;
    private String optionText;
    private boolean correct = false;
    private Integer optionOrder = 0;

    public QuestionOptionDto() {
    }

    public QuestionOptionDto(String optionText, boolean correct) {
        this.optionText = optionText;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Integer getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(Integer optionOrder) {
        this.optionOrder = optionOrder;
    }
}
