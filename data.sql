-- =================================================================
-- Smart Online Examination and Auto Evaluation System
-- MySQL Sample Seed Data DML Script
-- =================================================================

USE `smart_exam_system`;

-- Insert Roles
INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_FACULTY'),
(3, 'ROLE_STUDENT');

-- Insert Users (Passwords BCrypt encoded for 'admin123', 'faculty123', 'student123')
INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `email`, `phone`, `role_id`, `active`) VALUES
(1, 'admin', '$2a$10$w8.1c9bZ5mH/7yL0cR1v.u1u0i2Q4E5b6C7d8E9f0G1h2I3j4K5l6', 'System Administrator', 'admin@smartexam.com', '+1 800-555-0199', 1, 1),
(2, 'faculty1', '$2a$10$w8.1c9bZ5mH/7yL0cR1v.u1u0i2Q4E5b6C7d8E9f0G1h2I3j4K5l6', 'Prof. Alan Turing', 'turing@smartexam.com', '+1 800-555-0101', 2, 1),
(3, 'student1', '$2a$10$w8.1c9bZ5mH/7yL0cR1v.u1u0i2Q4E5b6C7d8E9f0G1h2I3j4K5l6', 'John Doe', 'john@smartexam.com', '+1 800-555-0103', 3, 1);

-- Insert Subjects
INSERT INTO `subjects` (`id`, `code`, `name`, `description`, `active`) VALUES
(1, 'CS101', 'Java Programming & OOP', 'Core Java, Object Oriented Principles, Inheritance, and Collections', 1),
(2, 'CS201', 'Web Application Development', 'HTML5, CSS3, JavaScript, Spring Boot, REST APIs, and Responsive UI', 1),
(3, 'CS301', 'Database Management Systems', 'Relational database concepts, SQL queries, normalization, indexing, and JPA', 1);
