package com.unigig.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_withValidRequest_returns201Created() throws Exception {
        String requestBody = """
                {
                  "fullName": "Ahmed Kamal",
                  "email": "ahmed@alexu.edu.eg",
                  "password": "Password123!",
                  "role": "FREELANCER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Ahmed Kamal"))
                .andExpect(jsonPath("$.email").value("ahmed@alexu.edu.eg"))
                .andExpect(jsonPath("$.role").value("FREELANCER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void login_withValidCredentials_returns200AndJwtToken() throws Exception {
        User existingUser = User.builder()
                .fullName("Ahmed Kamal")
                .email("ahmed@alexu.edu.eg")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.FREELANCER)
                .build();
        userRepository.save(existingUser);

        String loginBody = """
                {
                  "email": "ahmed@alexu.edu.eg",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.email").value("ahmed@alexu.edu.eg"))
                .andExpect(jsonPath("$.role").value("FREELANCER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void login_withWrongPassword_returns401Unauthorized() throws Exception {
        User existingUser = User.builder()
                .fullName("Ahmed Kamal")
                .email("ahmed@alexu.edu.eg")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.FREELANCER)
                .build();
        userRepository.save(existingUser);

        String loginBody = """
                {
                  "email": "ahmed@alexu.edu.eg",
                  "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withUnknownEmail_returns401Unauthorized() throws Exception {
        String loginBody = """
                {
                  "email": "nobody@alexu.edu.eg",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}