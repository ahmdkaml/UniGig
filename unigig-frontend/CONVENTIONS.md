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
