# Working-Day Simulator Evidence

Date: 2026-07-03

Command:

```powershell
scripts\dev\run-simulator-working-day.cmd
```

Precondition:

- PostgreSQL running through Docker Compose.
- Backend running with profile `dev`.
- Database reset before execution.

Output:

```text
Running simulator scenario 'working-day' against http://localhost:8080
Scenario: working day checkout and return
[1] Authenticating cabinet and operator: done
[2] Opening first CabinetAccess for checkout
[3] BEFORE snapshot: TAG-001, TAG-002, TAG-003
[4] AFTER snapshot : TAG-001, TAG-003
[OK] Checkout CabinetAccess closed: 7d2a8a32-ab47-43f5-8a6c-851d4a4dd1ec
[OK] TAG-002 assigned to operator as ACTIVE
[OK] End-of-day check detects 1 pending assignment before return
[5] Opening second CabinetAccess for return
[6] BEFORE snapshot: TAG-001, TAG-003
[7] AFTER snapshot : TAG-001, TAG-002, TAG-003
[OK] Return CabinetAccess closed: 40d66980-6463-4fe0-8d50-99b233c3bfc3
[OK] TAG-002 marked as RETURNED
[8] Running final end-of-day-check
[OK] No pending assignments
[OK] Operator can exit
{
    "operatorId": "00000000-0000-0000-0000-000000000201",
    "pendingAssignments": [],
    "pendingAssignmentsCount": 0,
    "requireSupervisorReview": false,
    "allowExit": true
}
```

Validated behavior:

- cabinet/operator authentication works;
- CabinetAccess opens and closes;
- BEFORE/AFTER snapshots are accepted;
- delta detects `TAG-002` as removed;
- ToolAssignment is created as active;
- end-of-day-check detects the temporary pending assignment;
- second CabinetAccess returns the tool;
- final end-of-day-check is clean.
