# 🚦 JUNCTION — Intelligent Destination Orchestration Platform

<p align="center">
  <strong>Intelligent Destination Orchestration for Mega-Events</strong><br>
  <em>Predictive Crowd Management, Multi-Modal Sensor Fusion, and Coordinated Urban Redistribution</em>
</p>

<p align="center">
  <img src="frontend/public/junction-qr.png" width="160" alt="Junction Attendee App QR Code" />
</p>

---

## 🏛️ Monorepo Architecture

JUNCTION is organized as a clean, production-ready, modular monorepo:

```text
Junction-App/
├── frontend/                 # Next.js 16 Presentation Frontend (React 19, Tailwind)
│   ├── src/                  # Pages, components, hooks, typed API client, proxy.ts
│   ├── public/               # Static assets, hero images, vector icons, APK download
│   ├── package.json          # Frontend dependencies & build scripts
│   ├── tsconfig.json         # TypeScript configuration with @/* alias
│   └── Dockerfile            # Multi-stage production container
│
├── backend/                  # Independent Spring Boot 4.1.2 Application Backend
│   ├── src/main/java/        # REST controllers, domain engines, JPA entities, security
│   ├── src/main/resources/   # Application configs (dev/prod), Flyway SQL migrations
│   ├── src/test/java/        # JUnit 5, MockMvc, and Testcontainers test suite
│   ├── pom.xml               # Maven configuration (Java 21 LTS)
│   └── Dockerfile            # Hardened Alpine Temurin 21 container
│
├── cv_bridge/                # Edge Computer Vision Subsystem (Python 3.11+)
│   ├── models/               # YOLOv12 model weights (yolov12n.pt, yolov12m.pt)
│   ├── tests/                # Pytest unit tests for metrics and serializers
│   ├── inference.py          # Real-time YOLOv12 + ByteTrack inference pipeline
│   ├── transport.py          # HTTP Bearer delivery to Spring Boot backend
│   └── config.example.json   # Camera calibration & tripwire setup
│
├── mobile/                   # Cross-Platform Mobile Applications
│   └── attendee_flutter_app/ # Flutter client for attendees (Android, iOS, Web)
│
├── contracts/                # Canonical API & Data Contracts
│   ├── openapi.yaml          # OpenAPI 3.0 specification for all backend endpoints
│   └── schemas/              # Reusable JSON schemas for observations and states
│
├── infrastructure/           # Deployment & Orchestration
│   ├── docker-compose.yml    # Multi-service local stack (PostgreSQL, Spring, Next.js)
│   ├── railway/              # Railway deployment configurations
│   └── deployment/           # Universal deployment guides
│
├── scripts/                  # Unified Repository Automation
│   ├── testing/              # System verification & input validation suites
│   ├── utilities/            # QR code generation and asset utilities
│   ├── development/          # Local dev helpers
│   └── deployment/           # Release automation scripts
│
├── assets/                   # Shared Media & Datasets
│   └── test-videos/          # Standardized CCTV test feeds and validation video
│
├── artifacts/                # Generated build outputs & inference logs
│
├── docs/                     # Authoritative Engineering Documentation
│   ├── BACKEND_DOCUMENTATION.md
│   ├── PROJECT_STRUCTURE.md
│   ├── MIGRATION_READINESS_REPORT.md
│   └── MIGRATION_LOG.md
│
├── .github/                  # GitHub Actions CI/CD workflows
├── .rocketride/              # 102 JSON schemas for tool integrations
├── .gitignore
├── package.json              # Monorepo root orchestration scripts
└── README.md                 # Primary system guide
```

---

## ⚡ Quick Start

### 1. Prerequisites
- **Node.js** 20+ and **npm**
- **Java 21 LTS** JDK
- **Python 3.11+** (for Computer Vision Bridge)
- **Docker** and Docker Compose (optional, for containerized run)

### 2. Run with Docker Compose (Recommended)
```bash
cd infrastructure
docker-compose up --build
```
- **Next.js Web Portal:** [http://localhost:3000](http://localhost:3000)
- **Spring Boot Backend:** [http://localhost:8080](http://localhost:8080)
- **OpenAPI Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **PostgreSQL 16:** `localhost:5432`

### 3. Run Manually for Local Development

#### Start Spring Boot Backend:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Start Next.js Frontend:
```bash
cd frontend
npm install
npm run dev
```

#### Run Computer Vision Bridge:
```bash
python -m cv_bridge.inference \
  --video assets/test-videos/demo_cctv.mp4 \
  --zone-id ZONE_WANKHEDE \
  --camera-id DEV_CCTV_WANKHEDE_01 \
  --resource-id WANKHEDE_EXIT \
  --http-url http://localhost:8080/api/observations \
  --frame-stride 2
```

---

## 🧪 Verification & Test Suites

Run test suites from the repository root:

```bash
# Master System Verification Suite (61 tests)
npm run test:full

# Input System & Sensor Fusion Suite (42 tests)
npm run test:input

# Computer Vision Bridge Test Suite (9 tests)
npm run test:cv

# Spring Boot Backend Unit & Integration Tests
npm run test:backend

# Next.js Frontend Production Build
npm run build
```

---

## 🔒 Security Architecture
- **Web Frontend**: Clerk Authentication with Next.js 16 `proxy.ts`.
- **Spring Boot Backend**: Spring Security OAuth2 Resource Server verifying Clerk RS256 JWT tokens via JWKS.
- **Edge Devices**: Constant-time verification of Bearer shared secrets for hardware camera observation ingestion.
- **Role-Based Access Control**: `ROLE_ORGANIZER`, `ROLE_RESTAURANT_PARTNER`, `ROLE_ATTENDEE`.
