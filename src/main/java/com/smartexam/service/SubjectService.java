package com.smartexam.service;

import com.smartexam.entity.Subject;
import java.util.List;
import java.util.Optional;

public interface SubjectService {
    Subject createSubject(Subject subject);
    Subject updateSubject(Long id, Subject subjectDetails);
    Optional<Subject> findById(Long id);
    List<Subject> findAllActive();
    List<Subject> findAll();
    Subject toggleSubjectStatus(Long id);
    void deleteSubject(Long id);
}
