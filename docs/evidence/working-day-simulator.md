# Working-Day Simulator Evidence

Date: 2026-07-11

Command:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\simulator.ps1
```

Precondition:

- PostgreSQL running through Docker Compose.
- Backend running with profile `dev`.
- Database reset before execution.

Output:

```text
Running simulator against http://localhost:8080
Scenario: working day checkout, exchange and final return
[1] Cabinet and operator authenticated
[2] Opening first CabinetAccess for checkout
[3] BEFORE snapshot: TAG-001, TAG-002, TAG-003
[4] AFTER snapshot : TAG-001, TAG-003
[OK] Checkout CabinetAccess closed: 95bc5479-d748-418a-981a-b24359cb9765
[OK] TAG-002 assigned to operator as ACTIVE
[OK] End-of-day check detects 1 pending assignment before exchange
[5] Opening second CabinetAccess for tool exchange
[6] BEFORE snapshot: TAG-001, TAG-003, TAG-004
[7] AFTER snapshot : TAG-001, TAG-002, TAG-003
[OK] Exchange CabinetAccess closed: 0c987179-7cc3-41dd-8027-ea6499ed281b
[OK] TAG-002 marked as RETURNED
[OK] TAG-004 assigned to operator as ACTIVE
[OK] End-of-day check now detects TAG-004 as the pending assignment
[8] Opening third CabinetAccess for final return
[9] BEFORE snapshot: TAG-001, TAG-002, TAG-003
[10] AFTER snapshot : TAG-001, TAG-002, TAG-003, TAG-004
[OK] Final return CabinetAccess closed: 92f1b76c-97cc-4c96-bdec-07be7a25f350
[OK] TAG-004 marked as RETURNED
[11] Running final end-of-day-check
[OK] No pending assignments
[OK] Operator can exit
{
    "operatorId":  "00000000-0000-0000-0000-000000000201",
    "pendingAssignments":  [

                           ],
    "pendingAssignmentsCount":  0,
    "requireSupervisorReview":  false,
    "allowExit":  true
}
```

Validated behavior:

- cabinet authentication works;
- operator authentication works;
- CabinetAccess opens and closes;
- BEFORE and AFTER snapshots are accepted;
- delta detects `TAG-002` as removed;
- ToolAssignment is created as active;
- end-of-day-check detects the temporary pending assignment;
- second CabinetAccess returns `TAG-002` and removes `TAG-004` in the same close;
- `TAG-002` becomes RETURNED and `TAG-004` becomes ACTIVE;
- third CabinetAccess returns `TAG-004`;
- final end-of-day-check is clean.
