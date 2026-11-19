package com.unigig.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

/**
 * ============================================================================
 * STRICT CONTRACT: BACKEND DTOs <-> FRONTEND MODELS
 * ============================================================================
 * Every Java DTO record and enum in `com.unigig.backend.dto` MUST strictly
 * mirror its corresponding TypeScript interface/type in
 * `unigig-frontend/src/app/core/models/*.models.ts`.
 *
 * - Field names (camelCase), nullability, and enum string constants MUST
 *   match 1-to-1 with the frontend models.
 * - Do NOT rename, add, or remove fields here without updating the matching
 *   TypeScript model in `core/models/` before merging into `dev`.
 * ============================================================================
 */

public record FreelancerProfileRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must not exceed 120 characters")
        String title,

        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        String bio,

        @NotNull(message = "Skills list is required")
        @Size(max = 25, message = "You can list up to 25 skills")
        List<@NotBlank(message = "Skill cannot be blank") @Size(max = 50) String> skills,

        @Size(max = 255, message = "Portfolio URL must not exceed 255 characters")
        @URL(message = "Portfolio URL must be a valid URL")
        String portfolioUrl

) {
}
