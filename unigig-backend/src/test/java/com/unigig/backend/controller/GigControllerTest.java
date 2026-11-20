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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // ---------- search & filters ----------

    @Test
    void getGigs_returnsOnlyOpenGigsPaginatedByTen() throws Exception {
        User client = createAndSaveUser("client-page@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        String token = jwtService.generateToken(client);

        for (int i = 1; i <= 11; i++) {
            gigRepository.save(Gig.builder()
                    .title("Open Gig " + i)
                    .description("Description " + i)
                    .status(GigStatus.OPEN)
                    .client(client)
                    .category(category)
                    .build());
        }

        gigRepository.save(Gig.builder()
                .title("Closed Gig")
                .description("Not open")
                .status(GigStatus.IN_PROGRESS)
                .client(client)
                .category(category)
                .build());

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.content[?(@.status != 'OPEN')]").isEmpty());

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getGigs_filterByKeywordAlone_returnsMatchingOpenGigs() throws Exception {
        User client = createAndSaveUser("client-kw@alexu.edu.eg", UserRole.CLIENT);
        Category category = createAndSaveCategory();
        String token = jwtService.generateToken(client);

        gigRepository.save(Gig.builder()
                .title("Angular Dashboard")
                .description("Build frontend UI")
                .status(GigStatus.OPEN)
                .client(client)
                .category(category)
                .build());

        gigRepository.save(Gig.builder()
                .title("Database Schema")
                .description("PostgreSQL tables")
                .status(GigStatus.OPEN)
                .client(client)
                .category(category)
                .build());

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "angular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Angular Dashboard"));
    }

    @Test
    void getGigs_filterByCategoryAlone_returnsMatchingOpenGigs() throws Exception {
        User client = createAndSaveUser("client-cat@alexu.edu.eg", UserRole.CLIENT);
        Category catA = categoryRepository.save(Category.builder().name("Web").build());
        Category catB = categoryRepository.save(Category.builder().name("Mobile").build());
        String token = jwtService.generateToken(client);

        gigRepository.save(Gig.builder()
                .title("Web Gig")
                .description("Angular app")
                .status(GigStatus.OPEN)
                .client(client)
                .category(catA)
                .build());

        gigRepository.save(Gig.builder()
                .title("Mobile Gig")
                .description("Android app")
                .status(GigStatus.OPEN)
                .client(client)
                .category(catB)
                .build());

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("categoryId", catB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Mobile Gig"));
    }

    @Test
    void getGigs_filterByKeywordAndCategoryTogether_returnsExactMatchOrEmpty() throws Exception {
        User client = createAndSaveUser("client-both@alexu.edu.eg", UserRole.CLIENT);
        Category catA = categoryRepository.save(Category.builder().name("Backend").build());
        Category catB = categoryRepository.save(Category.builder().name("Design").build());
        String token = jwtService.generateToken(client);

        gigRepository.save(Gig.builder()
                .title("Spring REST API")
                .description("Java endpoint")
                .status(GigStatus.OPEN)
                .client(client)
                .category(catA)
                .build());

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "Spring")
                        .param("categoryId", catB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.empty").value(true));

        mockMvc.perform(get("/api/gigs")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "Spring")
                        .param("categoryId", catA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring REST API"));
    }
}
