package com.unigig.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
