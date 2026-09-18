package com.smartexam.controller;

import com.smartexam.dto.ExamDto;
import com.smartexam.dto.QuestionDto;
import com.smartexam.entity.*;
import com.smartexam.service.ExamService;
import com.smartexam.service.QuestionService;
import com.smartexam.service.SubjectService;
import com.smartexam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExamService examService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User faculty = userService.findByUsername(userDetails.getUsername()).orElse(null);
        List<Exam> myExams = examService.findByFaculty(faculty);

        long totalQuestions = myExams.stream().mapToLong(e -> questionService.findByExamId(e.getId()).size()).sum();
        long totalAttempts = myExams.stream().mapToLong(e -> examService.getExamResultsForFaculty(e.getId()).size()).sum();

        model.addAttribute("faculty", faculty);
        model.addAttribute("myExams", myExams);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalAttempts", totalAttempts);
        return "faculty/dashboard";
    }

    @GetMapping("/exams/create")
    public String createExamForm(Model model) {
        model.addAttribute("examDto", new ExamDto());
        model.addAttribute("subjects", subjectService.findAllActive());
        return "faculty/exam-create";
    }

    @PostMapping("/exams/create")
    public String createExam(@Valid @ModelAttribute("examDto") ExamDto examDto,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("subjects", subjectService.findAllActive());
            return "faculty/exam-create";
        }

        User faculty = userService.findByUsername(userDetails.getUsername()).orElse(null);
        Exam created = examService.createExam(examDto, faculty);
        redirectAttributes.addFlashAttribute("successMessage", "Exam created successfully! Now add questions.");
        return "redirect:/faculty/exams/" + created.getId() + "/questions";
    }

    @GetMapping("/exams/{id}/edit")
    public String editExamForm(@PathVariable Long id, Model model) {
        Exam exam = examService.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));

        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setSubjectId(exam.getSubject().getId());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setQuestionRandomization(exam.isQuestionRandomization());
        dto.setOptionRandomization(exam.isOptionRandomization());
        dto.setMaxWarnings(exam.getMaxWarnings());
        dto.setPublished(exam.isPublished());

        model.addAttribute("examDto", dto);
        model.addAttribute("exam", exam);
        model.addAttribute("subjects", subjectService.findAllActive());
        return "faculty/exam-edit";
    }

    @PostMapping("/exams/{id}/edit")
    public String updateExam(@PathVariable Long id,
                             @Valid @ModelAttribute("examDto") ExamDto examDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("subjects", subjectService.findAllActive());
            return "faculty/exam-edit";
        }
        examService.updateExam(id, examDto);
        redirectAttributes.addFlashAttribute("successMessage", "Exam details updated successfully!");
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/exams/{id}/toggle-publish")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Exam exam = examService.togglePublishStatus(id);
        String status = exam.isPublished() ? "PUBLISHED" : "UNPUBLISHED (Draft)";
        redirectAttributes.addFlashAttribute("successMessage", "Exam is now " + status);
        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/exams/{id}/delete")
    public String deleteExam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        examService.deleteExam(id);
        redirectAttributes.addFlashAttribute("successMessage", "Exam deleted successfully.");
        return "redirect:/faculty/dashboard";
    }

    @GetMapping("/exams/{id}/questions")
    public String questionBuilder(@PathVariable Long id, Model model) {
        Exam exam = examService.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        List<Question> questions = questionService.findByExamId(id);

        QuestionDto questionDto = new QuestionDto();
        questionDto.setExamId(id);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("questionDto", questionDto);
        return "faculty/question-builder";
    }

    @PostMapping("/exams/{id}/questions")
    public String addQuestion(@PathVariable Long id,
                              @ModelAttribute QuestionDto questionDto,
                              RedirectAttributes redirectAttributes) {
        questionDto.setExamId(id);
        try {
            questionService.addQuestion(questionDto);
            redirectAttributes.addFlashAttribute("successMessage", "Question added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/faculty/exams/" + id + "/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, @RequestParam Long examId, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully.");
        return "redirect:/faculty/exams/" + examId + "/questions";
    }

    @GetMapping("/exams/{id}/results")
    public String viewResults(@PathVariable Long id, Model model) {
        Exam exam = examService.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found: " + id));
        List<StudentExam> attempts = examService.getExamResultsForFaculty(id);

        double avgPercentage = attempts.stream().mapToDouble(StudentExam::getPercentage).average().orElse(0.0);
        long passCount = attempts.stream().filter(a -> a.getPassStatus() == StudentExam.PassStatus.PASS).count();

        model.addAttribute("exam", exam);
        model.addAttribute("attempts", attempts);
        model.addAttribute("avgPercentage", Math.round(avgPercentage * 100.0) / 100.0);
        model.addAttribute("passCount", passCount);
        return "faculty/exam-results";
    }
}
