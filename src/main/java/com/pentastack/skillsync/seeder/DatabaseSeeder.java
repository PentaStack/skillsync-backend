package com.pentastack.skillsync.seeder;

import com.pentastack.skillsync.model.*;
import com.pentastack.skillsync.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAdmin();
        seedStudent();
        seedMentor();
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

            MentorProfile mentorProfile = MentorProfile.builder()
                    .name("Test Mentor")
                    .user(savedUser)
                    .title("Senior Distributed Systems Engineer")
                    .bio("Expert in high-performance distributed architecture, Go, and Java systems.")
                    .hourlyRate(new BigDecimal("150.00"))
                    .isVerified(true)
                    .averageRating(4.9)
                    .build();
            mentorProfileRepository.save(mentorProfile);
            savedUser.setMentorProfile(mentorProfile);
            log.info("Seeded Mentor user and profile: {}", email);
        } else {
            log.info("Mentor user already exists: {}", email);
        }
    }
}
