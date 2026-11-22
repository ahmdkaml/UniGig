# UniGig Frontend Conventions (`unigig-frontend`)

## 1. Directory Structure & File Naming (`src/app/`)
The frontend enforces a clean, flat 3-folder architecture under `src/app/`:
* `core/` — Singleton services, functional route guards, HTTP interceptors, and strict backend wire models:
  * `core/auth/` — `auth.service.ts` (`AuthService`), `auth.interceptor.ts` (`authInterceptor`), `auth.guard.ts` (`authGuard`).
  * `core/services/` — Domain HTTP services: `gig.service.ts` (`GigService`), `profile.service.ts` (`ProfileService`).
  * `core/models/` — Strict 1-to-1 wire contracts: `api-error.model.ts`, `auth.models.ts`, `gig.models.ts`, `profile.models.ts`.
* `shared/` — Reusable UI components shared across pages (`navbar.ts`, `navbar.html`).
* `features/` — Top-level domain feature folders (never nest one feature inside another):
  * `features/home/` — `home.ts`, `home.html` (renders the landing page for unsigned visitors and `<app-marketplace />` for authenticated users).
  * `features/auth/` — `auth.routes.ts`, `login.ts`, `login.html`, `register.ts`, `register.html`.
  * `features/marketplace/` — `marketplace.routes.ts`, `marketplace.ts`, `marketplace.html`.
  * `features/gigs/` — `gigs.routes.ts`, `post-gig.ts`, `post-gig.html`.
  * `features/profile/` — `profile.routes.ts`, `profile-shell.ts`, `freelancer-profile.ts`, `freelancer-profile.html`, `client-profile.ts`.

### Naming Convention (No `.component` Suffix)
* Follow the modern Angular 20 file and class naming style used throughout the repository:
  * File names: `<feature-name>.ts` and `<feature-name>.html` (e.g., `login.ts`, `post-gig.ts`, `freelancer-profile.ts`, `marketplace.ts`) — **never** use `.component.ts` or `.component.html`.
  * Class names: `Login`, `Register`, `Home`, `Marketplace`, `PostGig`, `FreelancerProfile`, `ClientProfile`, `ProfileShell`, `Navbar` — **never** append `Component`.
  * Route files: `<feature>.routes.ts` exporting uppercase route arrays (`AUTH_ROUTES`, `MARKETPLACE_ROUTES`, `GIG_ROUTES`, `PROFILE_ROUTES`) lazy-loaded from `app.routes.ts`.

---

## 2. Component, State & Template Standards
* **Standalone Components Only:** Every component is standalone (default in Angular 20; no `NgModule` and no redundant `standalone: true` property needed).
* **Zoneless & Signals:** The app runs with `provideZonelessChangeDetection()`. Use Angular Signals (`signal()`, `computed()`) for all reactive component and service state (`isLoading`, `isSubmitting`, `errorMessage`, `gigs`, `categories`, `authService.isAuthenticated`, `authService.currentUser`).
* **Modern Built-in Control Flow:** Always use `@if`, `@else`, and `@for (...; track ...)` in templates. Do not use legacy `*ngIf` or `*ngFor` directives or import `CommonModule` for control flow.
* **Reactive Forms:** Use `ReactiveFormsModule` and `FormBuilder` (`inject(FormBuilder)`) for all forms and filter bars (`Login`, `Register`, `PostGig`, `FreelancerProfile`, `Marketplace`).
* **Styling:** Use Tailwind CSS v4 with the consistent `slate-*` color palette (`bg-slate-50`, `bg-white`, `border-slate-200`, `text-slate-900`, primary buttons `bg-slate-900 text-white hover:bg-slate-800`).

---

## 3. API & Error Handling Rules
* **Relative API Paths:** Always call backend endpoints using relative paths (`/api/auth/...`, `/api/gigs`, `/api/categories`, `/api/profiles/...`). During local development (`npm start`), `proxy.conf.json` forwards `/api` to `http://localhost:8080`.
* **JWT Injection:** `authInterceptor` automatically attaches `Authorization: Bearer <token>` from `AuthService` to all outgoing requests except `/api/auth/login` and `/api/auth/register`.
* **Error Typing:** Type backend error payloads using `ApiError` (`core/models/api-error.model.ts`). Display `apiError.message` as the form/page-level alert and render `apiError.fieldErrors` inline or as a validation list.

---

## 4. API Contract: Backend DTOs ↔ Frontend Models (Strict 1-to-1 Parity)

1. **Exact Structural Parity:** Every TypeScript interface/type under `unigig-frontend/src/app/core/models/*.models.ts` MUST strictly match its corresponding Java DTO record or enum under `unigig-backend/src/main/java/com/unigig/backend/dto/` and `com.unigig.backend.entity`:
   * `ApiError` (`api-error.model.ts`) ↔ `ErrorResponse`
   * `UserRole`, `RegisterRequest`, `LoginRequest`, `AuthResponse` (`auth.models.ts`) ↔ `UserRole`, `RegisterRequest`, `LoginRequest`, `AuthResponse`
   * `FreelancerProfileRequest`, `FreelancerProfileResponse` (`profile.models.ts`) ↔ `FreelancerProfileRequest`, `FreelancerProfileResponse`
   * `GigStatus`, `Category`, `GigRequest`, `GigResponse`, `PageResponse<T>` (`gig.models.ts`) ↔ `GigStatus`, `CategoryResponse`, `CreateGigRequest`, `GigResponse`, `Page<T>`
2. **Zero Silent Drift:**
   * Property names (exact `camelCase` JSON keys), value types (`Long` → `number`, `String` → `string`, `boolean` → `boolean`, `Instant` → ISO-8601 `string`), optionality/nullability, and enum string constants (`'FREELANCER' | 'CLIENT'`, `'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELED'`) MUST remain identical on both sides.
   * Never rename, add, or remove a field in a backend DTO without updating the matching frontend model—and never add unbacked wire properties to a frontend DTO model.
3. **Pull Request Verification:** Any PR that modifies a backend DTO or a frontend `core/models` file MUST verify parity against its counterpart and pass `npx ng build --configuration production` (`Frontend CI`).
