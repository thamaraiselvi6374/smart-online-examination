package com.smartexam.repository;

import com.smartexam.entity.ExamActivity;
import com.smartexam.entity.StudentExam;
import com.smartexam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamActivityRepository extends JpaRepository<ExamActivity, Long> {
    List<ExamActivity> findByStudentExam(StudentExam studentExam);
    List<ExamActivity> findByUser(User user);
    List<ExamActivity> findTop20ByOrderByTimestampDesc();
}
