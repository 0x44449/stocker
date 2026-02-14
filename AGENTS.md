# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-app monorepo:
- `apps/api`: Spring Boot API (Java 25), Flyway migrations in `src/main/resources/db/migration`.
- `apps/news-analyzer`: FastAPI service for extraction/embedding/search.
- `apps/admin-web`: Next.js admin UI.
- `apps/stocker-mobile`: Expo React Native client.
- `infra/docker`: local Docker Compose stack.
- `docs`: architecture notes, coding rules, and archived work specs.

Prefer feature-based organization in backend code (`ingest/news`, `ingest/krx/*`) over layer-first expansion.

## Build, Test, and Development Commands
Run commands from each app directory unless noted.

- API (`apps/api`)
  - `./gradlew bootRun`: run local API.
  - `./gradlew build`: compile + test + package.
  - `./gradlew test --tests "ClassName.methodName"`: targeted test run.
- Admin Web (`apps/admin-web`)
  - `npm run dev`, `npm run build`, `npm run lint`.
- News Analyzer (`apps/news-analyzer`)
  - `uvicorn main:app --reload`: run local analyzer.
- Mobile (`apps/stocker-mobile`)
  - `npm start`, `npm run ios`, `npm run android`.
- Infra (repo root)
  - `./deploy.sh` or `docker compose -f infra/docker/docker-compose.yml up -d --build`.

## Coding Style & Naming Conventions
- Follow existing style per language; keep changes local and minimal.
- Backend DB changes: add new Flyway versioned files (`V16__...sql`), never edit applied migrations.
- Prefer pragmatic implementation over premature abstraction (YAGNI).
- Keep packages feature-oriented; move shared code only when reuse is proven.
- Write code comments in Korean, focused on why (not obvious what).

## Testing Guidelines
- API uses JUnit Platform (`./gradlew test`). Add or update tests for service/repository/controller changes.
- Analyzer/web/mobile have limited automated tests today; validate with focused local run checks before PR.
- For crawler/parsing changes, include at least one reproducible verification path in PR notes.

## Commit & Pull Request Guidelines
- Commit pattern in history follows Conventional Commits with Korean subjects, e.g. `feat: 뉴스 클러스터링 API 추가`.
- Common types: `feat`, `fix`, `refactor`, `chore`, `docs`.
- Keep commit subject concise (around 50 chars).
- PRs should include: purpose, changed modules, test/verification commands, schema/env changes, and screenshots for UI updates.
- Do not push or merge directly without explicit maintainer confirmation.
