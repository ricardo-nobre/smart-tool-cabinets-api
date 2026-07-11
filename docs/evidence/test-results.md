# Test Results Evidence

Date: 2026-07-11

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

Covered rules:

- inventory delta calculates removed, returned and unchanged tools;
- close creates an `ACTIVE` `ToolAssignment` when a tool disappears between `BEFORE` and `AFTER`;
- close marks a `ToolAssignment` as `RETURNED` when the tool reappears in the origin cabinet;
- return metadata is stored (`returnedAt`, `returnedToCabinetId`, `returnedViaCabinetAccessId`);
- close accepts an empty `AFTER` snapshot when the cabinet becomes empty;
- inactive tools do not create new assignments;
- a tool with an `ACTIVE` assignment cannot create a second open assignment;
- a tool with a `PENDING_REVIEW` assignment cannot create a second open assignment;
- a tool detected in a different cabinet becomes `PENDING_REVIEW` and blocks `allowExit`;
- a `PENDING_REVIEW` assignment becomes `RETURNED` when the tool later returns to the origin cabinet without supervisor intervention;
- the next end-of-day-check allows exit after that corrected physical return when no other pending assignments remain;
- end-of-day-check allows exit with no pending assignments;
- end-of-day-check reports `ACTIVE` and `PENDING_REVIEW` assignments as pending;
- end-of-day-check rejects an unknown operator;
- assignment status filters reject invalid values;
- `SupervisorResolution` resolves exactly one assignment;
- partial supervisor resolution keeps end-of-day blocked while another assignment remains pending;
- `SupervisorResolution` rejects `RETURNED`, already `RESOLVED`, wrong-operator and already-linked assignments;
- invalid supervisor, missing/invalid reason codes and blank reports are rejected;
- `TOOL_LOST`, `TOOL_DAMAGED` and `RFID_FAILURE` deactivate the tool;
- `MANUAL_VERIFICATION` preserves the current tool active state;
- `SupervisorResolution` request/response use singular assignment fields and do not expose `allowExit`;
- invalid snapshot sequences are rejected.
