# Workflows e casos de uso (API-first)

Este ficheiro descreve apenas o comportamento necessário para provar a API no relatório intermédio.

## Atores
- Operador
- Supervisor

## Componentes técnicos
- Armário inteligente (dispositivo)
- API/backend
- Base de dados
- Auditoria

## Fluxo principal (fim-a-fim)
1. Dispositivo autentica-se em `POST /api/device/auth`.
2. Operador autentica-se em `POST /api/device/operator-auth`.
3. Dispositivo abre acesso em `POST /api/device/cabinet-accesses`.
4. Dispositivo envia snapshot `BEFORE` em `POST /api/device/cabinet-accesses/{cabinetAccessId}/snapshots`.
5. Operador retira/devolve ferramentas.
6. Dispositivo envia snapshot `AFTER` no mesmo endpoint.
7. Dispositivo fecha acesso em `POST /api/device/cabinet-accesses/{cabinetAccessId}/close`.
8. Backend calcula delta e atualiza `ToolAssignment`.
9. Operador pode repetir o fluxo noutros armários durante o dia.

## End-of-day check
1. API avalia estado final da custódia em `GET /api/operators/{operatorId}/end-of-day-check`.
2. Se houver pendências, `requireSupervisorReview = true`.
3. Supervisor regista resolução formal em `POST /api/supervisor/resolutions`.
4. A decisão define `allowExit`, sem apagar a pendência factual.

## Regras de interpretação operacional
- Devolução parcial durante o dia é comportamento normal.
- O sistema atualiza custódia por operador com base em retirada e devolução (ou não devolução).
- Dados de origem podem existir para rastreabilidade, sem serem a regra central do fluxo intermédio.
- O sistema devolve estado operacional; não emite diagnóstico causal automático.
- A causa é registada pelo supervisor com `reasonCode` e `reportText`.

## Casos de uso essenciais
- UC-01 Autenticar dispositivo.
- UC-02 Autenticar operador.
- UC-03 Abrir acesso ao armário (`CabinetAccess`).
- UC-04 Registar snapshots `BEFORE`/`AFTER`.
- UC-05 Fechar acesso e obter `operationalResult`.
- UC-06 Consultar atribuições do operador.
- UC-07 Validar fim do dia.
- UC-08 Supervisor registar `reasonCode` + `reportText`.
- UC-09 Supervisor decidir `allowExit`.
- UC-10 Auditar operações relevantes.
