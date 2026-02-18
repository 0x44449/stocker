# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stocker is a stock situation analysis service that aggregates price data, official disclosures (DART), and news into AI-comprehensible "fact cards" for structured summaries. The core vision is an Instagram-like feed but for company/stock history instead of people. It provides situation analysis—not predictions or investment advice.

## Repository Structure

```
apps/
├── api/            # Java Spring Boot backend (사용자 API 전용)
├── ingest/         # Java Spring Boot (뉴스/KRX 데이터 수집 전용)
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
./gradlew test --tests "ClassName"        # Single class
./gradlew test --tests "ClassName.method" # Single method
./gradlew clean           # Clean artifacts
```

### Ingest (Spring Boot) - from `apps/ingest/`

```bash
./gradlew build           # Build
./gradlew bootRun         # Run application (port 8081)
./gradlew test            # Run all tests
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
./start-api.sh        # API bootRun (port 8080)
./start-ingest.sh     # Ingest bootRun (port 8081)
./start-analyzer.sh   # News Analyzer uvicorn (port 8001)
```

### Docker Deployment

```bash
docker compose -f infra/docker/docker-compose.yml up -d   # Start all services
./deploy.sh                   # Full deploy (supports --no-cache, --down, --clean)
./deploy.sh --no-cache api    # Rebuild specific service without cache
```

## Tech Stack

**API**: Java 25, Spring Boot 4.0.2, PostgreSQL (pgvector), Flyway, Spring Data JPA + QueryDSL, RestClient, **No Lombok**

**Ingest**: Java 25, Spring Boot 4.0.2, PostgreSQL, Spring Data JPA, RestClient, Jsoup, Commons CSV, **No Lombok**

**News Analyzer**: Python, FastAPI, LangChain + Ollama (qwen2.5:7b), sentence-transformers (KURE-v1), pgvector, APScheduler

**Admin Web**: Next.js 16, React 19, TypeScript, Tailwind CSS 4, Radix UI

**Mobile**: Expo 54, React Native 0.81, TypeScript

## API Architecture

Spring Boot 사용자 API 전용 (데이터 수집 기능 없음):

```
app.sandori.stocker.api
├── config/        # QuerydslConfig, WebConfig, AuthInterceptor, @AllowPublic, @Authenticated
├── domain/        # REST controllers (public endpoints)
│   ├── auth/      # AuthController, AuthService (Supabase JWT 인증)
│   ├── feed/      # HotStock, StockTopics, NewsAnomaly (피드 관련)
│   ├── headline/  # HeadlineController
│   └── newsmapping/ # NewsMappingController
├── entities/      # JPA entities
└── repositories/  # JPA + QueryDSL repositories
```

## Ingest Architecture

독립 Spring Boot 앱. 뉴스/KRX 데이터 수집 전용 (Flyway 미사용, API 앱이 스키마 관리):

```
app.sandori.stocker.ingest
├── controller/    # NewsCrawlController, StockMasterCrawlController, HealthController
├── news/          # News article crawling from press sites
│   └── provider/  # hk/, mk/, etoday/, sed/ (각 언론사 크롤러)
├── krx/           # KRX market data
│   ├── common/    # KrxAuthClient, KrxFileClient, KrxSessionProvider
│   ├── index/     # 시장지수
│   ├── investor/  # 투자자별 매매동향
│   ├── stock/     # 종목별 시세
│   └── master/    # 종목 마스터
├── entities/      # JPA entities (API 앱에서 복사)
└── repositories/  # JPA repositories (API 앱에서 복사)
```

### Ingest Module Patterns

**News Crawling** (`news/`):
- `NewsProvider` interface: Each press site (hk/, mk/, etoday/, sed/) implements parsing, URL filtering, sitemap hints
- `ProviderRegistry`: Auto-discovers all `NewsProvider` beans via Spring DI
- `NewsCrawlEngine`: Orchestrates robots.txt → sitemap → fetch → parse → save
- `CrawlLock`: Prevents concurrent crawl jobs via AtomicBoolean
- Rate limiting: Configurable delay between requests, respects HTTP 429

**KRX Data** (`krx/`):
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
- Docker ports: API=28080, Ingest=28081, Analyzer=28000, Admin Web=23000
- KRX credentials: set `KRX_USERNAME` and `KRX_PASSWORD` env vars (Ingest 서비스에서 사용)
- Ollama: local instance, accessed via `host.docker.internal:11434` from Docker

## Workflow

Claude Code가 설계/구현/리뷰/커밋을 모두 수행한다.

### 작업 흐름

1. zina가 작업 요청
2. Claude Code가 구현 계획을 제시하고 **반드시 zina 승인을 받은 후** 구현 시작
3. 구현 완료 후 변경사항 리뷰 및 커밋 메시지 제안
4. **zina 컨펌 후** 로컬 커밋 수행 (push는 명시적 요청 시에만)

### 구현 규칙

- 요청된 범위 내에서만 작업한다
- 요청하지 않은 에러 처리, 로깅, 검증, "개선"을 추가하지 않는다
- 불명확한 부분은 구현 전에 zina에게 확인한다
- 경계 조건, 예외 처리, 엣지 케이스를 임의로 결정하지 않는다
- 불확실한 부분은 `// TODO:` 로 남긴다

### 커밋 규칙

- **zina 컨펌 없이 절대 커밋하지 않는다**
- 커밋 메시지: `<type>: <subject>` (한글, 50자 이내)
- Type: `feat`, `fix`, `refactor`, `chore`, `docs`
- Body(선택): 변경 사항 간략 나열
- `git push`는 zina가 명시적으로 요청할 때만 수행

### 리뷰 규칙

- 구현 완료 후 자체 리뷰를 수행한다
- 불필요한 파일 변경이 없는지 확인한다
- `CODING_RULE.md`, `CODING_DECISIONS.md` 규칙 위반이 없는지 확인한다
- 리뷰 결과와 커밋 메시지를 zina에게 보고한다

### PROJECT_CONTEXT.md / CODING_DECISIONS.md 기록

- 논의 중 설계/코딩 결정이 나오면 zina에게 기록 여부를 확인한다
- 질문 형식: "이번에 [X] 결정했는데, CODING_DECISIONS에 기록할까요?"
- zina가 컨펌하면 해당 문서에 기록한다

### HANDOFF (세션 간 작업 인계)

긴 작업이 세션을 넘어갈 때 `docs/HANDOFF.md`를 사용하여 작업을 이어간다.

**새 세션 시작 시**: `docs/HANDOFF.md`가 존재하면 먼저 읽고 이전 작업을 이어간다.

**작업 중 기록 시점**: 작업이 길어져서 세션이 끊길 수 있을 때 HANDOFF.md에 현재 상태를 기록한다.

**HANDOFF.md 포함 내용**:
- 작업 목표: 무엇을 하고 있었는지
- 완료된 것: 이미 끝난 항목
- 진행 중인 것: 현재 하고 있던 작업과 상태
- 남은 것: 아직 안 한 항목
- 주의사항: 이어서 작업할 때 알아야 할 컨텍스트

**작업 완료 시**: 커밋 후 HANDOFF.md를 삭제한다.

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
