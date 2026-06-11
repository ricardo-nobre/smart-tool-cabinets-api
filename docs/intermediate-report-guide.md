# Guia do relatório intermédio (API-first)

Este guia organiza o relatório intermédio sem reabrir o domínio.
A narrativa deve provar definição, especificação, validação e implementação inicial da API central.

## 1) Estrutura recomendada do relatório

### 1. Introdução
Objetivo: contextualizar o problema e a motivação do projeto.

### 2. Enquadramento e delimitação
Objetivo: explicar o que entra no âmbito do intermédio e o que fica fora.

### 3. Requisitos que moldam a API
Objetivo: mostrar requisitos funcionais e não funcionais diretamente ligados ao contrato.

### 4. Abordagem técnica
Objetivo: apresentar arquitetura de alto nível orientada à API.

### 5. Especificação da API
Objetivo: documentar endpoints, payloads, respostas, segurança e semântica.

### 6. Modelo de dados essencial
Objetivo: justificar apenas as estruturas necessárias ao contrato.

### 7. Estado atual da implementação
Objetivo: mostrar o que já funciona em runtime de forma verificável.

### 8. Calendarização atualizada
Objetivo: comparar plano inicial vs progresso real e ajustar até 1 de maio.

### 9. Próximos passos
Objetivo: fechar backlog para o relatório final sem dispersão.

## 2) Mapeamento rápido secção -> artefactos

- Introdução / enquadramento: `docs/domain-rules.md` (secções 1 e 2).
- Requisitos / workflow: `docs/workflows.md`.
- Especificação de API: `docs/api/openapi.yaml`.
- Modelo essencial: `docs/spec.md` (secções 2 e 3).
- Evidência de implementação: respostas reais dos endpoints nucleares.
- Apoio visual: `docs/diagrams/*.puml`.

## 3) Calendarização recomendada até 1 de maio

- 16-19 abril: fechar coerência documental e contrato API.
- 20-24 abril: recolher evidências de runtime dos endpoints nucleares.
- 25-28 abril: redigir corpo principal do relatório intermédio.
- 29-30 abril: revisão final de coerência, linguagem e anexos.
- 1 maio: submissão.

## 4) Critério de qualidade do intermédio

- Coerência total entre `domain-rules`, `workflows`, `spec` e `openapi`.
- Demonstração fim-a-fim mínima, mas funcional.
- Âmbito controlado: foco na API e no estado de custódia por operador (devolvida ou não devolvida), sem sobrecarga de cenários acessórios.


