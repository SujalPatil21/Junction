# JUNCTION — Performance & Load Testing Suite (k6)

This directory contains the automated performance, load, stress, spike, soak, and concurrency benchmarks for the **JUNCTION — Intelligent Destination Orchestration Platform**.

## 1. Prerequisites

- **k6**: Installed (`k6 version` >= v0.45 or v2.0+)
- **Spring Boot Backend**: Running on port `8080` (`npm run dev:backend` or maven command)
- **PostgreSQL**: Running on port `5432` with initialized `junction_dev` schema

## 2. Directory Structure

```
scripts/testing/k6/
├── config.js                      # Environment endpoints, secrets, and SLA thresholds
├── utils/
│   ├── auth.js                    # Bearer token and Clerk JWT utilities
│   └── test-data.js               # Synthetic test-data generators
├── scenarios/
│   ├── rest-smoke.js              # Smoke verification (2 VUs, 15s)
│   ├── rest-load.js               # Progressive load testing (5 -> 10 -> 25 -> 50 -> 100 -> 150 -> 250 VUs)
│   ├── rest-stress.js             # Stress test to determine breaking point (up to 350 VUs)
│   ├── rest-spike.js              # Sudden traffic surge test (10 -> 200 VUs)
│   ├── rest-soak.js               # Endurance / memory leak soak test (50 VUs, sustained)
│   ├── mixed-workload.js          # Peak throughput benchmark (250 VUs, high-rate)
│   └── websocket-concurrency.js   # Real-time WebSocket connection & polling fallback benchmark
└── README.md                      # This documentation
```

## 3. Running Benchmarks

### Quick Commands (via root `package.json`)

```bash
# Smoke test (verify endpoints and credentials)
npm run test:k6:smoke

# Progressive load test (5 to 250 VUs)
npm run test:k6:load

# Peak mixed workload (GeoWatch comparison target)
npm run test:k6:mixed

# Stress test (find breaking point)
npm run test:k6:stress

# Spike test (sudden crowd surges)
npm run test:k6:spike

# Soak / endurance test (memory and GC stability)
npm run test:k6:soak

# WebSocket & real-time polling concurrency
npm run test:k6:websocket
```

### Direct k6 Invocations with Summary Export

```bash
k6 run --summary-export=docs/performance-results/smoke-summary.json scripts/testing/k6/scenarios/rest-smoke.js
k6 run --summary-export=docs/performance-results/load-summary.json scripts/testing/k6/scenarios/rest-load.js
k6 run --summary-export=docs/performance-results/mixed-summary.json scripts/testing/k6/scenarios/mixed-workload.js
k6 run --summary-export=docs/performance-results/stress-summary.json scripts/testing/k6/scenarios/rest-stress.js
k6 run --summary-export=docs/performance-results/spike-summary.json scripts/testing/k6/scenarios/rest-spike.js
k6 run --summary-export=docs/performance-results/soak-summary.json scripts/testing/k6/scenarios/rest-soak.js
k6 run --summary-export=docs/performance-results/ws-summary.json scripts/testing/k6/scenarios/websocket-concurrency.js
```

## 4. Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| `TARGET_URL` | `http://localhost:8080` | Target Spring Boot backend URL |
| `EDGE_SECRET` | `dev-local-secret-not-for-production` | Secret for `POST /api/observations` |
| `CLERK_JWT` | *(optional)* | Authenticated Clerk RS256 token for RBAC routes |

