package com.unigig.backend.dto;

import com.unigig.backend.entity.FreelancerProfile;

import java.time.Instant;
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

public record FreelancerProfileResponse(
        Long userId,
        String fullName,
        String title,
        String bio,
        List<String> skills,
        String portfolioUrl,
        Instant updatedAt
) {
    public static FreelancerProfileResponse from(FreelancerProfile profile) {
        return new FreelancerProfileResponse(
                profile.getUserId(),
                profile.getUser().getFullName(),
                profile.getTitle(),
                profile.getBio(),
                profile.getSkills(),
                profile.getPortfolioUrl(),
                profile.getUpdatedAt()
        );
    }
}
