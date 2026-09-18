-- =================================================================
-- Smart Online Examination and Auto Evaluation System
-- MySQL Normalized Database Schema Definition Script
-- Database Name: smart_exam_system
-- =================================================================

CREATE DATABASE IF NOT EXISTS `smart_exam_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smart_exam_system`;

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20),
    `role_id` BIGINT NOT NULL,
    `active` TINYINT(1) DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Subjects Table
CREATE TABLE IF NOT EXISTS `subjects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `active` TINYINT(1) DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Exams Table
CREATE TABLE IF NOT EXISTS `exams` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `subject_id` BIGINT NOT NULL,
    `faculty_id` BIGINT NOT NULL,
    `duration_minutes` INT NOT NULL,
    `total_marks` DOUBLE DEFAULT 0.0,
    `passing_marks` DOUBLE DEFAULT 0.0,
    `published` TINYINT(1) DEFAULT 0,
    `question_randomization` TINYINT(1) DEFAULT 1,
    `option_randomization` TINYINT(1) DEFAULT 1,
    `max_warnings` INT DEFAULT 3,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_exams_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_exams_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Questions Table
CREATE TABLE IF NOT EXISTS `questions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_id` BIGINT NOT NULL,
    `question_text` TEXT NOT NULL,
    `question_type` VARCHAR(30) NOT NULL, -- MCQ_SINGLE, MCQ_MULTIPLE, TRUE_FALSE, FILL_BLANK
    `difficulty` VARCHAR(20) DEFAULT 'MEDIUM', -- EASY, MEDIUM, HARD
    `marks` DOUBLE DEFAULT 1.0,
    `explanation` TEXT,
    `blank_answer` VARCHAR(500),
    CONSTRAINT `fk_questions_exam` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Question Options Table
CREATE TABLE IF NOT EXISTS `question_options` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id` BIGINT NOT NULL,
    `option_text` TEXT NOT NULL,
    `is_correct` TINYINT(1) DEFAULT 0,
    `option_order` INT DEFAULT 0,
    CONSTRAINT `fk_options_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Student Exams Attempt Table
CREATE TABLE IF NOT EXISTS `student_exams` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `exam_id` BIGINT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `submit_time` DATETIME,
    `status` VARCHAR(30) NOT NULL, -- IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED, TIMED_OUT, DISQUALIFIED
    `pass_status` VARCHAR(20) DEFAULT 'PENDING', -- PASS, FAIL, PENDING
    `total_questions` INT DEFAULT 0,
    `attempted_questions` INT DEFAULT 0,
    `correct_answers` INT DEFAULT 0,
    `wrong_answers` INT DEFAULT 0,
    `unanswered` INT DEFAULT 0,
    `total_marks` DOUBLE DEFAULT 0.0,
    `marks_obtained` DOUBLE DEFAULT 0.0,
    `percentage` DOUBLE DEFAULT 0.0,
    `accuracy_percentage` DOUBLE DEFAULT 0.0,
    `warning_count` INT DEFAULT 0,
    `time_taken_seconds` BIGINT DEFAULT 0,
    CONSTRAINT `fk_student_exams_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_student_exams_exam` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Student Answers Table
CREATE TABLE IF NOT EXISTS `student_answers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_exam_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `selected_option_ids` VARCHAR(255),
    `text_answer` TEXT,
    `is_correct` TINYINT(1),
    `marks_awarded` DOUBLE DEFAULT 0.0,
    `marked_for_review` TINYINT(1) DEFAULT 0,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_student_answers_attempt` FOREIGN KEY (`student_exam_id`) REFERENCES `student_exams` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_student_answers_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Exam Activity Audit Log Table
CREATE TABLE IF NOT EXISTS `exam_activity` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_exam_id` BIGINT,
    `user_id` BIGINT NOT NULL,
    `action_type` VARCHAR(50) NOT NULL,
    `description` VARCHAR(500),
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `message` VARCHAR(500) NOT NULL,
    `is_read` TINYINT(1) DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
