<div style="text-align: center;">

# Parking Nexus

**A lightweight Parking Lot Management System (MVP)**


[![Language: TypeScript](https://img.shields.io/badge/TypeScript-%5E5.9-blue)](https://www.typescriptlang.org/) 
[![Framework: React](https://img.shields.io/badge/React-%5E19.2.0-61DAFB)](https://reactjs.org/) 
[![UI: TailwindCSS](https://img.shields.io/badge/TailwindCSS-%5E3.4.16-06B6D4)](https://tailwindcss.com/) 
[![Backend: Spring Boot](https://img.shields.io/badge/Spring%20Boot-%5E3.5.8-6DB33F)](https://spring.io/projects/spring-boot) 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 🚀 Overview

Parking Nexus is an MVP parking lot management system with a Spring Boot backend and a Vite + React + TypeScript frontend. The focus is on functionality: user authentication (JWT), searching & reserving parking spots, checking in/out, session billing, and admin tools for user and activity log management.

This repository contains the full-stack scaffold with Docker support and local development scripts.

---

## 📁 Tech Stack

- Frontend
  - React 19 (Vite + TypeScript)
  - TailwindCSS
  - shadcn/ui (Radix + Tailwind components)
  - Axios (HTTP client)
  - @tanstack/react-query (server state)
  - Zustand (auth state)
  - react-router-dom (routing)

- Backend
  - Spring Boot 3.5.8 (Java 17)
  - Spring Security with JWT
  - Spring Data JPA + Flyway
  - PostgreSQL 15
  - Micrometer + Prometheus + Grafana for observability
  - Caffeine for local caching (Spring Cache abstraction)

- Dev / Tooling
  - Gradle (wrapper included) for backend
  - Vite + npm for frontend
  - Docker / docker-compose for full-stack local deployment

---

## 📂 Project Structure (top-level)

Root layout (important files and folders):

```
/ (repo root)
├─ backend/                # Spring Boot application (Gradle)
│  ├─ src/main/resources/application.yml
│  ├─ Dockerfile
│  └─ build.gradle
├─ frontend/               # Vite + React + TypeScript frontend
│  ├─ package.json
│  └─ src/
├─ docs/                   # API docs, DB schema
│  ├─ API_Documentation.md
│  └─ DATABASE_SCHEMA.sql
├─ docker-compose.yml      # Compose file for local full-stack
├─ scripts/                # dev helper scripts
└─ README.md               # <- you are here
```

Refer to the `frontend/README.md` and `backend/README.md` for module-level details.

---

## ✅ Prerequisites

- Node.js >= 18 (for Vite + frontend)
- npm (or yarn/pnpm) — the repo uses npm scripts by default
- Java 17 (toolchain used by Gradle wrapper) — Gradle wrapper will download matching distribution
- Docker & Docker Compose (for containerized setup)
- PostgreSQL (if running backend without Docker)

---

## 🛠️ Environment & Configuration

The repository expects environment variables for database and JWT configuration. When using Docker Compose the `.env` file (in repo root) is referenced by `docker-compose.yml`.

Create a `.env` file in the project root (example below):

```env
# Database (local / docker)
DB_NAME=parknexus_db
DB_USER=parknexus_user
DB_PASSWORD=change_me
DB_PORT=5432

# JWT
JWT_SECRET=changeme_supersecret_key
JWT_EXPIRATION=3600000

# Production (optional)
DATABASE_URL=jdbc:postgresql://<host>:5432/<db>

# Spring profile (for docker compose)
SPRING_PROFILES_ACTIVE=docker
```

.env keys referenced in code/config:
- DB_NAME, DB_USER, DB_PASSWORD, DB_PORT
- JWT_SECRET, JWT_EXPIRATION
- DATABASE_URL (production profile)

Note: For local development with the backend run via Gradle, application.yml uses the `local` profile and expects DB connection to `localhost:${DB_PORT}`. The docker-compose service maps host port 5433 to the container's 5432 by default (see `docker-compose.yml`).

---

## ⚙️ Installation & Run (Local Development)

This section shows two common flows: full-stack using Docker, and running frontend + backend locally without Docker.

A) Full-stack (recommended, Docker):

1. Create a `.env` file in the project root (see example above).
2. Start the stack:

```bash
# from repo root
docker compose up --build
```

3. Services (default ports):
- Backend (Spring Boot): http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- PostgreSQL (host port): 5433 (container port 5432) — mapped in docker-compose.yml

B) Run frontend + backend locally (no Docker):

1. Start backend using Gradle wrapper

```bash
# from repo root
cd backend
./gradlew bootRun
```

2. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend Vite dev server default: http://localhost:5173

---

## 📦 Scripts & Useful Commands

Frontend (inside `frontend/`):

```bash
npm install
npm run dev      # start Vite dev server
npm run build    # build production bundle
npm run preview  # preview built bundle
npm run lint     # run ESLint
```

Backend (inside `backend/`):

```bash
./gradlew bootRun     # run the Spring Boot app
./gradlew build       # build jar
./gradlew test        # run tests
```

Docker Compose (repo root):

```bash
docker compose up --build
docker compose down
```

Dev helper scripts (see `scripts/dev/`):
- `scripts/dev/start.sh` — wraps a docker compose dev flow (may reference docker-compose.dev.yml)

---

## 🔑 Authentication Flow

- The backend exposes `/api/auth/login` and `/api/auth/register` endpoints.
- On successful login/register the backend returns an `AuthResponse` with a JWT token and user object.
- The frontend stores the JWT in `localStorage` and user data in a Zustand store (`frontend/src/store/authStore.ts`).
- Protected routes are implemented via `ProtectedRoute` and the React Router layout redirects users by role to `/user/*` or `/admin/*`.

---

## 🔍 Monitoring & Observability

This project includes observability features via Spring Boot Actuator, Micrometer, Prometheus and Grafana. When running via the provided `docker-compose.yml` the stack includes Prometheus and Grafana services.

Actuator endpoints (backend)
- Base actuator path: `http://localhost:8080/actuator` (when enabled)
- Useful endpoints:
  - GET `/actuator/health` — basic health status
  - GET `/actuator/metrics` — available metrics list
  - GET `/actuator/metrics/{metric.name}` — fetch specific metric (e.g. `http.server.requests`, `jvm.memory.used`)
  - GET `/actuator/prometheus` — Prometheus scrape endpoint (Micrometer export)
  - GET `/actuator/caches` — list caches managed by Spring Cache (exposed when `management.endpoints.web.exposure.include` includes `caches`)

Example curl (actuator health / prometheus):

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
```

Prometheus & Grafana (compose)
- Prometheus UI: http://localhost:9090
  - Prometheus scrapes the backend Prometheus endpoint (`/actuator/prometheus`). The Prometheus configuration is included in `backend/prometheus/prometheus.yml` (or `prometheus/` in the repo depending on docker-compose bind mounts).
- Grafana: http://localhost:3000
  - Grafana is included in the compose stack and can be preconfigured to use Prometheus as a data source. You can add dashboards that visualize `http.server.requests`, `jvm`, cache metrics and custom application metrics.

Metrics you can expect
- HTTP server metrics (requests, response times): `http.server.requests`
- JVM metrics: `jvm.memory.*`, `jvm.gc.*`, `process.cpu.*`
- Data source metrics (HikariCP): `hikaricp.*`
- Cache metrics (if Micrometer integration present): cache hit/miss metrics per cache name

Security note
- Actuator endpoints may expose sensitive data. In production, lock down actuator endpoints with proper authentication and only expose the minimum required endpoints (for Prometheus, you usually only expose `/actuator/prometheus` to the Prometheus server).

---

## 🗄️ Caching (Spring Cache + Caffeine)

The backend uses Spring Cache with a Caffeine cache implementation for in-memory caching of frequently accessed read data (e.g., spot lists, building lookups). Key points:

- Cache provider: Caffeine (local in-memory cache)
- Abstraction: Spring Cache (`@Cacheable`, `@CacheEvict`) — controllers/services annotate methods to cache results
- Cache monitoring: If `management.endpoints.web.exposure.include` includes `caches`, Actuator exposes `/actuator/caches` to inspect cache names and some stats.

Typical cache usage examples (backend):
- Cache available spots per building/floor to reduce DB queries for rapidly-read data
- Cache building/floor metadata
- Short TTLs and explicit evicting on writes/events (e.g., when a reservation or check-in modifies spot status) to prevent stale reads

Cache safety notes
- Caffeine is an in-memory cache local to the JVM. In multi-instance (clustered) deployments consider using a distributed cache (Redis) or an event-driven cache invalidation strategy.

---

## 📡 API (Quick reference)

Base URL (local):
```
http://localhost:8080/api
```

Auth (public):
- POST /auth/register  — RegisterRequest { fullName, email, password } -> AuthResponse
- POST /auth/login     — LoginRequest { email, password } -> AuthResponse

User (JWT required):
- GET  /user/dashboard            — DashboardResponse
- GET  /user/spots/available     — List<SpotDTO>
- POST /user/reservation         — ReservationRequest -> ReservationDTO
- POST /user/checkin             — CheckInRequest -> SessionDTO
- POST /user/checkout/{sessionId}— -> BillDTO

Admin (Admin JWT required):
- GET /admin/dashboard
- GET /admin/users
- GET /admin/logs

For endpoint request/response shapes refer to `docs/API_Documentation.md` (placeholder) and backend controller DTO classes.

---

## 🧩 Frontend Notes (developer)

- Axios is configured centrally (look for `frontend/src/services/api.ts`). The app uses an interceptor to add `Authorization: Bearer <token>` from localStorage to outgoing requests.
- Server-state is handled via `@tanstack/react-query` located across `src/pages/*` and `src/services/*`.
- Global auth state is in `frontend/src/store/authStore.ts` (Zustand).
- Routing & layout components live under `frontend/src/components/layout` and pages under `frontend/src/pages`.

If you run into an import error like:

```
Uncaught SyntaxError: The requested module '/src/types/index.ts' does not provide an export named 'ApiError'
```

that typically means a type or exported symbol is referenced but not present in `frontend/src/types/index.ts`. Fix by either exporting the missing type from the types file or removing the import from the file that expects it.

---

## ✅ Features (MVP)

- User registration & login (JWT)
- Role-based routing (User vs Admin)
- User dashboard with stats
- Browse available parking spots and check-in
- Create reservations
- Active session list and checkout (billing)
- Admin dashboard: user list, activity logs, parking lot management
- Observability endpoints via Actuator, Prometheus & Grafana

---

## 🧪 Testing

Backend:

```bash
cd backend
./gradlew test
```

Frontend:
- No unit tests are included by default in the scaffold. Use `npm run lint` for static checks.

---

## 📦 Deployment

- A production build for the frontend can be produced with `cd frontend && npm run build` and then served by a static server or integrated into the backend build pipeline.
- Backend is containerized via `backend/Dockerfile` and can be deployed with Docker / Kubernetes.

---

## 🤝 Contributing

Contributions are welcome. Suggested workflow:
1. Fork the repo
2. Create a feature branch
3. Add tests where appropriate
4. Open a PR describing the change

Please follow existing code style and run linting before opening pull requests.

---

## 📝 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 👤 Author / Contact

- Name: Antonio Santiago (TonyS-dev)
- GitHub: https://github.com/TonyS-dev
- Email: santiagor.acarlos@gmail.com

---
