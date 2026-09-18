package com.smartexam.repository;

import com.smartexam.entity.StudentAnswer;
import com.smartexam.entity.StudentExam;
import com.smartexam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByStudentExam(StudentExam studentExam);
    Optional<StudentAnswer> findByStudentExamAndQuestion(StudentExam studentExam, Question question);
    List<StudentAnswer> findByStudentExamId(Long studentExamId);
}
