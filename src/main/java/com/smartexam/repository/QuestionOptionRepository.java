package com.smartexam.repository;

import com.smartexam.entity.QuestionOption;
import com.smartexam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestion(Question question);
    List<QuestionOption> findByQuestionId(Long questionId);
}
