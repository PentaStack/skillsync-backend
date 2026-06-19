package com.pentastack.skillsync.seeder;

import com.pentastack.skillsync.domain.MentorProfile;
import com.pentastack.skillsync.domain.repository.MentorProfileRepository;
import com.pentastack.skillsync.model.*;
import com.pentastack.skillsync.model.repository.*;
import com.pentastack.skillsync.domain.Stack;
import com.pentastack.skillsync.domain.repository.StackRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    @org.springframework.beans.factory.annotation.Qualifier("modelUserRepository")
    private final UserRepository userRepository;
    @org.springframework.beans.factory.annotation.Qualifier("modelStudentProfileRepository")
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final StackRepository stackRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedStudent();
        seedMentor();

        Map<String, Long> stackIds = stackRepository.findAll().stream()
            .collect(Collectors.toMap(s -> s.getName().toLowerCase(), Stack::getId));

        seedUnverifiedMentors(stackIds);
        seedExtraVerifiedMentors(stackIds);
        seedExtraStudents();
    }

    private void seedAdmin() {
        String email = "admin@skillsync.com";
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Seeded Admin user: {}", email);
        } else {
            log.info("Admin user already exists: {}", email);
        }
    }

    private void seedStudent() {
        String email = "student@skillsync.com";
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .build();
            User savedUser = userRepository.save(user);

            StudentProfile studentProfile = StudentProfile.builder()
                    .name("Test Student")
                    .user(savedUser)
                    .build();
            studentProfileRepository.save(studentProfile);
            savedUser.setStudentProfile(studentProfile);
            log.info("Seeded Student user and profile: {}", email);
        } else {
            log.info("Student user already exists: {}", email);
        }
    }

    private void seedMentor() {
        String email = "mentor@skillsync.com";
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("mentor123"))
                    .role(Role.MENTOR)
                    .build();
            User savedUser = userRepository.save(user);

            MentorProfile mentorProfile = new MentorProfile(
                    savedUser, null,
                    "Test Mentor",
                    "Senior Distributed Systems Engineer",
                    "Expert in high-performance distributed architecture, Go, and Java systems.",
                    true, 4.9,
                    new BigDecimal("150.00"));
            mentorProfile.setVerified(true);
            mentorProfileRepository.save(mentorProfile);
            savedUser.setMentorProfile(mentorProfile);
            log.info("Seeded Mentor user and profile: {}", email);
        } else {
            log.info("Mentor user already exists: {}", email);
        }
    }

    private void seedUnverifiedMentors(Map<String, Long> stackIds) {
        Object[][] unverified = {
            {"alice.unverified@test.com", "Alice Johnson",  "Junior Java Developer",       "Building REST APIs with Spring Boot.",                  new BigDecimal("80.00"),  "java"},
            {"bob.unverified@test.com",   "Bob Smith",      "React Frontend Engineer",     "Creating component libraries with React and TypeScript.", new BigDecimal("90.00"),  "react"},
            {"carol.unverified@test.com", "Carol Williams", "Go Backend Developer",        "Writing microservices and CLI tools in Go.",             new BigDecimal("85.00"),  "go"},
            {"dave.unverified@test.com",  "Dave Brown",     "Data Engineer",               "ETL pipelines, Python, and data warehousing.",           new BigDecimal("95.00"),  "python"},
            {"iris.unverified@test.com",  "Iris Martinez",  "DevOps Engineer",             "Kubernetes, Terraform, and CI/CD automation.",           new BigDecimal("110.00"), "devops"},
        };
        for (Object[] row : unverified) {
            String email = (String) row[0];
            if (userRepository.existsByEmail(email)) continue;
            String stackKey = (String) row[5];
            Long sid = stackIds.getOrDefault(stackKey, null);

            User user = User.builder().email(email).passwordHash(passwordEncoder.encode("password")).role(Role.MENTOR).build();
            userRepository.save(user);
            mentorProfileRepository.save(MentorProfile.builder()
                .name((String) row[1]).user(user).title((String) row[2]).bio((String) row[3])
                .hourlyRate((BigDecimal) row[4]).isVerified(false).averageRating(0.0).stackId(sid).build());
            log.info("Seeded unverified mentor: {}", email);
        }
    }

    private void seedExtraVerifiedMentors(Map<String, Long> stackIds) {
        Object[][] verified = {
            {"eve.verified@test.com",   "Eve Davis",      "Senior DevOps Architect",     "Cloud infrastructure and observability at scale.",      new BigDecimal("160.00"), 4.2, "devops"},
            {"frank.verified@test.com", "Frank Miller",    "Rust Systems Engineer",       "High-performance systems programming in Rust.",         new BigDecimal("175.00"), 4.8, "rust"},
        };
        for (Object[] row : verified) {
            String email = (String) row[0];
            if (userRepository.existsByEmail(email)) continue;
            String stackKey = (String) row[6];
            Long sid = stackIds.getOrDefault(stackKey, null);

            User user = User.builder().email(email).passwordHash(passwordEncoder.encode("password")).role(Role.MENTOR).build();
            userRepository.save(user);
            mentorProfileRepository.save(MentorProfile.builder()
                .name((String) row[1]).user(user).title((String) row[2]).bio((String) row[3])
                .hourlyRate((BigDecimal) row[4]).isVerified(true).averageRating((Double) row[5]).stackId(sid).build());
            log.info("Seeded verified mentor: {}", email);
        }
    }

    private void seedExtraStudents() {
        String[][] students = {
            {"grace.student@test.com",  "Grace Lee"},
            {"henry.student@test.com",  "Henry Wilson"},
            {"julia.student@test.com",  "Julia Clark"},
        };
        for (String[] row : students) {
            String email = row[0];
            if (userRepository.existsByEmail(email)) continue;
            User user = User.builder().email(email).passwordHash(passwordEncoder.encode("student123")).role(Role.STUDENT).build();
            userRepository.save(user);
            studentProfileRepository.save(StudentProfile.builder().name(row[1]).user(user).build());
            log.info("Seeded student: {}", email);
        }
    }
}
