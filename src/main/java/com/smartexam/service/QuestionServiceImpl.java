package com.smartexam.service;

import com.smartexam.dto.QuestionDto;
import com.smartexam.dto.QuestionOptionDto;
import com.smartexam.entity.Exam;
import com.smartexam.entity.Question;
import com.smartexam.entity.QuestionOption;
import com.smartexam.repository.ExamRepository;
import com.smartexam.repository.QuestionOptionRepository;
import com.smartexam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository optionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Override
    public Question addQuestion(QuestionDto dto) {
        Exam exam = examRepository.findById(dto.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + dto.getExamId()));

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setDifficulty(dto.getDifficulty());
        question.setMarks(dto.getMarks());
        question.setExplanation(dto.getExplanation());
        question.setBlankAnswer(dto.getBlankAnswer());

        Question saved = questionRepository.save(question);

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            int order = 1;
            for (QuestionOptionDto optDto : dto.getOptions()) {
                if (optDto.getOptionText() != null && !optDto.getOptionText().trim().isEmpty()) {
                    QuestionOption option = new QuestionOption();
                    option.setQuestion(saved);
                    option.setOptionText(optDto.getOptionText().trim());
                    option.setCorrect(optDto.isCorrect());
                    option.setOptionOrder(order++);
                    saved.getOptions().add(option);
                }
            }
        }

        // Recalculate total marks for the exam
        recalculateExamTotalMarks(exam);

        return questionRepository.save(saved);
    }

    @Override
    public Question updateQuestion(Long id, QuestionDto dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));

        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setDifficulty(dto.getDifficulty());
        question.setMarks(dto.getMarks());
        question.setExplanation(dto.getExplanation());
        question.setBlankAnswer(dto.getBlankAnswer());

        question.getOptions().clear();

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            int order = 1;
            for (QuestionOptionDto optDto : dto.getOptions()) {
                if (optDto.getOptionText() != null && !optDto.getOptionText().trim().isEmpty()) {
                    QuestionOption option = new QuestionOption();
                    option.setQuestion(question);
                    option.setOptionText(optDto.getOptionText().trim());
                    option.setCorrect(optDto.isCorrect());
                    option.setOptionOrder(order++);
                    question.getOptions().add(option);
                }
            }
        }

        Question updated = questionRepository.save(question);
        recalculateExamTotalMarks(question.getExam());
        return updated;
    }

    @Override
    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public List<Question> findByExamId(Long examId) {
        return questionRepository.findByExamId(examId);
    }

    @Override
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question != null) {
            Exam exam = question.getExam();
            questionRepository.delete(question);
            recalculateExamTotalMarks(exam);
        }
    }

    private void recalculateExamTotalMarks(Exam exam) {
        List<Question> questions = questionRepository.findByExam(exam);
        double total = questions.stream().mapToDouble(Question::getMarks).sum();
        exam.setTotalMarks(total);
        examRepository.save(exam);
    }
}
