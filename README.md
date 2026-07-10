# Smart Tool Cabinets API

Backend API for an academic MVP about communication between smart tool cabinets and a central system.

The demonstrable flow covers cabinet authentication, operator authentication, `CabinetAccess`, RFID snapshots, inventory delta, `ToolAssignment` custody and end-of-day validation.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Spring Web / Data JPA / Security
- Bean Validation
- OpenAPI / Swagger UI

## Run Locally

Start PostgreSQL:

```powershell
docker compose up -d
```

Start the backend:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Demo

Seed data is created by Flyway:

- cabinet: `CAB-001`
- device API key: `DEV-CAB-001`
- operator PIN: `1234`
- RFID tags: `TAG-001`, `TAG-002`, `TAG-003`, `TAG-004`

Run the simulator:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\simulator.ps1
```

The simulator calls the real HTTP API and validates the complete working-day flow: checkout, tool exchange in the same `CabinetAccess`, final return and clean end-of-day check.

To reset local demo data:

```powershell
scripts\dev\reset-db.cmd
```

## Main Endpoints

- `POST /api/device/auth`
- `POST /api/device/operator-auth`
- `POST /api/device/cabinet-accesses`
- `POST /api/device/cabinet-accesses/{id}/snapshots`
- `POST /api/device/cabinet-accesses/{id}/close`
- `GET /api/operators/{operatorId}/tool-assignments`
- `GET /api/operators/{operatorId}/end-of-day-check`
- `POST /api/supervisor/resolutions`

## Validation

```powershell
cd backend
mvn test
```

Runtime evidence is kept in `docs/evidence/`.

## Scope

This is not a commercial product. Authentication, token handling, auditing and device integration are simplified for the academic demonstrator. There is no dashboard, mobile app, analytics, Kubernetes or external hardware integration in this MVP.
