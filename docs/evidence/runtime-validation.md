# Runtime Validation Evidence

Date: 2026-07-10

## Maven Tests

Command:

```powershell
cd backend
mvn test
```

Result:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 39.266 s
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
Successfully validated 2 migrations
Creating Schema History table "public"."flyway_schema_history" ...
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - init schema"
Migrating schema "public" to version "2 - seed demo data"
Successfully applied 2 migrations to schema "public", now at version v2
Tomcat started on port 8080 (http) with context path '/'
Started SmartToolCabinetsApplication
```

Database confirmation:

```powershell
docker exec stc-postgres psql -U postgres -d smart_tool_cabinets -c "select version, description, success from flyway_schema_history order by installed_rank;"
```

Result:

```text
 version |  description   | success
---------+----------------+---------
 1       | init schema    | t
 2       | seed demo data | t
(2 rows)
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
