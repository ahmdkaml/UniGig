package com.unigig.backend.controller;

import com.unigig.backend.entity.Category;
import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;
import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.CategoryRepository;
import com.unigig.backend.repository.GigRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GigRepository gigRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    private Category createAndSaveCategory() {
        return categoryRepository.save(Category.builder().name("Test Category").build());
    }

    // Builds the request JSON. Pass null to leave a field out of the body entirely
    private String gigJson(String title, String description, Long categoryId, String budget) {
        List<String> fields = new ArrayList<>();
        if (title != null) fields.add("\"title\": \"" + title + "\"");
        if (description != null) fields.add("\"description\": \"" + description + "\"");
        if (categoryId != null) fields.add("\"categoryId\": " + categoryId);
        if (budget != null) fields.add("\"budget\": " + budget);
        return "{" + String.join(",", fields) + "}";
    }

    private void expectPostRejectedWith400(String token, String body) throws Exception {
        long before = gigRepository.count();

        mockMvc.perform(post("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(gigRepository.count()).isEqualTo(before);
    }

    // ---------- success ----------

    @Test
    void postGig_validClient_returns201AndSavesOpenGigLinkedToClient() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        String token = jwtService.generateToken(client);
        long before = gigRepository.count();

        mockMvc.perform(post("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gigJson("Build a landing page", "Need a one-page site", category.getId(), "150.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Build a landing page"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        assertThat(gigRepository.count()).isEqualTo(before + 1);
        Gig saved = gigRepository.findAll().stream()
                .filter(g -> g.getTitle().equals("Build a landing page"))
                .findFirst()
                .orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(GigStatus.OPEN);
        assertThat(saved.getClient().getId()).isEqualTo(client.getId());
        assertThat(saved.getCategory().getId()).isEqualTo(category.getId());
    }

    // ---------- missing required fields (400) ----------

    @Test
    void postGig_missingTitle_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson(null, "Need a one-page site", category.getId(), "100"));
    }

    @Test
    void postGig_missingDescription_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", null, category.getId(), "100"));
    }

    @Test
    void postGig_missingCategory_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", "Need a one-page site", null, "100"));
    }

    @Test
    void postGig_missingBudget_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", "Need a one-page site", category.getId(), null));
    }

    // ---------- budget must be greater than 0 (400) ----------

    @Test
    void postGig_budgetZero_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", "Need a one-page site", category.getId(), "0"));
    }

    @Test
    void postGig_negativeBudget_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", "Need a one-page site", category.getId(), "-25"));
    }

    // ---------- invalid category (400) ----------

    @Test
    void postGig_unknownCategory_returns400() throws Exception {
        User client = createAndSaveUser("client@alexu.edu.eg", UserRole.CLIENT);
        expectPostRejectedWith400(jwtService.generateToken(client),
                gigJson("Build a landing page", "Need a one-page site", 999999L, "100"));
    }

    // ---------- role check ----------

    @Test
    void postGig_freelancerRole_returns403AndSavesNothing() throws Exception {
        User freelancer = createAndSaveUser("freelancer@alexu.edu.eg", UserRole.FREELANCER);
        Category category = createAndSaveCategory();
        String token = jwtService.generateToken(freelancer);
        long before = gigRepository.count();

        mockMvc.perform(post("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gigJson("Build a landing page", "Need a one-page site", category.getId(), "100")))
                .andExpect(status().isForbidden());

        assertThat(gigRepository.count()).isEqualTo(before);
    }

    // ---------- authentication ----------

    @Test
    void postGig_unauthenticated_returns401() throws Exception {
        Category category = createAndSaveCategory();

        mockMvc.perform(post("/api/gigs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gigJson("Build a landing page", "Need a one-page site", category.getId(), "100")))
                .andExpect(status().isUnauthorized());
    }
}