package com.smartexam.controller;

import com.smartexam.dto.AnswerSaveDto;
import com.smartexam.entity.StudentExam;
import com.smartexam.entity.User;
import com.smartexam.service.ExamService;
import com.smartexam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamApiController {

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    @PostMapping("/save-answer")
    public ResponseEntity<?> saveAnswer(@RequestBody AnswerSaveDto dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        try {
            examService.saveStudentAnswer(dto, student);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Answer auto-saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/log-warning")
    public ResponseEntity<?> logWarning(@RequestParam Long attemptId,
                                         @RequestParam String reason,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User student = userService.findByUsername(userDetails.getUsername()).orElse(null);
        examService.recordSecurityWarning(attemptId, student, reason);

        StudentExam se = examService.getStudentExam(attemptId);
        Map<String, Object> response = new HashMap<>();
        response.put("warningCount", se.getWarningCount());
        response.put("maxWarnings", se.getExam().getMaxWarnings());
        response.put("disqualified", se.getStatus() == StudentExam.AttemptStatus.DISQUALIFIED);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-remaining/{attemptId}")
    public ResponseEntity<?> getTimeRemaining(@PathVariable Long attemptId) {
        StudentExam se = examService.getStudentExam(attemptId);
        long secondsRemaining = Math.max(0, Duration.between(LocalDateTime.now(), se.getEndTime()).getSeconds());

        Map<String, Object> response = new HashMap<>();
        response.put("secondsRemaining", secondsRemaining);
        response.put("status", se.getStatus().name());

        return ResponseEntity.ok(response);
    }
}
