package com.pentastack.skillsync.availability;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pentastack.skillsync.domain.MentorAvailability;
import com.pentastack.skillsync.domain.MentorProfile;
import com.pentastack.skillsync.domain.repository.MentorAvailabilityRepository;
import com.pentastack.skillsync.domain.repository.MentorProfileRepository;
import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.model.User;
import com.pentastack.skillsync.model.repository.UserRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvailabilityWindowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired @Qualifier("modelUserRepository")
    UserRepository userRepo;

    @Autowired MentorProfileRepository mentorRepo;
    @Autowired MentorAvailabilityRepository windowRepo;

    private MentorProfile mentorA;
    private MentorProfile mentorB;

    @BeforeEach
    void setUp() {
        windowRepo.deleteAll();
        mentorRepo.deleteAll();
        userRepo.deleteAll();

        User userA = userRepo.save(User.builder()
            .email("mentorA@test.dev").passwordHash("hash").role(Role.MENTOR).build());
        mentorA = mentorRepo.save(new MentorProfile(userA, null, "Mentor A", "Engineer", "Bio", true, 4.5, BigDecimal.valueOf(50)));

        User userB = userRepo.save(User.builder()
            .email("mentorB@test.dev").passwordHash("hash").role(Role.MENTOR).build());
        mentorB = mentorRepo.save(new MentorProfile(userB, null, "Mentor B", "Engineer", "Bio", true, 4.0, BigDecimal.valueOf(40)));
    }

    @Test
    void nonOwnerMentorCannotWriteAnotherMentorsWindow() throws Exception {
        mockMvc.perform(post("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorB@test.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dayOfWeek", "MONDAY", "startTime", "09:00", "endTime", "13:00"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void createWindowWithEndBeforeStartIsRejected() throws Exception {
        mockMvc.perform(post("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorA@test.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dayOfWeek", "MONDAY", "startTime", "13:00", "endTime", "09:00"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("endTime must be after startTime"));
    }

    @Test
    void createWindowShorterThan45MinutesIsRejected() throws Exception {
        mockMvc.perform(post("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorA@test.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dayOfWeek", "MONDAY", "startTime", "09:00", "endTime", "09:30"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Window must be at least 45 minutes"));
    }

    @Test
    void overlappingWindowOnSameDayIsRejected() throws Exception {
        windowRepo.save(new MentorAvailability(mentorA, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0)));

        mockMvc.perform(post("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorA@test.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dayOfWeek", "MONDAY", "startTime", "11:00", "endTime", "15:00"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Window overlaps with an existing window on that day"));
    }

    @Test
    void ownerCanCreateAndListAndDeleteWindow() throws Exception {
        mockMvc.perform(post("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorA@test.dev").roles("MENTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "dayOfWeek", "TUESDAY", "startTime", "10:00", "endTime", "14:00"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
            .andExpect(jsonPath("$.startTime").value("10:00:00"))
            .andExpect(jsonPath("$.endTime").value("14:00:00"));

        mockMvc.perform(get("/api/mentors/{id}/availability/windows", mentorA.getId())
                .with(user("mentorA@test.dev").roles("MENTOR")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}
