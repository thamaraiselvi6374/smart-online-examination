package com.smartexam.service;

import com.smartexam.entity.Subject;
import com.smartexam.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public Subject createSubject(Subject subject) {
        if (subjectRepository.existsByCode(subject.getCode())) {
            throw new IllegalArgumentException("Subject code already exists: " + subject.getCode());
        }
        return subjectRepository.save(subject);
    }

    @Override
    public Subject updateSubject(Long id, Subject details) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + id));
        existing.setName(details.getName());
        existing.setDescription(details.getDescription());
        existing.setActive(details.isActive());
        return subjectRepository.save(existing);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    @Override
    public List<Subject> findAllActive() {
        return subjectRepository.findByActiveTrue();
    }

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    public Subject toggleSubjectStatus(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
        subject.setActive(!subject.isActive());
        return subjectRepository.save(subject);
    }

    @Override
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}
