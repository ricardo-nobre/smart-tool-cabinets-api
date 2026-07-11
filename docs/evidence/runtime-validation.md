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
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 31.356 s
```

## Docker Compose

Command:

```powershell
docker compose ps
```

Result:

```text
NAME           IMAGE         COMMAND                  SERVICE    STATUS                    PORTS
stc-postgres   postgres:16   "docker-entrypoint.s..." postgres   Up ... (healthy)          0.0.0.0:5432->5432/tcp
```

## Flyway

The backend was started with the `dev` profile against a clean PostgreSQL volume.

Relevant startup output:

```text
Schema history table "public"."flyway_schema_history" does not exist yet
Successfully validated 3 migrations
Creating Schema History table "public"."flyway_schema_history" ...
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - init schema"
Migrating schema "public" to version "2 - seed demo data"
Migrating schema "public" to version "3 - remove allow exit from supervisor resolution"
Successfully applied 3 migrations to schema "public", now at version v3
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
(3 rows)
```

Supervisor resolution schema confirmation:

```powershell
docker exec stc-postgres psql -U postgres -d smart_tool_cabinets -c "select column_name from information_schema.columns where table_name = 'supervisor_resolution' order by ordinal_position;"
```

Result:

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

## Swagger / OpenAPI

Commands:

```powershell
Invoke-WebRequest http://localhost:8080/swagger-ui.html -UseBasicParsing
Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing
```

Results:

```text
/swagger-ui.html -> HTTP 200
/v3/api-docs     -> HTTP 200
```

Generated contract confirmation:

```text
CreateSupervisorResolutionRequest  -> operatorId, supervisorId, reasonCode, reportText, decisionAt, assignmentIds
CreateSupervisorResolutionResponse -> resolutionId, operatorId, supervisorId, decisionAt, reasonCode, reportText, resolvedAssignmentIds
```
