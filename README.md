# Sítio Guaratinguetá

Aplicação Spring Boot monolítica modular para gestão rural, com telas Thymeleaf em `/sitio/**`, FIPE/cache, frota, abastecimentos, categorias e produção.

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop com Docker Compose v2

No IntelliJ, configure o Project SDK e o Maven Runner para JDK 21.

## Arquitetura Docker

```text
Browser -> Nginx -> Spring Boot -> SQL Server
```

Serviços principais:

- `nginx`: proxy reverso oficial `nginx:1.28.3-alpine`, exposto em `80` por padrão;
- `app`: aplicação Spring Boot interna em `8083`;
- `sqlserver`: Microsoft SQL Server 2022 com volume persistente `sqlserver_data`;
- `sqlserver-init`: cria `DB_NAME`, logins técnicos e permissões mínimas quando necessário.

O compose base é adequado para produção simples porque expõe somente o Nginx. O arquivo `docker-compose.override.yml` é versionado para desenvolvimento e publica também a aplicação em `8083` e o SQL Server em `1433`.

## Executar com Docker completo

Crie o arquivo local de ambiente a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Revise as senhas locais no `.env` antes de subir: `MSSQL_SA_PASSWORD` para administração/bootstrap do SQL Server, `FLYWAY_PASSWORD` para migrations e `DB_PASSWORD` para o usuário técnico da aplicação. Depois suba tudo:

```powershell
docker compose up --build -d
```

URLs de desenvolvimento:

```text
http://localhost/sitio/painel
http://localhost:8083/sitio/painel
```

As telas `/sitio/**` redirecionam para `/login` quando não há sessão autenticada.

Para usar o host local planejado, adicione ao arquivo de hosts do sistema:

```text
127.0.0.1 sitioguaratingueta.test
127.0.0.1 www.sitioguaratingueta.test
```

Então acesse:

```text
http://sitioguaratingueta.test/sitio/painel
```

## Executar app local pelo IntelliJ

Suba a infraestrutura com Docker Compose e execute o Spring Boot localmente no IntelliJ com perfil `dev`.

Variáveis principais:

```text
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8083
SERVER_FORWARD_HEADERS_STRATEGY=native
DB_HOST=localhost
DB_PORT=1433
DB_NAME=sitio_db
DB_USERNAME=sitiopro_app
DB_PASSWORD=sua_senha_app
FLYWAY_USERNAME=sitiopro_migration
FLYWAY_PASSWORD=sua_senha_flyway
```

## Produção simples

Para validar a configuração base sem o override de desenvolvimento:

```powershell
docker compose -f docker-compose.yml --env-file .env config
```

Para subir somente com Nginx publicado:

```powershell
docker compose -f docker-compose.yml --env-file .env up --build -d
```

TLS, Certbot e renovação de certificados ainda não fazem parte desta etapa.

## Convenções Web

- páginas Thymeleaf: `/sitio/**`;
- futuras APIs REST próprias: `/api/v1/**`;
- endpoint FIPE atual: `/api/fipe/**`, preservado para a funcionalidade existente da frota.

Rotas antigas planejadas como `/gestao/**`, `/criacoes/**`, `/agricultura/**`, `/agua/**`, `/propriedade/**`, `/administracao/**` e `/configuracoes/roadmap` redirecionam temporariamente para a nova árvore `/sitio/**`.

## Variáveis de ambiente

As credenciais e dados de conexão ficam fora do código:

```text
SPRING_PROFILES_ACTIVE
SERVER_PORT
SERVER_FORWARD_HEADERS_STRATEGY
NGINX_HTTP_PORT
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
FLYWAY_USERNAME
FLYWAY_PASSWORD
MSSQL_SA_PASSWORD
DB_ENCRYPT
DB_TRUST_SERVER_CERTIFICATE
MSSQL_PID
JPA_DDL_AUTO
JPA_SHOW_SQL
FLYWAY_ENABLED
FLYWAY_BASELINE_ON_MIGRATE
SESSION_COOKIE_SECURE
SITIOPRO_INITIAL_ADMIN_ENABLED
SITIOPRO_INITIAL_ADMIN_LOGIN
SITIOPRO_INITIAL_ADMIN_PASSWORD
SITIOPRO_INITIAL_ADMIN_NAME
```

O arquivo `.env` não deve ser versionado. Use `.env.example` apenas como modelo de desenvolvimento.

Após a adoção do Flyway, o valor padrão de `JPA_DDL_AUTO` é `validate`. O Hibernate valida o mapeamento das entidades, mas não deve evoluir schema com `update`.

Em SQL Server, a senha do usuário `sa` é definida na primeira inicialização do volume por `MSSQL_SA_PASSWORD`. Alterar essa variável depois que `sqlserver_data` já existe não troca a senha salva no banco; mantenha o `.env` alinhado à senha original do volume ou planeje uma rotação explícita de credencial.

O usuário `sa` deve ficar restrito ao SQL Server e ao `sqlserver-init`. A aplicação usa `DB_USERNAME` com permissões de leitura/escrita, e o Flyway usa `FLYWAY_USERNAME` como dono técnico das migrations.

## Segurança e usuários

A interface Thymeleaf usa Spring Security com sessão, login por formulário e CSRF habilitado. Não há JWT, OAuth, API pública autenticável ou frontend SPA.

Regras principais:

- `/login`, `/error`, `/health` e assets estáticos são públicos;
- `/sitio/**`, aliases web antigos e `/api/fipe/**` exigem autenticação;
- `/sitio/admin/**`, `/sitio/configuracoes/**`, `/administracao/**` e `/configuracoes/roadmap` exigem perfil `ADMIN`;
- `/api/v1/**` fica negado até existir uma API versionada de verdade;
- logout é feito por `POST /logout` com CSRF.

Perfis disponíveis:

- `ADMIN`: administra usuários, configurações e módulos normais;
- `OPERADOR`: usa módulos operacionais e não acessa administração sensível.

Para criar o primeiro administrador, use variáveis externas somente no primeiro bootstrap:

```text
SITIOPRO_INITIAL_ADMIN_ENABLED=true
SITIOPRO_INITIAL_ADMIN_LOGIN=admin_local
SITIOPRO_INITIAL_ADMIN_PASSWORD=senha_forte_local
SITIOPRO_INITIAL_ADMIN_NAME=Administrador
```

O bootstrap é idempotente: só cria usuário quando a tabela `usuarios` está vazia. Depois que o primeiro ADMIN existir, volte `SITIOPRO_INITIAL_ADMIN_ENABLED=false`. Nunca versione senhas reais.

## Flyway e schema

O schema do banco é versionado por Flyway. As migrations ficam em:

```text
src/main/resources/db/migration
```

Convenção de nomes:

```text
V1__create_current_schema.sql
V2__add_current_lookup_indexes.sql
V3__create_users_and_security.sql
V4__add_business_audit_columns.sql
```

Regras:

- crie sempre uma nova migration para alterar schema;
- não edite uma migration que já foi aplicada em algum ambiente;
- não use `DROP` destrutivo sem plano explícito de preservação de dados;
- não crie tabelas para funcionalidades ainda não implementadas;
- mantenha tipos SQL Server compatíveis com as entidades JPA;
- rode `.\mvnw.cmd clean verify` antes de concluir mudanças relevantes.

Em banco completamente vazio, Flyway executa `V1`, `V2` e as próximas migrations em ordem. Em banco de desenvolvimento já criado antes pelo Hibernate, `FLYWAY_BASELINE_ON_MIGRATE=true` registra baseline na versão `1` sem recriar tabelas, e as migrations posteriores continuam sendo aplicadas. Depois disso, o Hibernate entra com `ddl-auto=validate`.

Para diagnosticar falha de migration:

```powershell
docker compose --env-file .env logs app
docker compose --env-file .env exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "<senha>" -C -d sitio_db -Q "SELECT * FROM dbo.flyway_schema_history ORDER BY installed_rank"
```

Nunca coloque credenciais reais no README, em migrations ou em arquivos versionados. Evite `docker compose down -v` no ambiente principal porque isso remove o volume persistente do SQL Server.

## Testes e build

```powershell
.\mvnw.cmd clean verify
docker compose --env-file .env.example config
docker compose -f docker-compose.yml --env-file .env.example config
```

## Estrutura principal

```text
infra
└── nginx
    └── nginx.conf
```

```text
src/main/java/com/example/sitiopro
├── abastecimento
├── categoria
├── dashboard
├── frota
├── health
├── planejamento
├── producao
├── shared
└── usuario
```

```text
src/main/resources
├── db
│   └── migration
├── static
│   ├── css
│   └── js
└── templates
    ├── abastecimento
    ├── admin
    ├── categoria
    ├── dashboard
    ├── fragments
    ├── frota
    ├── planejamento
    ├── producao
    ├── security
    └── usuario
```
