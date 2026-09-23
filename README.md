# Sítio Guaratinguetá

Aplicação Spring Boot monolítica modular para gestão rural, com telas Thymeleaf em `/sitio/**`, FIPE/cache, frota, abastecimentos, categorias e produção.

Versão candidata atual: `1.0.0-rc1`.

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

Revise as senhas locais no `.env` antes de subir: `MSSQL_SA_PASSWORD` para administração/bootstrap do SQL Server, `FLYWAY_PASSWORD` para migrations e `DB_PASSWORD` para o usuário técnico da aplicação. Os valores de `.env.example` são apenas fictícios e não devem ser reutilizados. Depois suba tudo:

```powershell
docker compose up --build -d
```

URLs de desenvolvimento:

```text
http://localhost/sitio/painel
http://localhost:8083/sitio/painel
```

As telas `/sitio/**` redirecionam para `/login` quando não há sessão autenticada.

## Dashboard operacional

O painel existente em `/sitio/painel` concentra o que precisa de ação no dia. A ordem visual prioriza alertas ativos, tarefas vencidas ou críticas, compromissos do dia, estoque abaixo do mínimo e vencimentos; em seguida apresenta clima, tendências operacionais, compras e saúde das integrações.

O `DashboardService` monta um read model próprio e consulta somente serviços locais. Tarefas e alertas usam contagens agregadas e listas limitadas aos cinco itens mais relevantes; estoque reutiliza os cálculos oficiais de saldo com leitura em lote; compras reutilizam o resumo do domínio; clima e integrações leem o último estado disponível no SQL Server e podem aproveitar os caches opcionais já existentes. O bloco `tendencias` agrega no banco a postura e o consumo de ração dos últimos sete dias, além das compras confirmadas dos últimos seis meses; unidades de consumo permanecem separadas e períodos sem movimento são preenchidos com zero no read model. Abrir o painel nunca dispara sincronização nem chamada a Open-Meteo ou Agrofit, e o dashboard completo não é armazenado em cache.

Estados vazios e degradados são parte do fluxo normal: ausência de tarefas, alertas ou compras produz mensagens discretas; clima pode ficar `NORMAL`, `DESATUALIZADO` ou `SEM_DADOS`; falha de Redis recorre ao banco pelo mecanismo fail-open; Elastic/Kibana não participa da geração da página. SQL Server permanece a dependência essencial e a única fonte de verdade.

`ADMIN` e `OPERADOR` autenticados podem acessar tanto a página quanto o DTO estável para futuros clientes móveis:

```text
GET /sitio/painel
GET /api/v1/painel/resumo
```

O endpoint retorna apenas DTOs do dashboard, sem entidades JPA, segredos ou credenciais. Cada geração registra o evento estruturado `dashboard.loaded`, com duração, alertas ativos, tarefas vencidas, itens críticos e estado do clima.

## Criações e Aves

`Criações` é uma fronteira do monólito modular para manejo rural por espécie. A fundação comum desta versão contém apenas instalações; o subdomínio funcional é `Aves`. Suínos e piscicultura continuam no roadmap e não herdam uma entidade animal genérica.

O manejo de aves é orientado a lotes. Cada lote mantém identificação, finalidade, origem, instalação atual, quantidade inicial e quantidade atual protegida. Mudanças quantitativas e operacionais são registradas por serviços transacionais e por um histórico imutável de eventos. A interface principal fica em:

```text
/sitio/criacoes
/sitio/criacoes/aves
/sitio/criacoes/aves/instalacoes
/sitio/criacoes/aves/lotes
/sitio/criacoes/aves/incubacoes
```

Instalações suportam incubadora, criadouro de pintinhos, galinheiro, piquete e outros alojamentos. A capacidade configurada funciona como limite rígido para criação e transferência de lotes; instalações ocupadas não podem ser inativadas.

A ficha de lote reúne alimentação, mortalidade, pesagens, postura, transferências, custos conhecidos, alertas, tarefas e linha do tempo. Alimentação chama o serviço oficial de Estoque na mesma transação: o movimento `CONSUMO` permanece a fonte de verdade do saldo e fica vinculado ao registro de alimentação. Falha ou saldo insuficiente reverte a operação inteira. Consumo acumulado, consumo médio, custo conhecido e autonomia são read models derivados; autonomia é exibida somente como estimativa quando há dados suficientes.

Mortalidade reduz a quantidade disponível e pode gerar alerta deduplicado quando o percentual no período configurado ultrapassa o limite. Postura é aceita somente em lotes compatíveis e produz métricas de hoje, 7 e 30 dias. Pesos usam `BigDecimal`. Transferências preservam origem, destino, usuário e data. Nenhuma dessas operações cria controle individual por ave.

Incubações registram ovos, origem, incubadora e previsão. A finalização valida eclodidos e perdas, calcula taxas e pode criar atomicamente um lote de pintinhos vinculado. Chaves de idempotência e bloqueios de atualização impedem que reenvios de alimentação, mortalidade ou finalização criem efeitos duplicados.

Os códigos operacionais são definidos exclusivamente pelo backend. Lotes usam `AV-AAAA-NNNN` e incubações usam `INC-AAAA-NNNN`; uma sequência anual no SQL Server, protegida por application lock transacional, evita duplicidade em requisições concorrentes sem derivar o valor de IDs. Os DTOs de criação e edição não aceitam o código, inclusive no lote resultante de uma incubação.

Alertas de mortalidade, eclosão próxima e incubação atrasada reutilizam a engine de Alertas. Tarefas manuais ou recorrentes podem ser vinculadas a `LOTE:{id}` ou `INCUBACAO:{id}` sem duplicar a implementação de recorrência. Dias padrão de incubação de galinha e antecedência de eclosão são administrados em `/sitio/admin/configuracoes`. Os limites de mortalidade permanecem externos:

```text
CRIACAO_AVES_MORTALIDADE_ALERTA_PERCENTUAL=5.0
CRIACAO_AVES_MORTALIDADE_PERIODO_DIAS=7
```

API autenticada, paginada nas coleções e baseada somente em DTOs:

```text
GET  /api/v1/criacoes/aves/resumo
GET|POST /api/v1/criacoes/aves/instalacoes
GET|POST /api/v1/criacoes/aves/lotes
POST /api/v1/criacoes/aves/lotes/{id}/alimentacoes|mortalidades|pesagens|posturas|transferencias
GET|POST /api/v1/criacoes/aves/incubacoes
POST /api/v1/criacoes/aves/incubacoes/{id}/finalizar
GET|POST /api/v1/criacoes/aves/incubacoes/{id}/ovoscopias
GET /sitio/criacoes/aves/incubacoes/{id}/ovoscopias/ficha.pdf
```

`ADMIN` cadastra e edita instalações e lotes, encerra lotes e cancela incubação. `OPERADOR` consulta e registra o manejo normal, inclusive incubação. Todas as mutações continuam protegidas por sessão e CSRF. SQL Server é a única fonte de verdade; Redis e Elastic não participam das regras e podem permanecer desligados. O roadmap preserva extensões naturais para Suínos e Peixes, sem antecipar campos, tabelas ou regras desses domínios.

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

## Produção com HTTPS

O `docker-compose.yml` e o override automático `docker-compose.override.yml` mantêm o ambiente DEV
simples, com HTTP local, SQL Server publicado e certificado autoassinado aceito somente localmente.
Produção usa explicitamente `docker-compose.prod.yml`; não reutilize o override de desenvolvimento.

Antes de subir produção, configure no `.env` o perfil e os caminhos absolutos para um certificado e
uma chave TLS válidos, mantidos fora do repositório:

```text
SPRING_PROFILES_ACTIVE=prod
NGINX_TLS_CERTIFICATE_PATH=/caminho/seguro/fullchain.pem
NGINX_TLS_PRIVATE_KEY_PATH=/caminho/seguro/privkey.pem
SQLSERVER_TLS_CERTIFICATE_PATH=/caminho/seguro/sqlserver.crt
SQLSERVER_TLS_PRIVATE_KEY_PATH=/caminho/seguro/sqlserver.key
SESSION_COOKIE_SECURE=true
```

Valide a composição sem iniciar containers:

```powershell
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env config
```

Suba produção sem o override DEV:

```powershell
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env up --build -d
```

O Nginx mantém `/nginx-health` em HTTP para o healthcheck interno e redireciona as demais requisições
HTTP para HTTPS com status `308`. Em HTTPS ele aceita TLS 1.2/1.3, envia HSTS e headers de proteção,
encaminha os headers `X-Forwarded-*` controlados pelo proxy e preserva o request ID. O domínio não é
fixado no arquivo: DNS e certificado são responsabilidades do ambiente. Renove o certificado com a
ferramenta do emissor e recrie/recarregue o container Nginx depois da renovação.

O perfil `prod` força cookie de sessão `Secure`, `HttpOnly`, `SameSite=Lax`, timeout de 30 minutos e
tracking apenas por cookie. A aplicação usa os headers do proxy para reconhecer o esquema HTTPS.

Para SQL Server, o perfil `prod` força `encrypt=true`, que cifra a conexão, e
`trustServerCertificate=false`, que exige validação da
cadeia e do hostname do certificado. O nome usado em `DB_HOST` deve existir no CN/SAN do certificado
apresentado pelo SQL Server, e a autoridade emissora deve estar no truststore da JVM. Um certificado
autoassinado do container DEV não satisfaz esse requisito; instale no SQL Server um certificado
emitido por CA confiável e disponibilize a CA à JVM antes de usar o perfil `prod`. Não troque
`DB_TRUST_SERVER_CERTIFICATE` para `true` como atalho em produção.
O override monta o certificado e a chave no SQL Server e aplica
`infra/sqlserver/mssql.prod.conf`, que força TLS 1.2 nas conexões. Restrinja a leitura da chave ao
usuário do container. Se a CA for interna, importe apenas a CA pública no truststore da imagem/JVM
da aplicação; nunca copie a chave privada para a aplicação.

Mantenha `JPA_DDL_AUTO=validate`, `JPA_SHOW_SQL=false`, o bootstrap do administrador desabilitado
depois do primeiro acesso e as portas da aplicação/SQL Server sem publicação externa. O Actuator
expõe apenas health, info e metrics; health público não mostra detalhes, info/metrics exigem ADMIN e
os demais endpoints são negados pelo Spring Security.

## Backup e restauração do SQL Server

O volume `sqlserver_data` é persistente, mas não substitui backup. Faça o backup lógico com a
aplicação em operação; o SQL Server produz um arquivo consistente. Os exemplos abaixo usam a senha
já disponível apenas dentro do container e não a imprimem no terminal.

Crie um backup e copie-o para uma pasta local ignorada pelo Git:

```powershell
New-Item -ItemType Directory -Force backups | Out-Null
docker compose exec sqlserver /bin/bash -lc 'mkdir -p /var/opt/mssql/backup && /opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "BACKUP DATABASE [sitio_db] TO DISK = N''/var/opt/mssql/backup/sitio_db_rc1.bak'' WITH COPY_ONLY, INIT, CHECKSUM, STATS = 10"'
docker compose exec sqlserver /bin/bash -lc '/opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "RESTORE VERIFYONLY FROM DISK = N''/var/opt/mssql/backup/sitio_db_rc1.bak'' WITH CHECKSUM"'
docker cp sitiopro-sqlserver:/var/opt/mssql/backup/sitio_db_rc1.bak ./backups/sitio_db_rc1.bak
```

Guarde pelo menos uma cópia fora da máquina do Docker e proteja o arquivo como dado confidencial.
Teste periodicamente a restauração em um banco separado, nunca por cima do banco principal:

```powershell
docker cp ./backups/sitio_db_rc1.bak sitiopro-sqlserver:/var/opt/mssql/backup/sitio_db_rc1.bak
docker compose exec sqlserver /bin/bash -lc '/opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d master -Q "RESTORE FILELISTONLY FROM DISK = N''/var/opt/mssql/backup/sitio_db_rc1.bak''"'
docker compose exec sqlserver /bin/bash -lc '/opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d master -Q "IF DB_ID(N''sitio_db_restore_test'') IS NOT NULL THROW 50001, ''Banco temporario ja existe'', 1; RESTORE DATABASE [sitio_db_restore_test] FROM DISK = N''/var/opt/mssql/backup/sitio_db_rc1.bak'' WITH MOVE N''sitio_db'' TO N''/var/opt/mssql/data/sitio_db_restore_test.mdf'', MOVE N''sitio_db_log'' TO N''/var/opt/mssql/data/sitio_db_restore_test_log.ldf'', CHECKSUM, RECOVERY, STATS = 10"'
docker compose exec sqlserver /bin/bash -lc '/opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d master -Q "DBCC CHECKDB ([sitio_db_restore_test]) WITH NO_INFOMSGS, ALL_ERRORMSGS; SELECT MAX(TRY_CONVERT(INT, version)) AS flyway_version FROM [sitio_db_restore_test].dbo.flyway_schema_history WHERE success = 1"'
```

Use os nomes lógicos retornados por `RESTORE FILELISTONLY` para restaurar como um banco de teste com
`MOVE` para arquivos novos. Valide login, Flyway, contagens essenciais e os fluxos críticos antes de
considerar o backup recuperável.

Depois do ensaio, pare qualquer instância temporária da aplicação e remova somente o banco de teste:

```powershell
docker compose exec sqlserver /bin/bash -lc '/opt/mssql-tools18/bin/sqlcmd -b -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -d master -Q "IF DB_ID(N''sitio_db_restore_test'') IS NOT NULL BEGIN ALTER DATABASE [sitio_db_restore_test] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [sitio_db_restore_test]; END"'
```

O arquivo no host fica em `./backups/`, que é ignorado pelo Git. A cópia dentro de
`/var/opt/mssql/backup/` pode ser removida depois de confirmar a cópia externa. Nunca reutilize os caminhos
`.mdf`/`.ldf` do banco principal no `MOVE` e nunca execute o restore de teste com o nome `sitio_db`.

Para uma restauração real, pare primeiro `app` e `nginx`, preserve um backup do estado atual, restaure
o arquivo a partir de `master` e só então suba novamente os serviços. `WITH REPLACE` só deve ser usado
após confirmar o arquivo, o banco de destino e os caminhos retornados por `RESTORE FILELISTONLY`.
Nunca execute `docker compose down -v`: esse comando remove o volume persistente do SQL Server.

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
NGINX_HTTPS_PORT
NGINX_TLS_CERTIFICATE_PATH
NGINX_TLS_PRIVATE_KEY_PATH
SQLSERVER_TLS_CERTIFICATE_PATH
SQLSERVER_TLS_PRIVATE_KEY_PATH
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
SESSION_COOKIE_SAME_SITE
SESSION_TIMEOUT
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

Configuração técnica e valores para a primeira inicialização (opcionais para localização):

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

### Configurações operacionais

`GET|POST /sitio/admin/configuracoes` exige ADMIN e oferece nome da propriedade, timezone IANA,
latitude/longitude decimais, dias padrão de incubação de galinha e antecedência do alerta de eclosão.
O atalho fica na Central Admin. CSRF e DTO com lista explícita de campos protegem a atualização.

A V15 cria `configuracoes_operacionais`, uma entidade tipada com registro único, constraints,
controle de versão e auditoria `criado_em/por` e `alterado_em/por`. No startup, somente se o registro
não existir, são importados `SITIO_NOME_PROPRIEDADE`, `SITIO_TIMEZONE`, `SITIO_LATITUDE`,
`SITIO_LONGITUDE`, `CRIACAO_AVES_GALINHA_INCUBACAO_DIAS` e `CRIACAO_AVES_ECLOSAO_PROXIMA_DIAS`.
Depois disso, o SQL Server é a fonte de verdade; mudar env ou reiniciar não sobrescreve alterações da tela.
Não há gravação durante GET nem fallback que esconda falhas do banco.

Sem valores iniciais válidos, os defaults são Sítio Guaratinguetá, `Etc/UTC`, 21 dias e 2 dias.
Coordenadas ficam nulas em conjunto, impedindo consultas externas para uma localização inventada.
O período de galinha afeta apenas novos ciclos; previsões já registradas permanecem intactas.
A antecedência também alimenta os alertas e o resumo de Aves.

Open-Meteo usa uma fotografia da configuração para consulta e persistência. Alterar localização/fuso
avança sua revisão, isolando previsões e cache anteriores até a próxima sincronização, sem misturar locais.
URLs, cron, timeouts, habilitação das integrações e todos os secrets continuam em deployment config/env.
A partir da V16, nome e coordenadas são lidos de Propriedade e editados pelo serviço desse domínio,
na mesma transação dos parâmetros operacionais. Timezone e incubação continuam em Configurações.
O formulário inclui a versão física para impedir sobrescritas de edições concorrentes.
Agricultura e os demais cadastros não são alterados por esta tela.

### Propriedade: fundação física

`/sitio/propriedade` reúne o imóvel principal, áreas, talhões, piquetes, estruturas e recursos hídricos.
O resumo mostra área total informada e contagens dos cadastros; as listas incluem ativos e inativos.
Município, UF, área total e coordenadas podem permanecer não informados até existir dado confiável.
Coordenadas são centrais e opcionais em par; não representam limites, CRS/datum confirmado ou levantamento GIS.

A V16 cria seis tabelas tipadas, com auditoria e `@Version`. O backfill transfere nome e coordenadas da
V15 para `propriedades`, valida a cópia e remove os campos físicos antigos de Configurações.
Uma FK mantém o vínculo operacional; a propriedade principal é selecionada por atributo, nunca por ID fixo.
Novas instalações importam os valores iniciais pelo bootstrap já existente, sem sobrescrever dados administrados.
Uma alteração de coordenadas pelo domínio também invalida logicamente o contexto climático anterior.

Áreas são classificações físicas. Talhões e piquetes recebem códigos imutáveis `TL-0001` e `PQ-0001`
derivados do IDENTITY do SQL Server, sem campo de código no request. Sequências podem conter lacunas.
As FKs compostas garantem que uma área vinculada pertença ao mesmo imóvel.
`InstalacaoCriacao.estrutura` é opcional: instalações existentes permanecem sem vínculo e continuam
funcionando. O formulário de Criações permite vincular/desvincular estruturas ativas. Capacidade física
não substitui capacidade ou ocupação de aves. Estruturas com instalações ativas devem ser desvinculadas
antes da desativação. Desativar uma área preserva os vínculos existentes e impede novos vínculos.

MVC: `/sitio/propriedade` e subrotas `areas`, `talhoes`, `piquetes`, `estruturas`,
`recursos-hidricos`, com lista, `/novo`, `/{id}` e `/{id}/editar`.
POST cria/atualiza; POST `/{id}/desativar` exige a versão atual. O imóvel é editado em `/editar`.
API: GET `/api/v1/propriedade/resumo`; listas/detalhes nas mesmas cinco subrotas;
POST cria, PUT `/{id}` atualiza e POST `/{id}/desativar` desativa. PUT na raiz atualiza o imóvel.
Listas usam `pagina`/ `tamanho` com limite de 100. Requests são tipados e não aceitam entidades JPA.
ADMIN altera; OPERADOR apenas consulta. Todas as mutações exigem sessão e CSRF.
Sem plantios, culturas, safras, manejo animal, telemetria ou irrigação nesta etapa.

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

## Cache Redis opcional

Redis é uma camada descartável de aceleração no backend. SQL Server continua sendo a única fonte de verdade: sincronizações e regras persistem primeiro no banco e somente depois invalidam o cache. Browser e aplicativos móveis nunca acessam Redis diretamente.

Os caches iniciais usam Spring Cache, DTOs em JSON e chaves prefixadas por `sitiopro:`:

```text
clima:resumo:v1          TTL padrão 5 minutos
integracoes:status:v1    TTL padrão 30 segundos
agrofit:culturas:v1      TTL padrão 12 horas
```

Configuração no `.env` local:

```text
REDIS_ENABLED=true
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_USERNAME=<usuário ACL local>
REDIS_PASSWORD=<senha somente no .env local>
REDIS_CONNECT_TIMEOUT=500ms
REDIS_COMMAND_TIMEOUT=1s
REDIS_RETRY_AFTER=10s
CACHE_TTL_CLIMA_RESUMO=5m
CACHE_TTL_INTEGRACOES_STATUS=30s
CACHE_TTL_AGROFIT_CULTURAS=12h
```

O serviço usa o perfil Compose `cache`, não publica a porta `6379` e cria uma ACL limitada às chaves `sitiopro:*`. Para subir o ERP com cache:

```powershell
docker compose --env-file .env --profile cache up --build -d
```

Com `REDIS_ENABLED=false`, a aplicação usa `NoOpCacheManager` e não cria conexão Redis. Se Redis estiver habilitado e ficar indisponível ou exceder timeout, o `CacheErrorHandler` trata a leitura como miss e o service consulta SQL Server. Depois da primeira falha, novas operações de cache são suspensas por `REDIS_RETRY_AFTER`; invalidações ocorridas nesse período ficam pendentes e são reaplicadas antes da próxima leitura quando Redis voltar. O componente `cacheRedis` aparece no health geral como diagnóstico opcional, mas liveness e readiness continuam baseados somente no núcleo e no SQL Server.

Para simular falha sem parar o ERP:

```powershell
docker compose --profile cache stop redis
curl.exe http://localhost/actuator/health/readiness
docker compose --profile cache start redis
```

Para remover somente os dados descartáveis do cache, recrie o container Redis ou, usando as credenciais locais, remova apenas chaves com prefixo `sitiopro:`. Não execute `docker compose down -v` para limpar cache, pois esse comando também pode remover o volume persistente do SQL Server.

## Tarefas e alertas

O módulo operacional separa dois conceitos persistidos no SQL Server:

- **Tarefa** é uma atividade manual ou gerada por recorrência/alerta. Usa os status `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA` e `CANCELADA`, e as prioridades `BAIXA`, `NORMAL`, `ALTA` e `CRITICA`.
- **Alerta** é uma condição detectada automaticamente. Usa os status `ATIVO`, `RECONHECIDO` e `RESOLVIDO`, e as severidades `INFO`, `ATENCAO`, `ALTA` e `CRITICA`.

Alertas não criam tarefas automaticamente. Um usuário autenticado pode transformar um alerta em uma única tarefa vinculada. O histórico de criação, transição, reconhecimento, resolução e vínculo é persistido em `tarefa_alerta_eventos`.

Recorrências suportadas:

- diária;
- semanal;
- mensal;
- intervalo entre 1 e 365 dias.

A definição da série fica em `tarefa_recorrencias`; cada ocorrência possui a data programada e uma constraint única. O scheduler usa transação, lock pessimista, `@Version`, índice único e `sp_getapplock` com dono transacional para evitar duplicações em reinicializações, chamadas repetidas ou múltiplas instâncias.

Regras automáticas atuais:

- item ativo abaixo do estoque mínimo;
- lote próximo do vencimento;
- lote vencido com saldo;
- integração habilitada/configurada desatualizada;
- última sincronização de integração com falha;
- chuva acumulada prevista nas próximas 24 horas acima do limite configurado.

As regras consomem serviços/read models oficiais de Estoque, Integrações e Clima. Se uma fonte estiver indisponível, seu ciclo falha isoladamente e os demais continuam. Redis, Elastic e APM não são necessários para ler ou alterar Tarefas/Alertas.

Configuração externa:

```text
SITIOPRO_TASKS_SCHEDULER_ENABLED=true
SITIOPRO_ALERTS_INTERVAL=PT5M
SITIOPRO_TASK_RECURRENCES_INTERVAL=PT1M
SITIOPRO_TASKS_INITIAL_DELAY=PT30S
SITIOPRO_LOT_EXPIRY_WARNING_DAYS=30
SITIOPRO_RAIN_24H_ALERT_MM=50.0
SITIOPRO_TASK_RECURRENCES_BATCH_SIZE=100
```

MVC:

```text
GET|POST /sitio/tarefas...
GET|POST /sitio/alertas...
```

API autenticada:

```text
GET  /api/v1/tarefas
GET  /api/v1/tarefas/resumo
GET  /api/v1/tarefas/{id}
POST /api/v1/tarefas
POST /api/v1/tarefas/{id}/iniciar
POST /api/v1/tarefas/{id}/concluir
GET  /api/v1/alertas
GET  /api/v1/alertas/{id}
POST /api/v1/alertas/{id}/reconhecer
POST /api/v1/alertas/{id}/criar-tarefa
```

`ADMIN` pode administrar todas as tarefas e resolver alertas. `OPERADOR` pode criar tarefas, alterar as não atribuídas, as próprias ou as que criou, e consultar/reconhecer alertas. Todas as mutações MVC/API exigem CSRF. Notificações push, e-mail, WhatsApp e clientes mobile permanecem no roadmap; a API e o resumo operacional já fornecem a base para essas evoluções.

## Propriedade: perímetro cadastral

V20 adiciona um perímetro por propriedade e seus vértices ordenados. As coordenadas usam
`BigDecimal` / `DECIMAL(12,9)`, com limites de latitude/longitude, ordem positiva e unicidade
de ordem e coordenadas por perímetro. Não repita o vértice inicial no final da lista.
O cadastro permite de zero a 500 vértices, inclusive levantamentos ainda incompletos.

- Consulta: `GET /sitio/propriedade/perimetro` e `GET /api/v1/propriedade/perimetro`.
- Formulário: `GET /sitio/propriedade/perimetro/editar`; gravação: `POST /sitio/propriedade/perimetro`.
- API: `PUT /api/v1/propriedade/perimetro` substitui atomicamente os metadados e a lista completa.
- ADMIN altera; ADMIN/OPERADOR consultam. Mutações exigem CSRF. Não há vínculo de propriedade recebido no DTO.

Use a `versao` retornada pelo GET (`-1` enquanto não existe perímetro); atualização obsoleta
retorna 409. A gravação serializa a criação pelo bloqueio da propriedade principal e mantém
auditoria de autor/data e versão. Campos do request: `versao`, `statusCrs`, `crs`, `datum`,
`observacao`, `vertices` (`ordem`, `latitude`, `longitude`, `altitudeGeodesicaM`, `marco`, `observacao`).

O status do CRS começa em `NAO_CONFIRMADO`, mesmo quando há texto de referência informado.
`CONFIRMADO` exige a identificação explícita do CRS pelo administrador. Não há inferência
de SIRGAS 2000/WGS84, transformação de coordenadas, cálculo de área oficial ou certificação jurídica.
O status do georreferenciamento é derivado da quantidade de vértices e da confirmação do CRS.

V21 confirma documentalmente o CRS oficial do memorial como SIRGAS 2000 / EPSG:4674. O
SQL Server 2022 do projeto possui o SRID 4674 em `sys.spatial_reference_systems`, então a
representação espacial do perímetro usa `geography`, não `geometry`: os vértices são
latitude/longitude/altitude geodésicas, e a conferência métrica do SQL Server permanece no
elipsoide associado ao SRID real. A orientação do anel oficial é reorientada apenas na coluna
`geography` para representar a área menor; a tabela de vértices preserva a ordem documental
original.

Valores documentais do memorial ficam separados dos cálculos GIS:

- Área documental: `1,8955 ha`.
- Perímetro documental: `919,71 m`.
- Área/perímetro calculados pelo SQL Server são exibidos apenas como conferência operacional.

A API de perímetro expõe `mapa` e `geoJson` operacionais. Essas representações não substituem
memorial, certificação fundiária, área jurídica ou perímetro oficial. V21 também prepara
`propriedade_talhoes` com colunas espaciais nullable para uma geometria futura, sem implementar
edição GIS de talhões nem integração QGIS.

## Agricultura: safras e cultivos

A operação agrícola está em `/sitio/agricultura`. ADMIN administra Safras, Culturas e Cultivos;
OPERADOR consulta e registra Plantio, Acompanhamento, Adubação, Irrigação, Tratamento,
Ocorrência, Colheita e tarefas de campo.
As mutações MVC/API exigem CSRF e usam DTOs explícitos, sem binding de entidades.

- A V17 cria seis tabelas tipadas. V1–V16 permanecem intactas; Hibernate continua em `validate`.
- A V18 preserva V1–V17, cria quatro tabelas operacionais e amplia Colheita com destino,
  movimento de Estoque e chave de idempotência.
- A V19 preserva V1–V18 e evolui Ocorrências com título, status, resolução, histórico,
  referências Agrofit locais e controle de versão. Registros V18 são migrados sem perda.
- Safra pertence à Propriedade. Cultivo referencia obrigatoriamente Safra, CulturaAgricola e
  Talhão oficial. FKs compostas impedem misturar propriedades.
- A soma das áreas dos cultivos não finalizados, inclusive planejados, reserva a área física
  do Talhão. Não há alocação por janelas futuras nesta etapa. Um bloqueio transacional da
  propriedade serializa reservas e encerramento de safras sem alterar seu cadastro físico.
- CulturaAgricola é um catálogo interno com ciclo estimado e referência Agrofit opcional.
  O vínculo usa os registros já sincronizados no SQL Server; nenhuma tela exige internet.
- A previsão de colheita pode ser informada ou calculada pelo ciclo da cultura. O primeiro
  plantio registra a data real. Datas operacionais usam o timezone persistido do sítio.
- Origem externa exige descrição. Origem Estoque exige item/local, unidade compatível e lote
  quando aplicável. O consumo passa por `EstoqueMovimentoService`, na mesma transação do plantio.
- Plantio, Acompanhamento e Colheita exigem a versão atual do Cultivo e avançam sua revisão.
  Reenvios obsoletos são recusados sem repetir registros ou consumo. Falhas revertem toda a operação.
- Colheitas podem ser parciais ou finais. Quantidades e perdas usam BigDecimal e a unidade
  é preservada entre colheitas do mesmo Cultivo. O destino pode ser `SEM_ESTOQUE` ou `ESTOQUE`.
  Neste último, item e local oficiais são obrigatórios, a unidade deve ser compatível e a entrada
  passa por `EstoqueMovimentoService`. Retry retorna o registro original sem duplicar saldo.
- Adubações e Tratamentos registram operações executadas, com origem externa ou consumo do
  Estoque oficial. Irrigações registram duração e/ou volume. Ocorrências fitossanitárias são
  tipadas como praga, doença, deficiência, dano climático, planta daninha ou outro, mantêm
  acompanhamentos históricos e podem ser encerradas com resolução. A perda total encerra o Cultivo.
- Referências Agrofit são opcionais e lidas exclusivamente do catálogo persistido no SQL Server;
  nenhuma tela faz chamada HTTP. Catálogo vazio ou integração degradada não bloqueiam o módulo.
  As referências não prescrevem produto, dose, aplicação ou diagnóstico agronômico.
- Ocorrências altas ou críticas sincronizam um alerta persistente por ocorrência. O alerta é
  atualizado sem duplicação e resolvido no encerramento. A tarefa de inspeção usa a chave
  automática estável do módulo Tarefas, sem scheduler novo.
- As operações da V18 usam chaves idempotentes únicas por Cultivo. Movimento de Estoque e
  registro agrícola compartilham a transação; qualquer rollback desfaz ambos.
- Tarefas usam o serviço existente com `AGRICULTURA / CULTIVO:{id}`. Não há scheduler novo.
  Avisos de colheita próxima (sete dias) ou atrasada são calculados do estado local, sem
  criar alertas persistentes duplicados; desaparecem após a finalização do Cultivo.
- A ficha consulta `ClimaConsultaService`, nunca o cliente Open-Meteo. A leitura climática
  fica separada da transação da ficha e pode degradar sem impedir a consulta do cultivo.
- Todos os registros operacionais preservam a auditoria existente (autor e datas).

Rotas MVC:

```text
GET /sitio/agricultura
GET /sitio/agricultura/{safras|culturas|cultivos}
GET /sitio/agricultura/{safras|culturas|cultivos}/novo
GET /sitio/agricultura/{safras|culturas|cultivos}/{id}
GET /sitio/agricultura/{safras|culturas|cultivos}/{id}/editar
POST /sitio/agricultura/{safras|culturas|cultivos}
POST /sitio/agricultura/{safras|culturas|cultivos}/{id}
GET /sitio/agricultura/colheitas
GET /sitio/agricultura/{adubacao|irrigacao|tratamentos|ocorrencias}
GET /sitio/agricultura/ocorrencias/{id}
GET /sitio/agricultura/ocorrencias/{id}/editar
GET /sitio/agricultura/cultivos/{id}/{plantios|acompanhamentos|adubacoes|irrigacoes|tratamentos|ocorrencias|colheitas|tarefas}/novo
POST /sitio/agricultura/cultivos/{id}/{plantios|acompanhamentos|adubacoes|irrigacoes|tratamentos|ocorrencias|colheitas|tarefas}
POST /sitio/agricultura/ocorrencias/{id}
POST /sitio/agricultura/ocorrencias/{id}/{encerrar|tarefa-inspecao}
POST /sitio/agricultura/cultivos/{id}/status
```

API:

```text
GET /api/v1/agricultura/resumo
GET /api/v1/agricultura/{safras|culturas|cultivos}
GET /api/v1/agricultura/{adubacoes|irrigacoes|tratamentos|ocorrencias}
GET /api/v1/agricultura/cultivos/{id}
GET /api/v1/agricultura/ocorrencias/{id}
POST /api/v1/agricultura/{safras|culturas|cultivos}
PUT /api/v1/agricultura/{safras|culturas|cultivos}/{id}
PUT /api/v1/agricultura/ocorrencias/{id}
POST /api/v1/agricultura/cultivos/{id}/{plantios|acompanhamentos|adubacoes|irrigacoes|tratamentos|ocorrencias|colheitas|tarefas|status}
POST /api/v1/agricultura/ocorrencias/{id}/{encerrar|tarefa-inspecao}
```

Os atalhos antigos de Plantios levam aos Cultivos; Áreas/Talhões leva ao cadastro oficial
da Propriedade. Agrofit permanece somente como referência da CulturaAgricola. Não há conversão
automática de unidade, Venda, Financeiro, GIS/QGIS, receituário, diagnóstico ou automação agronômica.

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
V9__create_tasks_and_alerts.sql
V10__create_criacoes_aves_schema.sql
V11__create_operational_code_sequences.sql
V12__link_producao_to_estoque_categories.sql
V13__add_commercial_packaging_to_purchase_items.sql
V14__incubation_operational_readiness.sql
V15__create_operational_settings.sql
V16__create_property_foundation.sql
V17__create_agriculture_foundation.sql
V18__add_agriculture_field_operations.sql
V19__evolve_agriculture_phytosanitary_occurrences.sql
V20__create_property_perimeter.sql
V21__confirm_sirgas2000_property_spatial.sql
V22__add_georeferenced_talhoes.sql
V23__add_aves_ovoscopia_operacional.sql
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

Os testes de integração usam Testcontainers 1.21.4 e descobrem o Docker Desktop pelo named pipe do Windows. Mantenha o Docker Desktop iniciado antes do `clean verify`; não configure `DOCKER_HOST` ou caminhos `npipe` específicos da máquina no projeto. Se a descoberta falhar, valide primeiro `docker context show` e `docker info` no mesmo terminal que executará o Maven.

## Estrutura principal

```text
infra
├── nginx
│   └── nginx.conf
├── redis
│   └── entrypoint.sh
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
├── tarefas
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
    ├── tarefas
    ├── alertas
    └── usuario
```
