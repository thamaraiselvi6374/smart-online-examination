package com.smartexam.controller;

import com.smartexam.dto.AnalyticsDto;
import com.smartexam.entity.Exam;
import com.smartexam.entity.ExamActivity;
import com.smartexam.entity.Subject;
import com.smartexam.entity.User;
import com.smartexam.repository.ExamActivityRepository;
import com.smartexam.service.AnalyticsService;
import com.smartexam.service.ExamService;
import com.smartexam.service.SubjectService;
import com.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ExamService examService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ExamActivityRepository activityRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userService.findByUsername(userDetails.getUsername()).orElse(null);
        AnalyticsDto analytics = analyticsService.getSystemAnalytics();
        List<ExamActivity> recentActivity = activityRepository.findTop20ByOrderByTimestampDesc();
        List<Exam> recentExams = examService.findAll();

        model.addAttribute("admin", admin);
        model.addAttribute("analytics", analytics);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("recentExams", recentExams);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("students", userService.findAllStudents());
        model.addAttribute("faculty", userService.findAllFaculty());
        model.addAttribute("allUsers", userService.findAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/create-faculty")
    public String createFaculty(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        try {
            User faculty = new User(username, password, fullName, email, phone, null, true);
            userService.createUser(faculty, "ROLE_FACULTY");
            redirectAttributes.addFlashAttribute("successMessage", "Faculty account created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated for " + user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/subjects")
    public String manageSubjects(Model model) {
        model.addAttribute("subjects", subjectService.findAll());
        model.addAttribute("newSubject", new Subject());
        return "admin/subjects";
    }

    @PostMapping("/subjects")
    public String createSubject(@ModelAttribute Subject subject, RedirectAttributes redirectAttributes) {
        try {
            subjectService.createSubject(subject);
            redirectAttributes.addFlashAttribute("successMessage", "Subject added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/{id}/toggle")
    public String toggleSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        subjectService.toggleSubjectStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Subject status updated!");
        return "redirect:/admin/subjects";
    }

    @GetMapping("/exams")
    public String manageExams(Model model) {
        model.addAttribute("exams", examService.findAll());
        return "admin/exams";
    }

    @GetMapping("/analytics")
    public String systemAnalytics(Model model) {
        model.addAttribute("analytics", analyticsService.getSystemAnalytics());
        model.addAttribute("leaderboard", analyticsService.getGlobalLeaderboard());
        return "admin/analytics";
    }

    @GetMapping("/activity-logs")
    public String activityLogs(Model model) {
        model.addAttribute("logs", activityRepository.findTop20ByOrderByTimestampDesc());
        return "admin/activity-logs";
    }
}
