# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stocker is a stock situation analysis service that aggregates price data, official disclosures (DART), and news into AI-comprehensible "fact cards" for structured summaries. It provides situation analysis—not predictions or investment advice.

## Build & Run Commands

All commands run from `apps/api/`:

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

## Tech Stack

- Java 25 with Spring Boot 4.0.2
- PostgreSQL with Flyway migrations
- Spring Data JPA, WebMVC, Validation
- No Lombok (explicit code preferred)

## Architecture

Monolithic Spring Boot with role-based package separation:

```
com.hanzi.stocker
├── api/        # REST controllers + request/response DTOs only (no business logic)
├── domain/     # Core business logic, entities, calculations
├── ingest/     # Batch jobs for data collection (price, DART disclosure)
├── infra/      # External integrations (DB repos, HTTP clients, LLM)
└── common/     # Config, exceptions, utilities
```

**Central service**: `SummaryService.getSituation(code, date)` orchestrates data retrieval, fact card generation, and optional LLM summarization.

## Database

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V1__init.sql`, `V2__add_daily_price.sql`, etc.
- JPA should use `ddl-auto: validate` (Flyway owns schema)
- Never modify already-applied migration files; create new versions instead

## Key Design Decisions

- **Fact cards over raw data**: Normalize/enrich data before LLM processing
- **Upsert strategy**: Daily prices and disclosures are idempotent; use native query upsert when JPA insufficient
- **Timezone**: KST (+09:00) for display, UTC for storage
- **WebClient**: Preferred over RestTemplate for HTTP calls; include timeouts and rate limit handling
