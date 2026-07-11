# Test Results Evidence

Date: 2026-07-11

Command:

```powershell
cd backend
mvn test
```

Result:

```text
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Covered rules:

- inventory delta calculates removed, returned and unchanged tools;
- close creates an ACTIVE ToolAssignment when a tool disappears between BEFORE and AFTER snapshots;
- close marks a ToolAssignment as RETURNED when a tool reappears;
- close accepts an empty AFTER snapshot when the cabinet becomes empty;
- end-of-day-check reports ACTIVE assignments as pending;
- SupervisorResolution marks ACTIVE and PENDING_REVIEW assignments as RESOLVED;
- SupervisorResolution rejects RETURNED and already RESOLVED assignments;
- SupervisorResolution rejects assignments from another operator;
- SupervisorResolution rejects duplicated assignment IDs;
- SupervisorResolution request/response no longer expose allowExit;
- partial supervisor resolution keeps end-of-day blocked while another assignment remains pending;
- AFTER snapshot without previous BEFORE fails;
- duplicate BEFORE snapshot fails;
- duplicate AFTER snapshot fails;
- Spring Boot application context loads with the test profile.
