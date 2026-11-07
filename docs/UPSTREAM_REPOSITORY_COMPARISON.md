# JUNCTION — Upstream Repository Comparison & Discovery Report

**Generated:** 2026-11-07  
**Target Repository:** `https://github.com/SujalPatil21/Junction.git` (`C:\Github\Junction-App`)  
**Source Repository:** `https://github.com/NihalMishra3009/JUNCTION-P.git` (`C:\Github\JUNCTION-P_upstream`)  
**Common Ancestor Commit:** `a67d91c` (*feat: Clerk auth + PostgreSQL/Drizzle role persistence*)  
**Integration Branch:** `integration/upstream-junction-features`

---

## 1. Executive Summary

A comprehensive Git history traversal and directory-tree diff was conducted between the Target repository (`Junction.git`) and the newly accessible Source repository (`JUNCTION-P.git`).

Both repositories branched from commit `a67d91c`. From this common ancestor:
- **Target Repository (`Junction.git`)**: Undertook the architectural transformation to a production-grade enterprise monorepo (`frontend/`, `backend/` in Spring Boot 4.1.2 Java 21, `mobile/attendee_flutter_app/`, `cv_bridge/`), replacing legacy Node.js/TypeScript backend API routes with Spring Boot REST services, JPA/Flyway migrations, strict Clerk JWT authentication, and integrating Google Photorealistic 3D Tiles (CesiumJS) into `DestinationMap`.
- **Source Repository (`JUNCTION-P.git`)**: Continued iterating on the flat Next.js architecture, creating sophisticated domain capabilities including:
  1. **Three.js 3D Stadium Digital Twin (`HoloJiStadiumTwin`)**: A full 3D interactive twin of Wankhede Stadium with GLTF model rendering, crowd particle simulation, zone heatmaps, gate topology, and situation analyzer.
  2. **Weather Digital Twin Layer (Open-Meteo Integration)**: Real-time dynamic weather forecasting affecting crowd transit speeds, taxi surge, and concourse capacity thresholds without requiring third-party API keys.
  3. **Nugen Domain Intelligence (Task 2 Alignment)**: Domain intelligence evaluation model with benchmark JSON/JSONL datasets, operational status card, and scenario assessment.
  4. **9-Channel Synchronized CCTV System**: Synchronized video canvas tracking at 60 FPS across 9 real surveillance feeds (`CCTV-01` to `CCTV-09`) with neutral naming, bounding boxes, and operator intervention controls.
  5. **AI Recommendation & Multi-Horizon Decision Planner**: Structured recommendation generation, social signal adapters, and closed-loop intervention lifecycle engines.
  6. **Latest Release Mobile APK**: Standalone Android release package `JUNCTION-Release.apk`.

All identified compatible capabilities are systematically integrated into the target's modern Spring Boot / Next.js architecture without regression.

---

## 2. Granular Feature & File Comparison Matrix

| Domain / Feature | Upstream Status (`JUNCTION-P`) | Target Status (`Junction`) | Integration Decision |
| :--- | :--- | :--- | :--- |
| **Backend Architecture** | Flat Next.js API routes (`src/app/api/*`) + Drizzle ORM | Spring Boot 4.1.2 Java 21 REST API (`backend/`) + Flyway + PostgreSQL 5432 | **Retain Spring Boot Target Architecture**. Reject upstream TypeScript backend API routes to prevent regression. |
| **3D Stadium Digital Twin** | Full Three.js + React Three Fiber stadium twin (`src/components/organizer/stadium/*`, `wankhede_stadium.glb`) | Only CesiumJS 3D Google Tiles & MapLibre 3D | **Integrate Completely** into `frontend/src/components/organizer/stadium/`, copy 3D model to `frontend/public/models/wankhede_stadium.glb`, add routes `/organizer/stadium`, `/organizer/stadium-preview`, and `/organizer/select`. |
| **Weather Digital Twin** | `weatherService.ts`, `weatherImpactModel.ts`, `weather.ts`, `WEATHER_TWIN_SETUP.md` | Missing | **Integrate Completely** into `frontend/src/services/` and `frontend/src/types/`. Wire into `AppContext.tsx` and What-If Simulator. |
| **Nugen Domain Intelligence** | `NugenDomainIntelligenceCard.tsx`, `nugenService.ts`, Nugen alignment datasets & docs | Missing | **Integrate Completely** into `frontend/src/components/organizer/`, `frontend/src/services/`, and `docs/`. Mount card in Organizer dashboard. |
| **CCTV Surveillance System** | 9-channel feed system with real MP4 surveillance clips and canvas overlay (`cctvFeeds.ts`, `CctvVideoFeedCard.tsx`, etc.) | Partial (older demo clips, missing 9 full video assets and modular CCTV components) | **Integrate Upstream 9-Channel Feeds**, sync MP4 video assets into `frontend/public/videos/`, copy updated CCTV components and test scripts. |
| **AI Recommendation Planner** | `aiRecommendationPlanner.ts`, `interventionEffectsEngine.ts`, `socialSignalAdapter.ts` | Basic recommendation engine | **Integrate Upstream AI Engines** into `frontend/src/services/` and `frontend/src/types/`. |
| **Attendee Flutter App** | `attendee_flutter_app` with barcode scanner and updated emergency contacts | `mobile/attendee_flutter_app` with Spring Boot backend sync, multi-emergency calling, barcode sheet | **Harmonize**: Maintain Spring Boot integration while adopting upstream release APK `JUNCTION-Release.apk` and design enhancements. |
| **Database Migrations** | `src/db/schema.ts` (6 tables) | Spring Boot Flyway V1 schema (identical 6 tables) | **Parity Confirmed**: Both have identical database schemas. JPA entities already mapped. |

---

## 3. Detailed Component Ingestion Plan

### 3.1 Dependencies
In `frontend/package.json`:
- `@react-three/fiber`: `^9.8.1`
- `@react-three/drei`: `^10.7.9`
- `@react-three/postprocessing`: `^3.1.2`
- `postprocessing`: `^6.36.3`
- `@google/genai`: `^2.24.0`

### 3.2 3D Stadium Digital Twin
Files to integrate:
- `frontend/src/components/organizer/stadium/` (14 components, 13 data modules, `stadium.css`)
- `frontend/src/components/organizer/map/WankhedeHoloDigitalTwin.tsx`
- `frontend/src/components/organizer/map/WankhedeHolo.module.css`
- `frontend/src/data/wankhedeSpatialGraph.ts`
- `frontend/public/models/wankhede_stadium.glb` (8.99 MB)
- Routes:
  - `frontend/src/app/organizer/stadium/page.tsx`
  - `frontend/src/app/organizer/stadium-preview/page.tsx`
  - `frontend/src/app/organizer/select/page.tsx` & `select.module.css`
- Sidebar Navigation: Add "Stadium Twin (HoloJi 3D)" to `frontend/src/app/organizer/layout.tsx`.

### 3.3 Weather Digital Twin
Files to integrate:
- `frontend/src/services/weatherService.ts`
- `frontend/src/services/weatherImpactModel.ts`
- `frontend/src/types/weather.ts`
- `docs/WEATHER_TWIN_SETUP.md`

### 3.4 Nugen Domain Intelligence (Task 2)
Files to integrate:
- `frontend/src/components/organizer/NugenDomainIntelligenceCard.tsx`
- `frontend/src/services/nugenService.ts`
- `docs/NUGEN_ALIGNMENT_DATASET.json`
- `docs/NUGEN_ALIGNMENT_DATASET.jsonl`
- `docs/NUGEN_TASK2_INTEGRATION.md`
- `docs/JUNCTION_NUGEN_DOMAIN_ALIGNMENT.md`
- `scripts/testing/scenarios/test-nugen-task2.ts`

### 3.5 9-Channel Synchronized CCTV System
Files to integrate:
- `frontend/src/components/organizer/cctv/CctvVideoFeedCard.tsx`
- `frontend/src/components/organizer/cctv/CctvFeedTableView.tsx`
- `frontend/src/components/organizer/cctv/CctvOperationalActions.tsx`
- `frontend/src/components/organizer/cctv/CctvTechnicalPanel.tsx`
- `frontend/src/components/organizer/cctv/cctvComponents.module.css`
- `frontend/src/data/cctvFeeds.ts`
- Video clips copied to `frontend/public/videos/`:
  - `12208082_1080_1920_60fps.mp4`
  - `12269372_2320_1080_30fps.mp4`
  - `12269404_2320_1080_30fps.mp4`
  - `13128176_1920_1080_24fps.mp4`
  - `cctv_concourse_aisle.mp4`
  - `cctv_gate_security.mp4`
  - `cctv_street_patrol.mp4`
  - `cctv_walkway_corridor.mp4`
  - `istockphoto-2192369057-640_adpp_is.mp4`
- `scripts/testing/scenarios/test-cctv-system.ts`

### 3.6 AI Decision Services & State Stores
Files to integrate:
- `frontend/src/services/aiRecommendationPlanner.ts`
- `frontend/src/services/canonicalRouteStore.ts`
- `frontend/src/services/interventionEffectsEngine.ts`
- `frontend/src/services/socialSignalAdapter.ts`
- `frontend/src/services/scenarioStore.ts`
- `frontend/src/types/aiRecommendation.ts`
- `scripts/testing/scenarios/test-recommendation-attendee-flow.ts`

### 3.7 Documentation & Assets
- `docs/CLERK_PARTNER_SETUP.md`
- `frontend/public/JUNCTION-Release.apk`
- `mobile/apk/JUNCTION-Release.apk`

---

## 4. Verification Strategy
1. **Frontend Compilation**: `npm --prefix frontend run build` verifying Next.js 16 App Router builds all routes with zero errors.
2. **Spring Boot Verification**: `mvn -f backend/pom.xml test` verifying 100% test passing.
3. **Computer Vision Bridge**: `pytest -c cv_bridge/pytest.ini cv_bridge/tests -v`.
4. **Mobile Analyzer**: `flutter analyze` & `flutter test` in `mobile/attendee_flutter_app`.
5. **System Validation Scripts**: `npm run test:full`, `npm run test:input`, `test-cctv-system.ts`, `test-nugen-task2.ts`.

