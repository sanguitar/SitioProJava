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

## Observabilidade

A observabilidade é opcional. O ERP continua funcionando sem Elasticsearch, Kibana, APM Server ou Elastic Agent.

Arquitetura local:

```text
Browser/API -> Nginx -> Spring Boot -> SQL Server
                         |
                         +-> logs ECS + Elastic APM Java Agent

Elastic Agent -> logs app/Nginx/SQL Server + métricas Docker -> Elasticsearch -> Kibana
APM Server <- Elastic APM Java Agent -> Elasticsearch -> Kibana
```

Versões fixadas:

```text
Elasticsearch/Kibana/APM Server/Elastic Agent: 9.5.0
Elastic APM Java Agent: 1.56.0
ECS Logging Java Logback Encoder: 1.8.0
```

Para subir somente o ERP:

```powershell
docker compose --env-file .env up --build -d
```

Para subir ERP + Elastic:

```powershell
docker compose --env-file .env `
  -f docker-compose.yml `
  -f docker-compose.observability.yml `
  up --build -d
```

Kibana local:

```text
http://localhost:5601
```

Use o usuário `elastic` e a senha definida localmente em `ELASTIC_PASSWORD`. Não versionar `.env`.

Portas administrativas:

- Kibana é publicado somente em `127.0.0.1:${KIBANA_PORT:-5601}`;
- Elasticsearch fica apenas na rede Docker;
- APM Server fica apenas na rede Docker;
- Elasticsearch/Kibana/APM não ficam atrás do Nginx nesta etapa.

Actuator:

```text
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
GET /actuator/info      ADMIN
GET /actuator/metrics   ADMIN
```

Endpoints sensíveis como `/actuator/env`, `/actuator/beans`, `/actuator/configprops`, `/actuator/mappings`, `/actuator/heapdump`, `/actuator/threaddump` e `/actuator/loggers` não são expostos.

Correlação:

- Nginx preserva `X-Request-ID` válido ou gera um novo;
- Spring devolve `X-Request-ID` na resposta e coloca `request.id` nos logs;
- Elastic APM adiciona `trace.id`/`transaction.id` aos logs quando o javaagent está ativo;
- mensagens seguras de erro inesperado retornam um código pesquisável no Kibana.

Exemplos de busca no Kibana:

```text
request.id : "93af927c"
trace.id : "<trace_id_do_apm>"
event.action : "LOGIN_FAILURE"
module : "estoque"
http.response.status_code : "500"
```

Dashboards provisionados:

```text
Sítio Guaratinguetá - System Overview
Sítio Guaratinguetá - API
Sítio Guaratinguetá - External Integrations
```

Artefatos versionados:

```text
infra/observability/elasticsearch/setup.sh
infra/observability/kibana/setup.sh
infra/observability/kibana/dashboards/system-overview.json
infra/observability/kibana/dashboards/api.json
infra/observability/elastic-agent/elastic-agent.yml
infra/observability/apm/apm-server.yml
```

Retenção e recursos para ambiente doméstico/dev:

- ILM inicial: rollover diário ou shard primário de 512 MB, deleção após 30 dias;
- heap Elasticsearch: `ELASTICSEARCH_HEAP=1g`;
- limite Elasticsearch: `ELASTICSEARCH_MEM_LIMIT=1536m`;
- limite Kibana: `KIBANA_MEM_LIMIT=1g`;
- limite APM Server: `APM_SERVER_MEM_LIMIT=256m`;
- limite Elastic Agent: `ELASTIC_AGENT_MEM_LIMIT=512m`;
- RAM extra esperada para observabilidade completa: aproximadamente 3 GB a 4 GB, além do ERP/SQL Server.

Para verificar disco:

```powershell
docker compose --env-file .env `
  -f docker-compose.yml `
  -f docker-compose.observability.yml `
  exec elasticsearch curl -u "elastic:$env:ELASTIC_PASSWORD" http://localhost:9200/_cat/allocation?v
```

Para desligar APM sem remover a stack:

```text
ELASTIC_APM_ENABLED=false
```

Para desligar a observabilidade inteira, suba somente o compose base. Não use `down -v` no ambiente principal, pois isso remove volumes persistentes.

Troubleshooting:

```powershell
docker compose --env-file .env ps
docker compose --env-file .env -f docker-compose.yml -f docker-compose.observability.yml ps
docker compose --env-file .env -f docker-compose.yml -f docker-compose.observability.yml logs kibana
docker compose --env-file .env -f docker-compose.yml -f docker-compose.observability.yml logs elastic-agent
```

O Elastic Agent roda como `root` no container para ler o Docker socket montado como somente leitura (`/var/run/docker.sock:ro`) e arquivos de log dos volumes. Não use esse container para mutações Docker.

## Convenções Web

- páginas Thymeleaf: `/sitio/**`;
- APIs REST próprias: `/api/v1/**`;
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
SITIOPRO_ENVIRONMENT
SITIOPRO_APP_VERSION
SITIOPRO_OBSERVABILITY_ENABLED
SITIOPRO_OBSERVABILITY_APM_SERVER_URL
SITIOPRO_LOG_FILE
SITIOPRO_LOG_LEVEL
HIBERNATE_SQL_LOG_LEVEL
HIBERNATE_BIND_LOG_LEVEL
ELASTIC_STACK_VERSION
ELASTIC_APM_AGENT_VERSION
ELASTIC_APM_ENABLED
ELASTIC_PASSWORD
KIBANA_SYSTEM_PASSWORD
ELASTIC_APM_SECRET_TOKEN
KIBANA_ENCRYPTION_KEY
KIBANA_PORT
ELASTICSEARCH_HEAP
ELASTICSEARCH_MEM_LIMIT
KIBANA_MEM_LIMIT
APM_SERVER_MEM_LIMIT
ELASTIC_AGENT_MEM_LIMIT
SITIOPRO_INTEGRATIONS_ZONE
SITIOPRO_INTEGRATION_RUNNING_TIMEOUT
SITIOPRO_OPEN_METEO_ENABLED
OPEN_METEO_BASE_URL
SITIO_LATITUDE
SITIO_LONGITUDE
SITIO_TIMEZONE
SITIOPRO_CLIMATE_CONTEXT
OPEN_METEO_CRON
OPEN_METEO_CONNECT_TIMEOUT
OPEN_METEO_READ_TIMEOUT
OPEN_METEO_STALE_AFTER
OPEN_METEO_FORECAST_DAYS
OPEN_METEO_RETENTION_DAYS
SITIOPRO_EMBRAPA_AGROFIT_ENABLED
EMBRAPA_AGROFIT_BASE_URL
EMBRAPA_AGROFIT_TOKEN
EMBRAPA_AGROFIT_CRON
EMBRAPA_AGROFIT_MAX_PAGES
```

O arquivo `.env` não deve ser versionado. Use `.env.example` apenas como modelo de desenvolvimento.

Após a adoção do Flyway, o valor padrão de `JPA_DDL_AUTO` é `validate`. O Hibernate valida o mapeamento das entidades, mas não deve evoluir schema com `update`.

Em SQL Server, a senha do usuário `sa` é definida na primeira inicialização do volume por `MSSQL_SA_PASSWORD`. Alterar essa variável depois que `sqlserver_data` já existe não troca a senha salva no banco; mantenha o `.env` alinhado à senha original do volume ou planeje uma rotação explícita de credencial.

O usuário `sa` deve ficar restrito ao SQL Server e ao `sqlserver-init`. A aplicação usa `DB_USERNAME` com permissões de leitura/escrita, e o Flyway usa `FLYWAY_USERNAME` como dono técnico das migrations.

## Segurança e usuários

A interface Thymeleaf usa Spring Security com sessão, login por formulário e CSRF habilitado. Não há JWT, OAuth, API pública autenticável ou frontend SPA.

Regras principais:

- `/login`, `/error`, `/health` e assets estáticos são públicos;
- `/actuator/health`, `/actuator/health/liveness` e `/actuator/health/readiness` são públicos com informação mínima;
- `/actuator/info` e `/actuator/metrics/**` exigem perfil `ADMIN`;
- `/sitio/**`, aliases web antigos e `/api/fipe/**` exigem autenticação;
- `/sitio/admin/**`, `/sitio/configuracoes/**`, `/administracao/**` e `/configuracoes/roadmap` exigem perfil `ADMIN`;
- `/api/v1/estoque/**` exige autenticação; POSTs continuam protegidos por CSRF enquanto a autenticação mobile definitiva não for definida;
- demais rotas `/api/v1/**` permanecem negadas;
- `/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**` exigem perfil `ADMIN`;
- logout é feito por `POST /logout` com CSRF.

Perfis disponíveis:

- `ADMIN`: administra usuários, configurações e módulos normais;
- `OPERADOR`: usa módulos operacionais e não acessa administração sensível.

Para criar o primeiro administrador, use variáveis externas somente no primeiro bootstrap:

```text
SITIOPRO_INITIAL_ADMIN_ENABLED=true
SITIOPRO_INITIAL_ADMIN_LOGIN=admin_local
SITIOPRO_INITIAL_ADMIN_PASSWORD=senha_forte_local
SITIOPRO_INITIAL_ADMIN_NAME=Administrador Local
```

O `.env` não é base de usuários e não participa da autenticação diária. Ele apenas fornece os dados do primeiro ADMIN quando `SITIOPRO_INITIAL_ADMIN_ENABLED=true`. O bootstrap é idempotente: se o login inicial já existir, não recria, não altera dados e não reseta senha; se o login não existir, só cria o ADMIN quando a tabela `usuarios` ainda está vazia. Depois que o primeiro ADMIN existir, volte `SITIOPRO_INITIAL_ADMIN_ENABLED=false`. A autenticação passa sempre por Spring Security, `UsuarioDetailsService`, `UsuarioRepository` e a tabela `usuarios`. Nunca versione senhas reais.

## Estoque

O módulo funcional de estoque usa um domínio único para a propriedade, em vez de estoques separados por criação ou área. As páginas principais ficam em:

```text
/sitio/estoque
/sitio/estoque/itens
/sitio/estoque/itens/novo
/sitio/estoque/itens/{id}
/sitio/estoque/movimentacoes
/sitio/estoque/movimentacoes/nova
/sitio/estoque/locais
/sitio/estoque/categorias
/sitio/estoque/inventario
```

O saldo é calculado a partir do histórico de movimentações, que é a fonte confiável. Não há saldo materializado nesta etapa. Entradas aumentam saldo, consumo/perda/descarte/ajuste de saída reduzem saldo e transferência altera locais sem mudar o total da propriedade. O service rejeita quantidade zero/negativa e não permite estoque negativo por padrão.

Custos ficam preparados para compras futuras: uma entrada pode registrar custo unitário e/ou custo total. O último preço vem da entrada mais recente com custo unitário, e o custo médio atual é a média ponderada simples das entradas com custo total.

API inicial:

```text
GET  /api/v1/estoque/resumo
GET  /api/v1/estoque/itens
GET  /api/v1/estoque/itens/{id}
POST /api/v1/estoque/itens
GET  /api/v1/estoque/movimentos
GET  /api/v1/estoque/movimentos/{id}
POST /api/v1/estoque/movimentos
```

Não há JWT nesta etapa. A API usa a sessão Spring Security atual e permanece protegida por CSRF nos métodos mutáveis. A documentação OpenAPI fica disponível para ADMIN em `/swagger-ui.html` e `/v3/api-docs`.

## Compras

O módulo funcional de compras registra fornecedores, compras em rascunho, itens comprados e a confirmação com entrada real no estoque. A compra não altera saldo manualmente: ao confirmar, ela chama o service oficial de estoque e cada item passa a apontar para a movimentação `ENTRADA` gerada.

Rotas principais da interface:

```text
/sitio/compras
/sitio/compras/nova
/sitio/compras/{id}
/sitio/compras/fornecedores
/sitio/compras/fornecedores/novo
/sitio/compras/fornecedores/{id}
```

Regras principais:

- compras nascem como `RASCUNHO`;
- rascunhos podem receber itens e ter dados de cabeçalho ajustados;
- confirmação exige fornecedor ativo, ao menos um item, itens de estoque ativos e local de destino ativo;
- confirmação é transacional e idempotente por status, bloqueio pessimista e vínculo único com movimento de estoque;
- compras `CONFIRMADA` ou `CANCELADA` não são editadas;
- cancelamento de rascunho é permitido apenas para `ADMIN`;
- totais são recalculados no servidor com `BigDecimal`.

API inicial:

```text
GET  /api/v1/compras
GET  /api/v1/compras/{id}
POST /api/v1/compras
POST /api/v1/compras/{id}/itens
POST /api/v1/compras/{id}/confirmar
GET  /api/v1/fornecedores
POST /api/v1/fornecedores
```

Não há integração externa com fornecedores, NF-e ou cotação nesta etapa.

## Integrações externas e clima

O backend é o único consumidor das APIs externas. Browser e futuros aplicativos móveis consultam dados locais:

```text
Open-Meteo / Embrapa Agrofit
            ↓
       RestClient
            ↓
 sincronização + Resilience4j
            ↓
       SQL Server
         ↙     ↘
  Thymeleaf   /api/v1
```

O painel `/sitio/admin/integracoes` é exclusivo de `ADMIN` e lê apenas configuração e SQL Server. Abrir o dashboard principal ou o painel administrativo nunca chama um provedor. A ação manual usa `POST /sitio/admin/integracoes/{fonte}/sincronizar`, sessão e CSRF. A trava persistida em `integracao_estados`, combinada com bloqueio pessimista, impede duas sincronizações simultâneas da mesma fonte e recupera locks órfãos após o timeout configurado.

Cada execução registra início, fim, fonte, resultado, contadores, erro seguro e correlação. Falhas externas preservam os dados anteriores e não alteram liveness/readiness do ERP. Resilience4j é aplicado somente ao HTTP externo: timeout e 5xx recebem retry limitado; erros 4xx permanentes não; HTTP 429 respeita `Retry-After` e não entra em loop automático. Circuit breaker e rate limiter são locais e não são indicadores de health.

### Open-Meteo

O adapter usa `GET /v1/forecast` e persiste previsão horária de temperatura, umidade relativa, precipitação, probabilidade de precipitação, vento, rajadas, ET0, umidade do solo de 0–1 cm e weather code. O upsert usa `fonte + contexto + data_hora_previsao`; dados iguais são contabilizados como ignorados, alterações são atualizadas e previsões antigas são removidas depois da retenção configurável, inicialmente 30 dias.

Configuração local mínima:

```text
SITIOPRO_OPEN_METEO_ENABLED=true
SITIO_LATITUDE=<latitude real somente no .env local>
SITIO_LONGITUDE=<longitude real somente no .env local>
SITIO_TIMEZONE=<timezone IANA da propriedade>
OPEN_METEO_CRON=0 17 */3 * * *
```

O cron usa seis campos do Spring e pode ser alterado sem recompilar. O padrão executa no minuto 17 aproximadamente a cada três horas. O dashboard mostra o último conjunto conhecido e sinaliza desatualização após `OPEN_METEO_STALE_AFTER`. A API própria consulta somente SQL Server:

```text
GET /api/v1/clima/resumo
GET /api/v1/clima/previsao?horas=168
```

Dados Open-Meteo exigem atribuição CC BY 4.0, mantida ao lado do resumo climático. A API gratuita é destinada a uso não comercial e possui limites oficiais; para uso comercial, configure o endpoint/plano oficial adequado antes da produção.

### Embrapa Agrofit piloto

O piloto usa a API AGROFIT v1 da Plataforma AgroAPI e somente o endpoint paginado `/culturas`. O acesso exige Bearer token obtido por cadastro e assinatura oficial na plataforma. O token fica exclusivamente em `EMBRAPA_AGROFIT_TOKEN` no `.env` local, não é persistido, exibido ou registrado em log.

```text
SITIOPRO_EMBRAPA_AGROFIT_ENABLED=true
EMBRAPA_AGROFIT_TOKEN=<token oficial somente no .env local>
EMBRAPA_AGROFIT_CRON=0 29 3 * * MON
EMBRAPA_AGROFIT_MAX_PAGES=1
```

O recorte padrão consulta apenas uma página e faz upsert pelo nome normalizado da cultura, sem remover itens ausentes. Uso, cache e persistência devem continuar respeitando o instrumento e os termos associados à assinatura AGROFIT. Sem token, o ERP permanece funcional e o painel mostra a fonte como não configurada ou desabilitada.

### Validação local

Para um smoke test real, habilite apenas a integração desejada no `.env`, suba a aplicação e use o botão **Sincronizar agora**. Depois confirme o histórico em `/sitio/admin/integracoes/{fonte}`, os dados em `/api/v1/clima/previsao` e os eventos `integration.sync.*` no dashboard Kibana **External Integrations**. Testes Maven usam servidor HTTP local e não consomem internet ou quota de terceiros.

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
V5__create_estoque_schema.sql
V6__create_compras_schema.sql
V7__create_external_integrations_schema.sql
V8__align_climate_integer_columns.sql
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
├── nginx
│   └── nginx.conf
└── observability
    ├── apm
    ├── elastic-agent
    ├── elasticsearch
    └── kibana
```

```text
src/main/java/com/example/sitiopro
├── abastecimento
├── categoria
├── dashboard
├── estoque
├── integracao
│   ├── core
│   ├── clima/openmeteo
│   └── embrapa/agrofit
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
