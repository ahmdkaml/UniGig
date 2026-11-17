package com.unigig.backend.controller;

import com.unigig.backend.entity.FreelancerProfile;
import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.FreelancerRepository;
import com.unigig.backend.repository.UserRepository;
import com.unigig.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User createAndSaveUser(String email, UserRole role) {
        User user = User.builder()
                .fullName("Ahmed Kamal")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    @Test
    void upsertMyProfile_validFreelancerRequest_returns200AndCreatesProfile() throws Exception {
        User freelancer = createAndSaveUser("ahmed@alexu.edu.eg", UserRole.FREELANCER);
        String token = jwtService.generateToken(freelancer);

        String requestBody = """
                {
                  "title": "Backend Java & Spring Boot Developer",
                  "bio": "Building scalable REST APIs and microservices.",
                  "skills": ["Java", "Spring Boot", "PostgreSQL"],
                  "portfolioUrl": "https://github.com/ahmdkaml"
                }
                """;

        mockMvc.perform(put("/api/profiles/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(freelancer.getId()))
                .andExpect(jsonPath("$.fullName").value("Ahmed Kamal"))
                .andExpect(jsonPath("$.title").value("Backend Java & Spring Boot Developer"))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.portfolioUrl").value("https://github.com/ahmdkaml"));
    }

    @Test
    void upsertMyProfile_clientRole_returns403Forbidden() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        String token = jwtService.generateToken(client);

        String requestBody = """
                {
                  "title": "Full Stack Developer",
                  "bio": "Trying to make a freelancer profile as a client.",
                  "skills": ["Angular"],
                  "portfolioUrl": "https://example.com"
                }
                """;

        mockMvc.perform(put("/api/profiles/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void upsertMyProfile_invalidPayload_returns400BadRequest() throws Exception {
        User freelancer = createAndSaveUser("ahmed@alexu.edu.eg", UserRole.FREELANCER);
        String token = jwtService.generateToken(freelancer);

        String invalidBody = """
                {
                  "title": "",
                  "bio": "Missing title and invalid URL",
                  "skills": ["Java"],
                  "portfolioUrl": "not-a-valid-url"
                }
                """;

        mockMvc.perform(put("/api/profiles/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyProfile_existingProfile_returns200() throws Exception {
        User freelancer = createAndSaveUser("ahmed@alexu.edu.eg", UserRole.FREELANCER);
        String token = jwtService.generateToken(freelancer);

        profileRepository.save(FreelancerProfile.builder()
                .user(freelancer)
                .title("Backend Developer")
                .bio("Java & Spring Boot")
                .skills(List.of("Java", "PostgreSQL"))
                .portfolioUrl("https://github.com/ahmdkaml")
                .build());

        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(freelancer.getId()))
                .andExpect(jsonPath("$.title").value("Backend Developer"));
    }

    @Test
    void getMyProfile_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getProfileById_existingProfile_returns200() throws Exception {
        User freelancer = createAndSaveUser("ahmed@alexu.edu.eg", UserRole.FREELANCER);
        User viewer = createAndSaveUser("viewer@alexu.edu.eg", UserRole.CLIENT);
        String viewerToken = jwtService.generateToken(viewer);

        profileRepository.save(FreelancerProfile.builder()
                .user(freelancer)
                .title("Backend Developer")
                .bio("Java & Spring Boot")
                .skills(List.of("Java"))
                .portfolioUrl("https://github.com/ahmdkaml")
                .build());

        mockMvc.perform(get("/api/profiles/" + freelancer.getId())
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(freelancer.getId()))
                .andExpect(jsonPath("$.fullName").value("Ahmed Kamal"));
    }

    @Test
    void getProfileById_missingProfile_returns404NotFound() throws Exception {
        User viewer = createAndSaveUser("viewer@alexu.edu.eg", UserRole.CLIENT);
        String viewerToken = jwtService.generateToken(viewer);

        mockMvc.perform(get("/api/profiles/999999")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isNotFound());
    }
}