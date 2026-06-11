# Domínio e regras (API-first)

Este documento fixa o mínimo de domínio e regras que a API tem de respeitar no relatório intermédio.
O objetivo não é modelar toda a operação da empresa, mas sim garantir coerência entre contrato, fluxo e dados.

## 1) Centro do sistema

- `ToolAssignment`: estado de custódia de cada ferramenta por operador.
- `CabinetAccess`: interação local entre dispositivo e armário.
- `SupervisorResolution`: decisão humana formal quando existem pendências.
- `AuditLog`: rastreabilidade transversal das ações relevantes.

## 2) Âmbito do intermédio

### Entra no âmbito principal
- Definição e validação da API principal de custódia.
- Fluxo fim-a-fim: autenticação -> acesso -> snapshots -> close -> end-of-day-check -> resolução de supervisor.
- Modelo de dados essencial para suportar esse fluxo.
- Evidência de comportamento real do backend face ao contrato.

### Fica fora do âmbito principal
- Modelação exaustiva de processos internos da empresa.
- Funcionalidades analíticas avançadas (ex.: estatística histórica detalhada).
- Variações operacionais de hardware (ex.: múltiplas gavetas e desbloqueio parcial).

### Pode entrar como evolução futura
- Observabilidade avançada de uso de ferramentas.
- Hardening de segurança e autenticação avançada.
- Extensão do dispositivo para topologias de armário mais complexas.

## 3) Regras normativas

1. O armário fecha sempre fisicamente.
2. O endpoint de close devolve `operationalResult`; não devolve erro terminal por falta de ferramenta.
3. O delta `BEFORE`/`AFTER` cria ou encerra movimentos de custódia.
4. Uma ferramenta só pode ter uma atribuição ativa de cada vez.
5. `ACTIVE` e `PENDING_REVIEW` contam como pendência no fim do dia.
6. `RETURNED` e `RESOLVED` não contam como pendência no fim do dia.
7. Dados de origem de atribuição podem ser guardados para rastreabilidade, sem serem a regra central da API intermédia.
8. O sistema deteta factos operacionais; não infere causa real.
9. A classificação causal é humana e pertence ao supervisor.

## 4) SupervisorResolution (obrigatório)

Campos obrigatórios:
- `operatorId`
- `supervisorId`
- `reasonCode`
- `reportText`
- `decisionAt`
- `allowExit`
- `assignmentIds`

Notas obrigatórias:
- Autorizar saída não apaga a pendência factual da ferramenta.
- Cada resolução deve gerar registo em `AuditLog`.
