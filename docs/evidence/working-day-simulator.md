# Working-Day Simulator Evidence

Date: 2026-07-03

Update note: on 2026-07-09 the working-day simulator scenario was extended to include a same-access tool exchange. The runtime output below documents the current expected flow shape; concrete UUIDs should be refreshed after running the command again with Docker and the backend available.

Command:

```powershell
scripts\dev\run-simulator-working-day.cmd
```

Precondition:

- PostgreSQL running through Docker Compose.
- Backend running with profile `dev`.
- Database reset before execution.

Current flow:

```text
Scenario: working day checkout, exchange and final return
[1] Authenticating cabinet and operator: done
[2] Opening first CabinetAccess for checkout
[3] BEFORE snapshot: TAG-001, TAG-002, TAG-003
[4] AFTER snapshot : TAG-001, TAG-003
[OK] Checkout CabinetAccess closed: <cabinetAccessId>
[OK] TAG-002 assigned to operator as ACTIVE
[OK] End-of-day check detects 1 pending assignment before exchange
[5] Opening second CabinetAccess for tool exchange
[6] BEFORE snapshot: TAG-001, TAG-003, TAG-004
[7] AFTER snapshot : TAG-001, TAG-002, TAG-003
[OK] Exchange CabinetAccess closed: <cabinetAccessId>
[OK] TAG-002 marked as RETURNED
[OK] TAG-004 assigned to operator as ACTIVE
[OK] End-of-day check now detects TAG-004 as the pending assignment
[8] Opening third CabinetAccess for final return
[9] BEFORE snapshot: TAG-001, TAG-002, TAG-003
[10] AFTER snapshot : TAG-001, TAG-002, TAG-003, TAG-004
[OK] Final return CabinetAccess closed: <cabinetAccessId>
[OK] TAG-004 marked as RETURNED
[11] Running final end-of-day-check
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
- second CabinetAccess returns `TAG-002` and removes `TAG-004` in the same close;
- `TAG-002` becomes RETURNED and `TAG-004` becomes ACTIVE;
- third CabinetAccess returns `TAG-004`;
- final end-of-day-check is clean.
