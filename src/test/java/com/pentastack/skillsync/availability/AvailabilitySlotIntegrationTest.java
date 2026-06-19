package com.pentastack.skillsync.availability;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pentastack.skillsync.domain.MentorAvailability;
import com.pentastack.skillsync.domain.MentorProfile;
import com.pentastack.skillsync.domain.ReviewSession;
import com.pentastack.skillsync.domain.Role;
import com.pentastack.skillsync.domain.SessionStatus;
import com.pentastack.skillsync.domain.StudentProfile;
import com.pentastack.skillsync.domain.User;
import com.pentastack.skillsync.domain.repository.MentorAvailabilityRepository;
import com.pentastack.skillsync.domain.repository.MentorProfileRepository;
import com.pentastack.skillsync.domain.repository.ReviewSessionRepository;
import com.pentastack.skillsync.domain.repository.StudentProfileRepository;
import com.pentastack.skillsync.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvailabilitySlotIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired @Qualifier("modelUserRepository")
    com.pentastack.skillsync.model.repository.UserRepository modelUserRepo;

    @Autowired UserRepository domainUserRepo;
    @Autowired MentorProfileRepository mentorRepo;
    @Autowired MentorAvailabilityRepository windowRepo;
    @Autowired ReviewSessionRepository sessionRepo;
    @Autowired StudentProfileRepository studentRepo;

    private MentorProfile mentor;

    @BeforeEach
    void setUp() {
        sessionRepo.deleteAll();
        windowRepo.deleteAll();
        studentRepo.deleteAll();
        mentorRepo.deleteAll();
        domainUserRepo.deleteAll();
        modelUserRepo.deleteAll();

        com.pentastack.skillsync.model.User modelUser = modelUserRepo.save(
            com.pentastack.skillsync.model.User.builder()
                .email("slotmentor@test.dev").passwordHash("hash")
                .role(com.pentastack.skillsync.model.Role.MENTOR).build());
        mentor = mentorRepo.save(new MentorProfile(modelUser, null, "Slot Mentor", "Engineer", "Bio", true, 4.5, BigDecimal.valueOf(50)));
    }

    @Test
    void noWindowForRequestedWeekday_returnsEmptySlots() throws Exception {
        windowRepo.save(new MentorAvailability(mentor, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

        // 2026-06-22 is a Monday — no window on Monday → empty
        mockMvc.perform(get("/api/mentors/{id}/availability", mentor.getId())
                .param("date", "2026-06-22")
                .with(user("slotmentor@test.dev").roles("MENTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-06-22"))
            .andExpect(jsonPath("$.slots").isEmpty());
    }

    @Test
    void windowWithNoBookings_returnsFiveSlots() throws Exception {
        // Monday 09:00–13:00 → 5 full 45-min blocks: 09:00,09:45,10:30,11:15,12:00 (12:45 end, leftover 12:45–13:00 dropped)
        windowRepo.save(new MentorAvailability(mentor, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

        mockMvc.perform(get("/api/mentors/{id}/availability", mentor.getId())
                .param("date", "2026-06-22")
                .with(user("slotmentor@test.dev").roles("MENTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.slots.length()").value(5))
            .andExpect(jsonPath("$.slots[0].startTime").value("2026-06-22T09:00:00"))
            .andExpect(jsonPath("$.slots[4].startTime").value("2026-06-22T12:00:00"))
            .andExpect(jsonPath("$.slots[4].endTime").value("2026-06-22T12:45:00"))
            .andExpect(jsonPath("$.slots[0].isBooked").value(false));
    }

    @Test
    void bookedSessionExcludesOverlappingSlot() throws Exception {
        windowRepo.save(new MentorAvailability(mentor, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

        // Save a SCHEDULED session at 10:30 directly (bypasses booking flow, tests slot-filter logic only)
        User studentUser = domainUserRepo.save(User.create("student@test.dev", "hash", Role.STUDENT));
        StudentProfile student = studentRepo.save(new StudentProfile(studentUser, "Test Student"));
        ReviewSession session = sessionRepo.save(new ReviewSession(mentor, student, LocalDateTime.of(2026, 6, 22, 10, 30), "direct"));
        // Ensure status is SCHEDULED (default from constructor)
        assert session.getStatus() == SessionStatus.SCHEDULED;

        // 10:30 slot blocked → 4 remaining slots
        mockMvc.perform(get("/api/mentors/{id}/availability", mentor.getId())
                .param("date", "2026-06-22")
                .with(user("slotmentor@test.dev").roles("MENTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.slots.length()").value(4));
    }
}
