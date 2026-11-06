# PROJECT STRUCTURE AUDIT & ARCHITECTURAL SITEMAP
**Project:** JUNCTION — Intelligent Destination Orchestration Platform  
**Version:** 3.0.0 (Post-Stage 7 Physical Restructuring Monorepo)  
**Date:** 2026-11-06  
**Repository:** `C:\Github\Junction-App`  

---

## 1. Monorepo Overview

JUNCTION has transitioned from a hybrid monolithic structure into a strictly segregated, production-ready multi-platform monorepo:

```
Junction-App/
├── frontend/                 # Next.js 16 Presentation Frontend (React 19, Tailwind)
├── backend/                  # Spring Boot 4.1.2 Application Backend (Java 21, JPA, Flyway)
├── cv_bridge/                # Edge Computer Vision Subsystem (YOLOv12, ByteTrack, PyTorch)
├── mobile/                   # Cross-Platform Mobile Applications (Flutter)
│   └── attendee_flutter_app/
├── contracts/                # Canonical API Contracts & Reusable Schemas
│   ├── openapi.yaml
│   └── schemas/
├── infrastructure/           # Docker Compose, Railway configs, deployment guides
├── scripts/                  # Unified Repository Automation (testing, utilities, dev)
├── assets/                   # Shared Media & CCTV Validation Datasets
│   └── test-videos/
├── artifacts/                # Build outputs, inference logs
├── docs/                     # Authoritative Engineering Documentation
└── .github/                  # CI/CD Workflows
```

---

## 2. Directory-by-Directory Breakdown

### `frontend/`
- **Responsibility:** User presentation, operational maps, attendee showcase, role-based dashboards.
- **Technology:** Next.js 16.3.4 (App Router), React 19.2.8, TypeScript 5, Tailwind CSS, Leaflet, Cesium.
- **Security:** Clerk Authentication with official Next.js 16 `src/proxy.ts`.
- **Backend Communication:** Directly connects to Spring Boot backend (`http://localhost:8080`) through typed REST client `src/lib/api-client/junctionApi.ts` and Next.js proxy rewrites in `next.config.ts`.
- **Key Files:**
  - `src/app/` — Application pages (organizer `/`, partner `/partner`, attendee `/attendee`)
  - `src/components/` — Modular UI components (map, cctv, organizer, analytics)
  - `src/lib/api-client/` — Typed API client (`junctionApi.ts`)
  - `src/proxy.ts` — Clerk auth proxy middleware
  - `Dockerfile` — Multi-stage standalone production container

### `backend/`
- **Responsibility:** The sole persistent application backend for JUNCTION.
- **Technology:** Java 21 LTS, Spring Boot 4.1.2, Spring Security 6, Spring Data JPA / Hibernate, Flyway 10+, Caffeine Cache, SpringDoc OpenAPI 3.
- **Port:** 8080 (avoids collision with Next.js port 3000).
- **Key Modules:**
  - `com.junction.controller` — 10 REST controllers exposing all platform APIs
  - `com.junction.service` — 12 domain engines (ZoneRegistry, Ingestion, Fusion, Forecast, Hotspot/Cascade, Lifecycle, Routing)
  - `com.junction.entity` — 6 JPA entities mapping to PostgreSQL core tables
  - `com.junction.repository` — 6 Spring Data JPA repositories
  - `com.junction.config` — Security, CORS, Caffeine LRU cache, Async executors
  - `src/main/resources/db/migration/` — `V1__create_junction_schema.sql`, `V2__seed_baseline_data.sql`
  - `Dockerfile` — Eclipse Temurin 21 Alpine container

### `cv_bridge/`
- **Responsibility:** Edge computer vision processing and hardware CCTV telemetry.
- **Technology:** Python 3.11+, PyTorch, Ultralytics YOLOv12, ByteTrack, OpenCV.
- **Safety Policy:** Strictly pedestrian counting, density estimation, and tripwire direction tracking. Zero biometric / facial recognition.
- **Integration:** Ships normalized observation batches to Spring Boot (`POST /api/observations`) authenticated via static Bearer token.
- **Key Files:**
  - `inference.py` — Real-time inference loop
  - `metrics.py` — Line intersection & spatial density math
  - `transport.py` — Multi-channel transport (HTTP delivery with Bearer auth)
  - `models/` — YOLOv12 model weights (`yolov12n.pt`, `yolov12m.pt`)
  - `tests/` — Pytest test suite (9 unit tests)

### `mobile/attendee_flutter_app/`
- **Responsibility:** Mobile client for event visitors.
- **Technology:** Flutter 3.22+, Dart 3.
- **Platforms:** Android, iOS, Web, Desktop.
- **Integration:** Synchronizes with Spring Boot backend (`/api/scenarios`) with multi-host candidate resolution (port 8080 prioritized).

### `contracts/`
- **Responsibility:** Single source of truth for platform API contracts.
- **Files:**
  - `openapi.yaml` — OpenAPI 3.0 specification for all Spring Boot endpoints.
  - `schemas/` — Reusable JSON schemas for `NormalizedObservation`, `ZoneState`, and `OperationalIntervention`.

### `infrastructure/`
- **Responsibility:** Multi-container orchestration and cloud deployment configurations.
- **Files:**
  - `docker-compose.yml` — Local stack orchestration (PostgreSQL, Spring Boot, Next.js).
  - `railway/` — Railway configuration templates.
  - `deployment/` — Universal deployment and installation guide.

### `scripts/`
- **Responsibility:** Unified automation across the monorepo.
- **Subdirectories:**
  - `scripts/testing/` — `test-full-system.ts` (61 tests), `test-input-system.ts` (42 tests).
  - `scripts/utilities/` — `generate_qr.js` (APK download QR code generator).
  - `scripts/development/` — Development setup scripts.
  - `scripts/deployment/` — Build and release utilities.

### `assets/test-videos/`
- **Responsibility:** Standardized CCTV video assets used for CV validation and demonstration.

---

## 3. Monorepo Root Directory Verification

The root directory contains strictly repository-level configuration:
- `.gitignore`
- `README.md`
- `MIGRATION_LOG.md`
- `PROJECT_STRUCTURE.md`
- `package.json` (root orchestration scripts)
- `tsconfig.json` (monorepo path mapping)
- `railway.json` (Railway container config pointing to `frontend/Dockerfile`)
- `pytest.ini` (root pytest configuration)
- `.github/` (CI/CD workflows)
- `.rocketride/` (102 tool schemas)
- `.claude/`
