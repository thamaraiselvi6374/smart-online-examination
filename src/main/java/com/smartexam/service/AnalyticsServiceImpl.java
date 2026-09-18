package com.smartexam.service;

import com.smartexam.dto.AnalyticsDto;
import com.smartexam.dto.LeaderboardDto;
import com.smartexam.entity.Exam;
import com.smartexam.entity.Question;
import com.smartexam.entity.StudentExam;
import com.smartexam.entity.Subject;
import com.smartexam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentExamRepository studentExamRepository;

    @Override
    public AnalyticsDto getSystemAnalytics() {
        AnalyticsDto dto = new AnalyticsDto();
        dto.setTotalStudents(userRepository.countByRoleName("ROLE_STUDENT"));
        dto.setTotalFaculty(userRepository.countByRoleName("ROLE_FACULTY"));
        dto.setTotalExams(examRepository.count());
        dto.setTotalSubjects(subjectRepository.count());

        List<StudentExam.AttemptStatus> completedStatuses = Arrays.asList(
                StudentExam.AttemptStatus.SUBMITTED,
                StudentExam.AttemptStatus.AUTO_SUBMITTED,
                StudentExam.AttemptStatus.TIMED_OUT
        );

        long attempts = studentExamRepository.countByStatusIn(completedStatuses);
        dto.setTotalAttempts(attempts);

        List<StudentExam> allAttempts = studentExamRepository.findAll();
        List<StudentExam> completed = allAttempts.stream()
                .filter(se -> completedStatuses.contains(se.getStatus()))
                .toList();

        if (!completed.isEmpty()) {
            double avgScore = completed.stream().mapToDouble(StudentExam::getPercentage).average().orElse(0.0);
            dto.setAverageScorePercentage(Math.round(avgScore * 100.0) / 100.0);

            long passCount = completed.stream().filter(se -> se.getPassStatus() == StudentExam.PassStatus.PASS).count();
            double passRate = (passCount * 100.0) / completed.size();
            dto.setOverallPassRate(Math.round(passRate * 100.0) / 100.0);
        }

        // Subject exam breakdown & average scores
        List<Subject> subjects = subjectRepository.findAll();
        Map<String, Integer> subjectExams = new HashMap<>();
        Map<String, Double> subjectAvgScores = new HashMap<>();

        for (Subject sub : subjects) {
            List<Exam> exams = examRepository.findBySubjectAndPublishedTrue(sub);
            subjectExams.put(sub.getName(), exams.size());

            double subAvg = 0.0;
            int count = 0;
            for (Exam ex : exams) {
                List<StudentExam> exAttempts = studentExamRepository.findByExam(ex).stream()
                        .filter(se -> completedStatuses.contains(se.getStatus()))
                        .toList();
                if (!exAttempts.isEmpty()) {
                    subAvg += exAttempts.stream().mapToDouble(StudentExam::getPercentage).average().orElse(0.0);
                    count++;
                }
            }
            subjectAvgScores.put(sub.getName(), count > 0 ? Math.round((subAvg / count) * 100.0) / 100.0 : 0.0);
        }

        dto.setSubjectExamCounts(subjectExams);
        dto.setSubjectAverageScores(subjectAvgScores);

        // Question difficulty breakdown
        Map<String, Integer> diffMap = new HashMap<>();
        diffMap.put("EASY", 0);
        diffMap.put("MEDIUM", 0);
        diffMap.put("HARD", 0);

        List<Question> questions = questionRepository.findAll();
        for (Question q : questions) {
            String diffKey = q.getDifficulty() != null ? q.getDifficulty().name() : "MEDIUM";
            diffMap.put(diffKey, diffMap.getOrDefault(diffKey, 0) + 1);
        }
        dto.setDifficultyDistribution(diffMap);

        return dto;
    }

    @Override
    public List<LeaderboardDto> getGlobalLeaderboard() {
        List<StudentExam> topPerformers = studentExamRepository.findTopPerformers();
        return mapToLeaderboardDtos(topPerformers);
    }

    @Override
    public List<LeaderboardDto> getExamLeaderboard(Long examId) {
        Exam exam = examRepository.findById(examId).orElse(null);
        if (exam == null) return Collections.emptyList();
        List<StudentExam> topPerformers = studentExamRepository.findTopPerformersByExam(exam);
        return mapToLeaderboardDtos(topPerformers);
    }

    private List<LeaderboardDto> mapToLeaderboardDtos(List<StudentExam> studentExams) {
        List<LeaderboardDto> list = new ArrayList<>();
        int rank = 1;
        for (StudentExam se : studentExams) {
            LeaderboardDto dto = new LeaderboardDto();
            dto.setRank(rank++);
            dto.setStudentName(se.getStudent().getFullName());
            dto.setExamTitle(se.getExam().getTitle());
            dto.setSubjectName(se.getExam().getSubject().getName());
            dto.setScore(se.getMarksObtained());
            dto.setTotalMarks(se.getTotalMarks());
            dto.setPercentage(se.getPercentage());

            long seconds = se.getTimeTakenSeconds() != null ? se.getTimeTakenSeconds() : 0L;
            long mins = seconds / 60;
            long secs = seconds % 60;
            dto.setTimeTakenFormatted(String.format("%02dm %02ds", mins, secs));

            list.add(dto);
        }
        return list;
    }
}
