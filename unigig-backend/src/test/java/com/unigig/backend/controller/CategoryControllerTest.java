package com.unigig.backend.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.UserRepository;
import com.unigig.backend.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String tokenFor(UserRole role) {
        User user = userRepository.save(User.builder()
                .fullName("Test User")
                .email(role.name().toLowerCase() + "@alexu.edu.eg")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(role)
                .build());
        return jwtService.generateToken(user);
    }

    @Test
    void listCategories_authenticatedUser_returnsSeededCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + tokenFor(UserRole.FREELANCER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void listCategories_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
}