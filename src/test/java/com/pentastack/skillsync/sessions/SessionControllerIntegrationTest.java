package com.pentastack.skillsync.sessions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pentastack.skillsync.domain.MentorAvailability;
import com.pentastack.skillsync.domain.MentorProfile;
import com.pentastack.skillsync.domain.Role;
import com.pentastack.skillsync.domain.SessionStatus;
import com.pentastack.skillsync.domain.Stack;
import com.pentastack.skillsync.domain.StudentProfile;
import com.pentastack.skillsync.domain.User;
import com.pentastack.skillsync.domain.repository.MentorAvailabilityRepository;
import com.pentastack.skillsync.domain.repository.MentorProfileRepository;
import com.pentastack.skillsync.domain.repository.ReviewSessionRepository;
import com.pentastack.skillsync.domain.repository.SessionAuditLogRepository;
import com.pentastack.skillsync.domain.repository.StackRepository;
import com.pentastack.skillsync.domain.repository.StudentProfileRepository;
import com.pentastack.skillsync.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.beans.factory.annotation.Qualifier("modelUserRepository")
    private com.pentastack.skillsync.model.repository.UserRepository modelUserRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private MentorProfileRepository mentorProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ReviewSessionRepository reviewSessionRepository;

    @Autowired
    private SessionAuditLogRepository sessionAuditLogRepository;

    @Autowired
    private MentorAvailabilityRepository mentorAvailabilityRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private FakeSessionAuditClassifier auditClassifier;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public FakeSessionAuditClassifier auditClassifier() {
            return new FakeSessionAuditClassifier();
        }
    }

    static class FakeSessionAuditClassifier implements SessionAuditClassifier {
        private AuditClassificationResult nextResult = AuditClassificationResult.success("GENERAL", 0.5, 1);

        void returnNext(AuditClassificationResult nextResult) {
            this.nextResult = nextResult;
        }

        @Override
        public AuditClassificationResult classify(String submissionDescription) {
            return nextResult;
        }
    }

    private MentorProfile mentor;
    private StudentProfile student;
    private StudentProfile otherStudent;
    private LocalDateTime firstSlot;

    @BeforeEach
    void setUp() {
        sessionAuditLogRepository.deleteAll();
        reviewSessionRepository.deleteAll();
        mentorAvailabilityRepository.deleteAll();
        mentorProfileRepository.deleteAll();
        modelUserRepository.deleteAll();
        studentProfileRepository.deleteAll();
        stackRepository.deleteAll();
        userRepository.deleteAll();

        Stack stack = stackRepository.save(new Stack("React Engineering", "Frontend competency reviews"));

        // model.User required by MentorProfile FK; domain.User required by SessionService role check
        com.pentastack.skillsync.model.User modelMentorUser = modelUserRepository.save(
            com.pentastack.skillsync.model.User.builder()
                .email("mentor@skillsync.dev").passwordHash("hash")
                .role(com.pentastack.skillsync.model.Role.MENTOR).build()
        );
        userRepository.save(User.create("mentor@skillsync.dev", "hash", Role.MENTOR));
        mentor = mentorProfileRepository.save(
            new MentorProfile(modelMentorUser, stack, "Mona Mentor", "Senior React Mentor", "Helps students debug UI systems", true, 4.8, BigDecimal.valueOf(60))
        );
        // firstSlot is 2026-07-06 (Monday); cover all test booking times (10:00–14:45)
        mentorAvailabilityRepository.save(new MentorAvailability(mentor, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));

        User studentUser = userRepository.save(
            User.create("student@skillsync.dev", "hash", Role.STUDENT)
        );
        student = studentProfileRepository.save(new StudentProfile(studentUser, "Sam Student"));

        User otherStudentUser = userRepository.save(
            User.create("other@skillsync.dev", "hash", Role.STUDENT)
        );
        otherStudent = studentProfileRepository.save(new StudentProfile(otherStudentUser, "Olive Other"));

        firstSlot = LocalDateTime.of(2026, 7, 6, 10, 0);
    }

    @Test
    void studentBooksSessionAndReceivesSuccessfulAuditLog() throws Exception {
        AuditClassificationResult auditResult = AuditClassificationResult.success("ASYNC_RACE", 0.91, 37);
        auditClassifier.returnNext(auditResult);

        mockMvc.perform(post("/api/sessions")
                .with(user("student@skillsync.dev").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "mentorId", mentor.getId(),
                    "startTime", firstSlot.toString(),
                    "description", "Reviewing an asynchronous race condition in my Node engine"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.mentorName").value("Mona Mentor"))
            .andExpect(jsonPath("$.studentName").value("Sam Student"))
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
            .andExpect(jsonPath("$.audit.status").value("SUCCESS"))
            .andExpect(jsonPath("$.audit.predictedTag").value("ASYNC_RACE"))
            .andExpect(jsonPath("$.audit.confidenceScore").value(0.91))
            .andExpect(jsonPath("$.audit.latencyMs").value(37));

        mockMvc.perform(get("/api/sessions")
                .with(user("student@skillsync.dev").roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].audit.status").value("SUCCESS"));
    }

    @Test
    void overlappingBookingForSameMentorIsRejected() throws Exception {
        auditClassifier.returnNext(AuditClassificationResult.success("REACT_STATE", 0.82, 21));

        bookAsStudent("student@skillsync.dev", firstSlot, "First review");

        mockMvc.perform(post("/api/sessions")
                .with(user("other@skillsync.dev").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "mentorId", mentor.getId(),
                    "startTime", firstSlot.plusMinutes(15).toString(),
                    "description", "Overlapping review"
                ))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Mentor is already booked for that time window"));
    }

    @Test
    void classifierFailureIsStoredAsFailedAuditTelemetry() throws Exception {
        auditClassifier.returnNext(AuditClassificationResult.failed("classifier unavailable", 112));

        long sessionId = bookAsStudent("student@skillsync.dev", firstSlot, "Please inspect this flaky test");

        mockMvc.perform(get("/api/sessions/{id}/audit-log", sessionId)
                .with(user("student@skillsync.dev").roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FAILED"))
            .andExpect(jsonPath("$.errorMessage").value("classifier unavailable"))
            .andExpect(jsonPath("$.latencyMs").value(112));
    }

    @Test
    void studentOnlySeesOwnSessionsAndCannotReadAnotherAuditLog() throws Exception {
        auditClassifier.returnNext(AuditClassificationResult.success("SYSTEM_DESIGN", 0.77, 19));

        long studentSessionId = bookAsStudent("student@skillsync.dev", firstSlot, "Student review");
        long otherSessionId = bookAsStudent("other@skillsync.dev", firstSlot.plusHours(2), "Other review");

        mockMvc.perform(get("/api/sessions")
                .with(user("student@skillsync.dev").roles("STUDENT")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(studentSessionId));

        mockMvc.perform(get("/api/sessions/{id}/audit-log", otherSessionId)
                .with(user("student@skillsync.dev").roles("STUDENT")))
            .andExpect(status().isForbidden());
    }

    @Test
    void mentorCompletesOwnSessionWithEvaluationNotes() throws Exception {
        auditClassifier.returnNext(AuditClassificationResult.success("CODE_REVIEW", 0.86, 24));

        long sessionId = bookAsStudent("student@skillsync.dev", firstSlot, "Review my PR");

        mockMvc.perform(put("/api/sessions/{id}", sessionId)
                .with(user("mentor@skillsync.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", SessionStatus.COMPLETED.name(),
                    "evaluationNotes", "Strong debugging notes; practice naming state transitions."
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.evaluationNotes").value("Strong debugging notes; practice naming state transitions."));
    }

    @Test
    void studentCanCancelAndRescheduleOwnScheduledSession() throws Exception {
        auditClassifier.returnNext(AuditClassificationResult.success("API_DESIGN", 0.8, 28));

        long cancelId = bookAsStudent("student@skillsync.dev", firstSlot, "Cancel this review");

        mockMvc.perform(put("/api/sessions/{id}", cancelId)
                .with(user("student@skillsync.dev").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", SessionStatus.CANCELED.name()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELED"));

        long rescheduleId = bookAsStudent("student@skillsync.dev", firstSlot.plusHours(3), "Move this review");
        LocalDateTime newTime = firstSlot.plusHours(4);

        mockMvc.perform(put("/api/sessions/{id}", rescheduleId)
                .with(user("student@skillsync.dev").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "startTime", newTime.toString()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.startTime").value("2026-07-06T14:00:00"))
            .andExpect(jsonPath("$.endTime").value("2026-07-06T14:45:00"));
    }

    @Test
    void concurrentBookingsSameSlot_exactlyOneSucceeds() throws Exception {
        int N = 10;
        CountDownLatch ready = new CountDownLatch(N);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger created = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(N);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try { go.await(); } catch (InterruptedException e) { return; }
                try {
                    int status = mockMvc.perform(post("/api/sessions")
                            .with(user("student@skillsync.dev").roles("STUDENT"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                "mentorId", mentor.getId(),
                                "startTime", firstSlot.toString(),
                                "description", "race test"
                            ))))
                        .andReturn().getResponse().getStatus();
                    if (status == 201) created.incrementAndGet();
                    else if (status == 409) conflict.incrementAndGet();
                } catch (Exception ignored) {}
            }));
        }

        ready.await();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        assertEquals(1, created.get(), "exactly one booking must succeed");
        assertEquals(N - 1, conflict.get(), "all others must get 409");
        assertEquals(1, reviewSessionRepository.count(), "exactly one session row persisted");
    }

    private long bookAsStudent(String email, LocalDateTime startTime, String description) throws Exception {
        String response = mockMvc.perform(post("/api/sessions")
                .with(user(email).roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "mentorId", mentor.getId(),
                    "startTime", startTime.toString(),
                    "description", description
                ))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
