# Backend Conventions (`unigig-backend`)

## 1. Package Responsibilities (`com.unigig.backend.*`)
* `config/` — Spring Security (`SecurityConfig`), CORS configuration, and `PasswordEncoder` (`BCryptPasswordEncoder`) beans.
* `controller/` — REST routing (`@RestController`, `@RequestMapping("/api/...")`), HTTP status codes, and `@Valid` request binding only. Controllers must not contain business rules.
  * `AuthController` (`/api/auth`) — `POST /register` (`201 Created`), `POST /login` (`200 OK`).
  * `ProfileController` (`/api/profiles`) — `GET /me` (`200 OK`), `PUT /me` (`200 OK`), `GET /{userId}` (`200 OK`).
  * `GigController` (`/api/gigs`) — `GET /api/gigs` (`200 OK`, paginated search with optional `keyword`, `categoryId`, `page`), `POST /api/gigs` (`201 Created`, `CLIENT` role required).
  * `CategoryController` (`/api/categories`) — `GET /api/categories` (`200 OK`, sorted alphabetically by `name`).
* `dto/` — Immutable Java `record` types for HTTP request/response payloads (`AuthResponse`, `RegisterRequest`, `LoginRequest`, `FreelancerProfileRequest`, `FreelancerProfileResponse`, `CreateGigRequest`, `GigResponse`, `CategoryResponse`, `ErrorResponse`). Never expose JPA `@Entity` classes directly in HTTP responses; use static factory methods (e.g., `GigResponse.from(gig)`, `FreelancerProfileResponse.from(profile)`, `CategoryResponse.from(category)`).
* `entity/` — JPA `@Entity` classes (`User`, `FreelancerProfile`, `Gig`, `Category`) and domain enums (`UserRole`: `FREELANCER`, `CLIENT`; `GigStatus`: `OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELED`).
* `exception/` — `GlobalExceptionHandler` (`@RestControllerAdvice`) translating validation, security, persistence, and unexpected exceptions into uniform `ErrorResponse` payloads.
* `repository/` — Spring Data JPA interfaces (`UserRepository`, `FreelancerRepository`, `GigRepository`, `CategoryRepository`). Custom JPQL filter queries belong here (e.g., `GigRepository.searchByStatusAndFilters`).
* `security/` — `JwtService` (HMAC-SHA256 token generation and validation with `userId` and `role` claims) and `JwtAuthenticationFilter` (`OncePerRequestFilter` populating `SecurityContextHolder` with `ROLE_<UserRole>`).
* `service/` — Business rules, role authorization checks (`UserRole.CLIENT` for posting gigs, `UserRole.FREELANCER` for upserting freelancer profiles), input normalization (trimming strings, lowercasing emails, deduplicating skills), and `@Transactional` / `@Transactional(readOnly = true)` boundaries.

---

## 2. Database & Flyway Migrations
* **Flyway owns all schema changes.** Never change `spring.jpa.hibernate.ddl-auto` away from `validate`.
* Add sequential SQL migrations under `src/main/resources/db/migration/` using `V<number>__<description>.sql`:
  * `V1__create_users_table.sql` — `users` table (`id`, `full_name`, `email`, `password_hash`, `role`, `created_at`).
  * `V2__create_freelancer_profiles_table.sql` — `freelancer_profiles` table (`user_id` PK/FK to `users`, `title`, `bio`, `skills TEXT[]`, `portfolio_url`, `created_at`, `updated_at`).
  * `V3__create_gigs_table.sql` — `categories` and `gigs` tables with indexes on `status`, `category_id`, and `client_id`.
  * `V4__seed_categories.sql` — Idempotent (`ON CONFLICT (name) DO NOTHING`) seeding of the 8 default campus gig categories.
* **Environment Configuration:** Datasource (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) and JWT properties (`JWT_SECRET`, `JWT_EXPIRATION_MS`) in `application.properties` must use `${ENV_VAR:default}` placeholders so local Docker, CI, and production environments work without code changes.

---

## 3. API & Error Contract
* Every error response must flow through `GlobalExceptionHandler` and return `ErrorResponse`:
  ```json
  {
    "timestamp": "2026-11-23T00:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/gigs",
    "fieldErrors": { "title": "Title is required" }
  }
  ```
* Standard exception-to-status mappings in `GlobalExceptionHandler`:
  * `MethodArgumentNotValidException` → `400 Bad Request` (with populated `fieldErrors` map)
  * `BadCredentialsException` → `401 Unauthorized`
  * `AccessDeniedException` → `403 Forbidden`
  * `EntityNotFoundException` / `NoResourceFoundException` → `404 Not Found`
  * `IllegalStateException` → `409 Conflict` (e.g., duplicate email registration)
  * `ResponseStatusException` → Explicit status code & reason from the exception
  * `Exception` (fallback) → `500 Internal Server Error` (`"An unexpected error occurred"`, never leaking stack traces)

---

## 4. Testing Standards (Rule 9)
* Follow Red-Green-Refactor and protect HTTP/database contracts.
* Use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` against a real PostgreSQL instance (matching `Backend CI`) for controller and integration tests (`AuthControllerTest`, `ProfileControllerTest`, `GigControllerTest`, `CategoryControllerTest`).
* Use `@WebMvcTest` for isolated exception-handler contract tests (`GlobalExceptionHandlerTest`).
* Every endpoint test suite must cover happy paths, validation failures (`400`), unauthenticated access (`401`), and role violations (`403`).

---

## 5. API Contract: Backend DTOs ↔ Frontend Models (Strict 1-to-1 Parity)

1. **Exact Structural Parity:** Every Java DTO `record` under `unigig-backend/src/main/java/com/unigig/backend/dto/` (and domain enums like `UserRole` and `GigStatus`) MUST strictly match its corresponding TypeScript interface/type under `unigig-frontend/src/app/core/models/*.models.ts`:
   * `ErrorResponse` ↔ `ApiError` (`api-error.model.ts`)
   * `UserRole`, `RegisterRequest`, `LoginRequest`, `AuthResponse` ↔ `auth.models.ts`
   * `FreelancerProfileRequest`, `FreelancerProfileResponse` ↔ `profile.models.ts`
   * `GigStatus`, `CategoryResponse` (`Category`), `CreateGigRequest` (`GigRequest`), `GigResponse`, `Page<T>` (`PageResponse<T>`) ↔ `gig.models.ts`
2. **Zero Silent Drift:**
   * Property names (exact `camelCase` JSON keys), value types (`Long`/`int` → `number`, `String` → `string`, `boolean` → `boolean`, `Instant` → ISO-8601 `string`), optionality/nullability, and uppercase enum constants (`FREELANCER`, `CLIENT`, `OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELED`) MUST remain identical on both sides.
   * Never rename, add, or remove a field in a backend DTO without updating the matching frontend model in the same PR.
