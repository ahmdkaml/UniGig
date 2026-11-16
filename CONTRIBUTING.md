# UniGig — Team Agile & Git Regulations

These regulations govern how the team collaborates, branches, reviews code, and maintains quality across the **UniGig** repository. Every teammate is expected to follow all nine rules on every ticket.

---

## 1. Always Branch From Latest `dev`
Never write code directly on `main` or `dev`. Before starting any task, pull the latest `dev` branch and create a dedicated feature or fix branch (`feat/UG-<id>-short-description` or `fix/UG-<id>-short-description`).

## 2. Keep PRs Clean & Synced With `dev`
Pull Requests must target `dev` and contain only commits belonging to your assigned ticket. You must sync your branch with `dev` **at least once every two days—and immediately whenever new changes are merged into `dev`**—so outdated history or unrelated commits never pollute your PR diff; any PR carrying stale `dev` history will be rejected.

## 3. Explainable AI Usage & Dual Review
AI assistance is allowed, but every Pull Request will be personally reviewed by **the team lead and one additional teammate**. You must be able to explain and defend every architectural and implementation decision in your code; any sloppy, unexplainable, or poorly structured code must be refactored before a PR can be accepted.

## 4. 48-Hour PR Freshness Rule
An open Pull Request under review must never remain idle for more than **two days** without addressing review comments and syncing with the latest `dev`. Keeping the PR up to date and moving toward merge is strictly the responsibility of the PR owner.

## 5. Follow Repository Standards & Module Conventions
All code must adhere to the established architecture, naming, and formatting standards across the repository. Module-specific conventions will be documented separately for each part of the codebase (`unigig-backend` and `unigig-frontend`) and must be reviewed and followed before submitting a Pull Request.

## 6. Mandatory AI Documentation Sync Pass
Before opening a Pull Request, run an AI review pass over your code changes alongside `README.md` and relevant module documentation, updating any affected docs so project documentation never goes stale.

## 7. Small, Atomic Pull Requests
Keep each Pull Request scoped to a single Jira ticket and roughly **under 350 lines of handwritten code** (excluding generated files such as `package-lock.json` or SQL migration boilerplate). If a change grows larger or mixes refactoring with new feature work, split it into multiple smaller PRs.

## 8. Green CI Required Before Review or Merge
A Pull Request will neither be reviewed nor accepted unless all automated CI checks (`Backend CI` and `Frontend CI`) pass completely without errors.

## 9. Test-First Development (Red-Green) & Contract-Protecting Tests
Tests come before implementation following a strict Red-Green-Refactor workflow, and Pull Requests must only be opened once the suite reaches Green. All core business logic and API endpoints must be covered by **contract-protecting tests** (verifying expected inputs, outputs, state transitions, and error handling) rather than trivial implementation-coupled assertions.