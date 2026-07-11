# Especificação Técnica

A fonte de verdade do contrato HTTP está em `docs/api/openapi.yaml`.

## Entidades Principais

- `cabinet`: armário físico e respetiva credencial demo por hash.
- `app_user`: operador, supervisor ou admin.
- `tool`: ferramenta RFID, com `active` para excluir ferramentas fora de serviço.
- `cabinet_access`: acesso curto de operador a um armário.
- `inventory_snapshot` e `inventory_snapshot_item`: leituras RFID usadas no delta.
- `tool_assignment`: custódia de ferramenta por operador.
- `supervisor_resolution`: decisão humana formal para uma assignment.
- `supervisor_resolution_assignment`: associação 1:1 entre resolução e assignment.
- `audit_log`: registo transversal mínimo.

## Estados de ToolAssignment

- `ACTIVE`: ferramenta retirada e sob responsabilidade do operador.
- `RETURNED`: ferramenta devolvida ao armário de origem.
- `PENDING_REVIEW`: ocorrência pendente, por exemplo devolução detetada noutro armário.
- `RESOLVED`: ocorrência formalmente encerrada por supervisor.

## Restrições

- `tool.tag_code` é único.
- Só pode existir um `CabinetAccess` aberto por armário.
- Só pode existir uma assignment aberta por ferramenta (`ACTIVE` ou `PENDING_REVIEW`).
- Cada `SupervisorResolution` cobre exatamente uma `ToolAssignment`.
- Cada `ToolAssignment` só pode ter uma resolução final.

## API Nuclear

- `POST /api/device/auth`
- `POST /api/device/operator-auth`
- `POST /api/device/cabinet-accesses`
- `POST /api/device/cabinet-accesses/{cabinetAccessId}/snapshots`
- `POST /api/device/cabinet-accesses/{cabinetAccessId}/close`
- `GET /api/operators/{operatorId}/tool-assignments`
- `GET /api/operators/{operatorId}/end-of-day-check`
- `POST /api/supervisor/resolutions`

## Regras de Saída

`allowExit` pertence apenas ao `end-of-day-check`. O supervisor resolve assignments; o backend recalcula `allowExit` numa consulta posterior.
