# Missing-Tool Resolution Evidence

Date: 2026-07-03

Command:

```powershell
scripts\dev\run-simulator-missing-tool.cmd
```

Precondition:

- PostgreSQL running through Docker Compose.
- Backend running with profile `dev`.
- Database reset before execution.

Output:

```text
Running simulator scenario 'missing-tool' against http://localhost:8080
Scenario: missing tool and supervisor resolution
BEFORE: TAG-001, TAG-003, TAG-004
AFTER : TAG-001, TAG-003
CabinetAccess: e6448838-f4c8-46c5-ac56-812a7f90c6c2
Close result: CLOSED_WITH_ASSIGNMENTS
Pending assignments before resolution: 1
SupervisorResolution: ceedd386-97cf-4ff3-b4d6-dcfbcf40102f
Pending assignments after resolution: 0
{
    "operatorId": "00000000-0000-0000-0000-000000000201",
    "pendingAssignments": [],
    "pendingAssignmentsCount": 0,
    "requireSupervisorReview": false,
    "allowExit": true
}
```

Validated behavior:

- missing tool creates an active ToolAssignment;
- end-of-day-check reports the pending assignment;
- supervisor resolution is created by API;
- assignment is resolved;
- final end-of-day-check allows exit.
