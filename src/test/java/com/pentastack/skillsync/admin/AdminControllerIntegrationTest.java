package com.pentastack.skillsync.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pentastack.skillsync.domain.Stack;
import com.pentastack.skillsync.domain.repository.StackRepository;
import com.pentastack.skillsync.model.MentorProfile;
import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.model.StudentProfile;
import com.pentastack.skillsync.model.User;
import com.pentastack.skillsync.model.repository.MentorProfileRepository;
import com.pentastack.skillsync.model.repository.StudentProfileRepository;
import com.pentastack.skillsync.model.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private MentorProfileRepository mentorProfileRepository;

    @Autowired
    private StackRepository stackRepository;

    private User adminUser;
    private User studentUser;
    private User mentorUser;

    @BeforeEach
    void setUp() {
        mentorProfileRepository.deleteAll();
        studentProfileRepository.deleteAll();
        stackRepository.deleteAll();
        userRepository.deleteAll();

        Stack stack = stackRepository.save(new Stack("Java", "Backend development"));

        adminUser = userRepository.save(User.builder()
            .email("admin@skillsync.dev")
            .passwordHash("hash")
            .role(Role.ADMIN)
            .build());

        studentUser = userRepository.save(User.builder()
            .email("student@skillsync.dev")
            .passwordHash("hash")
            .role(Role.STUDENT)
            .build());
        studentProfileRepository.save(StudentProfile.builder()
            .name("Sam Student")
            .user(studentUser)
            .build());

        mentorUser = userRepository.save(User.builder()
            .email("mentor@skillsync.dev")
            .passwordHash("hash")
            .role(Role.MENTOR)
            .build());
        mentorProfileRepository.save(MentorProfile.builder()
            .name("Mona Mentor")
            .user(mentorUser)
            .stack(stack)
            .title("Senior Java Engineer")
            .bio("Mentoring Java teams")
            .hourlyRate(BigDecimal.valueOf(120))
            .isVerified(true)
            .available(true)
            .build());
    }

    @Test
    void nonAdminCannotListPlatformUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("student@skillsync.dev").roles("STUDENT")))
            .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotUpdateUserStatus() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/status", studentUser.getId())
                .with(user("student@skillsync.dev").roles("STUDENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListPlatformUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("admin@skillsync.dev").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[?(@.email == 'admin@skillsync.dev')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'student@skillsync.dev')]").exists())
            .andExpect(jsonPath("$[?(@.email == 'mentor@skillsync.dev')]").exists());
    }

    @Test
    void adminCanBlockUserAndStatusPersists() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/status", studentUser.getId())
                .with(user("admin@skillsync.dev").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "BLOCKED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(studentUser.getId()))
            .andExpect(jsonPath("$.isBlocked").value(true))
            .andExpect(jsonPath("$.status").value("BLOCKED"));

        User reloaded = userRepository.findById(studentUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.isBlocked()).isTrue();
    }
}
