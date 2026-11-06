# JUNCTION Backend Technical Documentation & Spring Boot Migration Specification

**Document Version:** 1.0.0  
**Project:** JUNCTION — Intelligent Destination Orchestration Platform  
**Target Migration Framework:** Java 21 / Spring Boot 3.3.x  
**Analysis Date:** November 2026  
**Status:** Canonical Reference Specification  

---

## Executive Summary & Architectural Reality Check

> [!IMPORTANT]
> **Codebase Reality vs. Architecture Blueprint Notice**
> The project architecture specifications (`docs/JUNCTION_Production_System_Implementation_Blueprint.md` and `docs/JUNCTION_PS8_Alignment_Analysis.md`) initially designed an asynchronous Python 3.12 / **FastAPI Modular Monolith Core** (`services/api-monolith/`) alongside an Edge Computer Vision Daemon (`services/edge-cctv-daemon/`). 
> 
> In the actual working codebase within this repository:
> 1. The **Backend API Layer, Business Logic Engines, and Ingestion Subsystems** are implemented as **Next.js 16 (App Router) TypeScript API Route Handlers** (`src/app/api/*`) supported by domain-driven services in `src/services/*`.
> 2. The **Database Layer** is backed by **PostgreSQL** using **Drizzle ORM** (`drizzle-orm` v0.45.3) and `node-postgres` (`pg` v8.23.0) with custom migration runners (`src/db/migrate.ts`) and connection pooling (`src/db/index.ts`).
> 3. The **Authentication and Identity Subsystem** is powered by **Clerk** (`@clerk/nextjs` v7.9.7) integrated with relational PostgreSQL profiles and scoped restaurant memberships (`src/services/authService.ts`).
> 4. The **Computer Vision and Sensing Subsystem** is implemented in Python under `cv_bridge/` (`cv_bridge/inference.py`, `cv_bridge/metrics.py`, `cv_bridge/observation_serializer.py`, `cv_bridge/transport.py`), powered by **PyTorch**, **Ultralytics YOLOv12**, **ByteTrack**, and **Pydantic v2**, which streams observation payloads to the HTTP ingestion API at `/api/observations`.
> 
> As instructed by the core analysis guidelines (*"Do not assume anything. Extract information directly from the source code and configuration files"*), this document analyzes the **implemented operational backend** in full detail, cross-references the blueprint requirements, and provides an end-to-end migration path to **Java Spring Boot 3.3.x**.

---

## 1. Backend Overview

### 1.1 Project Purpose and Backend Responsibilities
**JUNCTION** is an intelligent destination orchestration platform engineered for mega-events (such as major international cricket tournaments at Wankhede Stadium, Mumbai). The backend system coordinates crowd movement, transport terminal throughput, road corridor capacity, and local hospitality inventory to prevent cascading bottlenecks across the urban transit network.

```mermaid
flowchart TB
    subgraph SENSING ["1. SENSING & INGESTION"]
        direction TB
        CCTV["CCTV Feeds / MP4 Streams"] --> CVB["Python CV Bridge<br/>(YOLOv12 + ByteTrack)"]
        CVB -->|"POST /api/observations<br/>(NormalizedObservation JSON)"| INGEST["Ingestion & Deduplication Pipeline<br/>(src/services/ingestionPipeline.ts)"]
        IOT["Simulated Sensors & Turnstiles"] --> INGEST
        PARTNER_ACT["Partner Operations (App/Web)"] -->|"POST /api/partner/inventory"| PARTNER_SVC["Partner Inventory Service"]
    end

    subgraph ENGINE_CORE ["2. CORE DOMAIN INTELLIGENCE"]
        direction TB
        INGEST --> FUSION["Sensor Fusion Engine<br/>(sensorFusionEngine.ts)"]
        ZONES["Zone Registry & Coverage Contracts<br/>(zoneRegistry.ts)"] --> FUSION
        FUSION --> FORECAST["Short-Horizon Forecasting Engine<br/>(forecastingService.ts)"]
        FUSION --> HOTSPOT["Hotspot & Cascade Spillover Engine<br/>(hotspotAndCascadeEngine.ts)"]
        FUSION --> REC["Recommendation Lifecycle Engine<br/>(recommendationLifecycleEngine.ts)"]
        HOTSPOT --> REC
        FUSION --> HOSP["Hospitality Demand Propagation<br/>(hospitalityDemandService.ts)"]
    end

    subgraph STORAGE ["3. PERSISTENCE LAYER"]
        direction TB
        PG[("PostgreSQL 15+ Database")]
        MEM[("In-Memory Telemetry Buffer<br/>(Fail-Safe Ring Buffer)")]
        PERSIST["Persistence Service<br/>(persistenceService.ts)"]
        PERSIST --> PG
        PERSIST --> MEM
    end

    subgraph API_SURFACE ["4. CLIENT / API SURFACE"]
        direction TB
        ORG_UI["Organizer Command Center"]
        ATT_UI["Attendee Mobile/Web (Flutter & Next.js)"]
        PTR_UI["Partner Operations Portal"]
        API_ROUTES["Next.js 16 API Routes (/api/*)"]
        API_ROUTES --> ENGINE_CORE
        ENGINE_CORE --> PERSIST
        ORG_UI --> API_ROUTES
        ATT_UI --> API_ROUTES
        PTR_UI --> API_ROUTES
    end
```

### 1.2 Core Responsibilities of the Backend
1. **Telemetry Ingestion & Deduplication:** Ingests high-frequency telemetry (headcounts, densities, inflow/outflow rates) from edge computer vision systems and IoT feeds, validating schemas, discarding duplicates via LRU caches, and tagging freshness.
2. **Zone-Aware Sensor Fusion:** Fuses disparate observations against explicit operational capacity baselines (e.g. exit concourse vs. station concourse) using weighted multi-sensor equations.
3. **Spatial Pressure & Cascade Modeling:** Models directional crowd propagation outward from origin venues (e.g., Wankhede Stadium) across transit hubs (Churchgate, Marine Lines, CSMT) and hospitality corridors within a 60-minute window.
4. **Short-Horizon Forecasting:** Calculates deterministic and trend-based forecasts across 15, 30, 45, and 60-minute horizons with expanding confidence bounds.
5. **Human-in-the-Loop Recommendation Lifecycle:** Generates explainable operational interventions (rerouting, gate pacing, shuttle dispatching, digital commercial vouchers), enforcing authorization constraints and audit trails.
6. **Partner Demand Redistribution:** Distributes event demand to hotels and restaurants based on walking proximity, real-time table/room availability, and dynamic voucher incentives.
7. **Security & Governance:** Enforces zero biometrics, aggregate-only metrics, and immutable audit logs for all administrative decisions.

### 1.3 Technologies, Frameworks, and Libraries

| Category | Implemented Technology | Target Spring Boot Equivalent |
|---|---|---|
| **Server Framework** | Next.js 16.3.4 (App Router / Node.js 20) | Spring Boot 3.3.x (Spring Web / Spring WebFlux) |
| **Language** | TypeScript 5.x / Python 3.11+ | Java 21 (LTS) |
| **Edge CV Pipeline** | Python 3.11, Ultralytics YOLOv12, ByteTrack, OpenCV | Python Microservice / Edge Daemon or Deep Java Library (DJL) |
| **ORM / Data Access** | Drizzle ORM v0.45.3 + `node-postgres` (pg v8.23.0) | Spring Data JPA / Hibernate 6 + HikariCP |
| **Database** | PostgreSQL 15+ / TimescaleDB compatible | PostgreSQL 15+ with TimescaleDB extension |
| **Authentication** | Clerk Authentication (`@clerk/nextjs` v7.9.7) | Spring Security 6 (OAuth2 Resource Server / JWT) |
| **Schema Validation** | Custom TypeScript TypeGuards & Pydantic v2 (CV Bridge) | Jakarta Bean Validation (`hibernate-validator`) |
| **State Management** | Node.js Global Singleton In-Memory Buffers | Spring `@Service` Singletons + Caffeine / Redis Cache |
| **Containerization** | Docker (Alpine 3.19 / Node 20 runner) | Multi-stage Docker (`eclipse-temurin:21-jre-alpine`) |
| **Hosting Platform** | Railway (Container PaaS) | AWS ECS / Railway / Kubernetes |

---

## 2. Project Structure

### 2.1 Backend Directory Tree
```text
C:\Github\Junction-App\
├── .env                              # Managed environment configuration
├── Dockerfile                        # Multi-stage production container definition
├── railway.json                      # Railway deployment and healthcheck configuration
├── package.json                      # Node.js dependencies and script entry points
├── tsconfig.json                     # TypeScript compiler configuration
├── drizzle.config.ts                 # Drizzle ORM schema mapping configuration
├── pytest.ini                        # Pytest configuration for Python CV tests
│
├── cv_bridge/                        # Computer Vision Edge Ingest Subsystem (Python)
│   ├── config.example.json           # Template camera calibration and tripwire config
│   ├── inference.py                  # CLI and runtime pipeline (YOLOv12 + ByteTrack)
│   ├── metrics.py                    # Spatial density and tripwire line-crossing math
│   ├── observation_serializer.py     # Serializer converting CV metrics to canonical JSON
│   ├── requirements.txt              # PyTorch, Ultralytics, Pydantic, OpenCV dependencies
│   ├── transport.py                  # HTTP POST and JSONL emission client
│   ├── yolo_compat.py                # PyTorch architecture compat patch for YOLOv12
│   └── tests/
│       ├── test_metrics.py           # Unit tests for centroid, tripwires, and density
│       └── test_serializer.py        # Unit tests for canonical schema validation
│
├── src/
│   ├── proxy.ts                      # Clerk authentication middleware definition
│   │
│   ├── app/                          # Next.js App Router (UI & API Surface)
│   │   ├── api/                      # REGISTERED HTTP BACKEND ENDPOINTS
│   │   │   ├── audit/
│   │   │   │   └── route.ts          # GET /api/audit (Audit log inspection)
│   │   │   ├── auth/
│   │   │   │   └── me/
│   │   │   │       └── route.ts      # GET /api/auth/me (User session & roles)
│   │   │   ├── health/
│   │   │   │   └── route.ts          # GET /api/health (Diagnostics & uptime)
│   │   │   ├── hotspots/
│   │   │   │   └── route.ts          # GET /api/hotspots (Spatial pressure hotspots)
│   │   │   ├── observations/
│   │   │   │   └── route.ts          # GET, POST /api/observations (CV ingestion)
│   │   │   ├── partner/
│   │   │   │   └── inventory/
│   │   │   │       └── route.ts      # POST /api/partner/inventory (Table capacity)
│   │   │   ├── predictions/
│   │   │   │   └── route.ts          # GET /api/predictions (15/30/60m forecasts)
│   │   │   ├── recommendations/
│   │   │   │   ├── route.ts          # GET /api/recommendations (Active interventions)
│   │   │   │   └── [id]/
│   │   │   │       ├── approve/
│   │   │   │       │   └── route.ts  # POST /api/recommendations/[id]/approve
│   │   │   │       └── reject/
│   │   │   │           └── route.ts  # POST /api/recommendations/[id]/reject
│   │   │   ├── scenarios/
│   │   │   │   └── route.ts          # GET, POST, OPTIONS /api/scenarios
│   │   │   └── zones/
│   │   │       ├── route.ts          # GET /api/zones (All fused zone states)
│   │   │       └── [zoneId]/
│   │   │           └── route.ts      # GET /api/zones/[zoneId] (Single zone & sensors)
│   │
│   ├── db/                           # Relational Database Layer (PostgreSQL)
│   │   ├── index.ts                  # Connection pool & Drizzle ORM client initialization
│   │   ├── schema.ts                 # PostgreSQL DDL table definitions & TypeScript types
│   │   ├── migrate.ts                # Programmatic DDL table migration script
│   │   └── seedDevData.ts            # Baseline developer profiles and restaurant seeding
│   │
│   ├── services/                     # Core Domain Intelligence Services
│   │   ├── authService.ts            # PostgreSQL profiles, RBAC, Clerk synchronization
│   │   ├── canonicalRouteStore.ts    # Static route templates and diversion corridors
│   │   ├── deviceRegistry.ts         # In-memory registry for hardware sensing nodes
│   │   ├── eventLogger.ts            # Structured logging and diagnostic tracing
│   │   ├── forecastingService.ts     # Multi-horizon trend and velocity forecast calculations
│   │   ├── hospitalityDemandService.ts# Hotel & restaurant demand propagation engine
│   │   ├── hotspotAndCascadeEngine.ts# Spatial choke points and outward cascade graphs
│   │   ├── index.ts                  # Barrel export for all domain services
│   │   ├── ingestionPipeline.ts      # LRU deduplication, bounds checking, and validation
│   │   ├── mockDataService.ts        # Scenario-driven baseline resources and alerts
│   │   ├── persistenceService.ts     # Dual-mode persistence (Durable PostgreSQL / In-Memory)
│   │   ├── recommendationLifecycleEngine.ts # Decision state machine & cooldown manager
│   │   ├── routingEngine.ts          # OpenStreetMap Dijkstra shortest-path solver
│   │   ├── sensorFusionEngine.ts     # Weighted multi-modal sensor fusion math
│   │   ├── sensorStreamSimulator.ts  # Synthetic device emulation for MVP testing
│   │   ├── simulationEngine.ts       # Discrete cohort egress flow calculator
│   │   ├── syntheticCVProvider.ts    # Synthetic video analytics provider
│   │   ├── transportSimulationEngine.ts # Station, train, and taxi boarding simulator
│   │   ├── zoneRegistry.ts           # Tiered zone definitions & coverage profiles
│   │   └── providers/                # Sensor modality adapters
│   │       ├── CctvCameraCvProvider.ts
│   │       ├── DensityBeaconProvider.ts
│   │       ├── EntranceExitCounterProvider.ts
│   │       ├── providerRegistry.ts
│   │       └── types.ts
│   │
│   ├── types/                        # Domain Contracts & Schemas
│   │   ├── audit.ts                  # Audit action types & security governance schemas
│   │   ├── auth.ts                   # User roles, demo profiles, and session contracts
│   │   ├── cctv.ts                   # Camera device definitions & RTSP metadata
│   │   ├── computerVision.ts         # Normalized CV observation contracts
│   │   ├── device.ts                 # Hardware device types & health statuses
│   │   ├── forecast.ts               # Forecast points & cascade projections
│   │   ├── hospitality.ts            # Hotels, restaurants, and demand signals
│   │   ├── index.ts                  # Master types barrel export
│   │   ├── map.ts                    # GeoJSON and coordinates
│   │   ├── observation.ts            # Canonical NormalizedObservation v1.0.0 schema
│   │   ├── recommendation.ts         # OperationalIntervention decision model
│   │   ├── resource.ts               # Venue, station, gate, and road resource definitions
│   │   ├── transport.ts              # Rail lines, stations, and vehicle loads
│   │   └── zone.ts                   # Tiered zones, coverage profiles, and diagnostics
│   │
│   └── data/                         # Static Cartography, Topologies, and Baselines
│       ├── mockHotels.ts
│       ├── mockRestaurants.ts
│       ├── mockResources.ts
│       ├── mockScenarios.ts
│       ├── mockUsers.ts
│       ├── osmRoadNetwork.ts
│       └── wankhedeSpatialGraph.ts
│
└── scripts/                          # Automated Verification & Test Harnesses
    ├── test-full-system.ts           # 5-suite end-to-end integration test runner
    └── test-input-system.ts          # Modality provider verification script
```

### 2.2 Entry Points and Application Initialization
1. **HTTP Web Application Entry:** `next.config.ts` initialized by `server.js` (generated via Next.js standalone build). The process binds to `0.0.0.0:3000` via Docker.
2. **Database Initialization:** `src/db/index.ts` lazily initializes a singleton connection pool using `pg.Pool` with connection pooling rules (10 connections in dev, 20 in production).
3. **Database Migration Runner:** `src/db/migrate.ts` executes standalone via `npx tsx src/db/migrate.ts` to run DDL statements and seed initial restaurants.
4. **Edge CV Runner Entry:** `python -m cv_bridge.inference` launches the OpenCV video reader, loads YOLOv12 weights into PyTorch, tracks people via ByteTrack, and starts streaming observations over HTTP.

---

## 3. Database Architecture

### 3.1 Database Type and Driver
- **Database Engine:** PostgreSQL 15+
- **Driver:** `pg` (`node-postgres` v8.23.0)
- **ORM / Query Builder:** `drizzle-orm` (v0.45.3) + `drizzle-kit` (v0.31.11)
- **Connection Configuration:** Parsed from `DATABASE_URL` (format: `postgresql://user:password@host:port/dbname`).

### 3.2 Database Session Management & Connection Pooling
Configured in `src/db/index.ts`:
- **Production Mode:** `max: 20` connections, `idleTimeoutMillis: 30000`, `connectionTimeoutMillis: 5000`.
- **Development Mode:** `max: 10` connections bound to `global.__junction_pg_pool` to avoid connection leaks during hot-module reloads.
- **Fail-Safe Dual-Storage Strategy:** Handled by `src/services/persistenceService.ts`. If `DATABASE_URL` is omitted, the backend runs seamlessly in `IN_MEMORY_SESSION` mode using in-memory ring buffers.

### 3.3 Database Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    PROFILES ||--o{ RESTAURANT_MEMBERSHIPS : "holds"
    PROFILES ||--o{ RESTAURANT_INVENTORY : "reports"
    RESTAURANTS ||--o{ RESTAURANT_MEMBERSHIPS : "assigned_to"
    RESTAURANTS ||--o{ RESTAURANT_INVENTORY : "has_history"

    PROFILES {
        uuid id PK "gen_random_uuid()"
        text clerk_user_id UK "Clerk user reference"
        text email "User email address"
        text name "Full name"
        user_role role "ORGANIZER | RESTAURANT_PARTNER | ATTENDEE"
        timestamptz created_at "Default NOW()"
        timestamptz updated_at "Default NOW()"
    }

    RESTAURANTS {
        text id PK "e.g. 'R1', 'R2'"
        text name "Restaurant name"
        text zone "e.g. 'ZONE_A'"
        text cuisine "Cuisine category"
        text address "Physical address"
        integer total_capacity "Default 100"
        integer available_covers "Default 50"
        integer expected_reservations "Default 0"
        text price_range "Default '₹₹'"
        double_precision lat "Latitude"
        double_precision lng "Longitude"
        boolean is_active "Default true"
        timestamptz created_at "Default NOW()"
        timestamptz updated_at "Default NOW()"
    }

    RESTAURANT_MEMBERSHIPS {
        uuid id PK "gen_random_uuid()"
        uuid profile_id FK "References profiles(id) ON DELETE CASCADE"
        text restaurant_id FK "References restaurants(id) ON DELETE CASCADE"
        membership_role role "OWNER | MANAGER | STAFF"
        timestamptz created_at "Default NOW()"
    }

    RESTAURANT_INVENTORY {
        uuid id PK "gen_random_uuid()"
        text restaurant_id FK "References restaurants(id) ON DELETE CASCADE"
        uuid reported_by_profile_id FK "References profiles(id) ON DELETE SET NULL"
        integer available_tables "Default 0"
        integer available_covers "Default 0"
        integer expected_covers "Default 0"
        integer out_of_order_tables "Default 0"
        text notes "Optional status notes"
        timestamptz recorded_at "Default NOW()"
    }

    AUDIT_EVENTS {
        text id PK "Unique event ID (e.g. 'AUDIT_1731000')"
        text actor_id "User ID or SYSTEM"
        text actor_role "ORGANIZER | PARTNER | SYSTEM | ATTENDEE"
        text category "RECOMMENDATION_APPROVAL | etc."
        text target_entity_type "RESOURCE | ZONE | RECOMMENDATION | etc."
        text target_entity_id "Target entity ID"
        text change_summary "Human readable summary"
        text rationale "Decision rationale"
        text previous_state_json "Serialized JSON before mutation"
        text new_state_json "Serialized JSON after mutation"
        boolean is_simulated_scenario "Default true"
        timestamptz timestamp "Default NOW()"
    }

    OPERATIONAL_INTERVENTIONS {
        text id PK "Unique intervention ID (e.g. 'INT_DIVERT_1731000')"
        text type "REROUTE_ATTENDEES | GATE_CAPACITY_CHANGE | etc."
        text title "Intervention title"
        text description "Detailed description"
        text target_zone_id "Target zone ID"
        text status "PROPOSED | APPROVED | REJECTED | EXECUTED"
        text urgency "LOW | MEDIUM | HIGH | CRITICAL"
        boolean requires_approval "Default true"
        text approval_role_required "ORGANIZER | TRANSIT_AUTHORITY | etc."
        text rationale "Evidence and justification"
        double_precision confidence_score "Default 0.8"
        integer expected_pressure_reduction_percent "Default 0"
        timestamptz proposed_at "Default NOW()"
        timestamptz expires_at "Expiration timestamp"
        timestamptz approved_at "Timestamp of approval"
        text approved_by_user_id "Actor ID who approved"
        text rejection_reason "Rejection explanation"
    }
```

### 3.4 Table Definitions & Constraints

#### Table 1: `profiles`
- **Primary Key:** `id` (UUID, default `gen_random_uuid()`)
- **Unique Key:** `clerk_user_id` (TEXT, NOT NULL, UNIQUE)
- **Columns:**
  - `email` (TEXT, NOT NULL)
  - `name` (TEXT, NULL)
  - `role` (ENUM `user_role`: `'ORGANIZER'`, `'RESTAURANT_PARTNER'`, `'ATTENDEE'`, NOT NULL, DEFAULT `'ATTENDEE'`)
  - `created_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)
  - `updated_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)

#### Table 2: `restaurants`
- **Primary Key:** `id` (TEXT, e.g. `'R1'`, `'R2'`)
- **Columns:**
  - `name` (TEXT, NOT NULL)
  - `zone` (TEXT, NOT NULL, e.g. `'ZONE_A'`)
  - `cuisine` (TEXT, NULL)
  - `address` (TEXT, NULL)
  - `total_capacity` (INTEGER, NOT NULL, DEFAULT `100`)
  - `available_covers` (INTEGER, NOT NULL, DEFAULT `50`)
  - `expected_reservations` (INTEGER, NOT NULL, DEFAULT `0`)
  - `price_range` (TEXT, DEFAULT `'₹₹'`)
  - `lat` (DOUBLE PRECISION, NULL)
  - `lng` (DOUBLE PRECISION, NULL)
  - `is_active` (BOOLEAN, NOT NULL, DEFAULT `TRUE`)
  - `created_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)
  - `updated_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)

#### Table 3: `restaurant_memberships`
- **Primary Key:** `id` (UUID, default `gen_random_uuid()`)
- **Foreign Keys:**
  - `profile_id` -> `profiles(id)` ON DELETE CASCADE
  - `restaurant_id` -> `restaurants(id)` ON DELETE CASCADE
- **Unique Constraint:** `UNIQUE(profile_id, restaurant_id)`
- **Columns:**
  - `role` (ENUM `membership_role`: `'OWNER'`, `'MANAGER'`, `'STAFF'`, NOT NULL, DEFAULT `'STAFF'`)
  - `created_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)

#### Table 4: `restaurant_inventory`
- **Primary Key:** `id` (UUID, default `gen_random_uuid()`)
- **Foreign Keys:**
  - `restaurant_id` -> `restaurants(id)` ON DELETE CASCADE
  - `reported_by_profile_id` -> `profiles(id)` ON DELETE SET NULL
- **Columns:**
  - `available_tables` (INTEGER, NOT NULL, DEFAULT `0`)
  - `available_covers` (INTEGER, NOT NULL, DEFAULT `0`)
  - `expected_covers` (INTEGER, NOT NULL, DEFAULT `0`)
  - `out_of_order_tables` (INTEGER, NOT NULL, DEFAULT `0`)
  - `notes` (TEXT, NULL)
  - `recorded_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)

#### Table 5: `audit_events`
- **Primary Key:** `id` (TEXT)
- **Columns:**
  - `actor_id` (TEXT, NOT NULL)
  - `actor_role` (TEXT, NOT NULL)
  - `category` (TEXT, NOT NULL)
  - `target_entity_type` (TEXT, NOT NULL)
  - `target_entity_id` (TEXT, NOT NULL)
  - `change_summary` (TEXT, NOT NULL)
  - `rationale` (TEXT, NULL)
  - `previous_state_json` (TEXT, NULL)
  - `newState_json` (TEXT, NULL)
  - `is_simulated_scenario` (BOOLEAN, NOT NULL, DEFAULT `TRUE`)
  - `timestamp` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)

#### Table 6: `operational_interventions`
- **Primary Key:** `id` (TEXT)
- **Columns:**
  - `type` (TEXT, NOT NULL)
  - `title` (TEXT, NOT NULL)
  - `description` (TEXT, NOT NULL)
  - `target_zone_id` (TEXT, NOT NULL)
  - `status` (TEXT, NOT NULL, DEFAULT `'PROPOSED'`)
  - `urgency` (TEXT, NOT NULL, DEFAULT `'MEDIUM'`)
  - `requires_approval` (BOOLEAN, NOT NULL, DEFAULT `TRUE`)
  - `approval_role_required` (TEXT, NULL)
  - `rationale` (TEXT, NOT NULL)
  - `confidence_score` (DOUBLE PRECISION, NOT NULL, DEFAULT `0.8`)
  - `expected_pressure_reduction_percent` (INTEGER, NOT NULL, DEFAULT `0`)
  - `proposed_at` (TIMESTAMPTZ, NOT NULL, DEFAULT `NOW()`)
  - `expires_at` (TIMESTAMPTZ, NOT NULL)
  - `approved_at` (TIMESTAMPTZ, NULL)
  - `approved_by_user_id` (TEXT, NULL)
  - `rejection_reason` (TEXT, NULL)

### 3.5 Migration Tool & Strategy
- **Current Strategy:** Dual approach:
  1. `drizzle.config.ts` specifies schema file `src/db/schema.ts` and dialect `postgresql` for `drizzle-kit`.
  2. Programmatic migration script `src/db/migrate.ts` executes DDL statements idempotently using `CREATE TABLE IF NOT EXISTS`, conditional enum creation blocks (`DO $$ BEGIN CREATE TYPE ... EXCEPTION ... END $$;`), column renames, and initial seeding.
- **Spring Boot Migration Target:** Replace Drizzle/script migrations with **Flyway** (`V1__initial_schema.sql`, `V2__seed_data.sql`).

---

## 4. Complete API Documentation

### 4.1 System & Observability Module

#### Endpoint: `GET /api/health`
- **Controller/Router File:** `src/app/api/health/route.ts`
- **Purpose:** System health-check and operational diagnostics. Used by Railway container orchestrators.
- **Authentication:** Public (No auth required).
- **Path Parameters:** None.
- **Query Parameters:** None.
- **Request Body:** None.
- **Response Status Codes:** `200 OK`.
- **Response JSON Example:**
```json
{
  "status": "HEALTHY",
  "service": "JUNCTION Destination Orchestration API Layer",
  "version": "1.0.0",
  "environment": "production",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "database": {
    "configured": true,
    "storageMode": "DURABLE_POSTGRES"
  },
  "telemetryBuffer": {
    "inMemoryAuditRecords": 14,
    "inMemoryInterventions": 3
  }
}
```
- **Database Operations:** Reads diagnostics from `persistenceService.getStorageDiagnostics()`.
- **External Services Called:** None.

---

#### Endpoint: `GET /api/audit`
- **Controller/Router File:** `src/app/api/audit/route.ts`
- **Purpose:** Retrieves the immutable audit trail of operational decisions and system state transitions.
- **Authentication:** Public in development; accessible to authenticated organizers.
- **Path Parameters:** None.
- **Query Parameters:**
  - `limit` (optional, integer, default: `100`): Maximum records to retrieve.
- **Request Body:** None.
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.
- **Response JSON Example:**
```json
{
  "success": true,
  "timestamp": "2026-11-06T17:05:00.000Z",
  "count": 1,
  "storageDiagnostics": {
    "storageMode": "DURABLE_POSTGRES",
    "dbConfigured": true,
    "inMemoryAuditRecordCount": 1,
    "inMemoryInterventionCount": 2
  },
  "auditTrail": [
    {
      "id": "AUDIT_1730910000000",
      "timestamp": "2026-11-06T17:04:30.000Z",
      "actorId": "user_2testClerkId",
      "actorRole": "ORGANIZER",
      "category": "RECOMMENDATION_APPROVAL",
      "targetEntityType": "RECOMMENDATION",
      "targetEntityId": "INT_DIVERT_MARINE_LINES_1730910000",
      "changeSummary": "Operational intervention 'Divert Northern Foot Traffic to Marine Lines Station' was APPROVED by user_2testClerkId (ORGANIZER)",
      "rationale": "Churchgate station concourse threshold saturation",
      "previousState": { "status": "PROPOSED" },
      "newState": { "status": "APPROVED" },
      "isSimulatedScenario": true
    }
  ]
}
```
- **Database Operations:** `SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT :limit`. Falls back to `persistenceService.inMemoryAuditLog` if DB connection fails.
- **External Services Called:** None.

---

### 4.2 Authentication & User Session Module

#### Endpoint: `GET /api/auth/me`
- **Controller/Router File:** `src/app/api/auth/me/route.ts`
- **Purpose:** Resolves the current authenticated user's PostgreSQL profile, assigned restaurant memberships, and role flags.
- **Authentication:** Requires valid Clerk Session Token (`auth()` resolves `userId`).
- **Path Parameters:** None.
- **Query Parameters:** None.
- **Request Body:** None.
- **Response Status Codes:**
  - `200 OK`: Authenticated and profile resolved.
  - `401 Unauthorized`: No active Clerk session.
  - `404 Not Found`: User profile not found in PostgreSQL.
  - `500 Internal Server Error`: Server failure.
- **Response JSON Example:**
```json
{
  "success": true,
  "profile": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "clerkUserId": "user_2abc123xyz",
    "name": "City Command Lead",
    "email": "organizer@junction.local",
    "role": "ORGANIZER",
    "createdAt": "2026-11-06T12:00:00.000Z",
    "updatedAt": "2026-11-06T12:00:00.000Z"
  },
  "memberships": [],
  "isOrganizer": true,
  "isRestaurantPartner": false
}
```
- **Database Operations:**
  - `SELECT * FROM profiles WHERE clerk_user_id = :userId LIMIT 1`
  - `SELECT * FROM restaurant_memberships INNER JOIN restaurants ON ... WHERE profile_id = :profileId`
- **External Services Called:** Clerk Server SDK (`auth()`, `currentUser()`).

---

### 4.3 Telemetry Ingestion & Computer Vision Module

#### Endpoint: `POST /api/observations`
- **Controller/Router File:** `src/app/api/observations/route.ts`
- **Purpose:** Primary telemetry ingestion gateway. Receives live or synthetic observations from edge computer vision daemons (`cv_bridge`) and IoT devices. Validates schemas, deduplicates, tags freshness, and stores into ring buffer.
- **Authentication:** Public / Edge Token (Configured as public route in `src/proxy.ts` for edge device ingest).
- **Path Parameters:** None.
- **Query Parameters:** None.
- **Request Body Schema:**
  - Accepts either a single `NormalizedObservation` object or `{ observations: NormalizedObservation[] }`.
  - Mandatory fields per observation: `id` (string), `sourceId` (string), `zoneId` (string), `value` (number).
  - Optional fields: `resourceId`, `eventId`, `metricType`, `confidence`, `qualityStatus`, `derivationType`, `metadata`.
- **Request JSON Example:**
```json
{
  "observations": [
    {
      "id": "OBS_CV_COUNT_DEV_CCTV_WANKHEDE_01_frame_120_a1b2c3d4",
      "sourceId": "DEV_CCTV_WANKHEDE_01",
      "sourceProvider": "YOLOv12-ByteTrack-Bridge",
      "metricType": "CROWD_COUNT",
      "zoneId": "ZONE_WANKHEDE",
      "resourceId": "WANKHEDE_EXIT",
      "observedAt": "2026-11-06T17:05:00.000Z",
      "receivedAt": "2026-11-06T17:05:00.120Z",
      "value": 420,
      "unit": "persons",
      "confidence": 0.88,
      "qualityStatus": "FRESH",
      "derivationType": "MEASURED",
      "freshnessSeconds": 0,
      "schemaVersion": "1.0.0",
      "metadata": {
        "model": "yolov12n.pt",
        "tracker": "ByteTrack",
        "frameIndex": 120,
        "meanConfidence": 0.88
      }
    }
  ]
}
```
- **Response Status Codes:**
  - `200 OK`: Batch ingested and processed.
  - `400 Bad Request`: Empty observations or missing mandatory fields.
  - `500 Internal Server Error`: Ingestion pipeline failure.
- **Response JSON Example:**
```json
{
  "success": true,
  "acceptedCount": 1,
  "quarantinedCount": 0,
  "deduplicatedCount": 0,
  "totalIngested": 1250,
  "lastIngestedAt": "2026-11-06T17:05:00.125Z"
}
```
- **Database Operations:** In-memory ring buffer buffering (200 most recent items).
- **External Services Called:** None.

---

#### Endpoint: `GET /api/observations`
- **Controller/Router File:** `src/app/api/observations/route.ts`
- **Purpose:** Returns the current ingestion telemetry status, total ingested counts, and a slice of recent observations.
- **Authentication:** Public.
- **Response Status Codes:** `200 OK`.
- **Response JSON Example:**
```json
{
  "status": "ONLINE",
  "provider": "JUNCTION-CV-Ingestion-Gateway",
  "totalIngestedCount": 1250,
  "lastIngestedAt": "2026-11-06T17:05:00.125Z",
  "latestObservations": [],
  "activeCount": 420
}
```

---

### 4.4 Spatial Intelligence, Zones & Hotspots Module

#### Endpoint: `GET /api/zones`
- **Controller/Router File:** `src/app/api/zones/route.ts`
- **Purpose:** Returns the canonical fused operational state for all 7 registered operational zones in the destination.
- **Authentication:** Public / Internal.
- **Query Parameters:**
  - `scenario` (optional, string, default: `'NORMAL'`): One of `'NORMAL'`, `'POST_EVENT_SURGE'`, `'TRANSPORT_DISRUPTION'`, `'HEAVY_RAIN'`, `'ACCOMMODATION_SATURATION'`, `'EVENT_DELAY'`.
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.
- **Response JSON Example:**
```json
{
  "success": true,
  "scenario": "NORMAL",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "zoneCount": 7,
  "zones": [
    {
      "id": "ZONE_WANKHEDE",
      "name": "Wankhede Stadium & Perimeter Concourse",
      "tier": "TIER_1_CRITICAL",
      "baseTier": "TIER_1_CRITICAL",
      "monitoringStatus": "CONTINUOUS",
      "isEscalated": false,
      "pressure": 35,
      "predictedPressure15": 42,
      "predictedPressure30": 50,
      "predictedPressure60": 58,
      "pressureLevel": "NORMAL",
      "trend": "STABLE",
      "inflowRate": 120,
      "outflowRate": 80,
      "netFlow": 40,
      "totalCapacity": 37000,
      "currentUtilization": 12950,
      "availableCapacity": 24050,
      "usableCapacity": 24050,
      "activeBottlenecks": [],
      "memberResources": ["WANKHEDE", "WANKHEDE_EXIT"],
      "lastUpdated": "2026-11-06T17:05:00.000Z",
      "confidence": 1.0,
      "source": "SIMULATED"
    }
  ]
}
```

---

#### Endpoint: `GET /api/zones/[zoneId]`
- **Controller/Router File:** `src/app/api/zones/[zoneId]/route.ts`
- **Purpose:** Returns comprehensive spatial state, coverage profile contracts, and registered sensing devices for a single zone.
- **Path Parameters:**
  - `zoneId` (mandatory, string): e.g. `'ZONE_WANKHEDE'`, `'ZONE_CHURCHGATE'`.
- **Query Parameters:**
  - `scenario` (optional, string, default: `'NORMAL'`).
- **Response Status Codes:**
  - `200 OK`: Zone retrieved.
  - `404 Not Found`: Unknown `zoneId`.
  - `500 Internal Server Error`: Processing failure.
- **Response JSON Example:**
```json
{
  "success": true,
  "zoneId": "ZONE_WANKHEDE",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "state": {
    "id": "ZONE_WANKHEDE",
    "name": "Wankhede Stadium & Perimeter Concourse",
    "tier": "TIER_1_CRITICAL",
    "pressure": 35,
    "confidence": 1.0
  },
  "coverageProfile": {
    "zoneId": "ZONE_WANKHEDE",
    "displayName": "Wankhede Stadium & Perimeter Concourse",
    "coverageStatus": "FULL",
    "dataSource": "SENSOR_FUSED",
    "registeredDeviceCount": 6,
    "deviceModalities": ["LIDAR", "TURNSTILE_GATE", "CCTV_CAMERA"],
    "maxPermittedConfidence": 1.0,
    "fallbackBehavior": "NONE",
    "freshnessMaxAgeSeconds": 30,
    "canTriggerOperationalRecommendations": true,
    "assumptions": "Complete optical LiDAR and turnstile hardware instrumentation on egress gates 1-7."
  },
  "registeredDevices": [
    {
      "id": "DEV_CCTV_WANKHEDE_01",
      "name": "Wankhede Gate 3 North Concourse CCTV",
      "type": "CCTV_CAMERA",
      "provider": "YOLOv12-ByteTrack-Bridge",
      "health": { "status": "HEALTHY", "lastSeenAt": "2026-11-06T17:04:00.000Z", "errorCountLastHour": 0 },
      "reliabilityScore": 0.95
    }
  ]
}
```

---

#### Endpoint: `GET /api/hotspots`
- **Controller/Router File:** `src/app/api/hotspots/route.ts`
- **Purpose:** Runs spatial pressure analysis to detect imminent congestion choke points and multi-stage cascade spillover outward from Wankhede Stadium.
- **Authentication:** Public.
- **Query Parameters:**
  - `scenario` (optional, string, default: `'NORMAL'`).
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.
- **Response JSON Example:**
```json
{
  "success": true,
  "scenario": "NORMAL",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "hotspotCount": 1,
  "hotspotPredictions": [
    {
      "id": "HOTSPOT_ZONE_WANKHEDE",
      "resourceId": "WANKHEDE",
      "zoneId": "ZONE_WANKHEDE",
      "name": "Wankhede Stadium & Perimeter Concourse",
      "location": { "latitude": 18.9389, "longitude": 72.8258 },
      "currentPressure": 86,
      "peakPredictedPressure": 94,
      "severity": "HIGH",
      "projectedOnsetMinutes": 10,
      "projectedDurationMinutes": 45,
      "radiusMeters": 350,
      "keyDrivers": ["Severe capacity saturation exceeding 85%"],
      "confidence": 0.95
    }
  ],
  "cascadeAnalysis": {
    "rootIncidentId": "INCIDENT_ZONE_WANKHEDE_1730910000",
    "rootResourceId": "ZONE_WANKHEDE",
    "timestamp": "2026-11-06T17:05:00.000Z",
    "affectedPathways": [
      [
        {
          "nodeId": "CASCADE_ORIGIN",
          "resourceId": "WANKHEDE_EXIT",
          "label": "Wankhede Gates & Concourse Egress",
          "currentPressure": 86,
          "projectedPressure": 86,
          "status": "HIGH",
          "leadTimeMinutes": 0,
          "influenceWeight": 1.0,
          "mitigationAvailable": true
        },
        {
          "nodeId": "CASCADE_TRANSIT",
          "resourceId": "CHURCHGATE",
          "label": "Churchgate Western Railway Terminal",
          "currentPressure": 71,
          "projectedPressure": 84,
          "status": "WATCH",
          "leadTimeMinutes": 20,
          "influenceWeight": 0.90,
          "mitigationAvailable": true
        }
      ]
    ],
    "summary": "Surge from ZONE_WANKHEDE is propagating outward across transit hubs and hospitality within a 40-minute window.",
    "confidence": 0.88
  }
}
```

---

#### Endpoint: `GET /api/predictions`
- **Controller/Router File:** `src/app/api/predictions/route.ts`
- **Purpose:** Generates 15, 30, and 60-minute forward predictive pressure models for all destination zones.
- **Authentication:** Public.
- **Query Parameters:**
  - `scenario` (optional, string, default: `'NORMAL'`).
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.
- **Response JSON Example:**
```json
{
  "success": true,
  "scenario": "NORMAL",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "predictions": [
    {
      "zoneId": "ZONE_WANKHEDE",
      "zoneName": "Wankhede Stadium & Perimeter Concourse",
      "currentPressure": 42,
      "pressureLevel": "NORMAL",
      "forecastHorizons": {
        "plus15min": 48,
        "plus30min": 56,
        "plus60min": 65
      },
      "confidence": 0.92,
      "dataProvenance": "SIMULATED",
      "timestamp": "2026-11-06T17:05:00.000Z"
    }
  ]
}
```

---

### 4.5 Recommendations & Decision Lifecycle Module

#### Endpoint: `GET /api/recommendations`
- **Controller/Router File:** `src/app/api/recommendations/route.ts`
- **Purpose:** Retrieves currently active and proposed operational interventions generated by the intelligence engine.
- **Authentication:** Public.
- **Query Parameters:**
  - `scenario` (optional, string, default: `'NORMAL'`).
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.
- **Response JSON Example:**
```json
{
  "success": true,
  "scenario": "NORMAL",
  "timestamp": "2026-11-06T17:05:00.000Z",
  "interventionCount": 1,
  "interventions": [
    {
      "id": "INT_DIVERT_MARINE_LINES_1730910000",
      "type": "REROUTE_ATTENDEES",
      "title": "Divert Northern Foot Traffic to Marine Lines Station",
      "description": "Churchgate concourse has reached 90% capacity. Signal attendee navigation to direct northbound commuters to Marine Lines.",
      "targetZoneId": "ZONE_CHURCHGATE",
      "secondaryAffectedResourceIds": ["MARINE_LINES"],
      "status": "PROPOSED",
      "urgency": "HIGH",
      "requiresApproval": true,
      "approvalRoleRequired": "ORGANIZER",
      "rationale": "Churchgate platform capacity exceeding safety threshold (90%) while Marine Lines has 45% available buffer.",
      "contributingSignals": [
        "Zone pressure: 90%",
        "Active crowd count: 9000 persons",
        "Queue wait time exceeding 15 minutes"
      ],
      "expectedPressureReductionPercent": 22,
      "timeToEffectMinutes: 10,
      "confidenceScore": 0.91,
      "proposedAt": "2026-11-06T17:00:00.000Z",
      "expiresAt": "2026-11-06T18:00:00.000Z",
      "rollbackFeasible": true,
      "rollbackPlan": "Restore standard route guidance on attendee navigation screens."
    }
  ]
}
```

---

#### Endpoint: `POST /api/recommendations/[id]/approve`
- **Controller/Router File:** `src/app/api/recommendations/[id]/approve/route.ts`
- **Purpose:** Human-in-the-loop decision endpoint. Approves an intervention, updating its lifecycle status to `APPROVED`, persisting it to the database, and generating an immutable audit event.
- **Authentication:** Authenticated Organizer (resolves profile via `getCurrentProfile("ORGANIZER")` or accepts actor override).
- **Path Parameters:**
  - `id` (mandatory, string): Intervention ID.
- **Request Body (Optional JSON):**
```json
{
  "actorId": "USER_COMMAND_LEAD_01"
}
```
- **Response Status Codes:**
  - `200 OK`: Intervention approved.
  - `500 Internal Server Error`: Approval failed or invalid status transition.
- **Response JSON Example:**
```json
{
  "success": true,
  "message": "Intervention 'INT_DIVERT_MARINE_LINES_1730910000' successfully approved by USER_COMMAND_LEAD_01.",
  "intervention": {
    "id": "INT_DIVERT_MARINE_LINES_1730910000",
    "status": "APPROVED",
    "approvedAt": "2026-11-06T17:05:00.000Z",
    "approvedByUserId": "USER_COMMAND_LEAD_01"
  },
  "auditRecord": {
    "id": "AUDIT_1730910300000",
    "timestamp": "2026-11-06T17:05:00.000Z",
    "actorId": "USER_COMMAND_LEAD_01",
    "actorRole": "ORGANIZER",
    "category": "RECOMMENDATION_APPROVAL",
    "targetEntityType": "RECOMMENDATION",
    "targetEntityId": "INT_DIVERT_MARINE_LINES_1730910000",
    "changeSummary": "Operational intervention 'Divert Northern Foot Traffic to Marine Lines Station' was APPROVED by USER_COMMAND_LEAD_01 (ORGANIZER)",
    "previousState": { "status": "PROPOSED" },
    "newState": { "status": "APPROVED" }
  }
}
```
- **Database Operations:**
  - Updates `operational_interventions` table.
  - Inserts row into `audit_events`.

---

#### Endpoint: `POST /api/recommendations/[id]/reject`
- **Controller/Router File:** `src/app/api/recommendations/[id]/reject/route.ts`
- **Purpose:** Rejects an intervention, updating its lifecycle status to `REJECTED`, logging reasons, and writing an audit record.
- **Authentication:** Authenticated Organizer.
- **Path Parameters:** `id` (string).
- **Response Status Codes:** `200 OK`, `500 Internal Server Error`.

---

### 4.6 Partner Management & Commercial Inventory Module

#### Endpoint: `POST /api/partner/inventory`
- **Controller/Router File:** `src/app/api/partner/inventory/route.ts`
- **Purpose:** Updates live restaurant inventory, available covers, and expected reservations. Enforces role-based and restaurant-scoped access control.
- **Authentication:** Requires authenticated Clerk session. User must have role `ORGANIZER` or hold a `restaurant_memberships` record for the specified restaurant.
- **Path Parameters:** None.
- **Request Body Schema:**
  - `restaurantId` (mandatory, string): Target restaurant identifier (e.g. `'R1'`).
  - `availableTables` (optional/coerced, integer): Unoccupied tables.
  - `availableCovers` (optional/coerced, integer): Available seats.
  - `expectedCovers` (optional/coerced, integer): Reserved/expected guests.
  - `outOfOrderTables` (optional/coerced, integer): Maintenance tables.
  - `notes` (optional, string): Operational comments.
- **Request JSON Example:**
```json
{
  "restaurantId": "R1",
  "availableTables": 8,
  "availableCovers": 32,
  "expectedCovers": 50,
  "outOfOrderTables": 1,
  "notes": "Rain cleared; terrace seating open."
}
```
- **Response Status Codes:**
  - `200 OK`: Inventory successfully logged and updated.
  - `400 Bad Request`: Missing mandatory `restaurantId`.
  - `401 Unauthorized`: No active Clerk session.
  - `403 Forbidden`: User does not hold authorization to manage this restaurant.
  - `404 Not Found`: Profile not found.
  - `500 Internal Server Error`: Database error.
- **Response JSON Example:**
```json
{
  "success": true,
  "message": "Restaurant inventory updated successfully.",
  "inventoryRecord": {
    "id": "770e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "R1",
    "reportedByProfileId": "550e8400-e29b-41d4-a716-446655440000",
    "availableTables": 8,
    "availableCovers": 32,
    "expectedCovers": 50,
    "outOfOrderTables": 1,
    "notes": "Rain cleared; terrace seating open.",
    "recordedAt": "2026-11-06T17:05:00.000Z"
  }
}
```
- **Database Operations:**
  1. Checks permission via `SELECT FROM restaurant_memberships WHERE profile_id = :pId AND restaurant_id = :rId`.
  2. Inserts audit/history row into `restaurant_inventory`.
  3. Updates current capacity on `restaurants` entity (`available_covers`, `expected_reservations`, `updated_at`).

---

### 4.7 Scenario Simulation & Single-Source-of-Truth Module

#### Endpoint: `GET /api/scenarios`
- **Controller/Router File:** `src/app/api/scenarios/route.ts`
- **Purpose:** Global single-source-of-truth endpoint for Web and Flutter mobile applications. Synchronizes active scenario, version counter, fused zones, interventions, hotels, restaurants, and alerts.
- **Authentication:** Public with CORS (`Access-Control-Allow-Origin: *`).
- **Response Status Codes:** `200 OK`.
- **Response JSON Example:**
```json
{
  "success": true,
  "activeScenario": "NORMAL",
  "scenarioVersion": 1,
  "isRec1Approved": false,
  "scenarioDetails": {
    "label": "Baseline / Normal Inflow",
    "description": "Standard arrival patterns prior to event commencement"
  },
  "zones": [],
  "interventions": [],
  "hotels": [],
  "restaurants": [],
  "alerts": [],
  "availableScenarios": [
    { "id": "NORMAL", "label": "Baseline / Normal Inflow" },
    { "id": "POST_EVENT_SURGE", "label": "Full Stadium Egress Wave" }
  ],
  "timestamp": "2026-11-06T17:05:00.000Z"
}
```

---

#### Endpoint: `POST /api/scenarios`
- **Controller/Router File:** `src/app/api/scenarios/route.ts`
- **Purpose:** Updates active scenario or toggles recommendation approval globally across all connected web and mobile clients.
- **Authentication:** Public with CORS.
- **Request Body Schema:**
  - `scenarioId` (optional, string): One of the valid ScenarioIds.
  - `isRec1Approved` (optional, boolean): Flag approving recommendation #1.
- **Request JSON Example:**
```json
{
  "scenarioId": "POST_EVENT_SURGE",
  "isRec1Approved": true
}
```
- **Response Status Codes:** `200 OK`, `400 Bad Request`, `500 Internal Server Error`.

---

#### Endpoint: `OPTIONS /api/scenarios`
- **Controller/Router File:** `src/app/api/scenarios/route.ts`
- **Purpose:** Handles CORS preflight requests from mobile and web origins. Returns `204 No Content`.

---

## 5. Authentication & Authorization

### 5.1 Authentication Mechanism
- **Provider:** **Clerk Authentication** (`@clerk/nextjs`).
- **Token Format:** Cryptographically signed RS256 JSON Web Tokens (JWT) issued by Clerk's identity service.
- **Session Verification:** Checked server-side via `auth()` and `currentUser()` from `@clerk/nextjs/server`.
- **Public vs. Protected Routes:** Defined in `src/proxy.ts` (and root `proxy.ts`). Public routes include `/`, `/login(.*)`, `/sign-in(.*)`, `/sign-up(.*)`, `/attendee(.*)`, `/api/observations(.*)`, and `/__clerk(.*)`.

### 5.2 Registration and Auto-Provisioning Workflow
1. When a user authenticates via Clerk, the backend invokes `getCurrentProfile(intendedRole)` in `src/services/authService.ts`.
2. It queries PostgreSQL for an existing profile by `clerk_user_id`.
3. If no record exists, it automatically provisions a new row in `profiles` with the user's email, name, and an initial role (`ATTENDEE`, `ORGANIZER`, or `RESTAURANT_PARTNER`).
4. For `RESTAURANT_PARTNER` profiles, it automatically seeds an initial membership linking the partner to restaurant `'R1'` (Trishna) with role `'MANAGER'`.

### 5.3 Role-Based Access Control (RBAC) Matrix

| Role | Capabilities | Protected Routes / Guards |
|---|---|---|
| **ORGANIZER** | Full destination command: view maps, run simulations, approve/reject interventions, access all partner data | `requireOrganizer()`, `POST /api/recommendations/*`, `/organizer/*` |
| **RESTAURANT_PARTNER** | Property-scoped access: view assigned restaurant inventory, submit table/cover updates | `requireRestaurantPartner()`, `POST /api/partner/inventory`, `/partner/*` |
| **ATTENDEE** | Public journey navigation, crowd-aware routing, food & hotel discovery | Public routes (`/attendee/*`) |
| **SYSTEM** | Automated sensor emulation, edge telemetry ingestion | `POST /api/observations` |

### 5.4 Scoped Membership Model
For partners, authorization is scoped by the `restaurant_memberships` table:
- **`OWNER`**: Complete management over the assigned restaurant property.
- **`MANAGER`**: Ability to report table availability, reservations, and inventory logs.
- **`STAFF`**: Read-only or basic status logging.
- When `POST /api/partner/inventory` executes, it checks:
  ```typescript
  if (profile.role !== "ORGANIZER") {
    const membership = await checkRestaurantAccess(profile.id, restaurantId);
    if (!membership) return 403 Forbidden;
  }
  ```

---

## 6. Business Logic & Processing Pipelines

### 6.1 Telemetry Ingestion Pipeline (`src/services/ingestionPipeline.ts`)
Processes incoming observations through 5 sequential validation gates:
1. **Mandatory Schema Validation:** Verifies `id`, `sourceId`, `zoneId`, and `value` exist. Rejections are logged to `eventLogger`.
2. **Range & Sanity Validation:** Discards negative counts, `NaN`, or non-numeric values.
3. **LRU Deduplication:** Maintains a cache of up to 2,000 recently processed IDs. Duplicate IDs are rejected with zero pipeline degradation.
4. **Freshness Scoring:** Calculates elapsed time (`Date.now() - observedAt`). Observations older than 180 seconds are downgraded to `"STALE"`.
5. **Confidence Bounds Checking:** Clamps confidence to `[0.1, 1.0]`, applying a 30% penalty if the quality status is `"STALE"`.

```mermaid
flowchart TD
    RAW[Raw Telemetry Payload] --> G1{Mandatory Fields<br/>id, sourceId, zoneId, value?}
    G1 -- No --> Q1[Quarantine & Log Rejection]
    G1 -- Yes --> G2{Sanity Check<br/>value >= 0 and not NaN?}
    G2 -- No --> Q2[Quarantine & Log Range Error]
    G2 -- Yes --> G3{LRU Cache<br/>ID seen recently?}
    G3 -- Yes --> DROP[Deduplicate & Drop]
    G3 -- No --> G4{Freshness Age<br/>Age > 180 seconds?}
    G4 -- Yes --> STALE[Tag STALE & Apply 0.7x Confidence Penalty]
    G4 -- No --> FRESH[Tag FRESH]
    STALE --> ACCEPT[Accept into Ring Buffer]
    FRESH --> ACCEPT
```

### 6.2 Zone-Aware Sensor Fusion Engine (`src/services/sensorFusionEngine.ts`)
Aggregates multi-source observations into an operational state:
- **Operational Capacity Basis:** Distinguishes between stadium seating capacity (37,000) and perimeter concourse egress throughput capacity (4,000 active persons in egress wave) to calculate true pressure.
- **Weighted Multi-Sensor Fusion:**
  $$\text{Fused Occupancy} = \frac{\sum (\text{Value}_i \times \text{Weight}_i)}{\sum \text{Weight}_i}$$
  where:
  $$\text{Weight}_i = \text{Confidence}_i \times \text{DeviceReliability}_i \times \text{FreshnessFactor}_i$$
- **Conflict Detection:** Flags discrepancy warnings if optical sensor readings deviate from historical trend models by more than threshold tolerances.

### 6.3 Short-Horizon Forecasting Engine (`src/services/forecastingService.ts`)
Generates 15, 30, 45, and 60-minute predictions using scenario-aware velocity curves:
- **Surge Velocity Coefficients:**
  - `POST_EVENT_SURGE`: $+0.35$ percentage points / minute.
  - `TRANSPORT_DISRUPTION`: $+0.45$ percentage points / minute.
  - `HEAVY_RAIN`: $+0.25$ percentage points / minute.
  - `NORMAL`: $-0.05$ percentage points / minute (natural dissipation).
- **Uncertainty Bounds:** Expands confidence intervals by $\pm 3.5\%$ per 15-minute horizon step.

### 6.4 Hotspot Detection & Cascade Spillover Engine (`src/services/hotspotAndCascadeEngine.ts`)
1. **Hotspot Detection:** Identifies zones where pressure $\ge 75\%$, trend is `"INCREASING"`, or available buffer $< 15\%$.
2. **Cascade Modeling:** Computes propagation delays outward along the urban network:
   - Origin: Wankhede Gates ($T = 0\text{ min}$, weight $1.0$).
   - Corridor: Marine Drive ($T = 12\text{ min}$, weight $0.85$).
   - Transit: Churchgate Station ($T = 20\text{ min}$, weight $0.90$).
   - Staging Bay: Taxi Bay ($T = 25\text{ min}$, weight $0.70$).
   - Hospitality: Marine Drive Dining ($T = 40\text{ min}$, weight $0.60$).

### 6.5 Recommendation Lifecycle Engine (`src/services/recommendationLifecycleEngine.ts`)
Enforces governance and cooldown mechanics:
- **Duplicate Proposal Suppression:** Implements a 5-minute cooldown (`cooldownMs = 300,000`) per intervention type to prevent alert fatigue.
- **Data Honesty Guard:** Prevents automated critical intervention triggers in uninstrumented or low-confidence zones (`coverage.canTriggerOperationalRecommendations === false`).
- **Decision State Machine:** Enforces valid state transitions:
  $$\text{PROPOSED} \longrightarrow \text{APPROVED} \longrightarrow \text{EXECUTING} \longrightarrow \text{EXECUTED} \longrightarrow \text{COMPLETED}$$
  $$\text{PROPOSED} \longrightarrow \text{REJECTED}$$
  Execution attempts on unapproved interventions throw strict runtime exceptions.

### 6.6 Computer Vision Pipeline (`cv_bridge/`)
- **YOLOv12 Person Detector:** Runs inference strictly on Class 0 (`'person'`). Non-person classes are ignored.
- **ByteTrack Trajectory Tracking:** Associates detections across frames using Kalman filters and Hungarian algorithm associations.
- **Centroid Ring Buffer:** Retains bounding box centroids in a volatile 30-frame ring buffer. Trajectories older than 30 frames are purged to preserve privacy.
- **Virtual Tripwire:** Intersects bounding box centroid vectors $(p_1 \to p_2)$ with a configured horizontal pixel line $(q_1 \to q_2)$ using counter-clockwise orientation math (`ccw`) to count directional line crossings (inflow vs. outflow).

---

## 7. Middleware & Request Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Web / Mobile / CV Bridge)
    participant MW as Clerk Middleware (src/proxy.ts)
    participant Route as Next.js API Route Handler
    participant Ingest as Ingestion Pipeline / Service
    participant Auth as AuthService & RBAC
    participant DB as PostgreSQL (Drizzle ORM)
    participant Buffer as Persistence Service (Memory Buffer)

    Client->>MW: HTTP Request (URL, Headers, JWT)
    Note over MW: Match route against isPublicRoute
    alt Public Route (/api/health, /api/observations, /api/scenarios)
        MW-->>Route: Forward Request Unmodified
    else Protected Route (/api/auth/me, /api/partner/inventory)
        MW->>MW: Validate Clerk Session Token
        alt Invalid / Expired Token
            MW-->>Client: 401 Unauthorized
        else Valid Token
            MW-->>Route: Inject Auth Session Context
        end
    end

    Route->>Auth: Validate Role & Scoped Permissions
    alt Permission Denied
        Route-->>Client: 403 Forbidden
    end

    Route->>Ingest: Execute Domain Logic / Validation
    alt Schema Validation Failed
        Ingest-->>Route: Reject with Error Details
        Route-->>Client: 400 Bad Request
    end

    Route->>DB: Execute SQL Queries (Pool Connection)
    alt Database Available
        DB-->>Route: Query Result
    else Database Offline / Unconfigured
        Route->>Buffer: Fall back to In-Memory Buffer
        Buffer-->>Route: Memory Snapshot
    end

    Route-->>Client: 200 OK (JSON Payload + CORS Headers)
```

### 7.1 Middleware Configuration (`src/proxy.ts`)
- **Framework:** `clerkMiddleware` from `@clerk/nextjs/server`.
- **Public Routes:** Registered using `createRouteMatcher`:
  - `'/'`
  - `'/login(.*)'`
  - `'/sign-in(.*)'`
  - `'/sign-up(.*)'`
  - `'/attendee(.*)'`
  - `'/api/observations(.*)'`
  - `'/__clerk(.*)'`
- **Route Matcher:** Excludes Next.js internal static assets (`_next`, `.ico`, `.css`, `.png`, etc.), running strictly on all API routes `/(api|trpc)(.*)`.

### 7.2 CORS Settings
Configured in `src/app/api/scenarios/route.ts` to allow cross-origin requests from Flutter mobile clients:
```typescript
const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
};
```

---

## 8. Configuration & Environment Variables

| Variable Name | Required | Default / Fallback | Purpose & Component |
|---|---|---|---|
| `DATABASE_URL` | Optional (Production: Required) | `postgresql://postgres:postgres@localhost:5432/junction_db` | PostgreSQL connection string for Drizzle ORM and `pg.Pool`. If absent, backend uses in-memory mode. |
| `NODE_ENV` | Optional | `development` | Runtime environment (`development`, `production`, `test`). Governs pool size and logging. |
| `PORT` | Optional | `3000` | HTTP port for the containerized application runner. |
| `HOSTNAME` | Optional | `0.0.0.0` | Network binding interface for Docker and Railway. |
| `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` | Required for Auth | None | Client-side Clerk public key for session token issuance. |
| `CLERK_SECRET_KEY` | Required for Auth | None | Server-side Clerk private key for verifying session tokens. |
| `JUNCTION_YOLO_MODEL` | Optional (CV Bridge) | `models/yolo/yolov12n.pt` | Path to YOLOv12 PyTorch weights file for computer vision inference. |
| `ROCKETRIDE_URI` | Optional | `http://localhost:50833` | Development platform service URI. |
| `ROCKETRIDE_APIKEY` | Optional | None | Development platform API key. |
| `ROCKETRIDE_DEPLOY_URI` | Optional | `https://api.rocketride.ai:443` | Production deployment orchestration URI. |
| `ROCKETRIDE_DEPLOY_APIKEY` | Optional | None | Production deployment orchestration API key. |
| `NEXT_PUBLIC_CESIUM_ION_TOKEN` | Optional | None | Cesium Ion access token for 3D digital twin terrain rendering. |
| `NEXT_PUBLIC_GOOGLE_TILES_KEY` | Optional | None | Google Photorealistic 3D Tiles API key for 3D stadium visualization. |
| `NEXT_PUBLIC_CARTO_API_KEY` | Optional | None | Carto map tile service API key. |

---

## 9. Dependencies & External Integrations

### 9.1 Node.js Backend Dependencies (`package.json`)
- **`next` (`16.3.4`):** App Router runtime hosting the API route handlers.
- **`drizzle-orm` (`^0.45.3`) & `drizzle-kit` (`^0.31.11`):** Type-safe SQL ORM and schema migration tools.
- **`pg` (`^8.23.0`) & `@types/pg` (`^8.23.1`):** PostgreSQL client with connection pooling.
- **`@clerk/nextjs` (`^7.9.7`):** Identity, session management, and JWT verification.
- **`dotenv` (`^18.0.4`):** Environment variable loading from `.env.local`.
- **`zustand` (`^5.0.3`):** State store used on client components.
- **`tsx` (Dev):** TypeScript execution engine for database migrations and integration test scripts.

### 9.2 Python Computer Vision Dependencies (`cv_bridge/requirements.txt`)
- **`torch` (`>=2.0.0`) & `torchvision` (`>=0.15.0`):** PyTorch deep learning inference runtime.
- **`ultralytics` (`>=8.0.0`):** YOLOv12 object detection engine and ByteTrack tracking.
- **`opencv-python-headless` (`>=4.7.0`):** Video frame decoding, bounding box drawing, and image manipulation.
- **`numpy` (`>=1.24.0`):** Vectorized mathematical calculations for centroids and tripwires.
- **`pydantic` (`>=2.0.0`):** Data validation and serialization.
- **`requests` (`>=2.28.0`):** HTTP client for POSTing telemetry to `/api/observations`.
- **`pytest` (`>=7.0.0`):** Unit testing framework for Python CV metrics.

### 9.3 External Integrations
1. **Clerk Identity Platform:** Cloud identity provider issuing user session tokens.
2. **Railway PaaS:** Cloud container hosting platform executing Docker builds and routing traffic.
3. **OpenStreetMap / Carto Tile CDN:** Cartographic basemap tile providers.
4. **Cesium Ion / Google Photorealistic 3D Tiles:** 3D digital twin visualization services.

---

## 10. Deployment & Infrastructure

### 10.1 Container Architecture (`Dockerfile`)
Multi-stage build optimized for minimal image size and security:
1. **Stage 1 (`deps`):** Base `node:20-alpine`. Installs `libc6-compat`, copies `package.json`, and executes `npm ci --ignore-scripts`.
2. **Stage 2 (`builder`):** Compiles TypeScript, bundles Next.js standalone build via `npm run build`.
3. **Stage 3 (`runner`):** Minimal Alpine Linux runner. Creates unprivileged system user `nextjs:nodejs` (`UID:GID 1001`), exposes port `3000`, copies `.next/standalone` artifacts, and executes `CMD ["node", "server.js"]`.

### 10.2 Railway Orchestration (`railway.json`)
```json
{
  "$schema": "https://railway.com/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "healthcheckPath": "/api/health",
    "healthcheckTimeout": 100,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

---

## 11. Error Handling & Validation

### 11.1 Error Response Formats
All API routes return standardized JSON error envelopes:
```json
{
  "success": false,
  "error": "Descriptive human-readable explanation of error condition"
}
```
For authentication endpoints:
```json
{
  "error": "Unauthorized"
}
```

### 11.2 Common Failure Modes & Handling
1. **Database Unavailability:** `PersistenceService` catches SQL exceptions gracefully and falls back to in-memory ring buffers (`IN_MEMORY_SESSION`), ensuring zero API downtime during database outages.
2. **Malformed Telemetry Ingest:** Handled in `IngestionPipeline`. Invalid observations are quarantined with a logged reason while remaining valid observations in the batch continue processing.
3. **Invalid Lifecycle Transitions:** `RecommendationLifecycleEngine` rejects illegal state transitions (e.g. attempting to execute a `PROPOSED` intervention without prior approval) with descriptive exceptions.

---

## 12. WebSocket / Real-Time Communication

> [!NOTE]
> **Implementation Status: NOT IMPLEMENTED in Current Codebase**
> The current codebase does not implement persistent WebSocket or Server-Sent Events (SSE) connections. Real-time updates across web and mobile clients are currently achieved via **HTTP polling against `/api/scenarios` and `/api/observations`**.
>
> In the architecture blueprint (`docs/JUNCTION_Production_System_Implementation_Blueprint.md`), an **SSE Event Broker** backed by Redis Pub/Sub was specified for version 1.0. The Spring Boot migration reference below provides the production implementation for both WebSockets (STOMP) and SSE (`SseEmitter`).

---

## 13. Testing Architecture

### 13.1 Existing Test Structure
The repository contains two distinct test harnesses:
1. **TypeScript Full-System Integration Test Suite (`scripts/test-full-system.ts`):** Executed via `npm run test:full` (`tsx scripts/test-full-system.ts`). Contains 5 comprehensive test suites:
   - **Suite 1:** Operational Zone Coverage & Honesty Contracts (checks coverage profiles, confidence caps, and fallback behaviors).
   - **Suite 2:** Multi-Modal Ingestion & Sensor Fusion (validates LRU deduplication, quality status scoring, and weighted fusion formulas).
   - **Suite 3:** OSM Road Network Routing (validates waypoint snapping and path adherence $\le 5\text{m}$).
   - **Suite 4:** Recommendation Evidence & Lifecycle (tests proposal generation, cooldown duplicate suppression, approval, execution guards, and persistence).
   - **Suite 5:** Hospitality Demand Propagation & Vouchers (validates hotel check-in demand and restaurant dining vouchers).
2. **TypeScript Input Provider Verification (`scripts/test-input-system.ts`):** Executed via `npm run test:input`. Verifies CCTV, beacon, and counter modality providers.
3. **Python Computer Vision Pytest Suite (`cv_bridge/tests/`):** Configured via `pytest.ini` (`pythonpath = .`, `testpaths = cv_bridge/tests`).
   - `test_metrics.py`: Verifies bounding box centroid math, line intersection algorithms (`ccw`), empty frame handling, and density per square meter.
   - `test_serializer.py`: Verifies schema serialization, metadata structure, and JSON output formatting.

---

## 14. Spring Boot Migration Considerations

### 14.1 Component Mapping Matrix

| Existing Component | Current Implementation | Target Spring Boot 3.3.x Equivalent |
|---|---|---|
| **API Endpoints** | Next.js API Routes (`src/app/api/*/route.ts`) | Spring Web `@RestController` & `@RequestMapping` |
| **Authentication** | Clerk Server SDK (`@clerk/nextjs/server`) | Spring Security 6 `SecurityFilterChain` with OAuth2 Resource Server / JWT |
| **Data Access** | Drizzle ORM (`drizzle-orm`) + `pg.Pool` | Spring Data JPA (`JpaRepository`) + Hibernate 6 + HikariCP |
| **Schema Validation** | TypeScript TypeGuards & Custom Checks | Jakarta Validation (`@Valid`, `@NotNull`, `@Min`, `@Pattern`) |
| **Database Migrations** | `src/db/migrate.ts` + Drizzle Kit | Flyway Database Migrations (`db/migration/V1__*.sql`) |
| **Dependency Injection** | Module Imports & Global Singleton Instances | Spring Core IoC (`@Service`, `@Component`, `@Configuration`, `@Bean`) |
| **Configuration** | `process.env` + `.env` | Spring Boot `application.yml` + `@ConfigurationProperties` |
| **Middleware** | `proxy.ts` (Clerk middleware) | Spring `OncePerRequestFilter` / `HandlerInterceptor` |
| **CORS** | Route-level headers in `route.ts` | Spring `@CrossOrigin` or `WebMvcConfigurer.addCorsMappings` |
| **Audit & Events** | `persistenceService.ts` | Spring Data JPA Auditing (`@EntityListeners(AuditingEntityListener.class)`) |
| **Async / Background** | Background Promises / Node.js timers | Spring `@EnableAsync` (`CompletableFuture`) & `@Scheduled` |
| **Real-time Push** | HTTP Polling (Current) | Spring WebSocket (`@EnableWebSocketMessageBroker`) or `SseEmitter` |

---

### 14.2 Database Layer: Drizzle ORM → Spring Data JPA

#### Entity 1: `ProfileEntity.java`
```java
package com.junction.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "clerk_user_id", nullable = false, unique = true)
    private String clerkUserId;

    @Column(nullable = false)
    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ATTENDEE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum UserRole {
        ORGANIZER, RESTAURANT_PARTNER, ATTENDEE
    }
}
```

#### Entity 2: `RestaurantEntity.java`
```java
package com.junction.domain.partner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantEntity {

    @Id
    private String id; // e.g. "R1"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String zone;

    private String cuisine;
    private String address;

    @Column(name = "total_capacity", nullable = false)
    @Builder.Default
    private Integer totalCapacity = 100;

    @Column(name = "available_covers", nullable = false)
    @Builder.Default
    private Integer availableCovers = 50;

    @Column(name = "expected_reservations", nullable = false)
    @Builder.Default
    private Integer expectedReservations = 0;

    @Column(name = "price_range")
    @Builder.Default
    private String priceRange = "₹₹";

    private Double lat;
    private Double lng;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

#### Entity 3: `RestaurantMembershipEntity.java`
```java
package com.junction.domain.partner.entity;

import com.junction.domain.auth.entity.ProfileEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "restaurant_memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "restaurant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantMembershipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipRole role = MembershipRole.STAFF;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum MembershipRole {
        OWNER, MANAGER, STAFF
    }
}
```

#### Entity 4: `AuditEventEntity.java`
```java
package com.junction.domain.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventEntity {

    @Id
    private String id;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @Column(nullable = false)
    private String category;

    @Column(name = "target_entity_type", nullable = false)
    private String targetEntityType;

    @Column(name = "target_entity_id", nullable = false)
    private String targetEntityId;

    @Column(name = "change_summary", nullable = false, length = 1000)
    private String changeSummary;

    @Column(length = 2000)
    private String rationale;

    @Column(name = "previous_state_json", columnDefinition = "TEXT")
    private String previousStateJson;

    @Column(name = "new_state_json", columnDefinition = "TEXT")
    private String newStateJson;

    @Column(name = "is_simulated_scenario", nullable = false)
    @Builder.Default
    private Boolean isSimulatedScenario = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}
```

#### Entity 5: `OperationalInterventionEntity.java`
```java
package com.junction.domain.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "operational_interventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalInterventionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "target_zone_id", nullable = false)
    private String targetZoneId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PROPOSED";

    @Column(nullable = false)
    @Builder.Default
    private String urgency = "MEDIUM";

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column(name = "approval_role_required")
    private String approvalRoleRequired;

    @Column(nullable = false, length = 2000)
    private String rationale;

    @Column(name = "confidence_score", nullable = false)
    @Builder.Default
    private Double confidenceScore = 0.8;

    @Column(name = "expected_pressure_reduction_percent", nullable = false)
    @Builder.Default
    private Integer expectedPressureReductionPercent = 0;

    @CreationTimestamp
    @Column(name = "proposed_at", nullable = false)
    private Instant proposedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by_user_id")
    private String approvedByUserId;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
}
```

---

### 14.3 API Controllers & Request Mapping

#### Controller 1: `ObservationIngestionController.java`
```java
package com.junction.api.controller;

import com.junction.domain.ingestion.dto.IngestionBatchRequest;
import com.junction.domain.ingestion.dto.IngestionBatchResponse;
import com.junction.domain.ingestion.dto.ObservationStatusResponse;
import com.junction.domain.ingestion.service.IngestionPipelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/observations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ObservationIngestionController {

    private final IngestionPipelineService ingestionPipelineService;

    @PostMapping
    public ResponseEntity<IngestionBatchResponse> ingestObservations(
            @Valid @RequestBody IngestionBatchRequest request) {
        IngestionBatchResponse result = ingestionPipelineService.processBatch(request.getObservations());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ObservationStatusResponse> getObservationStatus() {
        return ResponseEntity.ok(ingestionPipelineService.getStatus());
    }
}
```

#### Controller 2: `PartnerInventoryController.java`
```java
package com.junction.api.controller;

import com.junction.domain.partner.dto.UpdateInventoryRequest;
import com.junction.domain.partner.dto.UpdateInventoryResponse;
import com.junction.domain.partner.service.PartnerInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner/inventory")
@RequiredArgsConstructor
public class PartnerInventoryController {

    private final PartnerInventoryService partnerInventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'RESTAURANT_PARTNER')")
    public ResponseEntity<UpdateInventoryResponse> updateInventory(
            @Valid @RequestBody UpdateInventoryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String clerkUserId = jwt.getSubject();
        UpdateInventoryResponse response = partnerInventoryService.updateInventory(request, clerkUserId);
        return ResponseEntity.ok(response);
    }
}
```

#### Controller 3: `RecommendationController.java`
```java
package com.junction.api.controller;

import com.junction.domain.recommendation.dto.ApproveInterventionRequest;
import com.junction.domain.recommendation.dto.InterventionActionResponse;
import com.junction.domain.recommendation.dto.RecommendationsResponse;
import com.junction.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<RecommendationsResponse> getRecommendations(
            @RequestParam(defaultValue = "NORMAL") String scenario) {
        return ResponseEntity.ok(recommendationService.getActiveRecommendations(scenario));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<InterventionActionResponse> approveIntervention(
            @PathVariable String id,
            @RequestBody(required = false) ApproveInterventionRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        String actorId = (jwt != null) ? jwt.getSubject() : (body != null ? body.actorId() : "ORGANIZER_OPERATOR");
        return ResponseEntity.ok(recommendationService.approveIntervention(id, actorId));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<InterventionActionResponse> rejectIntervention(
            @PathVariable String id,
            @RequestBody(required = false) ApproveInterventionRequest body,
            @AuthenticationPrincipal Jwt jwt) {
        String actorId = (jwt != null) ? jwt.getSubject() : (body != null ? body.actorId() : "ORGANIZER_OPERATOR");
        return ResponseEntity.ok(recommendationService.rejectIntervention(id, actorId));
    }
}
```

---

### 14.4 Spring Security & Clerk JWT Verification

```java
package com.junction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/health",
                    "/api/observations/**",
                    "/api/scenarios/**",
                    "/api/zones/**",
                    "/api/hotspots/**",
                    "/api/predictions/**",
                    "/api/audit"
                ).permitAll()
                .requestMatchers("/api/partner/**").authenticated()
                .requestMatchers("/api/recommendations/*/approve", "/api/recommendations/*/reject").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

### 14.5 Application Configuration (`application.yml`)

```yaml
server:
  port: ${PORT:8080}
  shutdown: graceful

spring:
  application:
    name: junction-core-backend

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/junction_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 5000
      max-lifetime: 1800000

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    open-in-view: false
    show-sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration

  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${CLERK_JWKS_URI:https://api.clerk.com/v1/jwks}

junction:
  cooldown-ms: 300000
  ingestion:
    max-cache-size: 2000
    stale-threshold-seconds: 180
  fusion:
    default-safety-buffer-percent: 7
```

---

### 14.6 Potential Migration Challenges & Architectural Risks

1. **Dual Storage Fallback Preservation:** The existing backend executes seamlessly in `IN_MEMORY_SESSION` mode if PostgreSQL is down or unconfigured. In Spring Boot, standard JPA autoconfiguration will abort application startup if the datasource is unreachable.  
   *Mitigation:* Create a custom conditional `PersistenceStrategy` or configure an embedded H2/in-memory database fallback using `@Profile("dev")` or `@ConditionalOnProperty`.
2. **Computer Vision Bridge Protocol:** The Python YOLOv12 + ByteTrack bridge currently pushes directly to `/api/observations` via HTTP POST.  
   *Mitigation:* Maintain `/api/observations` with identical payload semantics (`NormalizedObservation`), ensuring the Python `cv_bridge` requires zero code changes to interface with Spring Boot.
3. **Clerk Token Validation:** Clerk JWKS endpoints occasionally rotate public keys.  
   *Mitigation:* Use Spring Security's `NimbusJwtDecoder` with built-in JWK Set caching to prevent network overhead on every authenticated request.
4. **CORS Handling for Flutter Native Client:** Mobile apps communicate with `/api/scenarios`. Ensure Spring Security does not block CORS preflight `OPTIONS` requests.

---

## 15. Migration Checklist

### 1. Database & Schema Migration
- [ ] Create Flyway baseline script `V1__create_junction_tables.sql` mapping `profiles`, `restaurants`, `restaurant_memberships`, `restaurant_inventory`, `audit_events`, and `operational_interventions`.
- [ ] Create Flyway seed script `V2__seed_baseline_restaurants.sql` matching `src/db/migrate.ts`.
- [ ] Implement JPA entities with Lombok and Hibernate annotations.
- [ ] Configure HikariCP connection pool with idle and connection timeouts matching `src/db/index.ts`.

### 2. Security & Authentication
- [ ] Configure Spring Security 6 `SecurityFilterChain` with stateless session management.
- [ ] Implement Clerk JWT decoding via JWKS URI.
- [ ] Implement `CurrentUserArgumentResolver` to inject authenticated profile IDs into controllers.
- [ ] Implement RBAC method security (`@PreAuthorize("hasRole('ORGANIZER')")`).
- [ ] Implement scoped restaurant permission verification service.

### 3. API Endpoints Parity
- [ ] Implement `GET /api/health` with storage diagnostics.
- [ ] Implement `GET /api/auth/me` with auto-provisioning logic.
- [ ] Implement `GET /api/audit` with limit query parameter and descending timestamp sort.
- [ ] Implement `POST /api/observations` with LRU deduplication and freshness tagging.
- [ ] Implement `GET /api/observations` telemetry status endpoint.
- [ ] Implement `POST /api/partner/inventory` with history logging and restaurant entity updates.
- [ ] Implement `GET /api/zones` and `GET /api/zones/{zoneId}`.
- [ ] Implement `GET /api/hotspots` with spatial cascade projections.
- [ ] Implement `GET /api/predictions` with 15/30/60m horizons.
- [ ] Implement `GET /api/recommendations`.
- [ ] Implement `POST /api/recommendations/{id}/approve` and `POST /api/recommendations/{id}/reject`.
- [ ] Implement `GET`, `POST`, `OPTIONS /api/scenarios` with global CORS headers.

### 4. Domain Logic & Intelligence Engines
- [ ] Port `IngestionPipeline` (LRU deduplication, schema validation, range checks).
- [ ] Port `SensorFusionEngine` (operational capacity basis, weighted sensor fusion, conflict detection).
- [ ] Port `ForecastingService` (scenario curve velocity calculations, expanding uncertainty intervals).
- [ ] Port `HotspotAndCascadeEngine` (hotspot thresholds and outward multi-stage propagation pathways).
- [ ] Port `RecommendationLifecycleEngine` (5-minute cooldown suppression, decision state machine, data honesty checks).
- [ ] Port `HospitalityDemandService` (demand propagation formulas and commercial voucher generation).

### 5. Verification & Testing
- [ ] Implement JUnit 5 + MockMvc unit tests for all REST controllers.
- [ ] Implement Testcontainers (PostgreSQL) integration tests replicating `scripts/test-full-system.ts`.
- [ ] Run `python -m cv_bridge.inference` pointing `--http-url` at the Spring Boot server (`http://localhost:8080/api/observations`) and verify 100% acceptance with zero quarantined records.
- [ ] Verify Flutter mobile app connects to `GET /api/scenarios` and reflects real-time scenario updates.

