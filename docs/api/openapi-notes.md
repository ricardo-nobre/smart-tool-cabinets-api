# Notas do contrato OpenAPI

Estas notas alinham a leitura de `docs/api/openapi.yaml` com o foco do relatório intermédio.

## Foco funcional
- Custódia por operador com base em retirada e devolução (ou não devolução).
- Fecho físico sempre permitido, com `operationalResult`.
- Pendências avaliadas no `end-of-day-check`.
- Resolução formal por supervisor com `reasonCode` e `reportText`.

## Simplificações assumidas nesta fase
- Não tratar regras ricas de devolução por armário como eixo central da API.
- `originCabinetCode` pode existir como dado técnico de rastreabilidade, sem protagonismo semântico.
- Sem overdesign de cenários operacionais fora do objetivo do intermédio.

## Regra de coerência
Se houver dúvida entre notas e contrato, prevalece `docs/api/openapi.yaml`.

