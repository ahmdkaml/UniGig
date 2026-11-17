# UniGig Frontend Conventions (`unigig-frontend`)

## 1. Directory Structure (`src/app/`)
- `core/` — Singleton services, HTTP interceptors, route guards, and shared API models (`core/models/api-error.model.ts`, `core/services/auth.service.ts`).
- `shared/` — Reusable dumb UI components, pipes, and directives with no feature-specific state.
- `features/` — Domain pages and feature flows grouped by capability (`features/auth/`, `features/gigs/`, `features/profile/`).

## 2. API & Error Handling Rules
- Always call backend endpoints using relative paths (`/api/...`); `proxy.conf.json` forwards `/api` to `http://localhost:8080` during `npm start`.
- Type backend error payloads using `ApiError` (`core/models/api-error.model.ts`). Display `fieldErrors` inline under matching form inputs and `message` as the form-level alert.

## 3. Component & State Standards
- Use Angular standalone components (no `NgModule`).
- Prefer Angular Signals (`signal`, `computed`) for local component state since the app uses zoneless change detection (`provideZonelessChangeDetection()`).
- Use Reactive Forms (`ReactiveFormsModule`) for all forms with validation.

## API Contract: Backend DTOs ↔ Frontend Models (Strict 1-to-1 Parity)

1. **Exact Structural Parity:** Every TypeScript interface/type under `unigig-frontend/src/app/core/models/*.models.ts` MUST strictly match its corresponding Java DTO record or enum under `unigig-backend/src/main/java/com/unigig/backend/dto/` (and domain enums like `UserRole`).
2. **Zero Silent Drift:**
   - Property names (exact `camelCase` JSON keys), value types (`Long` → `number`, `String` → `string`, `boolean` → `boolean`, `Instant`/`LocalDateTime` → ISO-8601 `string`), optionality/nullability, and enum string constants MUST remain identical on both sides.
   - Never rename, add, or remove a field in a backend DTO without updating the matching frontend model—and never add unbacked properties to a frontend DTO model.
3. **Pull Request Verification:** Any PR that modifies a backend DTO or a frontend `core/models` file MUST verify parity against its counterpart before merging into `dev`.
