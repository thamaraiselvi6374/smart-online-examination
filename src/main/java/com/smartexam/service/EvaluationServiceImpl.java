package com.smartexam.service;

import com.smartexam.entity.*;
import com.smartexam.repository.QuestionRepository;
import com.smartexam.repository.StudentAnswerRepository;
import com.smartexam.repository.StudentExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentExamRepository studentExamRepository;

    @Override
    public StudentExam evaluateExam(StudentExam studentExam) {
        Exam exam = studentExam.getExam();
        List<Question> questions = questionRepository.findByExam(exam);
        List<StudentAnswer> studentAnswers = studentAnswerRepository.findByStudentExam(studentExam);

        Map<Long, StudentAnswer> answerMap = studentAnswers.stream()
                .collect(Collectors.toMap(sa -> sa.getQuestion().getId(), sa -> sa));

        int totalQuestions = questions.size();
        int attemptedCount = 0;
        int correctCount = 0;
        int wrongCount = 0;
        double marksObtained = 0.0;
        double totalExamMarks = exam.getTotalMarks() != null && exam.getTotalMarks() > 0 ? exam.getTotalMarks() : 1.0;

        for (Question q : questions) {
            StudentAnswer answer = answerMap.get(q.getId());
            boolean isAttempted = false;
            boolean isCorrect = false;

            if (answer != null) {
                switch (q.getQuestionType()) {
                    case MCQ_SINGLE:
                    case TRUE_FALSE:
                        if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().trim().isEmpty()) {
                            isAttempted = true;
                            Long selectedOptId = parseLong(answer.getSelectedOptionIds().trim());
                            Optional<QuestionOption> correctOpt = q.getOptions().stream().filter(QuestionOption::isCorrect).findFirst();
                            if (correctOpt.isPresent() && correctOpt.get().getId().equals(selectedOptId)) {
                                isCorrect = true;
                            }
                        }
                        break;

                    case MCQ_MULTIPLE:
                        if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().trim().isEmpty()) {
                            isAttempted = true;
                            Set<Long> selectedIds = parseLongSet(answer.getSelectedOptionIds());
                            Set<Long> correctIds = q.getOptions().stream()
                                    .filter(QuestionOption::isCorrect)
                                    .map(QuestionOption::getId)
                                    .collect(Collectors.toSet());

                            if (!correctIds.isEmpty() && selectedIds.equals(correctIds)) {
                                isCorrect = true;
                            }
                        }
                        break;

                    case FILL_BLANK:
                        if (answer.getTextAnswer() != null && !answer.getTextAnswer().trim().isEmpty()) {
                            isAttempted = true;
                            String studentText = answer.getTextAnswer().trim().toLowerCase();
                            String expected = q.getBlankAnswer() != null ? q.getBlankAnswer().trim().toLowerCase() : "";

                            // Also check options if options were added for blank
                            Set<String> validAnswers = new HashSet<>();
                            if (!expected.isEmpty()) validAnswers.add(expected);
                            for (QuestionOption opt : q.getOptions()) {
                                if (opt.isCorrect() && opt.getOptionText() != null) {
                                    validAnswers.add(opt.getOptionText().trim().toLowerCase());
                                }
                            }

                            if (validAnswers.contains(studentText)) {
                                isCorrect = true;
                            }
                        }
                        break;
                }

                answer.setCorrect(isCorrect);
                answer.setMarksAwarded(isCorrect ? q.getMarks() : 0.0);
                studentAnswerRepository.save(answer);
            }

            if (isAttempted) {
                attemptedCount++;
                if (isCorrect) {
                    correctCount++;
                    marksObtained += q.getMarks();
                } else {
                    wrongCount++;
                }
            }
        }

        int unansweredCount = totalQuestions - attemptedCount;
        double percentage = Math.round((marksObtained / totalExamMarks) * 10000.0) / 100.0; // 2 decimal places
        double accuracy = attemptedCount > 0 ? Math.round((correctCount * 100.0 / attemptedCount) * 100.0) / 100.0 : 0.0;

        studentExam.setTotalQuestions(totalQuestions);
        studentExam.setAttemptedQuestions(attemptedCount);
        studentExam.setCorrectAnswers(correctCount);
        studentExam.setWrongAnswers(wrongCount);
        studentExam.setUnanswered(unansweredCount);
        studentExam.setTotalMarks(totalExamMarks);
        studentExam.setMarksObtained(marksObtained);
        studentExam.setPercentage(percentage);
        studentExam.setAccuracyPercentage(accuracy);

        if (studentExam.getSubmitTime() == null) {
            studentExam.setSubmitTime(LocalDateTime.now());
        }

        long timeTakenSec = Math.max(0, Duration.between(studentExam.getStartTime(), studentExam.getSubmitTime()).getSeconds());
        studentExam.setTimeTakenSeconds(timeTakenSec);

        if (marksObtained >= exam.getPassingMarks()) {
            studentExam.setPassStatus(StudentExam.PassStatus.PASS);
        } else {
            studentExam.setPassStatus(StudentExam.PassStatus.FAIL);
        }

        return studentExamRepository.save(studentExam);
    }

    private Long parseLong(String val) {
        try {
            return Long.parseLong(val.split(",")[0].trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Set<Long> parseLongSet(String val) {
        Set<Long> set = new HashSet<>();
        if (val == null || val.trim().isEmpty()) return set;
        String[] tokens = val.split(",");
        for (String t : tokens) {
            try {
                set.add(Long.parseLong(t.trim()));
            } catch (Exception ignored) {
            }
        }
        return set;
    }
}
