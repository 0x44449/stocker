# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stocker is a stock situation analysis service that aggregates price data, official disclosures (DART), and news into AI-comprehensible "fact cards" for structured summaries. The core vision is an Instagram-like feed but for company/stock history instead of people. It provides situation analysis—not predictions or investment advice.

## Repository Structure

```
apps/
├── api/            # Java Spring Boot backend (main service)
├── news-analyzer/  # Python FastAPI for news extraction/embedding
├── admin-web/      # Next.js admin dashboard
└── stocker-mobile/ # Expo React Native mobile app
infra/docker/       # Docker Compose for local development
docs/               # Project docs, coding rules, work specs
```

## Build & Run Commands

### API (Spring Boot) - from `apps/api/`

```bash
./gradlew build           # Build
./gradlew bootRun         # Run application
./gradlew test            # Run all tests
./gradlew test --tests "app.sandori.stocker.api.ingest.krx.index.KrxIndexCsvParserTest"  # Single class
./gradlew test --tests "KrxIndexCsvParserTest.parse_normalRow_parsesAllFields"     # Single method
./gradlew clean           # Clean artifacts
```

### Admin Web (Next.js) - from `apps/admin-web/`

```bash
npm run dev    # Development server (localhost:3000)
npm run build  # Production build
npm run lint   # ESLint
```

### News Analyzer (FastAPI) - from `apps/news-analyzer/`

```bash
uvicorn main:app --reload  # Development server
```

### Mobile (Expo) - from `apps/stocker-mobile/`

```bash
npm start      # Expo dev server
npm run ios    # iOS simulator
npm run android # Android emulator
```

### Quick Start Scripts (from project root)

```bash
./start-api.sh        # API bootRun
./start-analyzer.sh   # News Analyzer uvicorn (port 8001)
```

### Docker Deployment

```bash
docker compose -f infra/docker/docker-compose.yml up -d   # Start all services
./deploy.sh                   # Full deploy (supports --no-cache, --down, --clean)
./deploy.sh --no-cache api    # Rebuild specific service without cache
```

## Tech Stack

**API**: Java 25, Spring Boot 4.0.2, PostgreSQL (pgvector), Flyway, Spring Data JPA + QueryDSL, RestClient, Jsoup, **No Lombok**

**News Analyzer**: Python, FastAPI, LangChain + Ollama (qwen2.5:7b), sentence-transformers (KURE-v1), pgvector, APScheduler

**Admin Web**: Next.js 16, React 19, TypeScript, Tailwind CSS 4, Radix UI

**Mobile**: Expo 54, React Native 0.81, TypeScript

## API Architecture

Monolithic Spring Boot with feature-based package separation:

```
app.sandori.stocker.api
├── config/        # QuerydslConfig, WebConfig, AuthInterceptor, @AllowPublic, @Authenticated
├── domain/        # REST controllers (public endpoints)
│   ├── auth/      # AuthController, AuthService (Supabase JWT 인증)
│   ├── feed/      # HotStock, StockTopics, NewsAnomaly (피드 관련)
│   ├── headline/  # HeadlineController
│   ├── newsmapping/ # NewsMappingController
│   └── internal/  # Admin endpoints (crawler triggers)
├── ingest/        # Data collection modules
│   ├── news/      # News article crawling from press sites
│   └── krx/       # KRX market data (index, investor flow, stock prices)
├── entities/      # JPA entities
└── repositories/  # JPA + QueryDSL repositories
```

### Ingest Module Patterns

**News Crawling** (`ingest/news/`):
- `NewsProvider` interface: Each press site (hk/, mk/, etoday/, sed/) implements parsing, URL filtering, sitemap hints
- `ProviderRegistry`: Auto-discovers all `NewsProvider` beans via Spring DI
- `NewsCrawlEngine`: Orchestrates robots.txt → sitemap → fetch → parse → save
- `CrawlLock`: Prevents concurrent crawl jobs via AtomicBoolean
- Rate limiting: Configurable delay between requests, respects HTTP 429

**KRX Data** (`ingest/krx/`):
- `KrxSessionProvider`: Manages authenticated KRX sessions (credentials via env vars `KRX_USERNAME`, `KRX_PASSWORD`)
- `KrxFileClient`: Common OTP generation + CSV download flow using RestClient
- Per-data-type modules: `index/`, `investor/`, `stock/`, `master/`
- Each has: CrawlEngine + CrawlScheduler → CsvParser → Repository

### Authentication

Supabase JWT 기반 인증. `AuthInterceptor`가 `/api/**` 경로에 적용:

- 기본 동작: Bearer JWT 검증 + `allowed_user` 테이블 확인 (실패 시 401/403)
- `@AllowPublic`: 인증 완전 스킵 (공개 API용)
- `@Authenticated`: JWT 검증만 수행, 허용 사용자 확인 안함

### News Analyzer Pipeline

- **Extraction** (scheduled 8,10,13,16,19h UTC): LLM extracts company names from news articles
- **Embedding** (scheduled 9,11,14,17,20h UTC): Creates vector embeddings for similarity search
- **Clustering**: Groups similar news articles for topic-based display
- **Anomaly** (`anomaly/`): 뉴스 급증 감지 (news spike detection)
- **Search**: pgvector-based similar news retrieval

## Database

- Flyway migrations in `apps/api/src/main/resources/db/migration/` (V1 through V19)
- Naming: `V1__init.sql`, `V2__add_news_raw.sql`, etc.
- JPA uses `ddl-auto: validate` (Flyway owns schema)
- Never modify already-applied migration files; create new versions instead

## Key Design Decisions

Refer to `docs/CODING_DECISIONS.md` for the full decision log. Key decisions:

- **Upsert via saveAll()**: For small datasets (<5k, daily), JPA saveAll() over native SQL upsert to avoid column hardcoding
- **Timezone**: KST (+09:00) for display, UTC for storage
- **Dynamic queries via QueryDSL**: Not @Query native SQL. Simple queries use JPA method naming
- **DB vs business logic**: DB handles filtering/paging, service layer handles aggregation/status calculation
- **springdoc-openapi 3.0.1**: Required for Spring Boot 4.0 + QueryDSL compatibility (2.x incompatible)
- **Parsing**: Strict `parseLong()` by default, `parseLongOrNull()` only for known special cases (e.g. "무액면")
- **Crawl logging**: Structured logs to `CRAWL` logger (event=, jobId=, provider=)
- **No FK**: Foreign keys and CASCADE not used; data integrity managed at application level
- **Explicit PK naming**: New tables use `extraction_id`, `link_id` etc. instead of generic `id`

## Infrastructure

- PostgreSQL (pgvector/pgvector:pg17) via Docker Compose
- PostgreSQL port: 5433 externally (avoids conflict with local postgres 5432)
- Docker ports: API=28080, Analyzer=28000, Admin Web=23000
- KRX credentials: set `KRX_USERNAME` and `KRX_PASSWORD` env vars
- Ollama: local instance, accessed via `host.docker.internal:11434` from Docker

## Workflow: docs/WORK.md

This project uses a role-based workflow (defined in `docs/ARCHITECT.md`, `docs/CODER.md`, `docs/REVIEWER.md`):

1. **Architect** writes a work spec in `docs/WORK.md`
2. **Coder (Claude Code)** implements exactly what's in WORK.md — no more, no less
3. **Reviewer** checks implementation against WORK.md, then commits after user (zina) confirmation
4. On commit, WORK.md is archived to `docs/WORKS/YYYYMMDD_NNN_description.md`

As Coder, follow these rules:
- Implement only what's specified in `docs/WORK.md`
- Don't add unrequested error handling, logging, validation, or "improvements"
- Ask before deciding edge cases or ambiguous requirements
- Leave `TODO:` comments for uncertain parts

## Coding Rules (from docs/CODING_RULE.md)

- **No premature abstraction**: Don't extract common functions unless explicitly requested; inline instead
- **Minimize classes**: Avoid over-engineering simple flows into many classes
- **YAGNI**: Only build what's needed now; hardcode first, no interfaces until 2+ implementations
- **Happy path first**: Add error handling only when failures actually occur
- **Feature-based packages**: Group by feature (not layer), place shared code in nearest `common/`
- **Incremental development**: Work in small chunks, ask before deciding edge cases; leave TODOs for uncertain parts

### Comment Rules

- **Language**: All comments in Korean
- **Content**: Document "why" not "what"; explain magic numbers, workarounds, special cases
- **Functions**: Brief comments on main logic functions and externally exposed APIs
- **Branches**: Comment complex or important conditional logic
- **Data/format references**: Explain referenced data formats so readers don't need to look them up
- **Markers**: `// TODO:` for unfinished work, `// FIXME:` for known issues

## Commit Conventions

- Conventional format: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`
- Korean commit messages, subject under 50 chars
- Never push to remote without explicit instruction
- Never commit without user (zina) confirmation
