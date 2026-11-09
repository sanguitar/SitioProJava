# SitioPRO Java — auditoria de prontidão para aplicativos móveis

Data da auditoria: 2026-11-03

Commit auditado: `9191a6b feat: add operational water and modernize fleet`

Branch: `main`

Escopo: leitura estática do backend, migrations, segurança, contratos HTTP, testes e documentação existente. Nenhum código funcional, migration ou dado foi alterado.

## 1. Resumo executivo

O SitioPRO possui uma base backend consistente para continuar evoluindo: Java 21, Spring Boot 3.3.4, SQL Server, Flyway V1–V33, DTOs REST explícitos, validação, auditoria JPA, controle otimista em boa parte das entidades e várias operações idempotentes. Há 20 controllers REST sob `/api/v1`, totalizando 185 métodos mapeados no código (89 GET, 80 POST e 16 PUT), sem APIs legadas fora de `/api/v1`.

Ainda **não está pronto para um cliente iOS/Android de produção, especialmente offline-first**. Os bloqueadores são:

1. autenticação exclusiva por formulário/sessão `JSESSIONID` e CSRF, sem access token, refresh token, autenticação JSON nem resposta 401 uniforme para app nativo;
2. ausência de protocolo de sincronização incremental: não há cursor global, tombstones, `clientId`, outbox/inbox ou resolução padronizada de conflitos;
3. idempotência existe em muitos domínios, porém é campo de DTO e banco, não um contrato HTTP uniforme (`Idempotency-Key`) para todas as mutações;
4. paginação, filtros, ordenação e representação temporal são inconsistentes entre módulos;
5. OpenAPI é parcial, não declara o esquema de segurança e nem inclui os módulos mais recentes;
6. uploads/downloads importantes continuam apenas na camada web Thymeleaf;
7. faltam testes de contrato, autenticação mobile, sincronização, concorrência offline e desempenho.

Decisão recomendada: fechar um **Mobile API Foundation** antes de iniciar telas Swift. O app iOS pode começar após os itens P0 deste documento estarem contratados e testados; Android deve consumir exatamente o mesmo contrato OpenAPI.

## 2. Evidências e arquitetura atual

### 2.1 Stack e organização

| Área | Estado observado | Evidência |
|---|---|---|
| Runtime | Java 21 e Spring Boot 3.3.4 | `pom.xml` |
| Web | Spring MVC + Thymeleaf; REST separado em pacotes `*.api` | `src/main/java/com/example/sitiopro/**/api` |
| Persistência | Spring Data JPA + SQL Server; IDs `BIGINT/IDENTITY` predominantes | entidades dos domínios e migrations V1–V33 |
| Schema | Flyway V1–V33; Hibernate somente valida | `src/main/resources/db/migration`; `application-dev.yml` e `application-prod.yml` com `ddl-auto: validate` |
| Sessão | Spring Security form login e sessão HTTP | `usuario/security/SecurityConfig.java` |
| Auditoria | `AuditableEntity`, `@EnableJpaAuditing`, usuário autenticado como auditor | `shared/audit/AuditableEntity.java`; `shared/audit/AuditoriaConfig.java` |
| Concorrência | 37 usos de `@Version` encontrados; cobertura não é universal | entidades de propriedade, criações, tarefas, manutenção, água e frota |
| Erros REST | envelope comum com `timestamp`, `status`, `code`, `message`, `path`, `requestId` | `shared/api/ApiErrorResponse.java`; `shared/api/ApiExceptionHandler.java` |
| Observabilidade | request/trace IDs, MDC, Actuator protegido | `shared/observability/**`; `application.yml` |
| Cache | Redis opcional e desabilitado por padrão; operação deve funcionar sem ele | `application.yml`; `shared/cache/**` |

O desenho por feature está adequado para manter uma API modular. Controllers REST usam services e DTOs, sem acesso direto a repositories e sem exposição direta de entidades JPA nos controllers auditados.

### 2.2 Estado funcional por domínio

| Domínio | Estado real no código | Evidência principal |
|---|---|---|
| Administração/usuários | Funcional na web; configuração operacional tipada; sem API mobile de usuários/configurações | V3, V15; `usuario/**`; `administracao/configuracao/**` |
| Estoque | Funcional, API parcial, saldo por movimentos | V5; `EstoqueApiController`; `EstoqueMovimentoService` |
| Compras/fornecedores | Funcional; confirmação integra Estoque | V6; `ComprasApiController`; `CompraService` |
| Tarefas/alertas | Funcional e paginado; API não cobre cancelar/resolver | V9; `TarefasApiController`; `AlertasApiController` |
| Aves/incubação/ovoscopia | Funcional; API operacional extensa; PDF apenas web | V10, V14, V23; `AvesApiController`; `FichaOvoscopiaPdfService` |
| Suínos | Lotes, reprodução e sanidade funcionais | V24–V26; três controllers REST de suínos |
| Peixes | Lotes, água e sanidade funcionais | V27–V29; `PeixesApiController`; `PeixesSanidadeApiController` |
| Agricultura | Safras, cultivos, campo, ocorrências e mapa funcionais | V17–V19; `AgriculturaApiController` |
| Propriedade/GIS | Cadastro físico, perímetro e talhões georreferenciados funcionais | V16, V20–V22; `PropriedadeApiController`; repositories espaciais |
| Manutenção/patrimônio | Ativos, manutenções, planos e medidores funcionais | V30–V31; `ManutencaoApiController` |
| Água operacional | Nível, abastecimento, consumo, retirada, transferência, inspeção e alertas funcionais; irrigação hidráulica dedicada ainda não | V32; `AguaApiController`; `AguaService` |
| Frota/abastecimento | Modernizado para `BigDecimal`, idempotência e APIs v1 | V33; `FrotaApiController`; `AbastecimentoApiController` |
| Integrações | Open-Meteo e Agrofit persistidos; FIPE ainda tem adapter próprio acoplado ao service | V7–V8; `integracao/**`; `frota/service/FipeService.java` |
| Casa, despensa, segurança, reformas etc. | Placeholders planejados | `planejamento/PlanejamentoCatalogo.java` |

Observação: o catálogo visual está desatualizado em alguns pontos. Ele ainda marca Aves e Água como `PLANEJADO`, embora os controllers, services e migrations comprovem implementação. Isso deve ser corrigido antes de o roadmap ser usado como fonte de produto (`PlanejamentoCatalogo.java`, entradas de Aves e Água).

## 3. Inventário completo da API `/api/v1`

Convenções da tabela:

- **A/O**: ADMIN ou OPERADOR autenticado conforme `SecurityConfig`;
- **A**: somente ADMIN;
- **Auth**: qualquer usuário autenticado;
- **P**: usa `PaginaResponse`; **L**: lista sem paginação; **1**: recurso/resumo único;
- todas as mutações atuais dependem da sessão e de CSRF; portanto nenhuma é diretamente adequada a app nativo sem a fundação P0.

### 3.1 Painel, integrações e clima

| Método e rota | Contrato principal | Acesso | Forma | Prontidão mobile |
|---|---|---:|---:|---|
| GET `/api/v1/painel/resumo` | `DashboardOperacionalResumo` | Auth | 1 | Boa leitura online; timestamp sem offset |
| GET `/api/v1/clima/resumo` | `ClimaResumo` | Auth | 1 | Usa dado local persistido; boa degradação |
| GET `/api/v1/clima/previsao?horas=` | `List<PrevisaoClimaticaResponse>` | Auth | L | Limite por horas, mas sem paginação/cursor |
| GET `/api/v1/admin/integracoes` | `IntegracaoPainelResumo` | A | 1 | Diagnóstico administrativo; não é dado offline |

Evidência: `DashboardApiController`, `ClimaApiController`, `IntegracaoAdminApiController`.

### 3.2 Estoque e compras

| Método e rota | Request/response | Acesso | Forma | Observação mobile/offline |
|---|---|---:|---:|---|
| GET `/api/v1/estoque/resumo` | `EstoqueDashboardResumo` | Auth | 1 | Snapshot online |
| GET `/api/v1/estoque/itens` | `List<ItemEstoqueResumo>` | Auth | L | Bloqueador de escala: sem paginação/delta |
| GET `/api/v1/estoque/itens/{id}` | `ItemEstoqueDetalhe` | Auth | 1 | Adequado para detalhe |
| POST `/api/v1/estoque/itens` | `ItemEstoqueRequest` → detalhe | A | 1 | Sem idempotência HTTP uniforme |
| GET `/api/v1/estoque/movimentos?page=&size=` | `PaginaResponse<MovimentoEstoqueResponse>` | Auth | P | Nomes de parâmetros divergem do padrão `pagina/tamanho` |
| GET `/api/v1/estoque/movimentos/{id}` | movimento | Auth | 1 | Adequado para detalhe |
| POST `/api/v1/estoque/movimentos` | `MovimentoEstoqueRequest` → movimento | Auth | 1 | Movimento crítico; precisa chave HTTP e conflito formal |
| GET `/api/v1/compras` | `CompraFiltro` → `List<CompraResumo>` | Auth | L | Sem paginação e sem ordenação explícita no contrato |
| GET `/api/v1/compras/{id}` | `CompraDetalhe` | Auth | 1 | Adequado para detalhe |
| POST `/api/v1/compras` | `CompraRequest` → detalhe | Auth | 1 | Criação sem chave idempotente padronizada |
| POST `/api/v1/compras/{id}/itens` | `ItemCompraRequest` → detalhe | Auth | 1 | Retry offline pode duplicar item |
| POST `/api/v1/compras/{id}/confirmar` | detalhe | Auth | 1 | Transacional, mas falta chave/revisão explícita na chamada |
| GET `/api/v1/fornecedores` | `List<FornecedorResumo>` | Auth | L | Catálogo pequeno, mas sem delta/paginação |
| POST `/api/v1/fornecedores` | `FornecedorRequest` → resumo | Auth | 1 | Sem chave idempotente padronizada |

Evidência: `EstoqueApiController`, `ComprasApiController`, `FornecedoresApiController`.

### 3.3 Tarefas e alertas

| Método e rota | Contrato | Acesso | Forma | Observação |
|---|---|---:|---:|---|
| GET `/api/v1/tarefas` | `TarefaFiltro` → `PaginaResponse<TarefaResumo>` | Auth | P | Bom ponto inicial; paginação por número não é sync cursor |
| GET `/api/v1/tarefas/resumo` | `TarefaResumoOperacional` | Auth | 1 | Snapshot |
| GET `/api/v1/tarefas/{id}` | `TarefaDetalhe` | Auth | 1 | Inclui histórico |
| POST `/api/v1/tarefas` | `TarefaRequest` → detalhe | Auth | 1 | Falta idempotência padrão |
| POST `/api/v1/tarefas/{id}/iniciar` | detalhe | Auth | 1 | Falta precondição/revisão no contrato |
| POST `/api/v1/tarefas/{id}/concluir` | detalhe | Auth | 1 | Falta precondição/revisão no contrato |
| GET `/api/v1/alertas` | `AlertaFiltro` → `PaginaResponse<AlertaResumo>` | Auth | P | Boa consulta online |
| GET `/api/v1/alertas/{id}` | `AlertaDetalhe` | Auth | 1 | Inclui histórico |
| POST `/api/v1/alertas/{id}/reconhecer` | detalhe | Auth | 1 | Sem precondição explícita |
| POST `/api/v1/alertas/{id}/criar-tarefa` | `TarefaDetalhe` | Auth | 1 | Serviço deduplica, porém contrato HTTP não declara chave |

Evidência: `TarefasApiController`, `AlertasApiController`, V9.

### 3.4 Aves

| Método e rota | Operação/DTO | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/criacoes/aves/resumo` | `AvesResumo` | Auth | 1 |
| GET `/api/v1/criacoes/aves/instalacoes` | instalações, `pagina/tamanho` | Auth | P |
| GET `/api/v1/criacoes/aves/instalacoes/{id}` | instalação | Auth | 1 |
| POST `/api/v1/criacoes/aves/instalacoes` | `InstalacaoCriacaoRequest` | A | 1 |
| GET `/api/v1/criacoes/aves/lotes` | status/termo/página | Auth | P |
| GET `/api/v1/criacoes/aves/lotes/{id}` | `LoteAvesDetalhe` | Auth | 1 |
| POST `/api/v1/criacoes/aves/lotes` | `CriarLoteAvesRequest` | A | 1 |
| POST `/api/v1/criacoes/aves/lotes/{id}/alimentacoes` | alimentação | Auth | 1 |
| POST `/api/v1/criacoes/aves/lotes/{id}/mortalidades` | mortalidade | Auth | 1 |
| POST `/api/v1/criacoes/aves/lotes/{id}/pesagens` | pesagem | Auth | 1 |
| POST `/api/v1/criacoes/aves/lotes/{id}/posturas` | postura | Auth | 1 |
| POST `/api/v1/criacoes/aves/lotes/{id}/transferencias` | transferência | Auth | 1 |
| GET `/api/v1/criacoes/aves/incubacoes` | incubação paginada | Auth | P |
| GET `/api/v1/criacoes/aves/incubacoes/{id}` | `IncubacaoAvesDetalhe` | Auth | 1 |
| POST `/api/v1/criacoes/aves/incubacoes` | `CriarIncubacaoAvesRequest` | Auth | 1 |
| POST `/api/v1/criacoes/aves/incubacoes/{id}/finalizar` | finalização | Auth | 1 |
| GET `/api/v1/criacoes/aves/incubacoes/{id}/acompanhamentos` | acompanhamentos | Auth | L |
| GET `/api/v1/criacoes/aves/incubacoes/{id}/acompanhamentos/{acompanhamentoId}` | acompanhamento | Auth | 1 |
| POST `/api/v1/criacoes/aves/incubacoes/{id}/acompanhamentos` | acompanhamento | Auth | 1 |
| GET `/api/v1/criacoes/aves/incubacoes/{id}/ovoscopias` | ovoscopias | Auth | L |
| POST `/api/v1/criacoes/aves/incubacoes/{id}/ovoscopias` | ovoscopia em lote | Auth | 1 |
| POST `/api/v1/criacoes/aves/incubacoes/{id}/ajustar-previsao` | ajuste administrativo | A | 1 |
| POST `/api/v1/criacoes/aves/incubacoes/{id}/cancelar` | cancelamento | A | 1 |

As requests de manejo/incubação já carregam chaves de idempotência em vários fluxos e o banco possui unique constraints (V10, V14 e V23). Para mobile, isso deve ser convertido em política pública e uniforme, sem exigir que cada tela conheça detalhes internos. O PDF `/sitio/criacoes/aves/incubacoes/{id}/ovoscopias/ficha.pdf` não tem equivalente `/api/v1`.

### 3.5 Suínos

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/criacoes/suinos/resumo` | dashboard | Auth | 1 |
| GET `/api/v1/criacoes/suinos/lotes` | status/termo/página | Auth | P |
| GET `/api/v1/criacoes/suinos/lotes/{id}` | detalhe | Auth | 1 |
| POST `/api/v1/criacoes/suinos/lotes` | criar lote | A | 1 |
| POST `/api/v1/criacoes/suinos/lotes/{id}/entradas` | entrada | Auth | 1 |
| POST `/api/v1/criacoes/suinos/lotes/{id}/perdas` | perda | Auth | 1 |
| POST `/api/v1/criacoes/suinos/lotes/{id}/transferencias` | transferência | Auth | 1 |
| POST `/api/v1/criacoes/suinos/lotes/{id}/pesagens` | pesagem | Auth | 1 |
| POST `/api/v1/criacoes/suinos/lotes/{id}/alimentacoes` | alimentação/Estoque | Auth | 1 |
| GET `/api/v1/criacoes/suinos/reproducao/resumo` | reprodução | Auth | 1 |
| GET `/api/v1/criacoes/suinos/reproducao/animais` | animais | Auth | L |
| GET `/api/v1/criacoes/suinos/reproducao/animais/{id}` | animal | Auth | 1 |
| GET `/api/v1/criacoes/suinos/reproducao/animais/{id}/historico` | ciclos | Auth | L |
| GET `/api/v1/criacoes/suinos/reproducao/ciclos` | ciclos | Auth | L |
| GET `/api/v1/criacoes/suinos/reproducao/ciclos/{id}` | ciclo | Auth | 1 |
| POST `/api/v1/criacoes/suinos/reproducao/animais` | cadastrar reprodutor | A | 1 |
| POST `/api/v1/criacoes/suinos/reproducao/ciclos` | cobertura | Auth | 1 |
| POST `/api/v1/criacoes/suinos/reproducao/ciclos/{id}/checagem` | gestação | Auth | 1 |
| POST `/api/v1/criacoes/suinos/reproducao/ciclos/{id}/parto` | parto idempotente | Auth | 1 |
| POST `/api/v1/criacoes/suinos/reproducao/ciclos/{id}/desmame` | desmame idempotente | Auth | 1 |
| GET `/api/v1/criacoes/suinos/sanidade` | filtro lote/animal | Auth | L |
| GET `/api/v1/criacoes/suinos/sanidade/{id}` | registro | Auth | 1 |
| POST `/api/v1/criacoes/suinos/sanidade` | registro sanitário | Auth | 1 |
| POST `/api/v1/criacoes/suinos/sanidade/{id}/concluir-proxima-acao` | concluir pendência | Auth | 1 |

Evidência: `SuinosApiController`, `SuinosReproducaoApiController`, `SuinosSanidadeApiController`, V24–V26. Listas de reprodução e sanidade precisam paginação antes de uso prolongado no app.

### 3.6 Peixes

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/criacoes/peixes/resumo` | dashboard | Auth | 1 |
| GET `/api/v1/criacoes/peixes/lotes` | status/termo/página | Auth | P |
| GET `/api/v1/criacoes/peixes/lotes/{id}` | detalhe | Auth | 1 |
| POST `/api/v1/criacoes/peixes/lotes` | criar lote | A | 1 |
| POST `/api/v1/criacoes/peixes/lotes/{id}/entradas` | entrada | Auth | 1 |
| POST `/api/v1/criacoes/peixes/lotes/{id}/perdas` | perda | Auth | 1 |
| POST `/api/v1/criacoes/peixes/lotes/{id}/transferencias` | transferência | Auth | 1 |
| POST `/api/v1/criacoes/peixes/lotes/{id}/biometrias` | biometria | Auth | 1 |
| POST `/api/v1/criacoes/peixes/lotes/{id}/alimentacoes` | alimentação/Estoque | Auth | 1 |
| GET `/api/v1/criacoes/peixes/lotes/{id}/qualidade-agua` | medições | Auth | L |
| POST `/api/v1/criacoes/peixes/lotes/{id}/qualidade-agua` | medição | Auth | 1 |
| GET `/api/v1/criacoes/peixes/qualidade-agua/configuracao` | limites | A | 1 |
| PUT `/api/v1/criacoes/peixes/qualidade-agua/configuracao` | limites | A | 1 |
| GET `/api/v1/criacoes/peixes/sanidade` | filtro lote | Auth | L |
| GET `/api/v1/criacoes/peixes/sanidade/{id}` | registro | Auth | 1 |
| POST `/api/v1/criacoes/peixes/sanidade` | registro sanitário | Auth | 1 |
| POST `/api/v1/criacoes/peixes/sanidade/{id}/concluir-proxima-acao` | concluir pendência | Auth | 1 |

Evidência: `PeixesApiController`, `PeixesSanidadeApiController`, V27–V29. Históricos de água e sanidade devem ser paginados e sincronizáveis por cursor.

### 3.7 Agricultura

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/agricultura/resumo` | dashboard | A/O | 1 |
| GET `/api/v1/agricultura/mapa` | mapa operacional | A/O | 1 |
| GET `/api/v1/agricultura/cultivos/{id}` | ficha de cultivo | A/O | 1 |
| GET `/api/v1/agricultura/safras` | safras | A/O | P |
| POST `/api/v1/agricultura/safras` | criar safra | A | 1 |
| PUT `/api/v1/agricultura/safras/{id}` | atualizar safra | A | 1 |
| GET `/api/v1/agricultura/culturas` | culturas | A/O | P |
| POST `/api/v1/agricultura/culturas` | criar cultura | A | 1 |
| PUT `/api/v1/agricultura/culturas/{id}` | atualizar cultura | A | 1 |
| GET `/api/v1/agricultura/cultivos` | cultivos | A/O | P |
| POST `/api/v1/agricultura/cultivos` | criar cultivo | A | 1 |
| PUT `/api/v1/agricultura/cultivos/{id}` | atualizar cultivo | A | 1 |
| GET `/api/v1/agricultura/adubacoes` | histórico | A/O | P |
| GET `/api/v1/agricultura/irrigacoes` | histórico | A/O | P |
| GET `/api/v1/agricultura/tratamentos` | histórico | A/O | P |
| GET `/api/v1/agricultura/ocorrencias` | ocorrências | A/O | P |
| GET `/api/v1/agricultura/ocorrencias/{id}` | ocorrência/histórico | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/plantios` | plantio | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/acompanhamentos` | acompanhamento | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/colheitas` | colheita/Estoque | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/adubacoes` | adubação | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/irrigacoes` | irrigação | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/tratamentos` | tratamento | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/ocorrencias` | ocorrência | A/O | 1 |
| PUT `/api/v1/agricultura/ocorrencias/{id}` | atualizar ocorrência | A/O | 1 |
| POST `/api/v1/agricultura/ocorrencias/{id}/encerrar` | encerrar | A/O | 1 |
| POST `/api/v1/agricultura/ocorrencias/{id}/tarefa-inspecao` | criar tarefa | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/tarefas` | tarefa | A/O | 1 |
| POST `/api/v1/agricultura/cultivos/{id}/status` | transição de status | A | 1 |

As operações de campo V18/V19 possuem chaves idempotentes e índices únicos. Ainda faltam contratos de delta, tombstones e conflitos para edição simultânea offline.

### 3.8 Propriedade e GIS

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/propriedade/resumo` | propriedade | A/O | 1 |
| PUT `/api/v1/propriedade` | atualizar propriedade | A | 1 |
| GET `/api/v1/propriedade/perimetro` | perímetro + vértices + conferência + mapa | A/O | 1 |
| PUT `/api/v1/propriedade/perimetro` | substituir perímetro | A | 1 |
| GET `/api/v1/propriedade/areas` | áreas | A/O | P |
| GET `/api/v1/propriedade/areas/{id}` | detalhe | A/O | 1 |
| POST/PUT `/api/v1/propriedade/areas[/{id}]` | criar/atualizar | A | 1 |
| POST `/api/v1/propriedade/areas/{id}/desativar` | `VersaoRequest` | A | 1 |
| GET `/api/v1/propriedade/talhoes` | talhões | A/O | P |
| GET `/api/v1/propriedade/talhoes/geojson` | todos os talhões GeoJSON | A/O | 1 |
| GET `/api/v1/propriedade/talhoes/{id}` | detalhe | A/O | 1 |
| POST/PUT `/api/v1/propriedade/talhoes[/{id}]` | criar/atualizar geometria | A | 1 |
| POST `/api/v1/propriedade/talhoes/{id}/desativar` | `VersaoRequest` | A | 1 |
| GET `/api/v1/propriedade/piquetes` | piquetes | A/O | P |
| GET `/api/v1/propriedade/piquetes/{id}` | detalhe | A/O | 1 |
| POST/PUT `/api/v1/propriedade/piquetes[/{id}]` | criar/atualizar | A | 1 |
| POST `/api/v1/propriedade/piquetes/{id}/desativar` | versão | A | 1 |
| GET `/api/v1/propriedade/estruturas` | estruturas | A/O | P |
| GET `/api/v1/propriedade/estruturas/{id}` | detalhe | A/O | 1 |
| POST/PUT `/api/v1/propriedade/estruturas[/{id}]` | criar/atualizar | A | 1 |
| POST `/api/v1/propriedade/estruturas/{id}/desativar` | versão | A | 1 |
| GET `/api/v1/propriedade/recursos-hidricos` | recursos | A/O | P |
| GET `/api/v1/propriedade/recursos-hidricos/{id}` | detalhe | A/O | 1 |
| POST/PUT `/api/v1/propriedade/recursos-hidricos[/{id}]` | criar/atualizar | A | 1 |
| POST `/api/v1/propriedade/recursos-hidricos/{id}/desativar` | versão | A | 1 |

Evidência: `PropriedadeApiController`, V16 e V20–V22. O endpoint GeoJSON completo pode ficar pesado e precisa ETag/compactação/escopo por bounding box ou revisão para o app.

### 3.9 Manutenção/patrimônio

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/manutencao` | dashboard | A/O | 1 |
| GET `/api/v1/manutencao/ativos` | ativos | A/O | L |
| GET `/api/v1/manutencao/ativos/{id}` | ativo | A/O | 1 |
| POST/PUT `/api/v1/manutencao/ativos[/{id}]` | criar/atualizar | A | 1 |
| GET `/api/v1/manutencao/ativos/{id}/manutencoes` | histórico | A/O | L |
| GET `/api/v1/manutencao/ativos/{id}/planos` | planos | A/O | L |
| GET `/api/v1/manutencao/ativos/{id}/leituras` | leituras | A/O | L |
| GET `/api/v1/manutencao/registros/{id}` | manutenção | A/O | 1 |
| POST `/api/v1/manutencao/registros` | registrar manutenção | A/O | 1 |
| POST `/api/v1/manutencao/registros/{id}/concluir-proxima` | concluir próxima | A/O | 1 |
| POST/PUT `/api/v1/manutencao/planos[/{id}]` | criar/atualizar | A | 1 |
| POST `/api/v1/manutencao/planos/{id}/desativar?versao=` | desativar | A | 1 |
| POST `/api/v1/manutencao/leituras` | leitura | A/O | 1 |
| POST `/api/v1/manutencao/leituras/ajuste` | ajuste administrativo | A | 1 |

Listas históricas não são paginadas. O parâmetro `versao` em query na desativação diverge do uso de body em Propriedade.

### 3.10 Água operacional

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/agua` | dashboard | A/O | 1 |
| GET `/api/v1/agua/recursos` | recursos | A/O | L |
| GET `/api/v1/agua/recursos/{id}` | estado operacional | A/O | 1 |
| GET `/api/v1/agua/recursos/{id}/operacoes` | últimas 50 operações | A/O | L |
| GET `/api/v1/agua/operacoes?pagina=&tamanho=` | histórico global | A/O | P |
| POST `/api/v1/agua/operacoes` | `OperacaoAguaRequest` | A/O | 1 |
| PUT `/api/v1/agua/recursos/{id}/configuracao` | limite/inspeção/versão | A | 1 |

Água tem concorrência pessimista, idempotência por unique key, rollback transacional e tarefas/alertas. Porém recursos ainda são cadastros físicos genéricos e não há setor hidráulico, linha, válvula, vazão, turno de irrigação ou vínculo espacial.

### 3.11 Frota e abastecimentos

| Método e rota | Operação | Acesso | Forma |
|---|---|---:|---:|
| GET `/api/v1/frota` | veículos + resumo de abastecimento | A/O | L |
| GET `/api/v1/frota/{id}` | veículo operacional | A/O | 1 |
| POST/PUT `/api/v1/frota[/{id}]` | criar/atualizar veículo | A | 1 |
| GET `/api/v1/frota/fipe/marcas?tipo=` | FIPE | A/O | L |
| GET `/api/v1/frota/fipe/modelos?tipo=&marca=` | FIPE | A/O | L |
| GET `/api/v1/frota/fipe/anos?tipo=&marca=&modelo=` | FIPE | A/O | L |
| GET `/api/v1/frota/fipe/detalhes/{id}` | referência/cache | A/O | 1 |
| GET `/api/v1/abastecimentos?veiculoId=&pagina=&tamanho=` | abastecimentos | A/O | P |
| GET `/api/v1/abastecimentos/{id}` | abastecimento | A/O | 1 |
| POST `/api/v1/abastecimentos` | registro idempotente | A/O | 1 |

Não existe mais endpoint legado `/api/fipe`; a busca mostrou somente `/api/v1/**`. V33 substituiu os campos operacionais `Double/FLOAT` por `BigDecimal/DECIMAL`. Os únicos `float` restantes em Java são coordenadas gráficas internas do PDFBox em `FichaOvoscopiaPdfService`, não valores de negócio.

## 4. Lacunas entre web e REST

Funcionalidades web sem equivalente mobile completo:

| Funcionalidade web | Evidência | Lacuna REST |
|---|---|---|
| Login/logout/perfil/troca de senha | `AutenticacaoController`, `PerfilController`, `SecurityConfig` | Não há `/api/v1/auth/**`, refresh, revogação de token nem endpoint `me` |
| Administração de usuários | `UsuarioController` | Não há `/api/v1/admin/usuarios/**` |
| Configuração operacional | `ConfiguracaoOperacionalController` | Não há API v1 tipada para leitura/alteração |
| Cancelar compra | rota web `/sitio/compras/{id}/cancelar` em segurança/controller | API só cria, adiciona item e confirma |
| Remover item de compra | operação disponível na web/service | sem contrato REST correspondente |
| Cancelar tarefa | `/sitio/tarefas/{id}/cancelar` | API só inicia/conclui |
| Resolver alerta | `/sitio/alertas/{id}/resolver` | API reconhece e cria tarefa, mas não resolve manualmente |
| Editar/encerrar lotes de aves | `LotesAvesController` | API não oferece edição/encerramento do lote |
| PDF de ovoscopia | `IncubacoesAvesController#fichaOvoscopia` | download apenas `/sitio/**` |
| Exportação QGIS CSV/GeoJSON | `PropriedadeController` | GeoJSON de talhões existe, mas downloads CSV/perímetro não têm contrato API |
| Importação QGIS preview/confirmação | `PropriedadeController`, `MultipartFile` | upload somente web; falta API multipart, limite e contrato de erro mobile |
| Sincronização manual das integrações | `IntegracaoAdminController` | API administrativa é somente leitura |
| Catálogos de estoque (categoria, unidade, local) | controllers web/serviços de Estoque | API de itens depende desses IDs, mas não expõe catálogo completo e paginado |

Essas lacunas não exigem duplicar regra de negócio: os endpoints móveis devem chamar os mesmos services existentes.

## 5. Matriz de prontidão mobile

| Dimensão | Estado | Classificação | Evidência/ação |
|---|---|---|---|
| API versionada | `/api/v1` consistente; nenhuma API legada encontrada | Pronto com ressalvas | 20 `*ApiController` |
| DTOs | Controllers REST retornam DTOs, não entidades | Pronto | pacotes `dto`; controllers auditados |
| Autenticação nativa | Sessão/form login + CSRF; sem token | **Bloqueador** | `SecurityConfig` |
| Logout/revogação | Sessão web é invalidada; desativação revoga sessões registradas | Parcial | `UsuarioSessaoService`; não há revogação de refresh token/dispositivo |
| Autorização | ADMIN/OPERADOR explícitos por rota | Boa base | `SecurityConfig` |
| CSRF/CORS | CSRF correto para web; sem estratégia bearer/CORS mobile | **Bloqueador** | nenhuma configuração CORS; CSRF global |
| HTTPS/cookies | perfil prod usa cookie Secure/HttpOnly/SameSite e proxy headers | Boa web | `application-prod.yml`; não substitui auth nativa |
| Erros | envelope comum e `requestId` | Parcial | falta 401 JSON uniforme, detalhes estruturados por campo e conflito/sync padronizado |
| Paginação | `PaginaResponse` existe | Parcial | muitos endpoints retornam `List`; parâmetros `page/size` e `pagina/tamanho` divergem |
| Ordenação | em geral fixa no repository | Fraca | não há contrato comum `sort`; útil para UI, insuficiente para sync |
| Datas/horários | predomina `LocalDateTime` | **Importante** | JSON sem offset/UTC; risco Manaus/UTC e horário de verão de outras zonas |
| IDs | servidor usa `Long/IDENTITY` | Parcial | offline precisa `clientId UUID` ou mapa de IDs temporários |
| Versão/conflito | `@Version` em 37 pontos | Parcial | nem toda mutação recebe versão; 409 ainda não tem envelope de conflito uniforme |
| Idempotência | forte em aves/agricultura/água/criações/manutenção/frota | Parcial | campo varia por DTO; falta header/política única e armazenamento de resposta |
| Auditoria | criado/alterado por/em em base compartilhada | Boa base | timestamps locais e cobertura desigual precisam revisão |
| Soft delete/tombstones | alguns domínios usam `ativo/status` | **Bloqueador offline** | não há feed de exclusões/desativações por cursor |
| Delta sync | inexistente | **Bloqueador offline** | sem `updatedSince`, cursor monotônico ou change log |
| Upload/download | PDF/CSV/GeoJSON e upload QGIS existem na web | Parcial | faltam APIs, metadados, limites, checksums e retomada |
| OpenAPI | springdoc presente | Parcial/fraco | paths incompletos, sem esquema security, descrições antigas |
| GIS | EPSG:4674 e GeoJSON funcionais | Boa base | precisa metadado CRS consistente e estratégia de cache/tamanho |
| Integrações externas | adapters Open-Meteo/Agrofit resilientes e persistidos | Boa base | FIPE deve migrar para a mesma fundação; app nunca deve chamar provedores |
| Testes | 941 testes verdes no último relatório local | Boa base funcional | cobertura mobile/contrato/sync/performance ainda ausente |
| Performance | paginação e `EntityGraph` em áreas críticas | Parcial | listas ilimitadas e N+1 em resumo de frota |

## 6. Segurança e autenticação recomendadas

### Atualização — primeira etapa P0 implementada

A fundação de autenticação mobile foi implementada após esta auditoria: login JSON em `/api/v1/auth/login`, JWT Bearer curto, refresh token opaco rotativo armazenado somente como hash no SQL Server, logout/revogação imediata e respostas 401/403 no contrato `ApiErrorResponse`. A autenticação web por formulário/sessão e seu CSRF foram preservados. O OpenAPI agora cobre todos os paths `/api/v1/**`, declara `bearerAuth` e documenta os endpoints de autenticação e schemas de erro.

Defaults atuais: access token de 15 minutos e refresh token de 30 dias, configuráveis por ambiente. Produção exige segredo HS256 externo de pelo menos 32 bytes em Base64. A migration desta etapa é a V34.

Esta atualização resolve somente o recorte de autenticação, revogação, erro REST inicial e contrato OpenAPI solicitado. Idempotência HTTP comum, IDs offline, change log/cursor, tombstones e sincronização offline continuam pendentes e não foram implementados nesta etapa.

### 6.1 Estado atual

- `SecurityConfig` usa `formLogin`, `JSESSIONID`, logout POST, sessão de 30 minutos e `SessionRegistry`.
- `UsuarioDetailsService` autentica usuários persistidos; `PasswordEncoderFactories` usa encoder delegado.
- `UsuarioSessaoService` expira somente sessões do usuário desativado após commit.
- Bootstrap inicial não redefine usuário existente e não registra senha/hash (`InitialAdminBootstrap`).
- APIs protegidas recebem o mesmo cookie e CSRF do navegador. Isso força o app a simular browser e não fornece ciclo de credencial adequado a dispositivos.
- Acesso negado autenticado em `/api/**` retorna JSON 403, mas a ausência de autenticação segue o entry point do form login e pode redirecionar para HTML, em vez de 401 JSON.
- Não foi encontrada configuração CORS, JWT, OAuth2, access token ou refresh token.

### 6.2 Arquitetura alvo

Manter a cadeia web existente e adicionar uma cadeia separada para `/api/v1/**`:

1. `POST /api/v1/auth/login` recebe JSON e devolve access token curto + refresh token rotativo;
2. access token assinado com vida curta (por exemplo, 10–15 min), contendo `sub`, roles, `sessionId/tokenVersion` e `jti`;
3. refresh token opaco, aleatório, armazenado somente como hash no SQL Server, associado a usuário/dispositivo, com expiração, rotação e detecção de reutilização;
4. `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` e opção “revogar todos os dispositivos”;
5. ao desativar usuário ou trocar senha administrativamente, revogar refresh tokens e incrementar versão de segurança; access tokens restantes expiram rapidamente ou são rejeitados por versão cacheável no banco;
6. respostas 401/403 sempre no `ApiErrorResponse`, sem redirect HTML;
7. CSRF permanece para web/sessão; endpoints bearer stateless não dependem de CSRF;
8. tokens em iOS Keychain/Android Keystore; nunca em `UserDefaults`, SharedPreferences simples ou logs;
9. rate limit de login/refresh e auditoria de dispositivo, sem registrar credenciais;
10. CORS só precisa ser habilitado para clientes web de origem distinta; apps nativos não dependem de CORS.

Não é necessário introduzir um servidor OAuth externo na primeira versão. Spring Security Resource Server + serviço próprio de refresh token tipado no SQL Server atende o cenário, desde que haja revisão criptográfica e testes de revogação. Redis não é requisito.

## 7. Estratégia offline-first e sincronização

### 7.1 Modelo recomendado no app

- banco local: SwiftData/Core Data ou SQLite/GRDB no iOS; Room/SQLite no Android;
- tabelas espelho usam `serverId: Long?`, `clientId: UUID`, `version`, `updatedAt`, `syncState` e `deletedAt`;
- outbox local persistente por operação, com ordem, dependências e tentativa exponencial;
- UI lê sempre do banco local; rede só atualiza a base local;
- cada comando recebe UUID estável criado no dispositivo e é reenviado até confirmação.

### 7.2 Contrato servidor necessário

Criar uma fundação comum, sem substituir de imediato todos os endpoints:

- `Idempotency-Key: <UUID>` obrigatório em POSTs operacionais; servidor persiste chave, hash do request, status e resposta;
- `If-Match`/ETag ou `version` obrigatório em PUT/transições; divergência retorna 409/412 estruturado;
- `GET /api/v1/sync/bootstrap` por escopos e páginas;
- `GET /api/v1/sync/changes?cursor=...&limit=...` com cursor opaco monotônico;
- resposta inclui `nextCursor`, `hasMore`, upserts e tombstones;
- eventos de mudança precisam `changeId`, tipo, entidade, `serverId`, `clientId`, versão e timestamp UTC;
- `POST /api/v1/sync/commands` opcional para lote pequeno de comandos independentes, mantendo transação por comando e resultado individual;
- retenção do change log maior que a janela máxima offline; cursor expirado força novo bootstrap;
- exclusões/desativações geram tombstone. Não apagar fisicamente dados sincronizáveis sem política de retenção.

### 7.3 Conflitos

| Tipo | Política recomendada |
|---|---|
| Eventos imutáveis (medições, alimentação, abastecimento, movimentos) | idempotência por UUID; não fazer last-write-wins |
| Cadastros editáveis | optimistic locking; 409 com versão e snapshot atuais |
| Transições de estado | comando idempotente + validação de estado esperado |
| Quantidades/saldos | servidor autoritativo, nunca mesclar no cliente |
| Geometrias | conflito manual; nunca mesclar vértices automaticamente |
| Exclusão/desativação | tombstone vence edição offline posterior, salvo restauração administrativa explícita |

### 7.4 O que já pode ser reaproveitado

- unique constraints de `chave_idempotencia` em V10, V14, V18, V19 e V23–V33;
- `@Version` e `VersaoRequest` em vários cadastros;
- operações de Estoque como fonte autoritativa de saldo;
- locks pessimistas em transferências de água e lotes;
- auditoria `criado_em/criado_por/alterado_em/alterado_por`.

O que falta é transformar esses mecanismos internos em contrato HTTP coerente e cobrir os domínios que ainda não os usam.

## 8. GIS, mapas e dados espaciais

### 8.1 Estado atual

- V20 cria perímetro e vértices ordenados com latitude/longitude `DECIMAL` e validações.
- V21 confirma SIRGAS 2000, EPSG:4674, altitude geodésica, área/perímetro documental e `geography` SQL Server.
- V22 adiciona vértices e `geography` de talhões.
- `PerimetroSpatialRepository` e `TalhaoSpatialRepository` criam/validam `geography::STGeomFromText(..., 4674)`, preservam área documental e calculada separadamente e rejeitam talhão fora do perímetro.
- `GET /api/v1/propriedade/perimetro`, `/talhoes/geojson` e `/api/v1/agricultura/mapa` expõem dados para visualização.
- O frontend transforma EPSG:4674 apenas para renderização em EPSG:3857 (`static/js/propriedade.js`).

### 8.2 Avaliação

| Necessidade móvel | Situação |
|---|---|
| polígonos da propriedade/talhões | Pronto como base |
| pontos de poços/bombas/estruturas | Cadastro ainda não possui geometria espacial uniforme |
| linhas de rios/cercas/estradas | Não modelado |
| árvores/pontos de coleta de solo | Não modelado |
| galinheiros e instalações | vínculo cadastral existe; geometria específica não |
| edição offline de geometrias | Não pronta; requer `clientId`, versão e conflito manual |
| mapas offline | Responsabilidade do app; backend deve fornecer pacotes GeoJSON/delta, não tiles externos |

### 8.3 Recomendações

- manter EPSG:4674 como fonte oficial; não converter dados persistidos para EPSG:4326;
- padronizar GeoJSON com metadado explícito `crs/epsg/datum`, precisão e `version`;
- criar no futuro tipos espaciais explícitos por domínio: ponto, linha e polígono, sem tabela genérica prematura;
- endpoint mobile por escopo/revisão e ETag; gzip/Brotli no Nginx para GeoJSON;
- downloads de mapas offline devem usar armazenamento do app e licenças adequadas do provedor de tiles;
- fotos e anexos georreferenciados devem guardar metadados no SQL Server e binário em storage apropriado, não em JSON/base64 nem necessariamente no banco relacional.

## 9. Integrações externas

### 9.1 Fundação existente

`integracao/core` já oferece estado e histórico persistidos, prevenção de execução concorrente, scheduler, timeout, retry, circuit breaker, rate limiter, tratamento de `Retry-After`, cache opcional e observabilidade. Open-Meteo e Agrofit usam adapters isolados, persistem resultado e permitem que domínio/UI leiam somente dados locais.

Evidências: `IntegrationResilienceExecutor`, `IntegracaoOrquestrador`, `IntegracaoExecucaoService`, `OpenMeteoSincronizador`, `AgrofitSincronizador`, V7–V8.

FIPE é a exceção: `FipeService` usa `RestTemplate` diretamente e cache próprio. Deve migrar para a mesma fundação antes de ser considerada resiliente/observável no padrão atual.

### 9.2 Provedores e priorização

| Fonte | Estado | Uso recomendado | Prioridade |
|---|---|---|---|
| Open-Meteo | Implementado/persistido | clima e apoio operacional | manter |
| Embrapa Agrofit | piloto implementado/persistido | referência, nunca prescrição | manter/validar termos |
| FIPE | implementado com adapter legado | apoio de frota | refatorar para integration core |
| INMET | ausente | segunda fonte/observação oficial | P2 |
| NASA POWER | ausente | histórico agroclimático/solar | P2 |
| NASA FIRMS | ausente | focos de calor | P2/P3 |
| MapBiomas | ausente | contexto territorial/uso do solo | P3, conforme licença/API |
| INPE Queimadas/DETER/PRODES | ausente | alertas ambientais | P2/P3 conforme região e termos |
| OpenStreetMap | usado apenas como mapa-base frontend quando disponível | visualização, nunca regra de negócio | manter com cache/licença corretos |
| SoilGrids/ISRIC | ausente | referência de solo, não laudo | P3 |
| AgroAPI/Embrapa adicional | ausente | somente casos de uso aprovados | P3 |
| APIs ambientais/hídricas | ausentes | outorga, chuva, nível quando houver fonte oficial | após modelar Água |

### 9.3 Estrutura recomendada por integração

```text
integracao/
  core/                  # políticas comuns, execução, estado, observabilidade
  <provedor>/
    client/              # HTTP e autenticação externa
    dto/                 # payload externo
    mapper/              # tradução para modelo interno
    service/             # sincronizador/orquestração
    entity|repository/   # snapshot local, se necessário
```

Regra arquitetural: domínio e app móvel consomem somente APIs internas e dados persistidos. Indisponibilidade externa não pode derrubar telas nem comandos locais.

## 10. OpenAPI e estabilidade de contrato

Springdoc 2.6.0 e Swagger UI já estão instalados (`pom.xml`). `OpenApiConfig` define apenas título/versão/descrição e a descrição ainda cita poucos módulos. `application.yml` limita `paths-to-match` e omite `/api/v1/manutencao/**`, `/api/v1/agua/**`, `/api/v1/frota/**` e `/api/v1/abastecimentos/**`.

Problemas:

- não há `SecurityScheme` nem fluxos de autenticação;
- vários controllers recentes não possuem `@Tag/@Operation`;
- respostas 401/403/409/422/429 não estão documentadas globalmente;
- paginação e filtros não têm schema/convenção única;
- não há teste que compare o JSON OpenAPI ou detecte breaking change;
- não há geração/validação de clientes Swift/Kotlin.

Antes do mobile:

1. incluir todo `/api/v1/**` no documento, excluindo explicitamente apenas o que não é público ao app;
2. documentar bearer auth e refresh;
3. definir schemas comuns de página, erro, conflito, idempotência, upload e sync;
4. congelar `openapi.yaml/json` como artefato versionado ou de CI;
5. usar `openapi-diff`/equivalente no CI para bloquear quebra incompatível dentro de v1;
6. gerar clientes Swift/Kotlin ou ao menos modelos validados a partir do mesmo contrato.

## 11. Água Operacional — estado e lacunas

### Implementado

- tipos de operação: `MEDICAO_NIVEL`, `ABASTECIMENTO`, `CONSUMO`, `RETIRADA`, `TRANSFERENCIA`, `INSPECAO` (`TipoOperacaoAgua`);
- volumes `BigDecimal`, histórico antes/depois, observação, auditoria e `@Version`;
- idempotência com constraint única e lock de ordenação em transferências;
- capacidade e saldo não negativo;
- nível mínimo, próxima inspeção, tarefa automática e alerta de nível baixo/vencimento;
- integração com ativos/manutenções hídricas;
- MVC e API `/api/v1/agua/**`;
- V32 com FKs, CHECKs e índices de data/recurso.

### Parcial ou ausente

- bombas não são um tipo de `RecursoHidrico` (`TipoRecursoHidrico` possui poço, açude, nascente, caixa, cisterna, reservatório, tanque e outro); bombas entram como ativo patrimonial, sem topologia hidráulica explícita;
- não existem setores, linhas, registros/válvulas, fontes/destinos encadeados, vazão nominal, pressão ou turnos;
- irrigação agrícola registra execução no cultivo, mas não reserva/baixa volume do módulo Água;
- não há ponto/linha espacial de recurso hídrico;
- não há telemetria, e isso está corretamente fora do escopo atual;
- catálogo de planejamento ainda diz `PLANEJADO`, embora a operação base esteja funcional.

Conclusão: Água Etapa 1 está funcional para inventário volumétrico manual e inspeções, mas não é ainda uma rede operacional de irrigação. Para mobile de campo, o histórico atual é utilizável após a fundação de sync; topologia hidráulica pode ficar em P2.

## 12. Testes e qualidade

### 12.1 Evidência atual

- 102 arquivos de teste em `src/test/java`.
- Últimos relatórios locais existentes em `target/surefire-reports` (2026-10-08): **941 testes, 0 failures, 0 errors, 0 skipped**, distribuídos em 100 suites XML.
- Há Testcontainers/SQL Server para usuários, Estoque, Compras, Integrações, Tarefas, Aves, Suínos, Peixes, Agricultura, Propriedade, Manutenção, Água, Frota e outros.
- Há `SitioProSecurityTests`, `SitioProRoutesTests`, testes MVC e testes dedicados dos APIs mais recentes de Água, Frota, Abastecimento e Manutenção.
- `open-in-view=false` em dev e produção reduz dependência acidental de sessão JPA na serialização.

### 12.2 Lacunas antes do mobile

| Lacuna | Prioridade |
|---|---|
| testes de login/refresh/rotação/reuso/revogação/dispositivo | P0 |
| contrato 401/403 JSON e CSRF separado entre web/API | P0 |
| testes de idempotência HTTP em todos os comandos móveis | P0 |
| testes de cursor, tombstone, bootstrap e cursor expirado | P0 |
| concorrência offline: versão antiga, comando repetido e dependências | P0 |
| snapshot/diff do OpenAPI | P0/P1 |
| cobertura endpoint por endpoint para os 185 mappings | P1 |
| uploads maliciosos, tamanho, MIME e GeoJSON adversarial | P1 |
| desempenho de listas, dashboard, GeoJSON e sync em volume realista | P1 |
| testes de horário/offset/timezone | P1 |
| chaos/degradação de integrações e cache | já há base; ampliar em P2 |

O `clean verify` não foi repetido porque esta tarefa alterou apenas documentação. O número acima é evidência dos relatórios já existentes, não uma execução realizada nesta auditoria.

## 13. Performance e escalabilidade

Pontos positivos:

- `PaginaResponse` limita vários históricos a 100 itens;
- uso frequente de `EntityGraph` evita lazy loading/N+1 em Estoque, Compras, Tarefas, Aves, Agricultura e sanidade;
- migrations contêm índices para movimentos, históricos, água e abastecimento;
- `open-in-view=false` torna problemas de consulta mais visíveis;
- integração externa é cacheada/persistida.

Riscos concretos:

1. `AbastecimentoService#resumoFrota` lista veículos e busca todo o histórico para cada veículo (`findByVeiculoIdOrderByDataAscIdAsc`): N+1 e carga crescente. Substituir por agregações SQL e uma consulta limitada para consumo.
2. `/api/v1/frota` retorna lista completa e cálculos históricos; precisa paginação/projeção.
3. Estoque itens, fornecedores, compras, manutenção ativos/históricos, reprodução/sanidade e qualidade da água retornam listas completas.
4. `/api/v1/propriedade/talhoes/geojson` entrega o conjunto integral; adotar ETag, compressão e, se necessário, bounding box/revisão.
5. dashboards agregam vários services; medir query count e p95 antes do app iniciar refresh periódico.
6. paginação por offset é adequada à UI, mas não garante sincronização estável; para sync usar cursor/change ID.
7. nenhuma suíte de carga foi encontrada. Criar cenários com volume representativo e metas de p95/p99.

## 14. Roadmap recomendado

Estimativas relativas: **P** pequeno (até poucos dias), **M** médio (1–3 semanas), **G** grande (várias semanas, depende de validação de produto/segurança).

### P0 — bloqueadores para iniciar o app nativo

| Item | Esforço | Critério de aceite |
|---|---:|---|
| cadeia de segurança bearer separada + login JSON | G | iOS autentica sem cookie/form/CSRF; 401/403 JSON |
| refresh token rotativo, logout e revogação | G | revoga dispositivo/todos; desativação e troca de senha invalidam refresh |
| contrato OpenAPI completo e estável | M | todos os módulos v1 presentes; security/errors/pages documentados; diff no CI |
| convenção única de erro/conflito | M | validação por campos, 401, 403, 404, 409/412, 422, 429 e 500 previsíveis |
| idempotência HTTP comum | M/G | `Idempotency-Key` em todo comando móvel; replay devolve mesmo resultado |
| IDs offline e versionamento | M | `clientId UUID` + `serverId` + `version` nos agregados sincronizáveis |
| change log/cursor/tombstones/bootstrap | G | sincronização incremental testada e cursor expirado tratado |
| padronização de tempo | M | API usa `Instant`/`OffsetDateTime` UTC; datas civis permanecem `LocalDate` |
| testes da fundação mobile | G | auth, sync, conflito, retry e revogação no `clean verify` |

### P1 — necessário antes do beta de campo

| Item | Esforço | Resultado |
|---|---:|---|
| paginação/filtros/sort uniformes | M | `pagina/tamanho` ou cursor; máximo comum; metadados consistentes |
| cobrir lacunas REST essenciais | M | catálogos, perfil, cancelar/resolver, PDF, QGIS e configurações permitidas |
| API de upload/anexos/fotos | G | multipart seguro, MIME/tamanho/checksum, autorização e storage |
| otimizar Frota e dashboards | M | sem N+1, métricas de query e metas de latência |
| ETag/compactação GIS | P/M | mapa móvel eficiente e cacheável |
| testes de contrato Swift/Kotlin | M | clientes gerados compilam no CI |
| corrigir catálogo/roadmap funcional | P | produto não exibe módulos implementados como planejados |

### P2 — fechar operação avançada do ERP/mobile

| Item | Esforço | Resultado |
|---|---:|---|
| topologia de Água/irrigação | G | bombas, setores, linhas, turnos e vínculo com consumo agrícola |
| pontos/linhas GIS adicionais | G | poços, bombas, cercas, vias, rios e pontos de coleta |
| migrar FIPE para Integration Foundation | M | retry/rate limit/estado/degradação uniformes |
| INMET/NASA POWER/alertas ambientais | M/G | dados persistidos e domínio independente de disponibilidade |
| custos/relatórios essenciais | G | agregações consistentes para campo e gestão |
| observabilidade mobile | M | versão do app/dispositivo/correlation ID sem dados sensíveis |

### P3 — pode ficar para depois do primeiro mobile

- módulos Casa, Despensa, Segurança, Reformas, Deteriorações e demais placeholders;
- MapBiomas, FIRMS, SoilGrids e outras integrações sem caso operacional validado;
- edição GIS avançada e importações complexas no app;
- telemetria/IoT;
- análises financeiras e relatórios avançados;
- tiles offline próprios, caso o beta funcione com pacote de mapa licenciado de terceiro.

## 15. Ordem recomendada de execução

1. congelar este inventário e definir ADR de autenticação/token e ADR de offline sync;
2. corrigir OpenAPI e padronizar erros, tempo, paginação e versionamento;
3. implementar auth mobile, refresh/revogação e testes de segurança;
4. implementar idempotência HTTP, `clientId`, cursor/change log e tombstones;
5. expor os endpoints web essenciais faltantes sem duplicar services;
6. construir um cliente iOS mínimo gerado pelo OpenAPI: login, bootstrap, tarefas e uma operação offline idempotente;
7. validar conflito, perda de rede, retry, reinstalação e revogação em campo;
8. expandir iOS por módulos; iniciar Android reutilizando o mesmo contrato e casos de teste;
9. otimizar consultas/GeoJSON com métricas reais;
10. só então evoluir Água topológica, novos objetos GIS e integrações P2/P3.

## 16. Veredito

O backend está **maduro como ERP web e parcialmente preparado como API online**, mas **não está pronto para mobile offline-first**. A maior parte das regras de negócio pode ser reaproveitada sem reescrita. O trabalho crítico é transversal: segurança nativa, contratos estáveis, sincronização, idempotência HTTP, conflitos, tempo e testes de contrato.

Com os itens P0 concluídos, é seguro começar o app iOS em Swift sem criar uma segunda lógica de negócio. Android em Kotlin deve vir sobre a mesma OpenAPI e o mesmo protocolo de sincronização, evitando divergência entre plataformas.
