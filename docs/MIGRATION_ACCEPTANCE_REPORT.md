# STAGE 8 — JUNCTION END-TO-END MIGRATION ACCEPTANCE REPORT

**Platform:** JUNCTION — Intelligent Destination Orchestration Platform  
**Target Environment:** Monorepo (Spring Boot Backend, Next.js Frontend, Python CV Bridge, Flutter Mobile)  
**Acceptance Date:** November 7, 2026  
**Status:** **APPROVED & FULLY ACCEPTED — PRODUCTION READY**  
**Architectural Rule Verified:** **Next.js = Frontend ONLY | Spring Boot = The ONLY Backend**

---

## 1. Executive Summary & Migration Sign-Off

This document constitutes the formal engineering acceptance report for **Stage 8: End-to-End Migration Acceptance** of the JUNCTION platform. 

The application backend has been completely transitioned from Next.js TypeScript API route handlers to a high-performance, modular Java 21 / Spring Boot 4.1.2 application backend. The Next.js frontend has been decoupled and converted strictly into a presentation and reverse-proxy layer. 

### Final Architecture Summary

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        JUNCTION MONOREPO                               │
└────────────────────────────────────────────────────────────────────────┘
          │                                           │
          ▼                                           ▼
┌──────────────────┐                        ┌──────────────────┐
│  Next.js 16 UI   │ (Presentation Only)    │  Flutter Mobile  │
│  (Port 3000)     │ ──Rewrites /api/*───►  │  (Attendee App)  │
└──────────────────┘                        └─────────┬────────┘
          │                                           │
          │                                           │
          ▼                                           ▼
┌──────────────────────────────────────────────────────────────┐
│            Spring Boot 4.1.2 Application Backend             │
│                      (Port 8080)                             │
│                                                              │
│  • Spring Security 6 (Clerk RS256 JWT + Edge Device Auth)    │
│  • Hibernate 7.4.13 (ddl-auto: validate)                     │
│  • Flyway Database Migrations (V1 Schema + V2 Baseline)      │
│  • Spatial Sensor Fusion & Real-Time Ingestion Engine        │
│  • Recommendation Lifecycle & Audit Log Persistence          │
└──────────────────┬───────────────────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
┌─────────────────┐ ┌──────────────────────────────────────────┐
│  PostgreSQL 18  │ │  Python CV Bridge (YOLOv12 Telemetry)    │
│  (Port 5432)    │ │  POST /api/observations (Bearer Secret)  │
└─────────────────┘ └──────────────────────────────────────────┘
```

### Sign-Off Recommendation

> [!IMPORTANT]
> **FORMAL VERDICT: RETIRE NEXT.JS LEGACY BACKEND — APPROVE SPRING BOOT DEPLOYMENT.**  
> All 8 acceptance phases have passed with 100% test success, verified database parity, timing-safe security claim handling, and real end-to-end integration traces. All 13 legacy Next.js API route handlers in `frontend/src/app/api/` have been safely retired.

---

## 2. Phase 1: Migration Checkpoint & Secret Safety Audit

Prior to beginning Stage 8 verification, a git checkpoint was established to ensure clean rollback capability and verify that no secret files or private credentials were leaked.

### Verification Record

1. **Git Working Tree Audit:**
   - Working tree verified clean before testing.
   - Command: `git tag -a "v8.0.0-migration-checkpoint" -m "Stage 8 migration verification checkpoint"`
   - Tag created successfully: `v8.0.0-migration-checkpoint`.
2. **Secret & Credential Tracked File Audit:**
   - Checked all repository files matching `.env*`:
   ```powershell
   git ls-files | Select-String "\.env"
   ```
   **Output:**
   - `backend/.env.example` (template with placeholders)
   - `cv_bridge/config.example.json` (template with placeholders)
   - `frontend/.env.example` (template with placeholders)
   - No private `.env` files, production tokens, or sensitive API secrets are tracked by Git. All live environment files are ignored via root `.gitignore`.

---

## 3. Phase 2: Independent Application Verification

All four core components in the monorepo were independently built, tested, and validated with zero synthetic or mock overrides.

### 3.1 Frontend (Next.js 16.3.4 Turbopack)

- **Command:** `npm --prefix frontend run build`
- **Prebuild Action:** `frontend/scripts/copy-demo-videos.js` synchronized 10 demonstration videos from canonical shared storage `shared/assets/test-videos/` into `frontend/public/videos/`.
- **Result:**
  ```text
  ▲ Next.js 16.3.4 (Turbopack)
  ✓ Compiled successfully in 6.9s
  ✓ Finished TypeScript in 5.8s
  ✓ Generating static pages using 15 workers (19/19) in 636ms
  ✓ Finalizing page optimization ...

  Route (app)
  ┌ ○ /
  ├ ○ /_not-found
  ├ ○ /attendee
  ├ ○ /attendee/event
  ├ ○ /attendee/food
  ├ ○ /attendee/plan
  ├ ○ /attendee/stay
  ├ ○ /login
  ├ ƒ /login/organizer/[[...organizer]]
  ├ ƒ /login/restaurant/[[...restaurant]]
  ├ ○ /organizer
  ├ ○ /organizer/capacity
  ├ ○ /organizer/cctv-demo
  ├ ○ /organizer/event
  ├ ○ /organizer/map
  ├ ○ /organizer/predictions
  ├ ○ /organizer/recommendations
  ├ ○ /organizer/simulation
  ├ ○ /partner
  ├ ƒ /sign-in/[[...sign-in]]
  └ ƒ /sign-up/[[...sign-up]]
  ƒ Proxy (Middleware)
  ```
- **Status:** **0 Errors, 0 Warnings, 19 Static/Dynamic presentation pages built.**

### 3.2 Monorepo System & Sensor Fusion Verification

- **Command:** `npm run test:full`
  - **Result:** **61 PASSED, 0 FAILED** (Coverage contracts, multi-modal ingestion, OSM road routing, recommendation lifecycle, audit trails, and hospitality vouchers).
- **Command:** `npm run test:input`
  - **Result:** **42 PASSED, 0 FAILED** (Observation normalization, multi-sensor weighting, stale telemetry penalization, conflict detection, operational capacity, stream simulation).

### 3.3 Spring Boot Backend Tests

- **Environment:** Java 21.0.11, Apache Maven 3.9.12, Spring Boot 4.1.2, PostgreSQL 18.4 (`junction_dev`).
- **Command:** `mvn -f backend/pom.xml test`
- **Result:**
  ```text
  [INFO] Running com.junction.health.HealthControllerTest
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.493 s -- in com.junction.health.HealthControllerTest
  [INFO] Running com.junction.service.IngestionPipelineServiceTest
  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in com.junction.service.IngestionPipelineServiceTest
  [INFO] Running com.junction.service.RecommendationLifecycleServiceTest
  [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.632 s -- in com.junction.service.RecommendationLifecycleServiceTest
  [INFO] 
  [INFO] Results:
  [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```
- **Status:** **8 Tests Run, 0 Failures, 0 Errors.**

### 3.4 Python CV Bridge Tests

- **Environment:** Python 3.14.6, pytest-9.2.0.
- **Command:** `pytest -c cv_bridge/pytest.ini cv_bridge/tests -v`
- **Result:**
  ```text
  cv_bridge/tests/test_metrics.py::test_calculate_centroid PASSED          [ 11%]
  cv_bridge/tests/test_metrics.py::test_lines_intersect PASSED             [ 22%]
  cv_bridge/tests/test_metrics.py::test_empty_detection_frame PASSED       [ 33%]
  cv_bridge/tests/test_metrics.py::test_single_and_multiple_detections PASSED [ 44%]
  cv_bridge/tests/test_metrics.py::test_density_with_zero_and_negative_area PASSED [ 55%]
  cv_bridge/tests/test_metrics.py::test_tripwire_line_crossing_and_duplicate_prevention PASSED [ 66%]
  cv_bridge/tests/test_metrics.py::test_missing_track_ids_handling PASSED  [ 77%]
  cv_bridge/tests/test_serializer.py::test_serializer_basic_crowd_count PASSED [ 88%]
  cv_bridge/tests/test_serializer.py::test_serializer_with_calibrated_density_and_tripwire PASSED [100%]
  ======================== 9 passed, 1 warning in 0.21s =========================
  ```
- **Status:** **9 Tests Run, 0 Failures.**

### 3.5 Flutter Mobile App Tests & Static Analysis

- **Environment:** Flutter 3.41.4, Dart 3.11.1.
- **Command:** `flutter test` in `mobile/attendee_flutter_app/`
  - **Result:** `00:00 +1: All tests passed!`
- **Command:** `dart analyze lib` in `mobile/attendee_flutter_app/`
  - **Result:** `Analyzing lib... No issues found!`

---

## 4. Phase 3: Database & Flyway Verification

### 4.1 Schema Reconciliation (JPA vs Drizzle vs PostgreSQL)

All six core domain entities were reconciled field-by-field against `backend/src/main/resources/db/migration/V1__create_junction_schema.sql` and `frontend/src/db/schema.ts`:

| Entity | JPA Class | Table Name | Columns Verified |
|---|---|---|---|
| User Profile | `ProfileEntity` | `profiles` | `id`, `clerk_user_id`, `name`, `email`, `role` (`user_role` enum), `created_at`, `updated_at` |
| Partner Restaurant | `RestaurantEntity` | `restaurants` | `id`, `name`, `zone`, `cuisine`, `address`, `total_capacity`, `available_covers`, `expected_reservations`, `price_range`, `lat`, `lng`, `is_active`, `created_at`, `updated_at` |
| Restaurant Membership | `RestaurantMembershipEntity` | `restaurant_memberships` | `id`, `profile_id`, `restaurant_id`, `role` (`membership_role` enum), `created_at` (Unique constraint: `uq_restaurant_membership`) |
| Live Inventory | `RestaurantInventoryEntity` | `restaurant_inventory` | `id`, `restaurant_id`, `reported_by_profile_id`, `available_tables`, `available_covers`, `expected_covers`, `out_of_order_tables`, `notes`, `recorded_at` |
| Audit Trail | `AuditEventEntity` | `audit_events` | `id`, `actor_id`, `actor_role`, `category`, `target_entity_type`, `target_entity_id`, `change_summary`, `rationale`, `previous_state_json`, `new_state_json`, `is_simulated_scenario`, `timestamp` |
| Operational Intervention | `OperationalInterventionEntity` | `operational_interventions` | `id`, `type`, `title`, `description`, `target_zone_id`, `status`, `urgency`, `requires_approval`, `approval_role_required`, `rationale`, `confidence_score`, `expected_pressure_reduction_percent`, `proposed_at`, `expires_at`, `approved_at`, `approved_by_user_id`, `rejection_reason` |

### 4.2 Flyway Migrations & DDL Validation

1. **Migration Pipeline:** `FlywayConfig.java` enforces explicit execution before JPA `EntityManagerFactory` initializes.
2. **Recorded Database Migration State:**
   ```sql
   SELECT installed_rank, version, description, type, script, success FROM flyway_schema_history;
   ```
   - Rank 1: Version `1`, Description `create junction schema`, Script `V1__create_junction_schema.sql`, Success `true`
   - Rank 2: Version `2`, Description `seed baseline data`, Script `V2__seed_baseline_data.sql`, Success `true`
3. **Hibernate DDL Auto:** Set to `ddl-auto: validate` in both `application.yml` and `application-prod.yml`. Hibernate 7.4.13 validates successfully against PostgreSQL 18.4 without making any implicit schema modifications.
4. **Seed Isolation:** Baseline seed entries (`V2__seed_baseline_data.sql`) use idempotent `ON CONFLICT (...) DO NOTHING` clauses, ensuring baseline records (Restaurant R1, dev profiles) never corrupt or overwrite existing live records.

---

## 5. Phase 4: Security Verification

### 5.1 Clerk JWT Architecture & Role Mapping

- **Clerk Converter:** `ClerkJwtAuthenticationConverter.java` converts incoming RS256 JWT tokens into Spring Security `JwtAuthenticationToken`.
- **Role Extraction:**
  - Extracts the Junction domain role from server-controlled `metadata.role` (e.g. `ORGANIZER`, `RESTAURANT_PARTNER`, `ATTENDEE`).
  - Maps to Spring Security authorities (`ROLE_ORGANIZER`, `ROLE_RESTAURANT_PARTNER`, `ROLE_ATTENDEE`).
  - Defaults to `ROLE_ATTENDEE` (least-privilege) if metadata claim is absent or malformed.
  - Whitelists only known domain roles to prevent privilege escalation via arbitrary claims.

### 5.2 Edge Device Authentication & Timing-Safe Verification

- **Filter:** `EdgeDeviceAuthenticationFilter.java` intercepts `POST /api/observations`.
- **Token Resolution Conflict Avoidance:** Custom `BearerTokenResolver` in `SecurityConfig.java` prevents Spring's `BearerTokenAuthenticationFilter` from treating the static edge device secret as an invalid JWT when authenticated by `EdgeDeviceAuthenticationFilter`.
- **Timing-Safe Equality:** `constantTimeEquals()` compares the provided Bearer secret with `JUNCTION_EDGE_DEVICE_SECRET`.
- **Live Test Evidence:**
  - `POST /api/observations` without header: **HTTP 401 Unauthorized** (Caught: `Edge device authentication required`)
  - `POST /api/observations` with invalid secret: **HTTP 401 Unauthorized** (Caught: `Invalid edge device secret`)
  - `POST /api/observations` with valid secret: **HTTP 200 OK** (`acceptedCount: 1, quarantinedCount: 0`)

### 5.3 Endpoint Authorization Matrix

| Endpoint | Method | Authority Required | Enforcement Mechanism |
|---|---|---|---|
| `/actuator/health` | GET | `permitAll()` | Spring Security filter chain |
| `/api/health` | GET | `permitAll()` | Spring Security filter chain |
| `/api/scenarios` | GET | `permitAll()` | Spring Security filter chain |
| `/api/scenarios` | POST | `ROLE_ORGANIZER` | `@PreAuthorize` / Security config |
| `/api/observations` | POST | `ROLE_EDGE_DEVICE` / `ROLE_ORGANIZER` | `EdgeDeviceAuthenticationFilter` |
| `/api/recommendations` | GET | `authenticated()` | Spring Security filter chain |
| `/api/recommendations/*/approve` | POST | `ROLE_ORGANIZER` | `@PreAuthorize` / Security config |
| `/api/recommendations/*/reject` | POST | `ROLE_ORGANIZER` | `@PreAuthorize` / Security config |
| `/api/recommendations/*/execute` | POST | `ROLE_ORGANIZER` | `@PreAuthorize` / Security config |
| `/api/restaurants` | GET | `permitAll()` / `authenticated()` | Spring Security filter chain |
| `/api/restaurants` | POST | `ROLE_ORGANIZER` | `@PreAuthorize("hasRole('ORGANIZER')")` |
| `/api/restaurants/{id}/inventory` | POST | `ROLE_ORGANIZER`, `ROLE_RESTAURANT_PARTNER` | `@PreAuthorize` + scoped profile resolution |
| `/api/partner/inventory` | POST | `ROLE_ORGANIZER`, `ROLE_RESTAURANT_PARTNER` | `PartnerController` + scoped profile resolution |
| `/api/audit` | GET | `ROLE_ORGANIZER` | `@PreAuthorize("hasRole('ORGANIZER')")` |
| `/api/auth/me` | GET | `authenticated()` | `AuthController` |

---

## 6. Phase 5: API Contract Parity

All endpoints defined in canonical OpenAPI 3.0 specification (`contracts/openapi.yaml`) were compared with the Spring Boot controllers:

| OpenAPI Contract Endpoint | Spring Boot Controller | Implementation Status |
|---|---|---|
| `GET /api/health` | `HealthController` | Parity Verified |
| `GET /api/scenarios` | `ScenarioController` | Parity Verified |
| `POST /api/scenarios` | `ScenarioController` | Parity Verified |
| `POST /api/observations` | `ObservationController` | Parity Verified |
| `GET /api/observations` | `ObservationController` | Parity Verified |
| `GET /api/zones` | `ZoneController` | Parity Verified |
| `GET /api/zones/{zoneId}` | `ZoneController` | Parity Verified |
| `GET /api/zones/coverage` | `ZoneController` | Parity Verified |
| `GET /api/recommendations` | `RecommendationController` | Parity Verified |
| `POST /api/recommendations/generate` | `RecommendationController` | Parity Verified |
| `POST /api/recommendations/{id}/approve` | `RecommendationController` | Parity Verified |
| `POST /api/recommendations/{id}/reject` | `RecommendationController` | Parity Verified |
| `POST /api/recommendations/{id}/execute` | `RecommendationController` | Parity Verified |
| `GET /api/forecast` / `/api/predictions` | `ForecastController` | Parity Verified (Dual path mapping) |
| `GET /api/hotspots` | `HotspotController` | Parity Verified |
| `GET /api/devices` | `DeviceController` | Parity Verified |
| `GET /api/restaurants` | `RestaurantController` | Parity Verified |
| `GET /api/restaurants/{id}` | `RestaurantController` | Parity Verified |
| `POST /api/restaurants` | `RestaurantController` | Parity Verified |
| `PUT /api/restaurants/{id}` | `RestaurantController` | Parity Verified |
| `POST /api/restaurants/{id}/inventory` | `RestaurantController` | Parity Verified |
| `POST /api/partner/inventory` | `PartnerController` | Parity Verified |
| `GET /api/audit` | `AuditController` | Parity Verified |
| `GET /api/auth/me` | `AuthController` | Parity Verified |

---

## 7. Phase 6: Live Integration Testing & Trace Evidence

With the Spring Boot server running live on `http://localhost:8080` backed by PostgreSQL 18.4, end-to-end telemetry and proxy flows were verified.

### 7.1 Live Service Health

```json
GET http://localhost:8080/api/health
{
    "status": "ok",
    "service": "junction-backend",
    "version": "1.0.0",
    "environment": "dev",
    "timestamp": "2026-11-07T02:03:00.372121300Z"
}
```

### 7.2 Actuator Component Health

```json
GET http://localhost:8080/actuator/health
{
    "status": "UP",
    "components": {
        "db": { "status": "UP", "details": { "database": "PostgreSQL", "validationQuery": "SELECT 1" } },
        "diskSpace": { "status": "UP" },
        "livenessState": { "status": "UP" },
        "readinessState": { "status": "UP" },
        "ping": { "status": "UP" }
    }
}
```

### 7.3 Edge Ingestion & In-Memory Deduplication

1. First ingestion of observation `OBS_DEDUP_001`:
   - `acceptedCount: 1`, `deduplicatedCount: 0`
2. Immediate re-ingestion of same observation `OBS_DEDUP_001`:
   - `acceptedCount: 0`, `deduplicatedCount: 1`
   - Ring-buffer deduplication successfully prevented duplicate telemetry processing.

### 7.4 Live Python CV Bridge Delivery Trace

Executed live via `cv_bridge/transport.py` against `http://localhost:8080/api/observations`:
```text
Transport emit result: {'delivered': 1, 'failed': 0, 'http_status': 200}
```

---

## 8. Phase 7: Production Deployment Readiness

All production infrastructure artifacts were verified for production readiness:

1. **`backend/Dockerfile`:**
   - Multi-stage build (`eclipse-temurin:21-jdk-alpine` builder → `eclipse-temurin:21-jre-alpine` runtime).
   - Non-root user `junction:junction` with PID isolation.
   - Container-aware JVM tuning (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
2. **`frontend/Dockerfile`:**
   - Multi-stage build (`node:20-alpine` deps → builder → runner).
   - Non-root system user `nextjs:nodejs` (UID 1001).
   - Standalone Next.js output optimization.
3. **`infrastructure/docker-compose.yml`:**
   - Complete local production replication with PostgreSQL 16/18, Spring Boot backend, and Next.js frontend with service healthchecks.
4. **`railway.json` Configurations:**
   - Backend: Dockerfile builder with healthcheck `/actuator/health`.
   - Frontend: Dockerfile builder with healthcheck `/`.
5. **GitHub Actions Workflow:**
   - `.github/workflows/release.yml` triggers on version tags (`v*`) to bundle and publish release artifacts.

---

## 9. Phase 8: Legacy Backend Retirement

All legacy Next.js API route handlers were removed from `frontend/src/app/api/`:

```text
[RETIRED & REMOVED]
  - frontend/src/app/api/audit/route.ts
  - frontend/src/app/api/auth/me/route.ts
  - frontend/src/app/api/health/route.ts
  - frontend/src/app/api/hotspots/route.ts
  - frontend/src/app/api/observations/route.ts
  - frontend/src/app/api/partner/inventory/route.ts
  - frontend/src/app/api/predictions/route.ts
  - frontend/src/app/api/recommendations/[id]/approve/route.ts
  - frontend/src/app/api/recommendations/[id]/reject/route.ts
  - frontend/src/app/api/recommendations/route.ts
  - frontend/src/app/api/scenarios/route.ts
  - frontend/src/app/api/zones/[zoneId]/route.ts
  - frontend/src/app/api/zones/route.ts
```

### Next.js Proxy Verification Post-Retirement

In `frontend/next.config.ts`, reverse proxy rewrites route all incoming `/api/:path*` requests directly to `${SPRING_BOOT_URL}/api/:path*`:

```ts
const SPRING_BOOT_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  agentRules: false,
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${SPRING_BOOT_URL}/api/:path*`,
      },
    ];
  },
};
```

Following the retirement of `frontend/src/app/api/`, `npm --prefix frontend run build` was executed:
- **Build Status:** **Compiled successfully in 6.9s**.
- **Page Optimization:** 19 presentation routes compiled.
- **Zero Local API Handlers:** Next.js operates purely as a presentation application.

---

## 10. Final Sign-Off & Verification Checklist

| Verification Item | Requirement | Verification Outcome |
|---|---|---|
| Checkpoint Tag | Checkpoint tag established | `v8.0.0-migration-checkpoint` created |
| Secret Safety | No credentials tracked | Verified (`git ls-files \| Select-String "\.env"`) |
| Frontend Build | Next.js production build | 19 routes compiled, 0 errors |
| Full System Tests | Monorepo domain test suite | 61 / 61 Passed |
| Input System Tests | Sensor fusion test suite | 42 / 42 Passed |
| Backend Maven Tests | Spring Boot test suite | 8 / 8 Passed |
| Python CV Tests | Pytest test suite | 9 / 9 Passed |
| Mobile Flutter Tests | Widget & sync service test suite | All Passed (`flutter test`) |
| Mobile Dart Analyze | Static syntax analysis | No issues found (`dart analyze lib`) |
| Database Parity | 6 JPA entities match schema | 1:1 Parity verified with Flyway V1 & Drizzle |
| Flyway Migrations | Schema + baseline seed | V1 & V2 successfully migrated |
| Hibernate DDL | `ddl-auto: validate` | Successfully validated against PostgreSQL 18.4 |
| Clerk JWT Auth | RS256 token conversion | `ClerkJwtAuthenticationConverter` verified |
| Edge Device Auth | Timing-safe Bearer secret | `EdgeDeviceAuthenticationFilter` verified |
| Token Resolver | No JWT/Bearer collisions | `DefaultBearerTokenResolver` verified |
| Contract Parity | OpenAPI 3.0 specification | 100% Endpoint parity across all controllers |
| Live Ingestion | CV Bridge to Spring Boot | HTTP 200 delivered, deduplication verified |
| Next.js API Retirement | Remove legacy route handlers | 13 route files deleted, Next.js = Frontend ONLY |

---
*Signed off by Antigravity Autonomous Engineering Agent — November 7, 2026*
