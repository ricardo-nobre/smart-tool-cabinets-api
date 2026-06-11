# Documentação do projeto

A documentação está organizada para suportar o relatório intermédio com foco em API-first.

## Ordem recomendada de leitura
1. `docs/domain-rules.md`
2. `docs/workflows.md`
3. `docs/spec.md`
4. `docs/api/openapi.yaml`
5. `docs/api/openapi-notes.md`
6. `docs/intermediate-report-guide.md`

## O que cada ficheiro responde
- `docs/domain-rules.md`: domínio mínimo, regras obrigatórias e delimitação de âmbito.
- `docs/workflows.md`: fluxo fim-a-fim e casos de uso essenciais da API.
- `docs/spec.md`: modelo de dados essencial, endpoints nucleares e semântica de custódia por operador (devolvida/não devolvida e pendências).
- `docs/api/openapi.yaml`: contrato oficial de requests/responses e segurança.
- `docs/api/openapi-notes.md`: notas curtas de leitura do contrato e simplificações assumidas no intermédio.
- `docs/intermediate-report-guide.md`: estrutura recomendada do relatório intermédio, mapeamento para artefactos e calendarização até 1 de maio.

## Diagramas (apoio visual)
- `docs/diagrams/workflow-operator-day.puml`
- `docs/diagrams/supervisor-resolution-flow.puml`
- `docs/diagrams/use-case-overview.puml`
- `docs/diagrams/domain-high-level.puml`

## Regra simples
Se houver dúvida entre texto e contrato, vale o `docs/api/openapi.yaml`.
