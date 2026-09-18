package com.smartexam.controller;

import com.smartexam.entity.*;
import com.smartexam.service.AnalyticsService;
import com.smartexam.service.ExamService;
import com.smartexam.service.QuestionService;
import com.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        List<Exam> availableExams = examService.findAllPublished();
        List<StudentExam> history = examService.getStudentHistory(student);

        List<StudentExam> completedHistory = history.stream()
                .filter(se -> se.getStatus() != StudentExam.AttemptStatus.IN_PROGRESS)
                .toList();

        double avgScore = completedHistory.stream().mapToDouble(StudentExam::getPercentage).average().orElse(0.0);
        double maxScore = completedHistory.stream().mapToDouble(StudentExam::getPercentage).max().orElse(0.0);

        model.addAttribute("student", student);
        model.addAttribute("availableExams", availableExams);
        model.addAttribute("history", history);
        model.addAttribute("completedCount", completedHistory.size());
        model.addAttribute("avgScore", Math.round(avgScore * 100.0) / 100.0);
        model.addAttribute("bestScore", Math.round(maxScore * 100.0) / 100.0);
        return "student/dashboard";
    }

    @GetMapping("/exams/{id}/instructions")
    public String examInstructions(@PathVariable Long id, Model model) {
        Exam exam = examService.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        model.addAttribute("exam", exam);
        return "student/exam-instructions";
    }

    @PostMapping("/exams/{id}/start")
    public String startExam(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        try {
            StudentExam studentExam = examService.startExam(id, student);
            return "redirect:/student/exams/take/" + studentExam.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/exams/take/{attemptId}")
    public String takeExam(@PathVariable Long attemptId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        StudentExam studentExam = examService.getStudentExam(attemptId);

        if (!studentExam.getStudent().getId().equals(student.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized exam access.");
            return "redirect:/student/dashboard";
        }

        if (studentExam.getStatus() != StudentExam.AttemptStatus.IN_PROGRESS) {
            return "redirect:/student/results/" + attemptId;
        }

        // Calculate remaining time in seconds
        long secondsRemaining = Duration.between(LocalDateTime.now(), studentExam.getEndTime()).getSeconds();
        if (secondsRemaining <= 0) {
            examService.submitExam(attemptId, student, "AUTO_SUBMIT_EXPIRED");
            return "redirect:/student/results/" + attemptId;
        }

        List<Question> questions = questionService.findByExamId(studentExam.getExam().getId());
        if (studentExam.getExam().isQuestionRandomization()) {
            // Consistent shuffle per attempt using seed based on attempt ID
            Collections.shuffle(questions, new Random(attemptId));
        }

        // Hydrate existing saved answers
        Map<Long, StudentAnswer> savedAnswersMap = new HashMap<>();
        for (StudentAnswer sa : studentExam.getStudentAnswers()) {
            savedAnswersMap.put(sa.getQuestion().getId(), sa);
        }

        model.addAttribute("studentExam", studentExam);
        model.addAttribute("exam", studentExam.getExam());
        model.addAttribute("questions", questions);
        model.addAttribute("savedAnswersMap", savedAnswersMap);
        model.addAttribute("secondsRemaining", secondsRemaining);
        return "student/take-exam";
    }

    @PostMapping("/exams/take/{attemptId}/submit")
    public String submitExam(@PathVariable Long attemptId,
                             @RequestParam(value = "source", required = false, defaultValue = "MANUAL_SUBMIT") String source,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        examService.submitExam(attemptId, student, source);
        return "redirect:/student/results/" + attemptId;
    }

    @GetMapping("/results/{attemptId}")
    public String resultSummary(@PathVariable Long attemptId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        StudentExam studentExam = examService.getStudentExam(attemptId);

        model.addAttribute("studentExam", studentExam);
        model.addAttribute("exam", studentExam.getExam());
        return "student/result-summary";
    }

    @GetMapping("/results/{attemptId}/review")
    public String answerReview(@PathVariable Long attemptId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        StudentExam studentExam = examService.getStudentExam(attemptId);
        List<Question> questions = questionService.findByExamId(studentExam.getExam().getId());

        Map<Long, StudentAnswer> answersMap = new HashMap<>();
        for (StudentAnswer sa : studentExam.getStudentAnswers()) {
            answersMap.put(sa.getQuestion().getId(), sa);
        }

        model.addAttribute("studentExam", studentExam);
        model.addAttribute("questions", questions);
        model.addAttribute("answersMap", answersMap);
        return "student/answer-review";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaderboard", analyticsService.getGlobalLeaderboard());
        model.addAttribute("exams", examService.findAllPublished());
        return "student/leaderboard";
    }
}
