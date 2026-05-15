# CLAUDE.md

Behavioral guidelines and codebase reference for AI assistants working on this project.

---

## Behavioral Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

## Project Overview

Spring Boot 3.5 survey management platform for Woongjin Group. Employees receive survey links via email and complete them in a lightweight iFrame; admins view aggregated statistics and export Excel reports. Two distinct security contexts coexist: an employee JWT chain and a short-lived client token chain for survey participants.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 (Jakarta EE) |
| ORM (primary) | JPA / Hibernate 6.6+ |
| ORM (queries) | QueryDSL 6.10.1 (openfeign fork for Hibernate 6.6 compat) |
| ORM (complex SQL) | MyBatis 3.0.5 with XML mappers |
| Caching/Sessions | Spring Data Redis |
| Batch | Spring Batch (chunk-oriented statistics aggregation) |
| Excel export | Apache POI 5.3.0 (SXSSF streaming) |
| View | Thymeleaf 3 + Alpine.js (static JS) |
| Auth tokens | JJWT 0.12.6 |
| Build | Gradle 8.14.4 |
| DB (local) | MariaDB on port 3306 |
| DB (prod) | MySQL |
| Env loading | spring-dotenv 4.0.0 (reads `.env`) |

---

## Repository Structure

```
src/
├── main/
│   ├── java/com/woongjin/survey/
│   │   ├── SurveyApplication.java          # Entry point (@EnableScheduling)
│   │   ├── domain/                         # Business domains
│   │   │   ├── auth/                       # JWT login/logout/refresh
│   │   │   ├── employee/                   # Employee master data
│   │   │   ├── member/                     # Legacy member support
│   │   │   ├── noti/                       # Notification history
│   │   │   ├── statistics/                 # Analytics, batch jobs, Excel export
│   │   │   └── survey/                     # Survey CRUD, questions, answers
│   │   └── global/                         # Cross-cutting concerns
│   │       ├── config/                     # Batch, QueryDSL, DataInitializer
│   │       ├── cookie/                     # Cookie utilities
│   │       ├── exception/                  # GlobalExceptionHandler, BusinessException
│   │       ├── filter/                     # ClientTokenFilter
│   │       ├── jpa/                        # BaseEntity (auditing)
│   │       ├── jwt/                        # JwtTokenProvider, JwtAuthenticationFilter
│   │       ├── redis/                      # Redis config
│   │       ├── response/                   # ApiResponse wrapper
│   │       └── security/                   # Dual SecurityFilterChain config
│   └── resources/
│       ├── application.properties          # Base config (active profile: local)
│       ├── application-local.properties    # Local dev (MariaDB, DEBUG logs)
│       ├── application-prod.properties     # Production (MySQL, INFO logs)
│       ├── sql/
│       │   ├── schema.sql                  # DDL (auto-run on startup)
│       │   └── data.sql                    # Seed data
│       ├── mapper/                         # MyBatis XML mappers
│       │   ├── SurveyMapper.xml
│       │   └── MemberMapper.xml
│       ├── static/
│       │   ├── js/api.js                   # Client-side fetch helpers
│       │   └── css/                        # common, login, response styles
│       └── templates/                      # Thymeleaf HTML
│           ├── auth/                       # Login pages
│           ├── survey/                     # Survey form, intro, responses
│           ├── statistics/                 # Analytics dashboard
│           └── fragments/                  # Reusable template partials
└── test/
    └── java/com/woongjin/survey/
        ├── global/jwt/JwtTokenProviderTest.java
        ├── domain/auth/service/AuthServiceTest.java
        ├── domain/auth/infra/RedisTokenRepositoryTest.java
        └── domain/statistics/excel/sheet/SummarySheetWriterTest.java
```

---

## Domain Model

| Entity | Table | Description |
|---|---|---|
| Survey | SVY_BSS_TB | Metadata, period, target audience (age/gender/role) |
| Employee | EMP_TB | Login credentials, department, position, role (ADMIN/USER/VIEWER) |
| Question / QuestionItem | — | Survey content with conditional branching (QuestionBranch) |
| Answer | — | Structured JSON responses |
| SurveyTargetPerson | — | Target audience registry |
| SurveyParticipateStatus | — | Completion tracking |
| QuestionStat | — | Pre-aggregated statistics (populated by batch) |
| ExcelDownloadHist | — | Audit trail of Excel exports |

Soft delete is implemented via a `deletedDate` column (not physical DELETE).

---

## Architecture Patterns

### Layered + CQRS-Light
```
Controller → QueryService / CommandService → Repository → Entity
```
- `SurveyQueryService` handles reads; `SurveyCommandService` handles writes.
- Custom repositories follow `XxxRepositoryCustom` + `XxxRepositoryImpl` naming.

### Dual Security Chains (Spring Security order matters)

| Chain | Order | Paths | Auth Mechanism |
|---|---|---|---|
| Client | 1 | `/api/external/v1/thinkbig/surveys/**`, `/surveys/client/**` | `svy_client_token` cookie (ClientTokenFilter) |
| Employee | 2 | Everything else | `ACCESS_TOKEN` cookie (JwtAuthenticationFilter) |

- Client chain is stateless and allows iFrame embedding (`X-Frame-Options: ALLOWALL`).
- Employee chain auto-redirects to `/auth/login` on 401, checks `REFRESH_TOKEN` cookie first.

### Exception Handling
- `BusinessException` → HTTP 200 with `{ "success": false, "message": "..." }` — used for expected domain errors.
- System exceptions → standard 4xx/5xx.
- `@RestControllerAdvice` in `global/exception/` handles both.

### API Response Wrapper
All REST responses use:
```json
{ "success": true, "message": "...", "data": { ... } }
```

---

## Key API Endpoints

### Survey Participation (Client chain)
```
GET  /api/external/v1/thinkbig/surveys/check?empNo={empNo}   # Issue Redis survey token (requires X-Internal-Api-Key header)
GET  /surveys/client/intro                                    # Intro page; converts Redis token → Client JWT
GET  /api/external/v1/thinkbig/surveys/{surveyId}/questions  # Fetch survey questions
POST /api/external/v1/thinkbig/surveys/{surveyId}/submit     # Submit responses
PUT  /api/external/v1/thinkbig/surveys/{surveyId}/draft      # Save draft
```

### Statistics (Employee chain)
```
GET /api/internal/v1/surveys/{surveyId}/statistics/summary   # Basic stats
GET /api/internal/v1/surveys/{surveyId}/statistics/depts     # Org response rates
GET /api/internal/v1/surveys/{surveyId}/statistics/responses # Respondent list
GET /api/internal/v1/surveys/{surveyId}/statistics/questions # Per-question stats
GET /api/internal/v1/surveys/{surveyId}/statistics/export    # Excel download
```

### Auth (Employee chain)
```
POST /auth/login                                              # Form login → sets ACCESS_TOKEN + REFRESH_TOKEN cookies
POST /auth/logout                                             # Clears cookies + Redis tokens
GET  /auth/refresh-redirect?redirect=...                     # Auto-refresh on 401
POST /api/external/v1/admin/auth/reissue                     # Token refresh (API)
```

---

## Development Workflow

### Prerequisites
- Java 17
- MariaDB running on `localhost:3306` (root/1234)
- Redis running on `localhost:6379`
- `.env` file in project root (see below)

### Environment Variables (`.env`)
```
JWT_SECRET=<base64-encoded key>
JWT_ACCESS_TOKEN_EXPIRATION=1800000       # 30 min
JWT_REFRESH_TOKEN_EXPIRATION=604800000    # 7 days
CLIENT_SECRET=<base64-encoded key>
CLIENT_TOKEN_EXPIRATION=1800000
REDIS_HOST=localhost
REDIS_PORT=6379
INIT_ADMIN_ID=admin2
INIT_ADMIN_PW=1234
INIT_ADMIN_NAME=관리자2
INIT_ADMIN_EMAIL=admin@woongjin.com
INTERNAL_API_KEY=woongjin-internal-secret-2024
```

### Common Commands
```bash
./gradlew bootRun           # Start local server (port 8080)
./gradlew test              # Run all tests
./gradlew build             # Full build + tests
./gradlew clean compileJava # Clean then compile (clears QueryDSL build/generated/)
```

### Database Init
On startup, `schema.sql` runs automatically (`spring.sql.init.schema-locations`). `DataInitializer` creates the initial admin account if absent.

In local profile, `ddl-auto=update` — Hibernate will add missing columns. In prod, `ddl-auto=none`.

---

## Code Conventions

### Package Structure
```
domain/[feature]/
    controller/     # @RestController or @Controller
    service/        # Business logic (*QueryService, *CommandService)
    repository/     # Spring Data JPA + custom impl
    entity/         # @Entity classes
    dto/            # Request/Response DTOs
```

### Naming
- Entities: `PascalCase` with `@Table(name="UPPER_SNAKE_CASE_TB")`
- DTOs: `*Request`, `*Response`, `*ResponseDto`
- Services: `*Service`, `*QueryService`, `*CommandService`
- Repositories: `*Repository`, `*RepositoryCustom`, `*RepositoryImpl`

### Common Annotations
```java
@Slf4j                        // Lombok logger (use log.debug/info/warn/error)
@RequiredArgsConstructor      // Constructor injection (never @Autowired)
@Transactional                // On service methods that write
@RestControllerAdvice         // Global exception handlers
@StepScope                    // Spring Batch step-scoped beans
```

### QueryDSL
Use the `openfeign` fork (already in `build.gradle`) — the standard QueryDSL JAR is incompatible with Hibernate 6.6+. Custom query implementations extend `*RepositoryCustom` and live in `*RepositoryImpl`.

### Logging
- Local: DEBUG for `com.woongjin.survey`, Spring Security, MyBatis SQL
- Prod: INFO for app, WARN for security — don't add verbose logging to prod paths.

---

## Statistics Batch Job

`StatisticsBatchConfig` runs as a Spring Batch chunk job (size=100):
1. **Reader** (`@StepScope`): Fetches active survey IDs — re-evaluated each run.
2. **Processor**: Aggregates question responses → `QuestionStat` entities.
3. **Writer**: UPSERTs into the statistics table.

Job is triggered asynchronously (async `JobLauncher`). Auto-run is disabled in local profile (`spring.batch.job.enabled=false`).

---

## Excel Export

Uses Apache POI SXSSF (streaming) to handle large datasets without memory pressure. Produces a 4-sheet workbook:
1. Summary stats
2. Org response rates
3. Individual respondent answers
4. Per-question statistics

Filename uses RFC 5987 UTF-8 encoding for Korean filenames. Every export is recorded in `ExcelDownloadHist`.

---

## Testing Notes

- 4 test classes covering JWT, auth flows, Redis token CRUD, and Excel generation.
- No integration test database configured — tests use mocks or H2.
- Test organization uses `@Nested` classes for grouping related cases.
- No CI/CD pipeline — tests are run manually before pushing.

---

## What Does Not Exist (Don't Add Without Discussion)

- No Docker / docker-compose
- No GraphQL
- No frontend build pipeline (no npm/webpack) — JS/CSS are plain static files
- No GitHub Actions or other CI configuration
- No API versioning beyond the current `/v1/` prefix convention
