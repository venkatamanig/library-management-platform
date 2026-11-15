# Library Management System

Java 21 · Spring Boot 4.1.2 · Gradle · PostgreSQL · Redis · Spring Cloud Gateway · OpenAPI

This repository retains the existing LMS business service and adds external infrastructure around it. The root Gradle project is the LMS API; `:gateway` is a separate Spring Cloud Gateway application. The gateway uses Spring Cloud LoadBalancer with three statically configured service instances for local Docker development. Eureka is intentionally omitted because static service discovery is sufficient for this local topology.

## What is in the project

- Existing LMS business modules under `org.example.lms` (authentication/JWT, users, books/copies, borrowing, memberships, fines, notifications, audit, policies and reports).
- PostgreSQL as the persistent application database.
- Redis-backed Spring Cache for book details, book lists and active membership plans.
- TTLs: book detail 5 minutes, book list 2 minutes, active plans 10 minutes.
- Cache invalidation on book create/update/replace/delete and membership-plan create/update/deactivate.
- Separate Spring Cloud Gateway + Spring Cloud LoadBalancer project.
- Swagger UI and grouped OpenAPI documents with JWT Bearer support.
- Actuator health endpoints for the application and gateway.

## Requirements

- Docker Desktop / Docker Engine with Compose plugin.
- For running directly from IntelliJ: Java 21 and Gradle wrapper access to Maven Central / Gradle distribution.
- No local PostgreSQL or Redis installation is needed when using Docker Compose.

## Configure environment

```bash
cp .env.example .env
```

Edit `.env` and replace `POSTGRES_PASSWORD`, `JWT_SECRET`, `LMS_ADMIN_EMAIL` and `LMS_ADMIN_PASSWORD` with private values. `JWT_SECRET` should be a randomly generated secret with at least 32 bytes. Do not commit `.env`.

Windows PowerShell copy:

```powershell
Copy-Item .env.example .env
```

The initial administrator is created on startup when both `LMS_ADMIN_EMAIL` and `LMS_ADMIN_PASSWORD` are visible to the LMS process. `LMS_ADMIN_NAME` is optional. The password is stored using BCrypt. When multiple replicas start together, the initializer handles the unique-email race and does not create duplicate administrators.

### Admin initializer troubleshooting

The previous implementation used Spring `@Value` injection with blank defaults. That code is valid when variables reach the Spring application process. The common IntelliJ failure mode is setting variables on a different run configuration (for example, a Gradle tool process rather than the Boot application process). The revised initializer reads Spring's `Environment` and explicitly checks `System.getenv()` as a fallback, and prints only whether the two values are visible—not their contents. In IntelliJ, set the variables in the **Application** run configuration used to launch `LibraryManagementApplication`; for Gradle `bootRun`, set them in that process's environment. Restart the application after changing them.

## Start the complete stack

From the repository root:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f gateway lms-1 lms-2 lms-3
```

The public entry point is:

- API / Swagger UI: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- LMS health (through gateway): http://localhost:8080/actuator/health
- Gateway health is checked internally by Docker at `/actuator/health/readiness`; the gateway container port is not published separately.

Stop the stack:

```bash
docker compose down
```

Stop and delete persistent PostgreSQL and Redis data (destructive):

```bash
docker compose down -v
```

PostgreSQL and Redis data are held in named Docker volumes, so ordinary container restarts preserve data.

## Run from IntelliJ without Docker Compose

Start PostgreSQL and Redis separately, then set these environment variables on the **Application** run configuration:

```text
DB_URL=jdbc:postgresql://localhost:5432/lms
DB_USERNAME=lms
DB_PASSWORD=...
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=...
LMS_ADMIN_EMAIL=...
LMS_ADMIN_PASSWORD=...
LMS_ADMIN_NAME=Library Admin
```

Run `org.example.lms.LibraryManagementApplication`. To run the gateway separately, run `org.example.lmsgateway.GatewayApplication` with `LMS_INSTANCE_1_URI`, `LMS_INSTANCE_2_URI`, and `LMS_INSTANCE_3_URI` pointing to the running LMS ports.

## Build and tests

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew.bat clean build
```

Build the gateway specifically:

```bash
./gradlew :gateway:bootJar
```

## API examples

All examples below use the gateway address. The login response returns a Bearer token. Supply it to protected requests as `Authorization: Bearer <token>`.

### 1. Admin login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"YOUR_ADMIN_PASSWORD"}'
```

### 2. Create a book (Admin or Librarian)

```bash
curl -X POST http://localhost:8080/book \
  -H "Authorization: Bearer YOUR_JWT" \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","publicationYear":"2008","isbn":"9780132350884"}'
```

### 3. List books

```bash
curl http://localhost:8080/book \
  -H "Authorization: Bearer YOUR_JWT"
```

### 4. Get one book

```bash
curl http://localhost:8080/book/1 \
  -H "Authorization: Bearer YOUR_JWT"
```

### 5. Register a patron

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Sample Patron","email":"patron@example.com","password":"A-Strong-Password"}'
```

The exact accepted registration fields are defined in `RegistrationRequestDTO.java`; adjust the JSON if that DTO changes.

### 6. Borrow a copy

```bash
curl -X POST http://localhost:8080/borrow/1 \
  -H "Authorization: Bearer PATRON_OR_STAFF_JWT"
```

### 7. Return a borrowing

```bash
curl -X PATCH http://localhost:8080/borrow/1/return \
  -H "Authorization: Bearer PATRON_OR_STAFF_JWT"
```

### 8. View active membership plans

```bash
curl http://localhost:8080/memberships/plans \
  -H "Authorization: Bearer YOUR_JWT"
```

### 9. View reports dashboard

```bash
curl http://localhost:8080/reports/dashboard \
  -H "Authorization: Bearer ADMIN_OR_LIBRARIAN_JWT"
```

For request DTO details and additional routes, use Swagger UI. Existing role restrictions remain enforced by the LMS service; the gateway forwards the `Authorization` header and does not replace backend authorization.

## Cache behaviour

Cache entries are stored in Redis and shared by the replicas. Cache values are limited to non-sensitive book and membership-plan response DTOs. User credentials, account records and JWTs are not cached. Mutating the relevant catalogue or plan records evicts the corresponding cache region.

## Architecture notes and operational limits

- Gateway routes all requests through `lb://lms-service`. Spring Cloud LoadBalancer selects among the three configured service URIs.
- Static discovery is intentionally used for Docker Compose. If instances become dynamic or run across hosts, add Eureka/Consul/Kubernetes discovery rather than maintaining static URI lists.
- All LMS replicas use the same PostgreSQL database and Redis instance.
- The application currently uses Hibernate `ddl-auto=update` to evolve the existing schema in a development handover. This is not a substitute for reviewed, versioned database migrations in production.
- Docker Compose enables scheduled jobs only on `lms-1`; `lms-2` and `lms-3` run API traffic without running the same cron jobs. This prevents duplicate local notifications and expiry processing. It is not leader election: if `lms-1` is down, scheduled work pauses. For multi-host/high-availability deployments, replace this local single-runner setting with a distributed scheduler lock (for example ShedLock) and operational monitoring.
- JWT validation occurs in each LMS instance. Keep the same `JWT_SECRET` across all replicas.
- This Compose file is intended for local development, not as a hardened production deployment.

## Verification record

The source archive was inspected and the integration files were added. In the execution environment used to prepare this handover, Java 21 is available, but the Gradle wrapper distribution (Gradle 9.6.0) could not be downloaded because outbound DNS/network access to `services.gradle.org` was unavailable. Consequently, a clean Gradle build, application startup, PostgreSQL/Redis connectivity, gateway balancing, Swagger requests and end-to-end business API tests could not be truthfully reported as passing here. Run the commands above in an environment with Gradle/Maven Central and Docker access, then validate the flows before using real data.
