# Especificação técnica (API-first)

Este documento liga regras, modelo de dados e contrato da API para uso direto no relatório intermédio.
A fonte de verdade para requests e responses continua em `docs/api/openapi.yaml`.

## 1) Delimitação do problema

A API resolve o problema de custódia de ferramentas por operador com base em eventos de acesso ao armário e snapshots de inventário.

Fora do âmbito do intermédio:
- modelação empresarial completa;
- análise avançada de utilização histórica;
- variações de hardware não necessárias para o contrato principal.

## 2) Estruturas de dados essenciais

### `cabinet`
`id`, `code` (único), `location`, `api_key_hash`, `active`, `created_at`

### `app_user`
`id`, `username` (único), `full_name`, `role`, `pin_hash`, `nfc_uid`, `active`, `created_at`

### `tool`
`id`, `cabinet_id`, `tag_code` (único global), `display_name`, `type_code`, `serial_number`, `active`, `created_at`

### `cabinet_access`
`id`, `cabinet_id`, `operator_id`, `status`, `opened_at`, `closed_at`, `created_at`

### `inventory_snapshot`
`id`, `cabinet_access_id`, `snapshot_type`, `captured_at`, `source`, `created_at`

### `inventory_snapshot_item`
`id`, `snapshot_id`, `tag_code`, `tool_id` (nullable), `recognized`, `created_at`

### `tool_assignment`
`id`, `tool_id`, `operator_id`, `origin_cabinet_id`, `origin_cabinet_access_id`, `assigned_at`, `returned_at`, `status`, `pending_end_of_day`, `created_at`

Estados mínimos: `ACTIVE`, `RETURNED`, `PENDING_REVIEW`, `RESOLVED`.

`origin_cabinet_id` e `origin_cabinet_access_id` podem existir como contexto técnico de rastreabilidade,
mas o foco funcional do intermédio é o estado de custódia por operador (devolvida ou não devolvida, com pendência quando aplicável).

### `supervisor_resolution`
`id`, `operator_id`, `supervisor_id`, `reason_code`, `report_text`, `decision_at`, `allow_exit`, `created_at`

### `supervisor_resolution_assignment`
`supervisor_resolution_id`, `tool_assignment_id`, `created_at`

### `cabinet_event`
`id`, `cabinet_access_id`, `event_type`, `payload`, `occurred_at`, `created_at`

### `audit_log`
`id`, `actor_type`, `actor_ref`, `action`, `entity_type`, `entity_id`, `details_json`, `created_at`

## 3) Restrições funcionais mínimas
- `tool.tag_code` único global.
- máximo um `cabinet_access` aberto por armário.
- máximo uma `tool_assignment` ativa por ferramenta.
- `supervisor_resolution.report_text` obrigatório.

## 4) API nuclear para demonstração

### Device
- `POST /api/device/auth`
- `POST /api/device/operator-auth`
- `POST /api/device/cabinet-accesses`
- `POST /api/device/cabinet-accesses/{cabinetAccessId}/snapshots`
- `POST /api/device/cabinet-accesses/{cabinetAccessId}/close`
- `POST /api/device/cabinet-events`

### Operator
- `GET /api/operators/{operatorId}/tool-assignments`
- `GET /api/operators/{operatorId}/end-of-day-check`

### Supervisor
- `POST /api/supervisor/resolutions`
- `GET /api/supervisor/resolutions`

### Admin (suporte técnico mínimo)
- `POST /api/admin/cabinets`
- `POST /api/admin/tools`
- `POST /api/admin/users`

## 5) Semântica obrigatória do contrato
1. O close é sempre físico e devolve `operationalResult`.
2. A custódia é avaliada pelo estado atual de `tool_assignment` (`ACTIVE`, `RETURNED`, `PENDING_REVIEW`, `RESOLVED`).
3. O sistema deteta factos operacionais; não diagnostica a causa.
4. `reasonCode` e `reportText` são decisão humana do supervisor.

## 6) Evidências esperadas no relatório intermédio
- Coleção curta de requests/responses reais dos endpoints nucleares.
- Exemplo de `operationalResult` no close.
- Exemplo de `end-of-day-check` com pendências.
- Exemplo de criação de `SupervisorResolution` com `reportText` obrigatório.
