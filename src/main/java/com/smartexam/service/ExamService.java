package com.smartexam.service;

import com.smartexam.dto.AnswerSaveDto;
import com.smartexam.dto.ExamDto;
import com.smartexam.entity.Exam;
import com.smartexam.entity.StudentExam;
import com.smartexam.entity.User;

import java.util.List;
import java.util.Optional;

public interface ExamService {
    Exam createExam(ExamDto dto, User faculty);
    Exam updateExam(Long id, ExamDto dto);
    Optional<Exam> findById(Long id);
    List<Exam> findByFaculty(User faculty);
    List<Exam> findAllPublished();
    List<Exam> findAll();
    Exam togglePublishStatus(Long id);
    void deleteExam(Long id);

    StudentExam startExam(Long examId, User student);
    StudentExam getStudentExam(Long studentExamId);
    void saveStudentAnswer(AnswerSaveDto dto, User student);
    StudentExam submitExam(Long studentExamId, User student, String submissionSource);
    List<StudentExam> getStudentHistory(User student);
    List<StudentExam> getExamResultsForFaculty(Long examId);
    void recordSecurityWarning(Long studentExamId, User student, String warningReason);
}
