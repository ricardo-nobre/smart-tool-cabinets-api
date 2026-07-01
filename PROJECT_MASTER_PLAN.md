# Smart Tool Cabinets API - Project Master Plan

Data base: 30/06, 11h

Objetivo: terminar o projeto com dominio tecnico real pelo aluno, mantendo o repositorio funcional e demonstravel.

## 1. Regras de trabalho

- Nao apagar o projeto existente.
- Nao partir a branch `main`.
- Trabalhar por branches pequenas.
- O aluno escreve o codigo critico.
- O assistente ajuda com planeamento, revisao, explicacao e erros pontuais.
- Cada dia termina com commit ou nota clara do que ficou por fazer.
- Nao adicionar dashboard, frontend, app movel, Kubernetes ou seguranca avancada.
- Priorizar backend funcional, scripts de demonstracao, testes essenciais, evidencias e relatorio.

## 2. Estado confirmado em 30/06

- Branch `main` alinhada com `origin/main`.
- M2 concluido: refatoracao de `Session` para `CabinetAccess`.
- `mvn test` passa com 4 testes.
- Greps obrigatorios nao encontram nomes antigos no codigo principal.
- Docker Compose valida a configuracao.
- PostgreSQL sobe em Docker e fica `healthy`.
- Backend arranca com profile `dev`.
- Flyway valida 2 migrations.
- Swagger esta acessivel em `/swagger-ui.html`, `/swagger-ui/index.html` e `/v3/api-docs`.
- Scripts HTTP de demo executam contra a API.

## 3. Plano diario final - 30/06 a 11/07

### 30/06 - Controlo do projeto e ambiente

Objetivo do dia:
Assumir controlo tecnico do projeto e garantir que o aluno sabe correr tudo.

Tarefas:

- [x] Confirmar que M2 esta concluido.
- [x] Correr `mvn test`.
- [x] Correr greps para confirmar que `Session` desapareceu do codigo principal.
- [x] Correr Docker Compose.
- [x] Abrir Swagger.
- [x] Correr scripts de demo existentes.
- [ ] Criar `docs/student-learning-log.md`.
- [ ] Repetir scripts numa base limpa e guardar outputs relevantes.

Comandos principais:

```powershell
git pull --ff-only
git status --short --branch

cd backend
mvn test
cd ..

git grep -n "smarttoolcabinets.session" -- backend/src/main/java backend/src/test/java
git grep -n "DeviceSession\|OpenSession\|CloseSession\|SessionRepository\|class Session" -- backend/src/main/java backend/src/test/java README.md docs

docker compose config
docker compose up -d
docker compose ps
```

Tecnologias a dominar:

- Git: `status`, `diff`, `log`, `branch`, `commit`, `push`.
- Maven: `mvn test`, `mvn spring-boot:run`.
- Docker Compose: `config`, `up`, `ps`, `logs`, `down`.
- Swagger: localizar e testar endpoints.

Entregavel:

- Ambiente funcional.
- Learning log iniciado.
- M2 confirmado.
- Lista de duvidas tecnicas.

### 01/07 - Teste de devolucao de ferramenta

Objetivo do dia:
Escrever codigo novo pelo aluno para provar o ciclo retirada -> devolucao.

Branch:

```powershell
git checkout -b feature/return-tool-test
```

Teste a criar:

```text
close_marks_assignment_returned_when_tool_reappears
```

Fluxo do teste:

1. Primeiro `CabinetAccess`: BEFORE contem `TAG-002`, AFTER nao contem `TAG-002`.
2. Close cria `ToolAssignment ACTIVE`.
3. Segundo `CabinetAccess`: BEFORE nao contem `TAG-002`, AFTER contem `TAG-002`.
4. Close marca o `ToolAssignment` como `RETURNED`.

Criterio de aceitacao:

- O teste compila.
- O teste passa.
- O aluno consegue explicar cada linha importante.
- `mvn test` passa.

Tecnologias a dominar:

- JUnit 5.
- Spring Boot tests.
- Repositories em testes.
- Entidades JPA.
- `ToolAssignment`.
- `CabinetAccess`.

Entregavel:

- Teste de devolucao commitado.
- Nota no learning log explicando como funciona a devolucao.

### 02/07 - Script de devolucao de ferramenta

Objetivo do dia:
Criar um simulador HTTP simples para retirada e devolucao.

Branch:

```powershell
git checkout -b feature/return-tool-simulator
```

Criar:

```text
scripts/dev/simulator-return-tool.ps1
scripts/dev/run-simulator-return-tool.cmd
```

Fluxo:

1. Autenticar armario.
2. Autenticar operador.
3. Abrir `CabinetAccess`.
4. Enviar BEFORE com `TAG-001`, `TAG-002`, `TAG-003`.
5. Enviar AFTER sem `TAG-002`.
6. Fechar o acesso.
7. Confirmar assignment `ACTIVE`.
8. Abrir novo `CabinetAccess`.
9. Enviar BEFORE sem `TAG-002`.
10. Enviar AFTER com `TAG-002`.
11. Fechar o acesso.
12. Confirmar assignment `RETURNED` ou pendencia removida.

Criterio de aceitacao:

- Script corre numa base limpa.
- Output e legivel.
- Mostra retirada e devolucao.
- README menciona este script.

Tecnologias a dominar:

- PowerShell.
- HTTP requests.
- JSON.
- Bearer tokens.
- Sequencia real de chamadas API.

Entregavel:

- Script de devolucao funcional.
- Output guardado em `docs/evidence/`.

### 03/07 - Teste de SupervisorResolution

Objetivo do dia:
Provar que pendencias podem ser resolvidas por supervisor.

Branch:

```powershell
git checkout -b feature/supervisor-resolution-test
```

Teste a criar:

```text
supervisor_resolution_marks_assignments_resolved
```

Fluxo:

1. Criar operador com assignment `ACTIVE`.
2. Confirmar que `end-of-day-check` encontra pendencia.
3. Criar `SupervisorResolution`.
4. Confirmar assignment `RESOLVED`.
5. Confirmar que `end-of-day-check` ja nao bloqueia ou reflete o estado esperado.

Criterio de aceitacao:

- Teste passa.
- `mvn test` passa.
- O aluno consegue explicar que a decisao e humana e nao inventada pelo sistema.

Tecnologias a dominar:

- Services.
- Repositories.
- Relacao `supervisor_resolution_assignment`.
- Estados de `ToolAssignment`.

Entregavel:

- Teste de resolucao.
- Nota tecnica no learning log.

### 04/07 - Testes de snapshots invalidos

Objetivo do dia:
Provar regras de consistencia dos snapshots.

Branch:

```powershell
git checkout -b feature/snapshot-validation-tests
```

Testes a criar:

```text
create_after_snapshot_without_before_should_fail
create_duplicate_before_snapshot_should_fail
create_duplicate_after_snapshot_should_fail
```

Criterio de aceitacao:

- Testes passam.
- Se algum comportamento estiver errado, corrigir apenas o minimo no service.
- `mvn test` passa.

Tecnologias a dominar:

- `InventoryService`.
- `InventorySnapshot`.
- `InventorySnapshotItem`.
- Excecoes.
- Regras de estado.

Entregavel:

- Testes de validacao.
- Correcoes minimas, se forem necessarias.

### 05/07 - Consolidacao da API

Objetivo do dia:
Verificar que a API principal esta coerente e que nao ha endpoints partidos.

Tarefas:

- [ ] Rever controllers.
- [ ] Confirmar path variables explicitas.
- [ ] Confirmar request params explicitos.
- [ ] Confirmar DTOs alinhados com `CabinetAccess`.
- [ ] Confirmar codigos de erro principais.
- [ ] Rever `GlobalExceptionHandler`.
- [ ] Confirmar que Swagger mostra endpoints certos.
- [ ] Rever exemplos na OpenAPI.

Nao fazer:

- Nao alterar regras de negocio.
- Nao reescrever services.
- Nao adicionar funcionalidades novas.

Tecnologias a dominar:

- Spring Controllers.
- DTOs.
- Bean Validation.
- Exception handling.
- OpenAPI/Swagger.

Entregavel:

- Pequenas correcoes de API, se necessarias.
- Checklist M3 atualizado.

### 06/07 - Evidencias finais

Objetivo do dia:
Criar pacote de evidencias para relatorio e defesa.

Criar ou atualizar:

```text
docs/evidence/demo-runtime-validation.md
docs/evidence/demo-flows.md
docs/evidence/test-results.md
docs/evidence/known-limitations.md
```

Guardar evidencias de:

- `mvn test`.
- Docker Compose.
- Flyway V1-V2.
- Swagger.
- normal-flow.
- return-flow.
- missing-tool-flow.
- `tool_assignment`.
- `end-of-day-check`.

Criterio de aceitacao:

- Evidencias sao reais.
- Nao ha resultados inventados.
- Relatorio final pode usar estas evidencias.

Tecnologias a dominar:

- PostgreSQL via Docker.
- `psql`.
- Flyway.
- Logs.
- Markdown tecnico.

Entregavel:

- Pasta `docs/evidence/` completa.

### 07/07 - Relatorio final: estrutura e primeiras secoes

Objetivo do dia:
Comecar relatorio final de forma seria.

Escrever:

1. Introducao.
2. Enquadramento e delimitacao.
3. Evolucao desde relatorio inicial/intermedio.
4. Requisitos finais.
5. Arquitetura.

Criterio:

- PT-PT.
- Sem vender como produto comercial.
- Sem esconder limitacoes.
- Maximo 30 paginas no total.

Entregavel:

- Primeira versao do relatorio com estrutura completa.

### 08/07 - Relatorio final: implementacao, API, dados e simulador

Objetivo do dia:
Escrever o corpo tecnico principal.

Escrever:

1. Modelo de dados.
2. Especificacao da API.
3. Implementacao.
4. Delta.
5. Custodia.
6. `SupervisorResolution`.
7. Simulador por scripts HTTP.

Incluir:

- ERD.
- Tabela de endpoints.
- Explicacao de `CabinetAccess`.
- Explicacao de `ToolAssignment`.

Entregavel:

- Relatorio com parte tecnica completa.

### 09/07 - Relatorio final: validacao, limitacoes e conclusao

Objetivo do dia:
Fechar o relatorio.

Escrever:

1. Testes.
2. Validacao runtime.
3. Evidencias.
4. Limitacoes.
5. Evolucao futura.
6. Conclusao.

Criterio:

- Relatorio deve ficar fechado neste dia.
- Dia 10 nao e para escrever relatorio do zero.

Entregavel:

- Relatorio final em versao quase pronta.

### 10/07 - Apresentacao final e ensaio

Objetivo do dia:
Preparar apresentacao e defesa.

Tarefas:

- [ ] Criar slides finais.
- [ ] Criar guiao.
- [ ] Preparar demo de 3-5 minutos.
- [ ] Preparar plano B com screenshots.
- [ ] Ensaiar apresentacao completa.
- [ ] Preparar respostas a perguntas tecnicas.

Slides:

1. Capa.
2. Problema.
3. Objetivos.
4. Arquitetura.
5. Fluxo operacional.
6. Modelo de dados.
7. API.
8. Implementacao.
9. Simulador.
10. Testes/validacao.
11. Limitacoes.
12. Conclusao.

Entregavel:

- Slides finais.
- Guiao.
- Demo ensaiada.

### 11/07 - Revisao final e entrega

Objetivo do dia:
Nao criar nada novo. So revisao e submissao.

Tarefas:

- [ ] Verificar relatorio.
- [ ] Verificar slides.
- [ ] Verificar GitHub.
- [ ] Verificar zip/dossier.
- [ ] Verificar comandos de demo.
- [ ] Fazer backup.
- [ ] Submeter.

Proibido:

- Nao refatorar.
- Nao adicionar features.
- Nao mudar migrations.
- Nao mexer em seguranca.
- Nao alterar API.

Entregavel:

- Submissao final.

## 4. Regras de estudo tecnico diario

Todos os dias, antes de terminar, o aluno deve escrever no `docs/student-learning-log.md`:

1. O que fiz hoje.
2. Que ficheiros toquei.
3. Que comandos corri.
4. Que erro apareceu.
5. Como corrigi.
6. Que parte consigo explicar na defesa.
7. Que parte ainda nao entendo.

Objetivo:
Garantir que o aluno conhece o projeto e nao apenas executa comandos.

## 5. Tecnologias a dominar

### Maven

Comandos essenciais:

```powershell
mvn clean
mvn test
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Conceitos:

- `pom.xml`.
- Dependencies.
- Plugins.
- Lifecycle.
- Diferenca entre root `pom.xml` e `backend/pom.xml`.

### Docker Compose

Comandos essenciais:

```powershell
docker compose config
docker compose up -d
docker compose ps
docker compose logs
docker compose down
```

Conceitos:

- Container PostgreSQL.
- Volumes.
- Portas.
- Variaveis de ambiente.

### PostgreSQL

Entrar na base de dados:

```powershell
docker exec -it stc-postgres psql -U postgres -d smart_tool_cabinets
```

Queries uteis:

```sql
select * from cabinet;
select * from app_user;
select * from tool;
select * from cabinet_access;
select * from inventory_snapshot;
select * from inventory_snapshot_item;
select * from tool_assignment;
select * from supervisor_resolution;
select * from flyway_schema_history;
```

### Flyway

Conceitos:

- Migrations versionadas.
- V1 cria schema.
- V1 cria o schema final atual.
- V2 cria o seed demo.
- `flyway_schema_history` mostra o que foi aplicado.

### Spring Boot

Conceitos:

- Controller recebe HTTP.
- DTO transporta dados.
- Service aplica regras.
- Repository acede a base de dados.
- Entity representa tabela.
- Transaction garante consistencia.

### OpenAPI / Swagger

Conceitos:

- Swagger UI permite explorar endpoints.
- OpenAPI documenta o contrato da API.
- Exemplos ajudam a demonstrar o fluxo principal.

### PowerShell scripts

Conceitos:

- Autenticacao do armario.
- Autenticacao do operador.
- Bearer tokens.
- Chamadas HTTP.
- JSON de request/response.
- Sequencia de demonstracao.
