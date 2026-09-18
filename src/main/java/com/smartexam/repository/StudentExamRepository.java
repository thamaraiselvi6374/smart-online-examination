package com.smartexam.repository;

import com.smartexam.entity.StudentExam;
import com.smartexam.entity.User;
import com.smartexam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentExamRepository extends JpaRepository<StudentExam, Long> {
    List<StudentExam> findByStudent(User student);
    List<StudentExam> findByExam(Exam exam);
    List<StudentExam> findByStudentOrderByStartTimeDesc(User student);
    List<StudentExam> findByExamOrderByMarksObtainedDesc(Exam exam);
    Optional<StudentExam> findByStudentAndExamAndStatus(User student, Exam exam, StudentExam.AttemptStatus status);
    Optional<StudentExam> findFirstByStudentAndExamOrderByStartTimeDesc(User student, Exam exam);
    boolean existsByStudentAndExamAndStatusIn(User student, Exam exam, List<StudentExam.AttemptStatus> statuses);

    @Query("SELECT AVG(se.percentage) FROM StudentExam se WHERE se.student = :student AND se.status IN ('SUBMITTED', 'AUTO_SUBMITTED', 'TIMED_OUT')")
    Double findAveragePercentageByStudent(@Param("student") User student);

    @Query("SELECT MAX(se.percentage) FROM StudentExam se WHERE se.student = :student AND se.status IN ('SUBMITTED', 'AUTO_SUBMITTED', 'TIMED_OUT')")
    Double findMaxPercentageByStudent(@Param("student") User student);

    @Query("SELECT se FROM StudentExam se WHERE se.status IN ('SUBMITTED', 'AUTO_SUBMITTED', 'TIMED_OUT') ORDER BY se.percentage DESC, se.timeTakenSeconds ASC")
    List<StudentExam> findTopPerformers();

    @Query("SELECT se FROM StudentExam se WHERE se.exam = :exam AND se.status IN ('SUBMITTED', 'AUTO_SUBMITTED', 'TIMED_OUT') ORDER BY se.percentage DESC, se.timeTakenSeconds ASC")
    List<StudentExam> findTopPerformersByExam(@Param("exam") Exam exam);

    long countByExamAndStatusIn(Exam exam, List<StudentExam.AttemptStatus> statuses);
    long countByStatusIn(List<StudentExam.AttemptStatus> statuses);
}
