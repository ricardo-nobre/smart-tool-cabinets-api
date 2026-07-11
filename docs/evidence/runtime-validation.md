# Runtime Validation Evidence

Date: 2026-07-11

## Maven Tests

Command:

```powershell
cd backend
mvn test
```

Result:

```text
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 38.388 s
```

## Docker Compose Configuration

Command:

```powershell
docker compose config
```

Result:

```text
name: projeto
services:
  postgres:
    container_name: stc-postgres
    image: postgres:16
    ports:
      - mode: ingress
        target: 5432
        published: "5432"
        protocol: tcp
    volumes:
      - type: volume
        source: stc_pgdata
        target: /var/lib/postgresql/data
```

## PostgreSQL Reset And Startup

Commands:

```powershell
docker compose down -v
docker compose up -d
docker compose ps
```

Result:

```text
Volume projeto_stc_pgdata Removed
NAME           IMAGE         SERVICE    STATUS                    PORTS
stc-postgres   postgres:16   postgres   Up ... (healthy)          0.0.0.0:5432->5432/tcp
```

## Flyway

The backend was started with the `dev` profile against a clean PostgreSQL volume.

Relevant startup output:

```text
Schema history table "public"."flyway_schema_history" does not exist yet
Successfully validated 4 migrations
Creating Schema History table "public"."flyway_schema_history" ...
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - init schema"
Migrating schema "public" to version "2 - seed demo data"
Migrating schema "public" to version "3 - remove allow exit from supervisor resolution"
Migrating schema "public" to version "4 - enforce one resolution per assignment"
Successfully applied 4 migrations to schema "public", now at version v4
Tomcat started on port 8080 (http) with context path '/'
Started SmartToolCabinetsApplication
```

Database confirmation:

```powershell
docker exec stc-postgres psql -U postgres -d smart_tool_cabinets -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

Result:

```text
 version |                 description                  | success
---------+----------------------------------------------+---------
 1       | init schema                                  | t
 2       | seed demo data                               | t
 3       | remove allow exit from supervisor resolution | t
 4       | enforce one resolution per assignment        | t
(4 rows)
```

Supervisor resolution schema confirmation:

```text
  column_name
---------------
 id
 operator_id
 supervisor_id
 reason_code
 report_text
 decision_at
 created_at
(7 rows)
```

Supervisor resolution assignment constraints:

```text
                   constraint_name                   | constraint_type
-----------------------------------------------------+-----------------
 fk_resolution_assignment_assignment                 | FOREIGN KEY
 fk_resolution_assignment_resolution                 | FOREIGN KEY
 supervisor_resolution_assignment_pkey               | PRIMARY KEY
 uq_supervisor_resolution_assignment_resolution      | UNIQUE
 uq_supervisor_resolution_assignment_tool_assignment | UNIQUE
```

Open assignment index:

```text
uq_tool_assignment_open_per_tool
WHERE status IN ('ACTIVE', 'PENDING_REVIEW')
```

## Swagger / OpenAPI

Commands:

```powershell
Invoke-WebRequest http://localhost:8080/swagger-ui.html -UseBasicParsing
Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing
```

Results:

```text
/swagger-ui.html -> HTTP 200, length 734
/v3/api-docs     -> HTTP 200, length 9642
```

Generated contract confirmation:

```text
CreateSupervisorResolutionRequest  -> operatorId, supervisorId, reasonCode, reportText, decisionAt, assignmentId
CreateSupervisorResolutionResponse -> resolutionId, operatorId, supervisorId, decisionAt, reasonCode, reportText, resolvedAssignmentId
```

## Working-Day Simulator

Command:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\simulator.ps1
```

Result:

```text
[OK] TAG-002 assigned to operator as ACTIVE
[OK] End-of-day check detects 1 pending assignment before exchange
[OK] TAG-002 marked as RETURNED
[OK] TAG-004 assigned to operator as ACTIVE
[OK] End-of-day check now detects TAG-004 as the pending assignment
[OK] TAG-004 marked as RETURNED
[OK] No pending assignments
[OK] Operator can exit
```
