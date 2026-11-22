# UniGig — University Freelance Marketplace

A campus freelance marketplace connecting university clients with student freelancers, built for **CSE322 Software Engineering (Alexandria University)**.

---

## Repository Structure

```text
unigig/
├── .github/workflows/   # Independent CI pipelines (backend-ci.yml & frontend-ci.yml)
├── unigig-backend/      # Java 25 / Spring Boot 4.1 REST API (Flyway + PostgreSQL 17)
├── unigig-frontend/     # Angular 20 Zoneless SPA + Tailwind CSS v4 (Node 22/24)
├── docker-compose.yml   # Local PostgreSQL 17 database container
├── CONTRIBUTING.md      # Team Agile & Git workflow rules
└── README.md
```

---

## Implemented Features & REST API (`dev`)

### 1. Authentication & Security (`/api/auth`)
* **Stateless JWT Security:** BCrypt password hashing (`BCryptPasswordEncoder`) and HMAC-SHA256 JWT tokens containing `sub` (email), `userId`, and `role` (`FREELANCER` or `CLIENT`).
* `POST /api/auth/register` — Creates a `FREELANCER` or `CLIENT` account and returns `201 Created` with an `AuthResponse` (`token`, `id`, `fullName`, `email`, `role`). Returns `409 Conflict` if the email is already registered.
* `POST /api/auth/login` — Authenticates credentials and returns `200 OK` with an `AuthResponse` or `401 Unauthorized` on invalid credentials.

### 2. Freelancer & Client Profiles (`/api/profiles`)
* `GET /api/profiles/me` — Returns the authenticated freelancer's `FreelancerProfileResponse` (`200 OK` or `404 Not Found`).
* `PUT /api/profiles/me` — Creates or updates the authenticated freelancer's profile (`title`, `bio`, up to 25 deduplicated `skills`, `portfolioUrl`). Restricted to `FREELANCER` accounts (`403 Forbidden` for clients).
* `GET /api/profiles/{userId}` — Publicly readable endpoint to view a freelancer's profile by user ID.

### 3. Categories & Gig Marketplace (`/api/categories`, `/api/gigs`)
* `GET /api/categories` — Returns the seeded campus gig categories (`Web Development`, `Mobile Development`, `Graphic Design`, `Writing & Translation`, `Video & Audio`, `Data & Analytics`, `Tutoring`, `Other`) sorted alphabetically.
* `POST /api/gigs` — Allows authenticated `CLIENT` users to post a new gig (`title`, `description`, `categoryId`) with initial status `OPEN` (`201 Created`, or `403 Forbidden` for freelancers).
* `GET /api/gigs` — Paginated marketplace search (`10` items per page, ordered newest first) returning `OPEN` gigs filtered optionally by `keyword` (case-insensitive match on title or description) and `categoryId`.

### 4. Angular 20 Frontend Routes
* `/` (`Home`) — Displays the UniGig campus landing page for unsigned visitors and automatically renders the **Gig Marketplace** (`<app-marketplace />`) when signed in.
* `/auth/login` & `/auth/register` — Reactive authentication forms with inline `ApiError` validation feedback.
* `/marketplace` (`Marketplace`) — Keyword search, category dropdown filter, pagination controls, and **Post Gig** action button (`canActivate: [authGuard]`).
* `/gigs/new` (`PostGig`) — Validated reactive form for clients to publish a new campus gig (`canActivate: [authGuard]`).
* `/profile` (`ProfileShell`) & `/profile/:userId` (`FreelancerProfile`) — Role-aware profile view and skill/portfolio editor (`canActivate: [authGuard]`).

---

## Team Git & Jira Workflow

1. **Branches:** `dev` is the active integration branch; `main` holds tested milestone releases.
2. **Feature Branches:** Always branch off `dev` using the Jira ticket key:
   * `feat/UG-<id>-short-description`
   * `fix/UG-<id>-short-description`
3. **Pull Requests:** Target `dev` and include `UG-<id>` in the PR title to link Jira automatically. Both `Backend CI` and `Frontend CI` must pass before merging.

---

## Quick Start (Local Development)

### Prerequisites
* **Docker Desktop** (runs PostgreSQL without native DB setup)
* **JDK 25** (Temurin / Oracle)
* **Node.js 22+ LTS** & **Angular CLI** (`npm install -g @angular/cli`)

### 1. Start PostgreSQL
From the repository root:
```bash
docker compose up -d
```
*(Starts `unigig-postgres` on `localhost:5432` — database `unigig`, user/password `postgres`.)*

### 2. Run the Backend (`unigig-backend/`)
Ensure `unigig-backend/src/main/resources/application.properties` points to `localhost:5432/unigig` with `spring.flyway.enabled=true` and `spring.jpa.hibernate.ddl-auto=validate`, then run:
```bash
cd unigig-backend
./mvnw spring-boot:run
```
* **API Base URL:** `http://localhost:8080`
* **Tests / JAR Build:** `./mvnw test` | `./mvnw clean package`

### 3. Run the Frontend (`unigig-frontend/`)
```bash
cd unigig-frontend
npm install
npm start
```
* **App URL:** `http://localhost:4200` (proxies `/api` requests to `http://localhost:8080` via `proxy.conf.json`)
* **Production Build:** `npx ng build --configuration production`

---

## Architecture Boundaries

### Backend (`com.unigig.backend`)
| Package | Responsibility |
| :--- | :--- |
| `controller/` | REST endpoints (`AuthController`, `ProfileController`, `GigController`, `CategoryController`) & `@Valid` request binding |
| `dto/` | Immutable Java `record` request/response contracts |
| `service/` | Business rules, role checks (`FREELANCER` vs `CLIENT`), and `@Transactional` boundaries |
| `repository/` | Spring Data JPA interfaces & JPQL marketplace search queries |
| `entity/` | JPA `@Entity` classes (`User`, `FreelancerProfile`, `Gig`, `Category`) & enums (`UserRole`, `GigStatus`) mapped to Flyway migrations `V1`–`V4` |
| `config/` | Stateless JWT `SecurityConfig` filter chain and `BCryptPasswordEncoder` |
| `security/` | `JwtService` token signing/parsing and `JwtAuthenticationFilter` |
| `exception/` | `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping errors to `ErrorResponse` JSON |

See [unigig-backend/CONVENTIONS.md](file:///c:/Users/Dell/Downloads/UniGig/unigig-backend/CONVENTIONS.md) for detailed backend rules.

### Frontend (`unigig-frontend/src/app/`)
| Folder | Responsibility |
| :--- | :--- |
| `core/` | `AuthService`, functional `authInterceptor` & `authGuard`, domain services (`GigService`, `ProfileService`), and strict 1-to-1 wire models (`core/models/`) |
| `features/` | Standalone Signal-driven page views (`home`, `auth`, `marketplace`, `gigs`, `profile`) |
| `shared/` | Reusable UI components (`navbar`) |

See [unigig-frontend/CONVENTIONS.md](file:///c:/Users/Dell/Downloads/UniGig/unigig-frontend/CONVENTIONS.md) for detailed frontend rules.

---

## DTO & Model Synchronization Policy

The REST API boundary between `unigig-backend` (`com.unigig.backend.dto` and domain enums) and `unigig-frontend` (`src/app/core/models/`) enforces a **strict 1-to-1 contract**.

- **Exact Structural Parity:** Every TypeScript interface or type in `core/models/*.models.ts` must mirror its corresponding backend Java DTO record or enum—matching exact `camelCase` property names, JSON-compatible types (`Long` → `number`, ISO timestamps → `string`), optionality, and uppercase enum string literals.
- **No Unbacked Wire Fields:** Frontend model files represent pure HTTP wire contracts. Any UI-only or derived display state belongs in component state or view-models, never inside `core/models/`.
- **Coordinated Changes:** Any modification to a backend DTO or a frontend model must update its counterpart before merging into `dev`.
