# UniGig — University Freelance Marketplace

A full-stack campus freelance marketplace platform connecting university clients with student freelancers, built for **CSE322 Software Engineering (Alexandria University)**.

---

## Repository Structure

```text
unigig/
├── .github/
│   └── workflows/
│       ├── backend-ci.yml     # Automated Maven compile & test checks
│       └── frontend-ci.yml    # Automated Angular production build checks
├── unigig-backend/            # Java 25 / Spring Boot 4.1 REST API
├── unigig-frontend/           # Angular + Tailwind CSS SPA web application
├── docker-compose.yml         # Local PostgreSQL database container
└── README.md                  # Project & setup documentation
```

---

## Git & Jira Workflow (Team Guidelines)

1. **Branching Strategy (`dev` + `main`):**
   * `main` holds stable, tested milestone releases and deploys the frontend to GitHub Pages.
   * `dev` is the active integration branch for daily team development.
   * Always branch off **`dev`** using your Jira ticket key:
     * `feat/UG-<id>-short-description` (new features)
     * `fix/UG-<id>-short-description` (bug fixes)
2. **Pull Requests:**
   * Open Pull Requests targeting **`dev`**.
   * Include the Jira key (`UG-<id>`) in the PR title so Jira automatically links your branch/commits and transitions the ticket on merge.
3. **CI Checks:**
   * Both `Backend CI` and `Frontend CI` must pass before merging into `dev` or `main`.

---

## Quick Start (Running the Full Stack Locally)

To run the complete application locally, open **two terminals** from the repository root:

1. **Start the Database & Backend API (Terminal 1):**
   ```bash
   docker compose up -d
   cd unigig-backend
   ./mvnw spring-boot:run
   ```
   * Backend API runs at `http://localhost:8080`
   * Interactive Swagger API Docs available at `http://localhost:8080/swagger-ui.html`

2. **Start the Angular Frontend (Terminal 2):**
   ```bash
   cd unigig-frontend
   npm install
   npm start
   ```
   * Frontend web app runs at `http://localhost:4200`

---

## Backend (`unigig-backend/`)

### 1. Prerequisites
* **JDK 25** (Eclipse Temurin or Oracle JDK)
* **Docker Desktop** *(recommended for PostgreSQL)* — or a local **PostgreSQL 16+** installation
* **IDE:** IntelliJ IDEA or VS Code (*Extension Pack for Java*). If using Lombok, enable **Annotation Processing** in your IDE settings.

### 2. Environment Configuration
Copy `unigig-backend/src/main/resources/application.properties.example` to `application.properties` (never commit real secrets):

```properties
spring.application.name=unigig-backend
server.port=8080

# PostgreSQL Database
spring.datasource.url=jdbc:postgresql://localhost:5432/unigig
spring.datasource.username=postgres
spring.datasource.password=postgres

# Hibernate & Flyway (Flyway manages schema; Hibernate validates)
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

# JWT Configuration (Minimum 32 characters / 256 bits)
jwt.secret=replace-with-a-secure-256-bit-random-key-for-local-dev
jwt.expiration-ms=86400000
```

### 3. Testing & Building Executable JAR
```bash
cd unigig-backend
./mvnw test                                          # Run unit & integration tests
./mvnw clean package                                 # Build standalone JAR (NFR7)
java -jar target/unigig-backend-0.0.1-SNAPSHOT.jar   # Run executable build
```

### 4. Package Architecture (`com.unigig.backend`)
Structured around a strict three-layer architecture (NFR4):

| Package | Layer | Role & Responsibilities |
| :--- | :--- | :--- |
| `controller/` | **Presentation** | REST endpoints (`AuthController`, `UserController`, `GigController`). Validates incoming requests (`@Valid`) and returns HTTP responses. |
| `dto/` | **Data Contracts** | Immutable Java `record` types (`RegisterRequest`, `LoginRequest`, `AuthResponse`) decoupling API JSON payloads from database entities. |
| `service/` | **Business Logic** | Core domain workflows (`AuthService`, `UserService`, `GigService`), `@Transactional` boundaries, role switching, and gig status transitions. |
| `repository/` | **Persistence** | Spring Data JPA interfaces (`UserRepository`, `GigRepository`) handling database queries and pagination. |
| `model/` | **Domain Model** | Hibernate `@Entity` classes (`User`, `Freelancer`, `Gig`, `Application`, `Comment`) and status/role enums mapped to PostgreSQL tables. |
| `config/` | **Security & Config** | `SecurityConfig` (stateless filter chain & CORS), `JwtAuthenticationFilter`, `JwtService`, and `BCryptPasswordEncoder` beans. |
| `exception/` | **Error Handling** | Centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) translating validation and business errors into clean JSON responses. |

### 5. Core Backend Libraries
* **Spring Web (`spring-boot-starter-web`):** Embedded Apache Tomcat server, Spring MVC request routing, and Jackson 3 JSON serialization.
* **Spring Data JPA (`spring-boot-starter-data-jpa`):** Hibernate ORM entity persistence, HikariCP connection pooling, and repository query generation.
* **PostgreSQL Driver (`postgresql`):** JDBC driver connecting Spring Boot to PostgreSQL.
* **Flyway (`spring-boot-starter-flyway`):** Version-controlled SQL schema migrations (`src/main/resources/db/migration/V1__*.sql`).
* **Spring Security (`spring-boot-starter-security`):** Stateless JWT authentication filter chain, `BCrypt` password hashing, and role-based access control (`CLIENT`, `FREELANCER`, `ADMIN`).
* **Validation (`spring-boot-starter-validation`):** Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`) for API input validation.
* **JJWT (`io.jsonwebtoken:jjwt-*`):** Cryptographic signing and verification of JWT session tokens.
* **SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`):** Auto-generated interactive Swagger API documentation.

---

## Frontend (`unigig-frontend/`)

### 1. Prerequisites
* **Node.js 22 LTS** & **npm**
* **Angular CLI** (`npm install -g @angular/cli`)

### 2. Local Setup & Running
1. **Install dependencies:**
   ```bash
   cd unigig-frontend
   npm install
   ```
2. **Start the development server:**
   ```bash
   npm start
   ```
   * App runs at `http://localhost:4200` with live reload.
3. **Production Build:**
   ```bash
   npx ng build --configuration production
   ```

### 3. Folder Architecture (`unigig-frontend/src/app/`)
Organized by domain features and shared infrastructure to minimize merge conflicts:

| Directory | Role & Responsibilities |
| :--- | :--- |
| `core/guards/` | Route protection (`auth.guard.ts`, `role.guard.ts`) preventing unauthorized navigation. |
| `core/interceptors/` | HTTP interceptors (`jwt.interceptor.ts`) automatically attaching `Authorization: Bearer <token>` headers to API calls. |
| `core/models/` | TypeScript interfaces matching backend JSON DTOs (`User`, `Gig`, `AuthResponse`). |
| `core/services/` | Singleton HTTP services (`auth.service.ts`, `gig.service.ts`, `user.service.ts`) communicating with the Spring Boot API. |
| `features/` | Page-level standalone components (`auth/`, `home/`, `marketplace/`, `dashboard/`, `profile/`). |
| `shared/` | Reusable presentational UI components (`navbar`, `gig-card`, modals, form validation helpers). |

### 4. Core Frontend Stack
* **Angular (Standalone Components, Client-Side SPA):** Component-based frontend architecture configured without SSR (`--ssr=false`) for static GitHub Pages deployment and client-side JWT session management.
* **Tailwind CSS v4 (`@tailwindcss/postcss`):** Utility-first CSS framework for responsive layouts, dashboards, and custom UI components.
* **Angular Router & HttpClient:** Client-side navigation and reactive REST API communication.

---

## Deployment Architecture

* **Frontend:** Hosted statically on **GitHub Pages** from the `main` branch.
* **Backend & Database:** Containerized via Docker on a local host machine and exposed securely over HTTPS via a **Cloudflare Tunnel** (`cloudflared`).