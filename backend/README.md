# JUNCTION Backend — Spring Boot Application

The official application backend for the **JUNCTION Intelligent Destination Orchestration Platform**.

## Tech Stack
- **Language / Runtime:** Java 21 (Eclipse Temurin LTS)
- **Framework:** Spring Boot 4.1.2 (Spring Framework 7, Jakarta EE 11)
- **Data & Persistence:** Spring Data JPA / Hibernate, PostgreSQL, Flyway
- **Caching:** Caffeine Cache (LRU Ingestion deduplication & zone states)
- **Security:** Spring Security 6 with Clerk OAuth2/OIDC JWT RS256 Verification & Edge Device Shared Secrets
- **Documentation:** SpringDoc OpenAPI 3 / Swagger UI

---

## Directory Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/junction/
│   │   │   ├── JunctionApplication.java
│   │   │   ├── common/             # Exceptions, DTOs, base utilities
│   │   │   ├── config/             # Security, Cache, Async, CORS configs
│   │   │   ├── entity/             # JPA entity definitions (6 core tables)
│   │   │   ├── health/             # Health check endpoints
│   │   │   ├── repository/         # Spring Data JPA repositories
│   │   │   ├── security/           # Edge device filters & auth tokens
│   │   │   └── service/            # Core business & domain engines
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/       # Flyway SQL migrations
│   └── test/                       # Unit & integration tests
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Getting Started

### 1. Prerequisites
- **Java 21 JDK** installed and available on `PATH`
- **Maven 3.9+** (or use container build)
- **PostgreSQL 15+** (for local development, or use dev profile defaults)

### 2. Environment Variables
Copy `.env.example` to `.env` and provide local or test credentials:
```bash
cp .env.example .env
```

### 3. Build & Run
```bash
# Build
mvn clean package -DskipTests

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verification Endpoints
- **Health Check:** `GET http://localhost:8080/api/health`
- **Actuator Health:** `GET http://localhost:8080/actuator/health`
- **Swagger UI (Dev only):** `http://localhost:8080/swagger-ui.html`

