# Backend Conventions (`unigig-backend`)

## 1. Package Responsibilities (`com.unigig.backend.*`)
* `config` — Spring Security, CORS, and infrastructure beans.
* `controller` — HTTP routing, status codes, and `@Valid` request binding only. No business logic or direct repository calls.
* `dto` — Immutable Java `record` types for API requests and responses. Never expose JPA `@Entity` classes directly in HTTP responses.
* `exception` — Custom domain exceptions and `GlobalExceptionHandler` mappings.
* `model` — JPA `@Entity` classes and domain enums.
* `repository` — Spring Data JPA interfaces (`JpaRepository`).
* `security` — JWT token generation, validation, and authentication filters.
* `service` — Business rules, validation checks, and `@Transactional` boundaries.

## 2. Database & Migrations
* **Flyway owns all schema changes.** Never change `spring.jpa.hibernate.ddl-auto` away from `validate`.
* Add sequential SQL migrations under `src/main/resources/db/migration/` using `V<number>__<description>.sql` (e.g., `V1__create_users_table.sql`).

## 3. API & Error Contract
* Every error response must flow through `GlobalExceptionHandler` and return `ErrorResponse`.
* Keep endpoints under `/api/...` and return explicit HTTP status codes (`201 Created`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `409 Conflict`).

## 4. Testing (Rule 9)
* Write failing tests first (Red -> Green).
* Use `@WebMvcTest` for controller/HTTP contract tests and real PostgreSQL (never H2) for `@DataJpaTest` repository tests.

## API Contract: Backend DTOs ↔ Frontend Models (Strict 1-to-1 Parity)

1. **Exact Structural Parity:** Every TypeScript interface/type under `unigig-frontend/src/app/core/models/*.models.ts` MUST strictly match its corresponding Java DTO record or enum under `unigig-backend/src/main/java/com/unigig/backend/dto/` (and domain enums like `UserRole`).
2. **Zero Silent Drift:**
   - Property names (exact `camelCase` JSON keys), value types (`Long` → `number`, `String` → `string`, `boolean` → `boolean`, `Instant`/`LocalDateTime` → ISO-8601 `string`), optionality/nullability, and enum string constants MUST remain identical on both sides.
   - Never rename, add, or remove a field in a backend DTO without updating the matching frontend model—and never add unbacked properties to a frontend DTO model.
3. **Pull Request Verification:** Any PR that modifies a backend DTO or a frontend `core/models` file MUST verify parity against its counterpart before merging into `dev`.
