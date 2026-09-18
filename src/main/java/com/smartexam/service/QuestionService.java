package com.smartexam.service;

import com.smartexam.dto.QuestionDto;
import com.smartexam.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    Question addQuestion(QuestionDto dto);
    Question updateQuestion(Long id, QuestionDto dto);
    Optional<Question> findById(Long id);
    List<Question> findByExamId(Long examId);
    void deleteQuestion(Long id);
}
