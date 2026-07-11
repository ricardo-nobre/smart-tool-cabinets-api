# Smart Tool Cabinets API

Backend desenvolvido para a unidade curricular de Projeto da LEIRT.

## Requisitos

- Java 21
- Maven
- Docker e Docker Compose
- PowerShell

## Base de dados

```powershell
scripts\dev\start-local.cmd
```

Para repor a base de dados antes de repetir a demonstração:

```powershell
scripts\dev\reset-db.cmd
```

## Backend

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Testes

```powershell
cd backend
mvn test
```

## Simulador

Com o backend em execução, noutro terminal:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\simulator.ps1
```

Documentação técnica, diagramas e registos de validação encontram-se em `docs/`.
