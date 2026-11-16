# UniGig — University Freelance Marketplace

A campus freelance marketplace connecting university clients with student freelancers, built for **CSE322 Software Engineering (Alexandria University)**.

---

## Repository Structure

```text
unigig/
├── .github/workflows/   # Independent CI pipelines (backend-ci.yml & frontend-ci.yml)
├── unigig-backend/      # Java 25 / Spring Boot 4.1 REST API
├── unigig-frontend/     # Angular SPA + Tailwind CSS (Node 22)
├── docker-compose.yml   # Local PostgreSQL 17 database container
└── README.md
```

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
* **Node.js 22 LTS** & **Angular CLI** (`npm install -g @angular/cli`)

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
* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **Tests / JAR Build:** `./mvnw test` | `./mvnw clean package`

### 3. Run the Frontend (`unigig-frontend/`)
```bash
cd unigig-frontend
npm install
ng serve
```
* **App URL:** `http://localhost:4200`
* **Production Build:** `npx ng build --configuration production`

---

## Architecture Boundaries

### Backend (`com.unigig.backend`)
| Package | Responsibility |
| :--- | :--- |
| `controller/` | REST endpoints & `@Valid` request handling |
| `dto/` | Immutable Java `record` request/response contracts |
| `service/` | Business rules, role switching, and `@Transactional` boundaries |
| `repository/` | Spring Data JPA interfaces |
| `model/` | JPA `@Entity` classes & enums mapped to Flyway SQL migrations |
| `config/` | Stateless JWT security filter chain, BCrypt encoder, and CORS |
| `exception/` | `@RestControllerAdvice` mapping errors to consistent JSON |

### Frontend (`unigig-frontend/src/app/`)
| Folder | Responsibility |
| :--- | :--- |
| `core/` | Singleton services (`AuthService`), JWT `HttpInterceptorFn`, route guards, and TypeScript interfaces |
| `features/` | Page views (`auth`, `home`, `marketplace`, `dashboard`, `profile`) |
| `shared/` | Reusable UI components (navbar, cards, modals) |

---

## Deployment Overview
* **Frontend:** Static Angular SPA hosted on **GitHub Pages** from `main`.
* **Backend & DB:** Containerized locally via Docker and exposed over HTTPS using a **Cloudflare Tunnel** URL configured in `unigig-frontend/src/environments/environment.prod.ts`.