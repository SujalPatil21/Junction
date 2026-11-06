# JUNCTION Infrastructure & Deployment

This directory contains the deployment and infrastructure assets for the **JUNCTION Intelligent Destination Orchestration Platform**.

## Contents
- `docker-compose.yml`: Multi-container local orchestration for PostgreSQL, Spring Boot backend, and Next.js frontend.
- `railway/`: Railway platform deployment configurations.
- `deployment/`: Detailed installation and deployment guides.

## Running Locally with Docker Compose
```bash
cd infrastructure
docker-compose up --build
```
This starts:
- **PostgreSQL 16**: `localhost:5432`
- **Spring Boot Backend**: `http://localhost:8080` (API & Swagger UI at `/swagger-ui.html`)
- **Next.js Frontend**: `http://localhost:3000` (Presentation Dashboard)

