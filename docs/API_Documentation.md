# API Documentation

This document contains a concrete reference for the backend HTTP API used by the frontend at `frontend/`.

Base URL (local):

```
http://localhost:8080/api
```

Authentication
--------------
All protected endpoints require a Bearer JWT in the `Authorization` header.

Header example:

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

Common error response shape (HTTP error responses will follow this minimal contract):

```json
{
  "message": "Human friendly error message",
  "status": 400,
  "timestamp": "2025-12-05T12:34:56Z",
  "path": "/api/some/endpoint"
}
```

API: Auth (Public)
------------------
1) POST /api/auth/register
- Description: Register a new user
- Request body (JSON):

```json
{
  "fullName": "Tony Santiago",
  "email": "tony@example.com",
  "password": "s3cretP@ss"
}
```

- Success (201) response body (AuthResponse):

```json
{
  "token": "<JWT_TOKEN>",
  "user": {
    "id": "uuid",
    "email": "tony@example.com",
    "fullName": "Tony Santiago",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-12-05T12:00:00Z",
    "updatedAt": "2025-12-05T12:00:00Z"
  }
}
```

2) POST /api/auth/login
- Description: Authenticate a user and return a JWT token + user object
- Request body:

```json
{
  "email": "tony@example.com",
  "password": "s3cretP@ss"
}
```

- Success (200) response body (AuthResponse): same shape as register

Curl example (login):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"tony@example.com","password":"s3cretP@ss"}'
```


API: User Endpoints (Requires JWT)
----------------------------------
All user endpoints are prefixed with `/api/user`.

1) GET /api/user/dashboard
- Description: Returns user-facing dashboard statistics and recent activity
- Query params: none
- Response (200) `DashboardResponse` (example):

```json
{
  "totalSpots": 200,
  "occupiedSpots": 32,
  "availableSpots": 168,
  "occupancyPercentage": 16.0,
  "activeReservations": 1,
  "activeSessions": 0,
  "totalReservations": 12,
  "totalCompletedSessions": 20,
  "totalEarnings": 1234.50,
  "outstandingFees": 0.0,
  "averageSessionFee": 12.34,
  "recentActivity": [
    { "action": "CHECKIN", "details": "Checked in to Spot A1", "timestamp": "..." }
  ]
}
```

2) GET /api/user/spots/available
- Description: Retrieve a paginated list (or full list) of available parking spots.
- Query params (optional): `buildingId`, `floorId`, `type`, `status` (e.g., AVAILABLE), `page`, `size`
- Response (200): `ParkingSpot[]` (array)

Example response item:

```json
{
  "id": "uuid",
  "spotNumber": "A1",
  "type": "STANDARD",
  "status": "AVAILABLE",
  "floorId": "uuid",
  "floorNumber": 1,
  "buildingId": "uuid",
  "buildingName": "Main Lot",
  "buildingAddress": "123 Main St",
  "hourlyRate": 2.5
}
```

Curl example:

```bash
curl -H "Authorization: Bearer <JWT>" \
  "http://localhost:8080/api/user/spots/available"
```

3) POST /api/user/reservation
- Description: Create a reservation for a spot
- Request body (ReservationRequest):

```json
{
  "spotId": "uuid",
  "startTime": "2025-12-05T14:00:00Z",
  "vehicleNumber": "ABC-1234",
  "durationMinutes": 120
}
```

- Success (201) response (ReservationDTO / Reservation):

```json
{
  "id": "uuid",
  "userId": "uuid",
  "spotId": "uuid",
  "spotNumber": "A1",
  "buildingName": "Main Lot",
  "floorNumber": 1,
  "startTime": "2025-12-05T14:00:00Z",
  "endTime": "2025-12-05T16:00:00Z",
  "status": "PENDING",
  "createdAt": "2025-12-05T12:05:00Z"
}
```

4) POST /api/user/checkin
- Description: Check into a parking spot; may accept optional `reservationIds` to cancel after check-in
- Request body (CheckInRequest) extended with reservationIds (optional):

```json
{
  "spotId": "uuid",
  "vehicleNumber": "ABC-1234",
  "reservationIds": [ "res-uuid-1" ]
}
```

- Success (200) response: `ParkingSession` (session DTO):

```json
{
  "id": "uuid",
  "userId": "uuid",
  "spotId": "uuid",
  "spotNumber": "A1",
  "checkInTime": "2025-12-05T14:05:00Z",
  "status": "ACTIVE",
  "createdAt": "..."
}
```

5) POST /api/user/checkout/{sessionId}
- Description: Checkout (end session) and produce a bill
- Path param: `sessionId` (UUID)
- Response (200): `Bill` DTO (example):

```json
{
  "sessionId": "uuid",
  "spotId": "uuid",
  "spotNumber": "A1",
  "checkInTime": "2025-12-05T14:05:00Z",
  "checkOutTime": "2025-12-05T16:21:00Z",
  "durationMinutes": 136,
  "amountDue": 5.50,
  "paymentId": "pay-uuid",
  "paymentStatus": "COMPLETED",
  "transactionId": "txn-123",
  "paymentMethod": "CREDIT_CARD",
  "paidAt": "2025-12-05T16:22:00Z",
  "message": "Thank you"
}
```

API: Admin Endpoints (Requires Admin JWT)
----------------------------------------
All admin endpoints are prefixed with `/api/admin`.

1) GET /api/admin/dashboard
- Description: Returns admin statistics and recent activity
- Response (200): `AdminDashboardResponse` containing counts and `recentActivity`

2) GET /api/admin/users
- Description: Return a list of all users or paginated users
- Query params: `page`, `size`, `role`, `active`, `searchTerm`
- Response (200): `User[]` or `PageResponse<User>` depending on implementation

3) GET /api/admin/logs
- Description: Return activity logs (paginated)
- Query params: `page`, `size`, `userId`, `action`, `startDate`, `endDate`
- Response (200): `PageResponse<ActivityLog>`

Example of an `ActivityLog` item:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "userEmail": "user@example.com",
  "action": "CHECKIN",
  "details": "User checked in to spot A1",
  "createdAt": "2025-12-05T14:05:00Z"
}
```

Monitoring & Observability
--------------------------
The backend exposes Actuator endpoints that provide health, metrics and a Prometheus scrape endpoint. When running the full stack via `docker-compose` the repository includes Prometheus and Grafana services which can scrape the backend and visualize metrics.

Actuator endpoints (examples):

- GET `/actuator/health` — basic application health
- GET `/actuator/info` — application info if configured
- GET `/actuator/metrics` — list available metrics
- GET `/actuator/metrics/{metric.name}` — fetch a specific metric (e.g. `/actuator/metrics/http.server.requests`)
- GET `/actuator/prometheus` — Prometheus format metrics (scrape target)
- GET `/actuator/caches` — list caches and (if available) simple stats

These endpoints are typically under `http://localhost:8080/actuator`.

Prometheus & Grafana (compose):

- Prometheus UI: `http://localhost:9090`
  - Prometheus scrapes the backend's `/actuator/prometheus` endpoint. Check `backend/prometheus/prometheus.yml` for the scrape configuration.

- Grafana UI: `http://localhost:3000`
  - Use Grafana to add Prometheus as a data source (URL pointing to `http://prometheus:9090` within compose) and import or create dashboards that visualize metrics such as `http.server.requests`, JVM metrics, HikariCP metrics, and cache metrics.

Example curl commands:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus | head -n 20
```

Security considerations:

- Production: secure actuator endpoints with proper authentication and restrict exposure. For Prometheus scraping, it's common to expose only `/actuator/prometheus` to the Prometheus host and protect other endpoints.

Caching
-------
The backend uses Spring Cache with Caffeine as the in-memory cache. This speeds up read-heavy endpoints and reduces DB load for frequently accessed data (e.g., lists of available spots, building metadata).

Cache interaction and endpoints:

- There is no public REST API solely for cache management by default; however, when Actuator exposes `caches` you can inspect caches at `GET /actuator/caches` and get details for a specific cache at `GET /actuator/caches/{cacheName}`.
- Cache annotations used in code:
  - `@Cacheable` — caches a method's result
  - `@CacheEvict` — invalidates cache entries after writes
  - `@Caching` — combine multiple cache operations

Cache best practices:

- Use short TTLs for dynamic data (e.g., spot availability) and evict caches on state-changing operations (reservations, check-ins, checkouts).
- For multi-instance deployments, consider using a distributed cache (Redis) and synchronize invalidation across instances.

Security notes
--------------
- The API uses JWT authentication. Tokens are issued by `/api/auth/login` and `/api/auth/register`.
- For sensitive production deployments, always use HTTPS and short-lived JWTs with refresh tokens or revocation.
- The backend enforces role-based access: `ADMIN` endpoints require a user with `ADMIN` role.

Rate limiting and abuse control are not described here and should be implemented for production readiness.

Examples (curl) - protected endpoint

```bash
curl -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  "http://localhost:8080/api/user/spots/available"
```

Notes & mapping to frontend
--------------------------
- The frontend expects these endpoint paths and DTO shapes located in `frontend/src/types/index.ts`.
- Central axios instance: `frontend/src/services/api.ts` (adds `Authorization: Bearer <token>` header automatically when a valid token is present in `localStorage`).

OpenAPI / Postman
-----------------
- This repo does not include a formal OpenAPI (swagger) spec file, but SpringDoc is present in the backend dependencies and can generate a Swagger UI at runtime (if enabled): typically `/swagger-ui.html` or `/v3/api-docs` depending on SpringDoc configuration.
