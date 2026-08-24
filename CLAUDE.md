# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Castify is a Kotlin/Spring Boot backend for a talent casting marketplace: clients post collaborations and rate talents, talents maintain searchable profiles with portfolios, and the two sides message each other. Backend only — no frontend lives in this repo.

Stack: Kotlin 2.2.21 on Java 21, Spring Boot 4.0.0, PostgreSQL 17, Flyway, JWT (jjwt), springdoc-openapi, JUnit 5 + MockK, Maven multi-module.

## Commands

```bash
./mvnw clean install                  # build all modules + run all tests (what CI runs, with -B)
./mvnw test                           # all tests
./mvnw test -pl castify-usecase       # one module
./mvnw test -pl castify-usecase -Dtest=SearchTalentsUseCaseTest      # one test class
./mvnw test -pl castify-usecase -Dtest=SearchTalentsUseCaseTest#'method name'
./mvnw spring-boot:run -pl castify-app                               # run (local profile by default)
SPRING_PROFILES_ACTIVE=develop ./mvnw spring-boot:run -pl castify-app
docker compose up -d postgres         # only Postgres is in docker-compose.yml
```

Swagger UI at `http://localhost:8080/swagger-ui.html` (local/develop only; disabled in production). OpenAPI JSON at `/v3/api-docs`.

`Castify_API.postman_collection.json` at the repo root is a working request collection, but like the README it drifts from the code — verify routes against the controllers.

## Architecture

Five Maven modules enforcing clean/hexagonal architecture. The dependency direction is enforced by the POMs — respect it:

```
castify-app  ──▶ castify-web ──▶ castify-usecase ──▶ castify-domain
      └────────▶ castify-persistence ──▶ castify-domain
```

`castify-web` does **not** see `castify-persistence`; `castify-app` is the only module that wires both. `castify-domain` has no Spring dependency beyond stdlib.

### The pattern every feature follows

1. **Domain** (`castify-domain`) — immutable Kotlin `data class` aggregates (`TalentProfile`, `Collaboration`, `TalentRating`, …). Invariants live in `init { require(...) }` blocks; state transitions are `copy()`-returning methods (`accept()`, `updateTags()`, `verify()`). Each aggregate ships a repository **interface** in the same package (`TalentRepository`, `CollaborationRepository`) — this is the port.
2. **Use case** (`castify-usecase`) — one `@Service` class per operation, named `<Verb><Noun>UseCase` with a single `execute(...)`. Depends only on domain interfaces. Request/response DTOs live in `usecase/dto/` with `fromDomain()` factories. `usecase/port/TokenService.kt` is a port implemented by the web layer's `JwtService`.
3. **Persistence** (`castify-persistence`) — `entity/*Entity.kt` are separate mutable JPA classes with `fromDomain()`/`toDomain()` mapping, `jpa/*JpaRepository.kt` are Spring Data interfaces, and `adapter/*RepositoryAdapter.kt` are the `@Component`s implementing the domain ports. Domain classes are never JPA-annotated.
4. **Web** (`castify-web`) — thin `@RestController`s that pull the caller from `SecurityUtils` and delegate to a use case. No business logic.

Adding a feature means touching all four layers in that order. Adapters that update an aggregate with collections (see `TalentRepositoryAdapter.save`) reload the existing entity and mutate its collections in place rather than replacing it — this preserves the `@Version` field and avoids optimistic-locking failures.

### Authentication

`JwtAuthenticationFilter` validates the `Bearer` token and sets an `Authentication` whose **principal is the raw `UUID`** of the user, with a single `ROLE_<UserRole>` authority. Controllers read it via `SecurityUtils.getCurrentUserId()` / `getCurrentUserRole()`, which throw `UnauthorizedException` (→ 401) when the principal isn't a UUID — on an anonymous request Spring substitutes the string `"anonymousUser"`, so these must never blind-cast. There is no `UserDetailsService` and no session.

`JwtAuthenticationEntryPoint` makes anonymous requests to protected routes return **401**, not Spring's default 403, with the same JSON shape as `GlobalExceptionHandler`. It hand-writes that JSON on purpose: Spring Boot 4 auto-configures Jackson 3's `tools.jackson.databind.ObjectMapper` while Jackson 2 is also on the classpath via `jjwt-jackson`, so injecting `ObjectMapper` by type is ambiguous.

Public endpoints are whitelisted by path in `SecurityConfig.securityFilterChain` — **a new public route must be added there or it 401s**. The whitelist is path-pattern based and does not always match controller structure (e.g. `/api/talents/*` is public but `/api/talents/my-profile` is therefore also matched by it). Note `/api/upload/**` is whitelisted **for GET only**: the upload POSTs resolve the caller through `SecurityUtils` and must stay authenticated. Role gating uses `@PreAuthorize("hasRole('CLIENT')")` on controller methods (`@EnableMethodSecurity` is on).

### Errors

Domain throws from the `DomainException` sealed hierarchy (`ResourceNotFoundException`, `BadRequestException`, `ConflictException`, `ForbiddenException`, `ValidationException`, `DatabaseException`, …), each carrying an `errorCode`. `GlobalExceptionHandler` maps them to `ErrorResponse`/`ValidationErrorResponse` with status, errorCode, path, timestamp, and a correlation ID taken from `X-Correlation-ID`/`X-Request-ID` or generated. Use these exceptions rather than raw `ResponseStatusException`.

Two traps when adding error handling: a controller-scoped `@ExceptionHandler` **takes precedence over** `GlobalExceptionHandler`, so a local catch-all silently flattens every domain status into 500 (this is why `FileUploadController` deliberately has none); and a broad `catch (e: Exception)` inside a handler method does the same thing, so rethrow `DomainException` explicitly before any generic catch.

### Statistics — read before touching ratings or collaborations

`TalentStatistics` (rating averages, collaboration counts, success rate, earnings, profile completeness, `TalentTier`) used to be maintained by Postgres triggers. Migration `V20__remove_statistics_triggers.sql` deleted them because they caused optimistic-locking conflicts. All of it now lives in `TalentStatisticsService` (`castify-usecase/rating`), called from within the same `@Transactional` unit as the rating/collaboration write. **Do not reintroduce DB triggers for statistics.** `TalentProfileEnricher` is the component that merges statistics onto `TalentProfileResponse`, including a batch path for search results.

A rating is only `isVerified` (and therefore counted in averages) when it is linked to a `collaborationId`.

### Talent search

`POST /api/talents/search` with a JSON body — not a GET with query params, despite what the README says. `TalentRepositoryAdapter.search` uses the JPA Criteria API only for verification status and boolean capabilities; **language, tag, Haversine-radius filtering, and pagination all happen in memory** over the full result set. This is a known scalability limit — keep it in mind before adding filters, and prefer pushing new predicates into the Criteria query.

Tags are not free text: `PredefinedTags` in `castify-domain/talent/TagCategory.kt` is the source of truth, split into `PRIMARY`/`SECONDARY` categories, validated in `TalentProfile.updateTags()`, and served to clients via `GET /api/talents/available-tags`.

### Database & migrations

Migrations live in `castify-app/src/main/resources/db/migration/` as `V<n>__description.sql` and are currently at V22. `FlywayConfig` in `castify-app` builds the `Flyway` bean **manually and runs `migrate()` inside the bean factory method** — Spring Boot's autoconfiguration was unreliable in this multi-module setup. Hibernate `ddl-auto` is `none`, so schema changes only ever come from a new migration file. `open-in-view` is off; anything needing a collection must fetch it explicitly (see `findByUserIdWithCollections`).

### File uploads

`FileUploadController` (`/api/upload/**`, entirely `permitAll`) stores files under `${file.upload.dir}` (default `uploads/`) in `portfolio/images`, `portfolio/videos`, `portfolio/audio`, etc., and serves them back through `GET /api/upload/{subdir}/{filename}`. The stored `fileUrl` is that API path, not a filesystem path. Note the per-type limits inside the upload use cases (up to 100MB for portfolio video) are larger than `spring.servlet.multipart.max-file-size` (10MB) in `application.yml` — raise the multipart limit if large uploads need to work.

### Profiles

`local` (default, localhost Postgres, DEBUG logging, Swagger on, `flyway.clean` allowed), `develop` (env-driven DB, Swagger on, deployed automatically), `production` (all of `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`/`JWT_SECRET`/`ALLOWED_ORIGINS` required, Swagger and api-docs off). `castify-app/PROFILES.md` has the per-profile detail; `.env.*.example` files list the variables. Note the `local` profile defaults `DB_USERNAME` to `lazarmarkovic` with an empty password.

## Conventions & gotchas

- Kotlin compiler plugins `spring` (all-open) and `jpa` (no-arg) are configured in the parent POM — that's why JPA entity classes need no explicit `open`/no-arg constructor.
- Source roots are `src/main/kotlin` / `src/test/kotlin` (set in the parent POM); there are no Java sources.
- Tests are pure unit tests with MockK — no Spring context, no Testcontainers, no database. `castify-usecase/src/test/.../TestHelpers.kt` holds shared fixtures. There is no integration-test layer, so multi-layer wiring is only exercised by running the app.
- **The README is substantially out of date.** It documents removed features (social links, `talent_social_link`), old endpoint shapes (`/api/talents/me/*`, GET-based search), and a `LocationMode` set that no longer matches the code (`REMOTE`/`SPECIFIC`/`HYBRID`, with `PrimaryLocation` required for the latter two). Treat the controllers, domain classes, and migrations as the source of truth, and prefer updating the README when you change behaviour it describes.

## CI/CD

`.github/workflows/backend-tests.yml` runs `./mvnw clean install -B` on PRs to `main` and `develop`.

`.github/workflows/deploy-develop.yml` fires on every push to `develop`: builds the `Dockerfile`, pushes to `ghcr.io/<repo>:sha-<short>` and `:develop`, then rolls the Hetzner stack over via `docker compose pull && up -d` and blocks until `/actuator/health` returns 200 through Caddy. **Pushing to `develop` deploys.** A manual run with the `image_tag` input skips the build and redeploys an existing tag — that's the rollback path.

The server runs three containers (`caddy` → `app` → `db`) defined by `deploy/docker-compose.prod.yml`, which CI re-copies on every deploy, so the box cannot drift from what's committed. `deploy/bootstrap-server.sh` provisions a fresh Ubuntu box; `deploy/README.md` is the full runbook. Secrets live in `/opt/castify/.env` on the server (generated, not in git). Required GitHub secrets: `HETZNER_SERVER_IP`, `HETZNER_SSH_KEY`.
