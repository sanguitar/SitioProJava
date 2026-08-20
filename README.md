# Sítio PRO

Aplicação Spring Boot monolítica modular para gestão de produção, categorias, frota, FIPE/cache e abastecimentos do sítio.

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop com Docker Compose v2

## Executar com Docker

Crie o arquivo local de ambiente a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Revise `DB_PASSWORD` no `.env`. Depois suba tudo:

```powershell
docker compose up --build
```

A aplicação ficará em:

```text
http://localhost:8083/sitio/painel
```

O Compose sobe:

- aplicação Spring Boot;
- Microsoft SQL Server 2022;
- volume persistente `sqlserver_data`;
- serviço de inicialização que cria o banco `DB_NAME` quando ainda não existir;
- healthcheck para SQL Server e aplicação.

## Variáveis de ambiente

As credenciais e dados de conexão ficam fora do código:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
DB_ENCRYPT
DB_TRUST_SERVER_CERTIFICATE
```

O arquivo `.env` não deve ser versionado. Use `.env.example` apenas como modelo de desenvolvimento.

## Executar localmente sem Docker

Com um SQL Server já rodando, defina as variáveis e execute:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_HOST="localhost"
$env:DB_PORT="1433"
$env:DB_NAME="sitio_db"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="sua_senha_local"
mvn spring-boot:run
```

## Testes e build

```powershell
mvn clean verify
```

## Estrutura principal

```text
src/main/java/com/example/sitiopro
├── abastecimento
├── categoria
├── dashboard
├── frota
├── health
├── producao
└── shared
```

```text
src/main/resources
├── static
│   ├── css
│   └── js
└── templates
    ├── abastecimento
    ├── categoria
    ├── dashboard
    ├── fragments
    ├── frota
    └── producao
```
