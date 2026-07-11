# Workflows

## Fluxo Operacional

1. O armário autentica-se.
2. O operador autentica-se por PIN ou NFC.
3. O backend cria um `CabinetAccess`.
4. O armário envia snapshot `BEFORE`.
5. O operador retira ou devolve ferramentas.
6. O armário envia snapshot `AFTER`.
7. O backend fecha o `CabinetAccess`, calcula o delta e atualiza `ToolAssignment`.
8. O operador pode repetir o fluxo noutros armários.

## Retirada

`BEFORE - AFTER` cria assignments `ACTIVE`, desde que a ferramenta esteja ativa e não tenha outra assignment aberta.

## Devolução

`AFTER - BEFORE` procura uma assignment aberta da ferramenta: `ACTIVE` ou `PENDING_REVIEW`.

- se reaparecer no armário de origem, a assignment `ACTIVE` ou `PENDING_REVIEW` passa para `RETURNED`;
- se reaparecer noutro armário, a assignment passa ou permanece `PENDING_REVIEW`;
- se não existir assignment aberta, o close fecha com discrepância.

## Fim do Dia

O `end-of-day-check` não cria novas pendências. Ele apenas consulta assignments `ACTIVE` e `PENDING_REVIEW`.

Sem pendências:

- `pendingAssignmentsCount = 0`
- `requireSupervisorReview = false`
- `allowExit = true`

Com pendências:

- `pendingAssignmentsCount > 0`
- `requireSupervisorReview = true`
- `allowExit = false`

## Supervisor

Quando há pendências, o supervisor cria uma resolução para uma assignment específica. A resolução marca essa assignment como `RESOLVED`. Num novo `end-of-day-check`, `allowExit` é recalculado. Se restarem outras assignments pendentes, continua `false`.
