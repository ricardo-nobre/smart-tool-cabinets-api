# Domínio e Regras

Este documento resume as regras finais do MVP da API de smart tool cabinets.

## Conceitos

- `CabinetAccess`: interação curta entre operador e armário.
- `InventorySnapshot`: leitura RFID `BEFORE` ou `AFTER` associada a um `CabinetAccess`.
- `ToolAssignment`: custódia de uma ferramenta por um operador.
- `SupervisorResolution`: decisão humana formal sobre uma única pendência.

## Custódia

1. Uma ferramenta presente no snapshot `BEFORE` e ausente no `AFTER` cria uma `ToolAssignment` em estado `ACTIVE`.
2. Uma ferramenta só pode ter uma assignment aberta de cada vez.
3. São assignments abertas: `ACTIVE` e `PENDING_REVIEW`.
4. Uma ferramenta inativa (`tool.active=false`) não deve originar nova assignment operacional.
5. Uma ferramenta devolvida ao armário de origem passa para `RETURNED`.
6. Uma ferramenta detetada noutro armário passa para `PENDING_REVIEW` e guarda o contexto da deteção.

## End-Of-Day Check

O endpoint `GET /api/operators/{operatorId}/end-of-day-check` recalcula sempre:

- `pendingAssignmentsCount`: número de assignments `ACTIVE` ou `PENDING_REVIEW`;
- `requireSupervisorReview`: `true` se existir pelo menos uma pendência;
- `allowExit`: `true` apenas se não existir nenhuma pendência.

Não existe scheduler fixo às 18h. O check é chamado quando o operador tenta terminar o dia ou quando um sistema externo pretende validar a saída.

## SupervisorResolution

1. O supervisor só intervém quando o `end-of-day-check` indica pendências.
2. Cada `SupervisorResolution` trata exatamente uma `ToolAssignment`.
3. A resolução pode tratar assignments `ACTIVE` ou `PENDING_REVIEW`.
4. Assignments `RETURNED` ou `RESOLVED` não podem ser resolvidas novamente.
5. `RESOLVED` significa encerramento formal da ocorrência, não devolução física.
6. `allowExit` não é enviado, escolhido ou persistido pelo supervisor.

Reason codes admitidos:

- `TOOL_LOST`
- `TOOL_DAMAGED`
- `RFID_FAILURE`
- `MANUAL_VERIFICATION`
- `OTHER`

Efeito em `tool.active`:

- `TOOL_LOST`, `TOOL_DAMAGED` e `RFID_FAILURE` desativam a ferramenta;
- `MANUAL_VERIFICATION` e `OTHER` preservam o estado atual da ferramenta.

## Migrações

A V3 remove `allow_exit` de `supervisor_resolution`. Esta alteração direta à V3 é aceitável neste projeto porque ainda não existe produção e as bases locais de demonstração são descartáveis. Em sistemas de produção, uma migração já aplicada não deve ser alterada retroativamente.

A V4 reforça a relação uma resolução para uma assignment e ajusta o índice parcial para impedir assignments abertas duplicadas por ferramenta.
