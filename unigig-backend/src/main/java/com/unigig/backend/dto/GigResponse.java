package com.unigig.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;

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

public record GigResponse(
        Long id,
        String title,
        String description,
        GigStatus status,
        Long categoryId,
        String categoryName,
        Long clientId,
        String clientName,
        Instant createdAt
) {
    public static GigResponse from(Gig gig) {
        return new GigResponse(
                gig.getId(),
                gig.getTitle(),
                gig.getDescription(),
                gig.getStatus(),
                gig.getCategory().getId(),
                gig.getCategory().getName(),
                gig.getClient().getId(),
                gig.getClient().getFullName(),
                gig.getCreatedAt()
        );
    }
}
