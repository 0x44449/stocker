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

# Run a single test class
./gradlew test --tests "com.hanzi.stocker.ingest.krx.index.KrxIndexCsvParserTest"

# Run a single test method
./gradlew test --tests "KrxIndexCsvParserTest.parse_normalRow_parsesAllFields"

# Clean build artifacts
./gradlew clean
```

## Tech Stack

- Java 25 with Spring Boot 4.0.2
- PostgreSQL with Flyway migrations
- Spring Data JPA, WebMVC, Validation
- Jsoup for HTML parsing
- No Lombok (explicit code preferred)

## Architecture

Monolithic Spring Boot with role-based package separation:

```
com.hanzi.stocker
├── api/           # REST controllers (public + internal admin endpoints)
├── api/internal/  # Internal admin endpoints (crawler triggers)
└── ingest/        # Data collection modules
    ├── news/      # News article crawling from press sites
    └── krx/       # KRX market data (index, investor flow, stock prices)
```

### Ingest Module Patterns

**News Crawling** (`ingest/news/`):
- `NewsProvider` interface: Each press site implements parsing, URL filtering, sitemap hints
- `ProviderRegistry`: Auto-discovers all `NewsProvider` beans via Spring DI
- `NewsCrawlEngine`: Orchestrates robots.txt → sitemap → fetch → parse → save
- `CrawlLock`: Prevents concurrent crawl jobs via AtomicBoolean
- Rate limiting: Configurable delay between requests, respects HTTP 429

**KRX Data** (`ingest/krx/`):
- `KrxSessionProvider`: Manages authenticated KRX sessions (credentials via env vars `KRX_USERNAME`, `KRX_PASSWORD`)
- `KrxFileClient`: Common OTP generation + CSV download flow
- Per-data-type modules: `index/` for market indices, `investor/` for investor flow, `stock/` for stock prices
- Each has: CrawlEngine + CrawlScheduler → CsvParser → Repository

## Database

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V1__init.sql`, `V2__add_news_raw.sql`, etc.
- JPA uses `ddl-auto: validate` (Flyway owns schema)
- Never modify already-applied migration files; create new versions instead

## Key Design Decisions

- **Fact cards over raw data**: Normalize/enrich data before LLM processing
- **Upsert strategy**: Daily data is idempotent; use native query upsert when JPA insufficient
- **Timezone**: KST (+09:00) for display, UTC for storage
- **HTTP clients**: Use timeouts and rate limit handling; respect robots.txt for crawlers
- **Crawl logging**: Structured logs to `CRAWL` logger for monitoring (event=, jobId=, provider=)

## Infrastructure

- PostgreSQL via Docker Compose: `docker compose -f infra/docker/docker-compose.yml up -d`
- KRX credentials: set `KRX_USERNAME` and `KRX_PASSWORD` env vars

## Coding Rules (from docs/CODING_RULE.md)

- **No premature abstraction**: Don't extract common functions unless explicitly requested; inline instead
- **Minimize classes**: Avoid over-engineering simple flows into many classes
- **YAGNI**: Only build what's needed now; hardcode first, no interfaces until 2+ implementations
- **Happy path first**: Add error handling only when failures actually occur
- **Feature-based packages**: Group by feature (not layer), place shared code in nearest `common/`
- **Incremental development**: Work in small chunks, ask before deciding edge cases; leave TODOs for uncertain parts

## Commit Conventions

- Conventional format: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`
- Korean commit messages
- Never push to remote without explicit instruction
