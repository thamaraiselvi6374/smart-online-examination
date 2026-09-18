package com.smartexam.service;

import com.smartexam.dto.AnswerSaveDto;
import com.smartexam.dto.ExamDto;
import com.smartexam.entity.*;
import com.smartexam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentExamRepository studentExamRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private ExamActivityRepository activityRepository;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public Exam createExam(ExamDto dto, User faculty) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + dto.getSubjectId()));

        Exam exam = new Exam();
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setSubject(subject);
        exam.setFaculty(faculty);
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setQuestionRandomization(dto.isQuestionRandomization());
        exam.setOptionRandomization(dto.isOptionRandomization());
        exam.setMaxWarnings(dto.getMaxWarnings() != null ? dto.getMaxWarnings() : 3);
        exam.setPublished(dto.isPublished());

        return examRepository.save(exam);
    }

    @Override
    public Exam updateExam(Long id, ExamDto dto) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + dto.getSubjectId()));

        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setSubject(subject);
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setQuestionRandomization(dto.isQuestionRandomization());
        exam.setOptionRandomization(dto.isOptionRandomization());
        exam.setMaxWarnings(dto.getMaxWarnings());
        exam.setPublished(dto.isPublished());

        return examRepository.save(exam);
    }

    @Override
    public Optional<Exam> findById(Long id) {
        return examRepository.findById(id);
    }

    @Override
    public List<Exam> findByFaculty(User faculty) {
        return examRepository.findByFaculty(faculty);
    }

    @Override
    public List<Exam> findAllPublished() {
        return examRepository.findByPublishedTrue();
    }

    @Override
    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    @Override
    public Exam togglePublishStatus(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        exam.setPublished(!exam.isPublished());
        return examRepository.save(exam);
    }

    @Override
    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }

    @Override
    public StudentExam startExam(Long examId, User student) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        if (!exam.isPublished()) {
            throw new IllegalStateException("Exam is not published yet.");
        }

        // Check if student already has an active or submitted attempt
        Optional<StudentExam> existing = studentExamRepository.findFirstByStudentAndExamOrderByStartTimeDesc(student, exam);
        if (existing.isPresent()) {
            StudentExam se = existing.get();
            if (se.getStatus() == StudentExam.AttemptStatus.IN_PROGRESS) {
                // If existing test attempt is in progress and time has not expired, return it
                if (LocalDateTime.now().isBefore(se.getEndTime())) {
                    return se;
                } else {
                    // Time expired while disconnected -> submit automatically
                    se.setStatus(StudentExam.AttemptStatus.TIMED_OUT);
                    return evaluationService.evaluateExam(se);
                }
            } else {
                throw new IllegalStateException("You have already attempted this examination.");
            }
        }

        // Create new attempt
        StudentExam studentExam = new StudentExam();
        studentExam.setStudent(student);
        studentExam.setExam(exam);
        studentExam.setStartTime(LocalDateTime.now());
        studentExam.setEndTime(LocalDateTime.now().plusMinutes(exam.getDurationMinutes()));
        studentExam.setStatus(StudentExam.AttemptStatus.IN_PROGRESS);

        List<Question> questions = questionRepository.findByExam(exam);
        studentExam.setTotalQuestions(questions.size());
        studentExam.setTotalMarks(exam.getTotalMarks());

        StudentExam saved = studentExamRepository.save(studentExam);

        // Pre-create empty StudentAnswers for each question to track state cleanly
        for (Question q : questions) {
            StudentAnswer sa = new StudentAnswer();
            sa.setStudentExam(saved);
            sa.setQuestion(q);
            sa.setMarkedForReview(false);
            studentAnswerRepository.save(sa);
        }

        // Log Activity
        activityRepository.save(new ExamActivity(saved, student, "START", "Student started examination: " + exam.getTitle()));

        return saved;
    }

    @Override
    public StudentExam getStudentExam(Long studentExamId) {
        return studentExamRepository.findById(studentExamId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found: " + studentExamId));
    }

    @Override
    public void saveStudentAnswer(AnswerSaveDto dto, User student) {
        StudentExam studentExam = studentExamRepository.findById(dto.getStudentExamId())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        if (!studentExam.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Unauthorized attempt modification");
        }

        if (studentExam.getStatus() != StudentExam.AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot update answers for completed exam");
        }

        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Optional<StudentAnswer> saOpt = studentAnswerRepository.findByStudentExamAndQuestion(studentExam, question);
        StudentAnswer sa = saOpt.orElseGet(() -> {
            StudentAnswer newSa = new StudentAnswer();
            newSa.setStudentExam(studentExam);
            newSa.setQuestion(question);
            return newSa;
        });

        sa.setSelectedOptionIds(dto.getSelectedOptionIds());
        sa.setTextAnswer(dto.getTextAnswer());
        sa.setMarkedForReview(dto.isMarkedForReview());
        sa.setUpdatedAt(LocalDateTime.now());

        studentAnswerRepository.save(sa);
    }

    @Override
    public StudentExam submitExam(Long studentExamId, User student, String submissionSource) {
        StudentExam studentExam = studentExamRepository.findById(studentExamId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        if (studentExam.getStatus() != StudentExam.AttemptStatus.IN_PROGRESS) {
            return studentExam;
        }

        if ("AUTO_SUBMIT_EXPIRED".equals(submissionSource) || LocalDateTime.now().isAfter(studentExam.getEndTime().plusSeconds(10))) {
            studentExam.setStatus(StudentExam.AttemptStatus.TIMED_OUT);
        } else if ("DISQUALIFIED".equals(submissionSource)) {
            studentExam.setStatus(StudentExam.AttemptStatus.DISQUALIFIED);
        } else {
            studentExam.setStatus(StudentExam.AttemptStatus.SUBMITTED);
        }

        studentExam.setSubmitTime(LocalDateTime.now());
        StudentExam evaluated = evaluationService.evaluateExam(studentExam);

        activityRepository.save(new ExamActivity(evaluated, student, submissionSource, "Exam submitted with status: " + evaluated.getStatus()));
        return evaluated;
    }

    @Override
    public List<StudentExam> getStudentHistory(User student) {
        return studentExamRepository.findByStudentOrderByStartTimeDesc(student);
    }

    @Override
    public List<StudentExam> getExamResultsForFaculty(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
        return studentExamRepository.findByExamOrderByMarksObtainedDesc(exam);
    }

    @Override
    public void recordSecurityWarning(Long studentExamId, User student, String warningReason) {
        StudentExam se = studentExamRepository.findById(studentExamId).orElse(null);
        if (se != null && se.getStatus() == StudentExam.AttemptStatus.IN_PROGRESS) {
            se.setWarningCount(se.getWarningCount() + 1);
            studentExamRepository.save(se);

            activityRepository.save(new ExamActivity(se, student, "TAB_SWITCH_WARNING", warningReason + " (Warning #" + se.getWarningCount() + ")"));

            // Force auto submit if warning count exceeds threshold
            if (se.getWarningCount() >= se.getExam().getMaxWarnings()) {
                submitExam(studentExamId, student, "DISQUALIFIED");
            }
        }
    }
}
