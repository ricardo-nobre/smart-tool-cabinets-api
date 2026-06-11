# Smart Tool Cabinets - Base Tecnica (Semana 1)

Este repositório contem a base tecnica inicial do backend para o projeto LEIRT.

## Objetivo desta base

- Estruturar o backend por dominios
- Disponibilizar configuracoes iniciais (OpenAPI, Security, DB, Flyway)
- Criar skeletons de entidades, repositories, services e controllers
- Deixar TODOs explicitos para implementacao manual da logica de negocio

## Estrutura principal

- `backend/src/main/java/smarttoolcabinets/config` - configuracoes transversais
- `backend/src/main/java/smarttoolcabinets/common` - excecoes e utilitarios comuns
- `backend/src/main/java/smarttoolcabinets/{cabinet,tool,user,session,event,inventory,audit}`
  - `controller` - contratos REST
  - `service` - orquestracao de casos de uso (skeleton)
  - `repository` - acesso a dados com Spring Data JPA
  - `domain` - entidades persistentes
  - `dto` - contratos de entrada/saida da API

## Requisitos locais

- Java 21
- Maven 3.9+ (ou equivalente)
- Docker / Docker Compose

## Arranque rapido

1. Subir PostgreSQL local:

```powershell
docker compose up -d
```

2. Arrancar backend (quando `mvn` estiver disponivel):

```powershell
Set-Location backend
mvn spring-boot:run
```

3. Swagger UI:

- `http://localhost:8080/swagger-ui.html`

## Estado atual

- Configuracao base feita
- Migration inicial `V1__init_schema.sql` criada
- Seed tecnico `V2__seed_reference_data.sql` criado
- Endpoints skeleton disponiveis para:
  - `device auth`
  - `device sessions`
  - `device events`
  - `admin cabinets`
  - `admin tools`
  - `admin users`

## O que falta preencher manualmente

- Regras de negocio nos services
- Validacoes principais
- Mapeamento DTO <-> entidade
- Queries especificas de dominio
- Fluxo completo de tentativa de fecho e missing tools
- Persistencia e auditoria completa no fluxo end-to-end

