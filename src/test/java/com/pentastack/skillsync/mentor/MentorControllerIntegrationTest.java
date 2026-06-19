package com.pentastack.skillsync.mentor;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import java.util.Map;

import com.pentastack.skillsync.model.MentorProfile;
import com.pentastack.skillsync.model.Role;
import com.pentastack.skillsync.domain.Stack;
import com.pentastack.skillsync.model.User;
import com.pentastack.skillsync.model.repository.MentorProfileRepository;
import com.pentastack.skillsync.domain.repository.StackRepository;
import com.pentastack.skillsync.model.repository.StudentProfileRepository;
import com.pentastack.skillsync.model.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MentorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StackRepository stackRepository;

    @Autowired
    private MentorProfileRepository mentorProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    private MentorProfile javaMentor;
    private MentorProfile reactMentor;
    private Stack javaStack;
    private Stack reactStack;

    @BeforeEach
    void setUp() {
        mentorProfileRepository.deleteAll();
        studentProfileRepository.deleteAll();
        stackRepository.deleteAll();
        userRepository.deleteAll();

        javaStack = stackRepository.save(new Stack("Java", "JVM development"));
        reactStack = stackRepository.save(new Stack("React", "Frontend engineering"));

        javaMentor = mentorProfileRepository.save(MentorProfile.builder()
            .user(userRepository.save(User.builder().email("java.mentor@skillsync.dev").passwordHash("hash").role(Role.MENTOR).build()))
            .stack(javaStack)
            .name("Java Mentor")
            .title("Senior Java Engineer")
            .bio("Enterprise JVM mentoring")
            .available(true)
            .averageRating(4.9)
            .hourlyRate(BigDecimal.valueOf(150))
            .build());

        reactMentor = mentorProfileRepository.save(MentorProfile.builder()
            .user(userRepository.save(User.builder().email("react.mentor@skillsync.dev").passwordHash("hash").role(Role.MENTOR).build()))
            .stack(reactStack)
            .name("React Mentor")
            .title("Staff Frontend Engineer")
            .bio("React and TypeScript mentoring")
            .available(false)
            .averageRating(4.5)
            .hourlyRate(BigDecimal.valueOf(120))
            .build());
    }

    @Test
    void listMentorsReturnsPaginatedEnvelopeWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/mentors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.items[0].name").exists())
            .andExpect(jsonPath("$.items[0].hourlyRate").exists());
    }

    @Test
    void listMentorsSupportsKeywordStackAndSortFilters() throws Exception {
        mockMvc.perform(get("/api/mentors")
                .param("keyword", "react")
                .param("stack", String.valueOf(reactStack.getId()))
                .param("sort_by", "price"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].name").value("React Mentor"));
    }

    @Test
    void listMentorsSupportsMultiStackFilter() throws Exception {
        mockMvc.perform(get("/api/mentors")
                .param("stack", javaStack.getId() + "," + reactStack.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void getMentorDetailReturnsProfileFields() throws Exception {
        mockMvc.perform(get("/api/mentors/{id}", javaMentor.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(javaMentor.getId()))
            .andExpect(jsonPath("$.title").value("Senior Java Engineer"))
            .andExpect(jsonPath("$.rating").value(4.9))
            .andExpect(jsonPath("$.hourlyRate").value(150))
            .andExpect(jsonPath("$.stacks", hasSize(1)))
            .andExpect(jsonPath("$.stacks[0].name").value("Java"));
    }

    @Test
    void getMentorDetailReturnsNotFoundForMissingId() throws Exception {
        mockMvc.perform(get("/api/mentors/{id}", 99999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Mentor not found"));
    }

    @Test
    void mentorRegistrationSucceedsWithDefaultStack() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "New Mentor",
                    "email", "new.mentor@skillsync.dev",
                    "password", "password123",
                    "role", "MENTOR",
                    "title", "Expert Backend Developer",
                    "hourlyRate", 100,
                    "bio", "Test bio for registration"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.email").value("new.mentor@skillsync.dev"));
    }
}
