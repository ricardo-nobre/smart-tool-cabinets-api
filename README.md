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
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
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

O simulador apresenta um menu interativo que permite:

- escolher entre os quatro armários de demonstração;
- autenticar o operador por PIN ou NFC, sem introduzir credenciais manualmente;
- retirar, devolver ou trocar ferramentas em fluxos separados;
- trocar de armário sem reiniciar o simulador;
- verificar as pendências de fim do dia.

Antes de apresentar o menu, o simulador confirma que a API e a base de dados
estão prontas. O fluxo normal é:

1. escolher o armário;
2. escolher PIN ou NFC;
3. movimentar ferramentas ou consultar o estado do operador.

Se a API estiver noutra porta:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\dev\simulator.ps1 -BaseUrl http://localhost:18080
```

Documentação técnica e diagramas encontram-se em `docs/`.
