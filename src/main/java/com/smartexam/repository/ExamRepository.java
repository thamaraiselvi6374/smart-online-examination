package com.smartexam.repository;

import com.smartexam.entity.Exam;
import com.smartexam.entity.User;
import com.smartexam.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByFaculty(User faculty);
    List<Exam> findByPublishedTrue();
    List<Exam> findBySubjectAndPublishedTrue(Subject subject);
    long countByPublishedTrue();
    List<Exam> findTop5ByOrderByCreatedAtDesc();
}
