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

More
----
- See the root `README.md` for general full-stack development instructions.
- Expand or generate OpenAPI docs via SpringDoc at runtime (if enabled): `/v3/api-docs` and `/swagger-ui.html`.

