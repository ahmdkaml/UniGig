/**
 * ============================================================================
 * STRICT CONTRACT: FRONTEND MODELS <-> BACKEND DTOs
 * ============================================================================
 * Every interface and type in this file MUST strictly mirror the corresponding
 * Java DTO record / enum in `unigig-backend` (`com.unigig.backend.dto` &
 * `com.unigig.backend.entity.UserRole`).
 *
 * - Field names (camelCase), nullability, and enum string literals MUST match
 *   1-to-1 with the serialized JSON contract of the backend DTOs.
 * - Do NOT invent frontend-only fields, rename properties, or alter types here
 *   without updating the corresponding backend DTO (and vice versa) in the
 *   same PR or coordinated ticket.
 * ============================================================================
 */

export type UserRole = 'FREELANCER' | 'CLIENT';

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}
