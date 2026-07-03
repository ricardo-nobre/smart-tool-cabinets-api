# Runtime Validation Evidence

Date: 2026-07-03

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
Total time: 31.119 s
```

## Docker Compose

Command:

```powershell
docker compose config
```

Result:

```text
services:
  postgres:
    image: postgres:16
    container_name: stc-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: smart_tool_cabinets
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
```

Command:

```powershell
docker compose ps
```

Result:

```text
NAME           IMAGE         SERVICE    STATUS                    PORTS
stc-postgres   postgres:16   postgres   Up ... (healthy)          0.0.0.0:5432->5432/tcp
```

## Flyway

The backend was started with the `dev` profile against a clean PostgreSQL volume.

Relevant startup output:

```text
Successfully validated 2 migrations
Migrating schema "public" to version "1 - init schema"
Migrating schema "public" to version "2 - seed demo data"
Successfully applied 2 migrations to schema "public", now at version v2
Tomcat started on port 8080 (http)
```

Database confirmation:

```sql
select version, description, success
from flyway_schema_history
order by installed_rank;
```

Result:

```text
version | description    | success
--------+----------------+--------
1       | init schema    | t
2       | seed demo data | t
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
