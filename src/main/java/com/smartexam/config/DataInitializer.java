package com.smartexam.config;

import com.smartexam.entity.*;
import com.smartexam.repository.*;
import com.smartexam.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentExamRepository studentExamRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            seedRoles();
            seedUsers();
            seedSubjects();
            seedExamsAndQuestions();
            seedSampleAttempts();
        }
    }

    private void seedRoles() {
        roleRepository.save(new Role("ROLE_ADMIN"));
        roleRepository.save(new Role("ROLE_FACULTY"));
        roleRepository.save(new Role("ROLE_STUDENT"));
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
        Role facultyRole = roleRepository.findByName("ROLE_FACULTY").get();
        Role studentRole = roleRepository.findByName("ROLE_STUDENT").get();

        // Admin User
        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "System Administrator", "admin@smartexam.com", "+1 800-555-0199", adminRole, true));

        // Faculty Users
        userRepository.save(new User("faculty1", passwordEncoder.encode("faculty123"), "Prof. Alan Turing", "turing@smartexam.com", "+1 800-555-0101", facultyRole, true));
        userRepository.save(new User("prof_smith", passwordEncoder.encode("faculty123"), "Dr. Sarah Smith", "smith@smartexam.com", "+1 800-555-0102", facultyRole, true));

        // Student Users
        userRepository.save(new User("student1", passwordEncoder.encode("student123"), "John Doe", "john@smartexam.com", "+1 800-555-0103", studentRole, true));
        userRepository.save(new User("alice", passwordEncoder.encode("student123"), "Alice Williams", "alice@smartexam.com", "+1 800-555-0104", studentRole, true));
        userRepository.save(new User("bob", passwordEncoder.encode("student123"), "Bob Marley", "bob@smartexam.com", "+1 800-555-0105", studentRole, true));
    }

    private void seedSubjects() {
        subjectRepository.save(new Subject("CS101", "Java Programming & OOP", "Core concepts of Java, Object Oriented Principles, Inheritance, and Collections", true));
        subjectRepository.save(new Subject("CS201", "Web Application Development", "HTML5, CSS3, JavaScript, Spring Boot, REST APIs, and Responsive UI", true));
        subjectRepository.save(new Subject("CS301", "Database Management Systems", "Relational database concepts, SQL queries, normalization, indexing, and JPA", true));
        subjectRepository.save(new Subject("CS401", "Data Structures & Algorithms", "Arrays, Linked Lists, Trees, Sorting, Searching, and Dynamic Programming", true));
    }

    private void seedExamsAndQuestions() {
        User faculty = userRepository.findByUsername("faculty1").get();
        Subject javaSubject = subjectRepository.findByCode("CS101").get();
        Subject dbSubject = subjectRepository.findByCode("CS301").get();

        // 1. Java Exam
        Exam javaExam = new Exam();
        javaExam.setTitle("Java Core & OOP Midterm Exam");
        javaExam.setDescription("Assessment covering OOP principles, JVM architecture, exception handling, and Java Collections framework.");
        javaExam.setSubject(javaSubject);
        javaExam.setFaculty(faculty);
        javaExam.setDurationMinutes(20);
        javaExam.setPassingMarks(10.0);
        javaExam.setPublished(true);
        javaExam.setQuestionRandomization(true);
        javaExam.setOptionRandomization(true);
        javaExam.setMaxWarnings(3);
        javaExam = examRepository.save(javaExam);

        // Question 1: MCQ Single Choice
        Question q1 = new Question();
        q1.setExam(javaExam);
        q1.setQuestionText("Which feature of Object-Oriented Programming allows a subclass to provide a specific implementation of a method that is already provided by its superclass?");
        q1.setQuestionType(Question.QuestionType.MCQ_SINGLE);
        q1.setDifficulty(Question.Difficulty.EASY);
        q1.setMarks(5.0);
        q1.setExplanation("Method Overriding allows a child class to provide a custom implementation of a method defined in its parent class.");
        q1 = questionRepository.save(q1);

        q1.getOptions().add(new QuestionOption("Method Overloading", false, 1));
        q1.getOptions().add(new QuestionOption("Method Overriding", true, 2));
        q1.getOptions().add(new QuestionOption("Encapsulation", false, 3));
        q1.getOptions().add(new QuestionOption("Abstraction", false, 4));
        for (QuestionOption opt : q1.getOptions()) opt.setQuestion(q1);
        questionRepository.save(q1);

        // Question 2: MCQ Multiple Choice
        Question q2 = new Question();
        q2.setExam(javaExam);
        q2.setQuestionText("Select ALL of the following that are valid interfaces in the Java Collections Framework:");
        q2.setQuestionType(Question.QuestionType.MCQ_MULTIPLE);
        q2.setDifficulty(Question.Difficulty.MEDIUM);
        q2.setMarks(5.0);
        q2.setExplanation("List and Set are core collection interfaces. ArrayList is a concrete class, and Vector is a legacy class.");
        q2 = questionRepository.save(q2);

        q2.getOptions().add(new QuestionOption("List", true, 1));
        q2.getOptions().add(new QuestionOption("ArrayList", false, 2));
        q2.getOptions().add(new QuestionOption("Set", true, 3));
        q2.getOptions().add(new QuestionOption("Vector", false, 4));
        for (QuestionOption opt : q2.getOptions()) opt.setQuestion(q2);
        questionRepository.save(q2);

        // Question 3: True / False
        Question q3 = new Question();
        q3.setExam(javaExam);
        q3.setQuestionText("In Java, a class can directly inherit from multiple concrete parent classes using the 'extends' keyword.");
        q3.setQuestionType(Question.QuestionType.TRUE_FALSE);
        q3.setDifficulty(Question.Difficulty.EASY);
        q3.setMarks(5.0);
        q3.setExplanation("Java does not support multiple class inheritance to avoid diamond problem ambiguity. Interface implementation supports multiple inheritance.");
        q3 = questionRepository.save(q3);

        q3.getOptions().add(new QuestionOption("True", false, 1));
        q3.getOptions().add(new QuestionOption("False", true, 2));
        for (QuestionOption opt : q3.getOptions()) opt.setQuestion(q3);
        questionRepository.save(q3);

        // Question 4: Fill in the Blank
        Question q4 = new Question();
        q4.setExam(javaExam);
        q4.setQuestionText("What keyword is used in Java to restrict a class from being inherited or a method from being overridden?");
        q4.setQuestionType(Question.QuestionType.FILL_BLANK);
        q4.setDifficulty(Question.Difficulty.MEDIUM);
        q4.setMarks(5.0);
        q4.setBlankAnswer("final");
        q4.setExplanation("The 'final' keyword prevents inheritance when applied to classes and overriding when applied to methods.");
        questionRepository.save(q4);

        javaExam.setTotalMarks(20.0);
        examRepository.save(javaExam);

        // 2. DBMS Exam
        Exam dbExam = new Exam();
        dbExam.setTitle("DBMS Relational SQL & Normalization Test");
        dbExam.setDescription("Covers SQL queries, primary/foreign keys, joins, and normal forms.");
        dbExam.setSubject(dbSubject);
        dbExam.setFaculty(faculty);
        dbExam.setDurationMinutes(15);
        dbExam.setPassingMarks(7.0);
        dbExam.setPublished(true);
        dbExam.setQuestionRandomization(true);
        dbExam.setOptionRandomization(true);
        dbExam.setMaxWarnings(3);
        dbExam = examRepository.save(dbExam);

        // Question 1: MCQ Single
        Question dbq1 = new Question();
        dbq1.setExam(dbExam);
        dbq1.setQuestionText("Which SQL clause is used to filter records resulting from a GROUP BY clause?");
        dbq1.setQuestionType(Question.QuestionType.MCQ_SINGLE);
        dbq1.setDifficulty(Question.Difficulty.EASY);
        dbq1.setMarks(5.0);
        dbq1.setExplanation("The HAVING clause was added to SQL because the WHERE keyword could not be used with aggregate functions.");
        dbq1 = questionRepository.save(dbq1);

        dbq1.getOptions().add(new QuestionOption("WHERE", false, 1));
        dbq1.getOptions().add(new QuestionOption("HAVING", true, 2));
        dbq1.getOptions().add(new QuestionOption("ORDER BY", false, 3));
        dbq1.getOptions().add(new QuestionOption("FILTER", false, 4));
        for (QuestionOption opt : dbq1.getOptions()) opt.setQuestion(dbq1);
        questionRepository.save(dbq1);

        // Question 2: Fill in Blank
        Question dbq2 = new Question();
        dbq2.setExam(dbExam);
        dbq2.setQuestionText("A Normal Form is in ________ Normal Form (3NF) if it is in 2NF and has no transitive functional dependencies.");
        dbq2.setQuestionType(Question.QuestionType.FILL_BLANK);
        dbq2.setDifficulty(Question.Difficulty.HARD);
        dbq2.setMarks(5.0);
        dbq2.setBlankAnswer("Third");
        dbq2.setExplanation("Third Normal Form (3NF) requires 2NF and elimination of transitive dependency.");
        questionRepository.save(dbq2);

        dbExam.setTotalMarks(10.0);
        examRepository.save(dbExam);
    }

    private void seedSampleAttempts() {
        User alice = userRepository.findByUsername("alice").get();
        User bob = userRepository.findByUsername("bob").get();
        Exam javaExam = examRepository.findAll().get(0);

        // Alice attempt
        StudentExam aliceExam = new StudentExam();
        aliceExam.setStudent(alice);
        aliceExam.setExam(javaExam);
        aliceExam.setStartTime(LocalDateTime.now().minusHours(2));
        aliceExam.setEndTime(LocalDateTime.now().minusHours(2).plusMinutes(20));
        aliceExam.setSubmitTime(LocalDateTime.now().minusHours(2).plusMinutes(12));
        aliceExam.setStatus(StudentExam.AttemptStatus.SUBMITTED);
        aliceExam.setTotalQuestions(4);
        aliceExam.setTotalMarks(20.0);
        aliceExam = studentExamRepository.save(aliceExam);

        List<Question> questions = questionRepository.findByExam(javaExam);
        for (Question q : questions) {
            StudentAnswer sa = new StudentAnswer();
            sa.setStudentExam(aliceExam);
            sa.setQuestion(q);

            if (q.getQuestionType() == Question.QuestionType.MCQ_SINGLE) {
                QuestionOption correctOpt = q.getOptions().stream().filter(QuestionOption::isCorrect).findFirst().get();
                sa.setSelectedOptionIds(correctOpt.getId().toString());
            } else if (q.getQuestionType() == Question.QuestionType.MCQ_MULTIPLE) {
                List<Long> ids = q.getOptions().stream().filter(QuestionOption::isCorrect).map(QuestionOption::getId).toList();
                sa.setSelectedOptionIds(ids.get(0) + "," + ids.get(1));
            } else if (q.getQuestionType() == Question.QuestionType.TRUE_FALSE) {
                QuestionOption correctOpt = q.getOptions().stream().filter(QuestionOption::isCorrect).findFirst().get();
                sa.setSelectedOptionIds(correctOpt.getId().toString());
            } else if (q.getQuestionType() == Question.QuestionType.FILL_BLANK) {
                sa.setTextAnswer("final");
            }
            studentAnswerRepository.save(sa);
        }
        evaluationService.evaluateExam(aliceExam);

        // Bob attempt
        StudentExam bobExam = new StudentExam();
        bobExam.setStudent(bob);
        bobExam.setExam(javaExam);
        bobExam.setStartTime(LocalDateTime.now().minusDays(1));
        bobExam.setEndTime(LocalDateTime.now().minusDays(1).plusMinutes(20));
        bobExam.setSubmitTime(LocalDateTime.now().minusDays(1).plusMinutes(18));
        bobExam.setStatus(StudentExam.AttemptStatus.SUBMITTED);
        bobExam.setTotalQuestions(4);
        bobExam.setTotalMarks(20.0);
        bobExam = studentExamRepository.save(bobExam);

        for (Question q : questions) {
            StudentAnswer sa = new StudentAnswer();
            sa.setStudentExam(bobExam);
            sa.setQuestion(q);

            if (q.getQuestionType() == Question.QuestionType.MCQ_SINGLE) {
                QuestionOption firstOpt = q.getOptions().get(0);
                sa.setSelectedOptionIds(firstOpt.getId().toString());
            } else if (q.getQuestionType() == Question.QuestionType.FILL_BLANK) {
                sa.setTextAnswer("static");
            }
            studentAnswerRepository.save(sa);
        }
        evaluationService.evaluateExam(bobExam);
    }
}
