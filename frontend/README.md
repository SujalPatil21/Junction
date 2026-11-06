# JUNCTION Frontend

The presentation and user interface layer of the **JUNCTION Intelligent Destination Orchestration Platform**, built with **Next.js 16** (App Router), **React 19**, **TypeScript 5**, and **Tailwind CSS**.

## Architecture & Responsibilities
- **Frontend Presentation ONLY**: Does not execute database queries or backend domain engines directly.
- **Direct Spring Boot Integration**: Communicates with the Spring Boot application backend (`http://localhost:8080`) through typed API client (`src/lib/api-client/junctionApi.ts`) and Next.js rewrites.
- **Role-Based Portals**:
  - **City Command Lead / Organizer**: `/` & `/dashboard`
  - **Commercial Restaurant Partner**: `/partner`
  - **Attendee Mobile / Web Experience**: `/attendee`
- **Authentication**: Clerk OAuth2 / Session management with `src/proxy.ts`.

## Getting Started

### 1. Install Dependencies
```bash
npm install
```

### 2. Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Ensure `NEXT_PUBLIC_API_URL` points to your Spring Boot backend (default: `http://localhost:8080`).

### 3. Run Development Server
```bash
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.

### 4. Production Build
```bash
npm run build
npm run start
```

