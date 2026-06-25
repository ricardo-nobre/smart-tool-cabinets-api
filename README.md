# Smart Tool Cabinets API

Backend API for an academic MVP about communication between smart tool cabinets and a central system.

The project focuses on a demonstrable operational flow: cabinet authentication, operator authentication, short cabinet accesses, RFID inventory snapshots, custody tracking through tool assignments, end-of-day pending checks and supervisor resolutions.

This is not intended to be a commercial product. Security, auditing and device integration are intentionally simplified for the final project demonstrator.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- OpenAPI / Swagger UI

## Local Requirements

- Java 21
- Maven 3.9+
- Docker / Docker Compose

## Quick Start

Start PostgreSQL:

```powershell
docker compose up -d
```

Run the backend with the development profile:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI is available in the `dev` profile:

```text
http://localhost:8080/swagger-ui.html
```

## Demo Data

Flyway seeds minimal local data for the demonstrator:

- cabinet: `CAB-001`
- device API key for the demo auth flow: `DEV-CAB-001`
- operator: `operator1`, PIN `1234`
- supervisor: `supervisor1`
- admin: `admin`
- demo RFID tags: `TAG-001`, `TAG-002`, `TAG-003`, `TAG-004`

The token mechanism is deliberately simple for the MVP. Tokens such as `DEV-TOKEN-CAB-001`, `OPERATOR-TOKEN-DEMO`, `SUPERVISOR-TOKEN-DEMO` and `ADMIN-TOKEN-DEMO` are accepted by the development security filter based on their prefix.

## Main Endpoints

- `POST /api/device/auth`
- `POST /api/device/operator-auth`
- `POST /api/device/cabinet-accesses`
- `POST /api/device/cabinet-accesses/{id}/snapshots`
- `POST /api/device/cabinet-accesses/{id}/close`
- `GET /api/operators/{operatorId}/tool-assignments`
- `GET /api/operators/{operatorId}/end-of-day-check`
- `POST /api/supervisor/resolutions`

The public API and backend code use the `CabinetAccess` concept.

## Simulator Scripts

With PostgreSQL and the backend running:

```powershell
scripts\dev\run-simulator-normal.cmd
scripts\dev\run-simulator-missing-tool.cmd
```

The scripts call the real HTTP API and exercise two demonstrator flows:

- normal flow: operator checks out a tool, creating an active ToolAssignment;
- missing-tool flow: an active assignment remains pending, end-of-day check detects it, and a supervisor resolution is created.

For repeated local demonstrations, reset the database first so previous active assignments do not affect the next run:

```powershell
scripts\dev\reset-db.cmd
```

## Useful Commands

```powershell
mvn test
```

```powershell
docker compose ps
```

```powershell
scripts\dev\start-local.cmd
scripts\dev\stop-local.cmd
```

`scripts\dev\reset-db.cmd` recreates the local PostgreSQL volume and should only be used when local data can be discarded.

## Current State

Implemented:

- Spring Boot backend structure by domain
- PostgreSQL schema managed by Flyway
- basic development security filter
- cabinet and operator authentication for the demo flow
- CabinetAccess open/close endpoints
- inventory snapshots with recognized and unknown RFID tags
- inventory delta calculation
- ToolAssignment creation/return logic
- operator pending-assignment queries
- supervisor resolution endpoint
- minimal simulator scripts
- focused unit test for inventory delta

Known limitations:

- authentication is simplified and not production-grade;
- token values are not persisted or cryptographically validated;
- simulator is intentionally minimal and HTTP-script based;
- OpenAPI is only required to track the demonstrable MVP endpoints;
- no dashboard, mobile app, analytics, Kubernetes or external integrations are part of this MVP.

## Next Steps

- render the EA diagram and include it in the final report;
- review OpenAPI examples against the final demo flow;
- improve credential handling if time allows.
