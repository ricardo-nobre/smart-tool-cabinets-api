# Notas do Contrato OpenAPI

- O contrato descreve um MVP académico, não segurança ou operação enterprise.
- `allowExit` só existe em `EndOfDayCheckResponse`.
- `SupervisorResolution` recebe `assignmentId` singular e resolve uma única `ToolAssignment`.
- `reasonCode` e `reportText` são obrigatórios.
- Assignments resolúveis: `ACTIVE` e `PENDING_REVIEW`.
- `RESOLVED` é encerramento formal da ocorrência, não devolução física.
- Tokens Bearer são simplificados para demonstração.
