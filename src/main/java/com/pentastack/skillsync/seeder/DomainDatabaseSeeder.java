package com.pentastack.skillsync.seeder;

import com.pentastack.skillsync.domain.*;
import com.pentastack.skillsync.domain.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DomainDatabaseSeeder implements CommandLineRunner {

    private final UserRepository domainUserRepository;
    private final StudentProfileRepository domainStudentProfileRepository;
    private final MentorProfileRepository domainMentorProfileRepository;
    private final StackRepository stackRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final SessionAuditLogRepository sessionAuditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Stack javaStack = ensureStack("Java", "Enterprise JVM development");
        Stack reactStack = ensureStack("React", "Modern frontend engineering");
        Stack goStack = ensureStack("Go", "High-performance backend systems");
        Stack pythonStack = ensureStack("Python", "Data science and automation");
        Stack devopsStack = ensureStack("DevOps", "CI/CD and cloud infrastructure");
        Stack rustStack = ensureStack("Rust", "Systems programming and performance");
        ensureStack("TypeScript", "Typed JavaScript for scale");
        ensureStack("Kubernetes", "Container orchestration at scale");
        ensureStack("Node.js", "Server-side JavaScript runtime");
        ensureStack("Angular", "Enterprise-grade frontend framework");
        ensureStack("Vue.js", "Progressive frontend framework");
        ensureStack("Spring Boot", "Java enterprise application framework");
        ensureStack("AWS", "Amazon Web Services cloud platform");
        ensureStack("Docker", "Containerisation and deployment");
        ensureStack("Machine Learning", "AI, ML models and data science");
        ensureStack("Mobile", "iOS and Android app development");
        ensureStack("Database", "SQL, NoSQL and data engineering");
        ensureStack("Testing", "Automated testing and QA engineering");

        seedStudent("student@skillsync.com", "Test Student");
        seedStudent("grace.student@skillsync.com", "Grace Lee");
        seedStudent("henry.student@skillsync.com", "Henry Wilson");
        seedStudent("julia.student@skillsync.com", "Julia Clark");

        var mentorRef = seedMentor("mentor@skillsync.com", "Test Mentor", javaStack,
            "Senior Distributed Systems Engineer",
            "Expert in high-performance distributed architecture, Go, and Java systems.",
            true, 4.9, "150.00");
        var mentorReact = seedMentor("mentor.react@skillsync.com", "React Mentor", reactStack,
            "Staff Frontend Engineer",
            "Specializes in React, TypeScript, and design systems.",
            true, 4.7, "120.00");
        var mentorGo = seedMentor("mentor.go@skillsync.com", "Go Mentor", goStack,
            "Platform Engineer",
            "Builds resilient microservices and observability pipelines.",
            false, 4.5, "135.00");
        var mentorPython = seedMentor("mentor.python@skillsync.com", "Python Mentor", pythonStack,
            "Senior Data Engineer",
            "Data pipelines, ML infrastructure, and Python optimization.",
            false, 4.3, "140.00");
        var mentorDevops = seedMentor("mentor.devops@skillsync.com", "DevOps Mentor", devopsStack,
            "Cloud Infrastructure Architect",
            "Kubernetes, Terraform, and GitOps workflows.",
            true, 4.8, "160.00");
        var mentorRust = seedMentor("mentor.rust@skillsync.com", "Rust Mentor", rustStack,
            "Systems Performance Engineer",
            "Zero-cost abstractions, concurrent systems, and embedded programming.",
            true, 4.9, "180.00");

        var student1 = domainStudentProfileRepository.findByUser_Email("student@skillsync.com").orElse(null);
        var grace = domainStudentProfileRepository.findByUser_Email("grace.student@skillsync.com").orElse(null);
        var henry = domainStudentProfileRepository.findByUser_Email("henry.student@skillsync.com").orElse(null);
        var julia = domainStudentProfileRepository.findByUser_Email("julia.student@skillsync.com").orElse(null);

        if (student1 == null) {
            log.warn("Seeded student not found in domain — skipping session seeds");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        seedSessionWithAudit(mentorRef, student1, now.minusDays(3), "System design review for distributed cache",
            SessionStatus.COMPLETED, "Excellent system design understanding. Suggested Redis Cluster with consistent hashing.",
            "SYSTEM_DESIGN", 0.92, AuditStatus.SUCCESS, 145L);
        seedSessionWithAudit(mentorReact, student1, now.plusDays(1), "React performance optimization — virtual scrolling",
            SessionStatus.SCHEDULED, null, "FRONTEND_REVIEW", 0.88, AuditStatus.SUCCESS, 98L);
        seedSessionWithAudit(mentorRef, student1, now.minusDays(5), "Go concurrency patterns deep dive",
            SessionStatus.CANCELED, null, "CODE_REVIEW", 0.75, AuditStatus.SUCCESS, 112L);
        seedSessionWithAudit(mentorDevops, student1, now.minusDays(2), "Kubernetes cluster migration strategy",
            SessionStatus.COMPLETED, "Proposed a solid blue-green deployment strategy. Need to review rollback plan.",
            "CI_CD_PIPELINE", 0.95, AuditStatus.SUCCESS, 167L);
        seedSessionWithAudit(mentorReact, grace, now.plusDays(4), "State management patterns in large React apps",
            SessionStatus.SCHEDULED, null, "REACT_PERFORMANCE", 0.91, AuditStatus.SUCCESS, 103L);
        seedSessionWithAudit(mentorRust, henry, now.minusDays(1), "Rust ownership model and async runtime",
            SessionStatus.COMPLETED, "Great grasp of lifetimes. Recommended Tokio for async networking.",
            "SYSTEM_DESIGN", 0.89, AuditStatus.SUCCESS, 134L);
        seedSessionWithAudit(mentorGo, grace, now.plusDays(7), "Building CLI tools in Go with cobra",
            SessionStatus.SCHEDULED, null, "CODE_REVIEW", 0.82, AuditStatus.SUCCESS, 88L);
        seedSessionWithAudit(mentorPython, julia, now.minusDays(4), "Optimizing ETL pipelines with Python",
            SessionStatus.COMPLETED, "Switching from Pandas to Polars for large datasets would help.",
            "CI_CD_PIPELINE", 0.93, AuditStatus.SUCCESS, 156L);
        seedSessionWithAudit(mentorPython, student1, now.plusDays(10), "ML model deployment strategies",
            SessionStatus.SCHEDULED, null, "SYSTEM_DESIGN", 0.87, AuditStatus.SUCCESS, 121L);
        seedSessionWithAudit(mentorReact, henry, now.minusDays(6), "Accessibility audit for component library",
            SessionStatus.COMPLETED, "Fixed ARIA labels and keyboard navigation issues. Much improved.",
            "FRONTEND_REVIEW", 0.96, AuditStatus.SUCCESS, 177L);
    }

    private Stack ensureStack(String name, String description) {
        return stackRepository.findAll().stream()
            .filter(stack -> stack.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> stackRepository.saveAndFlush(new Stack(name, description)));
    }

    private void seedStudent(String email, String displayName) {
        if (domainUserRepository.existsByEmail(email)) return;
        User user = domainUserRepository.save(
            User.create(email, passwordEncoder.encode("student123"), Role.STUDENT)
        );
        domainStudentProfileRepository.save(new StudentProfile(user, displayName));
        log.info("Seeded domain student: {} ({})", email, displayName);
    }

    private MentorProfile seedMentor(
        String email,
        String displayName,
        Stack stack,
        String title,
        String bio,
        boolean available,
        double rating,
        String hourlyRate
    ) {
        if (domainUserRepository.existsByEmail(email)) {
            return domainMentorProfileRepository.findByUser_Email(email).orElse(null);
        }
        User user = domainUserRepository.save(
            User.create(email, passwordEncoder.encode("mentor123"), Role.MENTOR)
        );
        MentorProfile profile = new MentorProfile(
            user, stack, displayName, title, bio, available, rating, new BigDecimal(hourlyRate)
        );
        domainMentorProfileRepository.save(profile);
        log.info("Seeded domain mentor: {} ({})", email, displayName);
        return profile;
    }

    private void seedSessionWithAudit(
        MentorProfile mentor,
        StudentProfile student,
        LocalDateTime startTime,
        String description,
        SessionStatus status,
        String evaluationNotes,
        String predictedTag,
        double confidenceScore,
        AuditStatus auditStatus,
        long latencyMs
    ) {
        ReviewSession session = new ReviewSession(mentor, student, startTime, description);
        if (status == SessionStatus.COMPLETED) {
            session.complete(evaluationNotes);
        } else if (status == SessionStatus.CANCELED) {
            session.cancel();
        }
        reviewSessionRepository.save(session);

        SessionAuditLog audit = new SessionAuditLog(
            session, predictedTag, confidenceScore, auditStatus, null, latencyMs
        );
        sessionAuditLogRepository.save(audit);
        log.info("Seeded session #{} (mentor={}, status={})", session.getId(),
            mentor.getDisplayName(), status);
    }
}
