package com.unigig.backend.dto;

import com.unigig.backend.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotNull(message = "Role is required")
        UserRole role
) {}
