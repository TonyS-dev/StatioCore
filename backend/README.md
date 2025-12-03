# ParkNexus — Backend

This folder contains the Spring Boot backend for ParkNexus.

Overview
--------
- Framework: Spring Boot 3.5.8
- Language: Java 17 (Gradle toolchain)
- Persistence: Spring Data JPA, PostgreSQL
- Migrations: Flyway
- Security: Spring Security + JWT
- Observability: Actuator + Micrometer (Prometheus) + Grafana

Quick start (development)
-------------------------
A) Run locally (requires PostgreSQL running locally):

1. Ensure PostgreSQL is running and the credentials from `application.yml` are available or set via environment variables (see Environment section below).

2. Run the app with Gradle wrapper:

```bash
cd backend
./gradlew bootRun
```

The service will start on port defined in `application.yml` (default 8080).

B) Run with Docker Compose (recommended for full-stack dev):

From repo root:

```bash
# ensure .env is present in repo root
docker compose up --build
```

This will start Postgres, the backend app, Prometheus and Grafana as configured in `docker-compose.yml`.

Build & Tests
-------------

```bash
cd backend
./gradlew build    # build a fat jar
./gradlew test     # run unit & integration tests
```

Important files & folders
-------------------------
- `build.gradle` — Gradle build configuration
- `src/main/java` — application source code
- `src/main/resources/application.yml` — Spring profiles and DB configuration
- `Dockerfile` — container image build
- `prometheus/` — Prometheus config files
- `docs/DATABASE_SCHEMA.sql` — canonical DB schema used for reference

Environment & Spring profiles
-----------------------------
The `application.yml` defines several profiles: `local`, `docker`, and `prod`.

Key environment variables used by the backend (set in `.env` or environment):

- `DB_NAME` - database name
- `DB_USER` - database user
- `DB_PASSWORD` - database password
- `DB_PORT` - local DB port (default 5432)
- `JWT_SECRET` - HMAC secret for signing JWTs
- `JWT_EXPIRATION` - JWT expiration in milliseconds
- `DATABASE_URL` - production JDBC URL (optional)
- `SPRING_PROFILES_ACTIVE` - to select `docker` profile in Docker Compose

Flyway & Database
-----------------
- Migrations are applied via Flyway on startup. Flyway SQL scripts should live in `src/main/resources/db/migration`.
- The repo includes a canonical `docs/DATABASE_SCHEMA.sql` for reference.

Observability
-------------
- Actuator endpoints are exposed when enabled. Prometheus scrape endpoint is available and Prometheus is included in the Compose stack.
- Grafana is included in the Compose stack and configured to read from Prometheus.

Notes for developers
--------------------
- The backend exposes REST endpoints under `/api/*`. See `docs/API_Documentation.md` for detailed API shapes and examples.
- Use the Gradle wrapper (`./gradlew`) so developers don't need a matching Gradle installation.

Troubleshooting
---------------
- If Flyway fails on startup, check the DB connectivity and that the DB user has the proper privileges to create/alter tables.
- If you see authentication errors, ensure `JWT_SECRET` is set and the frontend is sending `Authorization: Bearer <token>`.

API Documentation (Swagger/OpenAPI)
-----------------------------------
The backend includes comprehensive Swagger/OpenAPI 3.0 documentation for all REST endpoints.

**Access Interactive API Docs:**

When the backend is running, visit:

🔗 **http://localhost:8080/swagger-ui/index.html**

Features:
- Complete API reference with request/response schemas
- Interactive "Try it out" functionality
- Authentication support (JWT Bearer token)
- Parameter descriptions and examples
- Organized by API groups (Auth, User, Admin)

**OpenAPI Specification:**

Raw OpenAPI JSON/YAML available at:
- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

Use these URLs to:
- Import into Postman/Insomnia
- Generate client SDKs
- Share API contracts with frontend developers

**Testing Protected Endpoints:**

1. Use `/api/auth/login` to get a JWT token
2. Click "Authorize" button in Swagger UI
3. Enter: `Bearer {your-token}`
4. All protected endpoints will now include your auth token

API Structure
-------------
- `/api/auth/*` - Public authentication endpoints (login, register)
- `/api/user/*` - User endpoints (requires USER role)
- `/api/admin/*` - Admin endpoints (requires ADMIN role)

More
----
- See the root `README.md` for general full-stack development instructions.
- All controllers use comprehensive Swagger annotations (@Operation, @ApiResponse, etc.)
- API documentation is automatically generated from code annotations

