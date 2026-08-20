# SitioPro - padrões de engenharia

Este documento registra os padrões permanentes do projeto para manter o SitioPro simples, testável e evolutivo.

## Referências técnicas

Priorize documentação oficial da tecnologia, Spring, Java/JDK, Microsoft SQL Server, Docker, Flyway e especificações oficiais. Só use fontes secundárias quando a documentação oficial não resolver a dúvida.

Antes de adicionar uma biblioteca, confirme necessidade real, compatibilidade com Java 21 e Spring Boot atual, manutenção ativa e se o Spring/JDK já oferecem solução equivalente. Não atualize Spring Boot sem pedido explícito.

## Java e domínio

Use Java 21. Use `record` para DTOs imutáveis quando adequado e `enum` para estados/tipos bem definidos. Use `BigDecimal` para dinheiro, preços, custos, taxas e quantidades com precisão decimal importante. Use `LocalDate`, `LocalDateTime` ou `Instant` conforme o significado do dado.

## SOLID e simplicidade

SOLID deve reduzir acoplamento e melhorar testabilidade. Não crie automaticamente interface + implementação, factories, managers, facades, adapters, strategies, builders ou providers quando existe uma única implementação e nenhum problema concreto.

Prefira nomes claros, responsabilidades pequenas, baixo acoplamento, alta coesão e tratamento explícito de erros. Não fragmente código simples em dezenas de métodos privados sem ganho real. Comentários devem explicar o motivo, não repetir o código.

## Spring

Use constructor injection. Controllers recebem HTTP, validam entrada, convertem dados, chamam services e retornam view/response. Controllers não acessam repositories. Services orquestram regras e definem `@Transactional` quando a operação precisa ser atômica. Repositories cuidam exclusivamente de persistência.

Não coloque regra de negócio em controller, repository, template Thymeleaf ou JavaScript.

## Organização

Mantenha organização por domínio/feature, por exemplo `estoque`, `compras`, `tarefas`, `aves`, `suinos`, `piscicultura`, `agricultura`, `agua`, `casa`, `manutencao`, `patrimonio`, `dashboard`, `usuario`. Dentro de cada domínio, crie apenas as camadas necessárias.

## DTOs, REST e clientes futuros

A futura API ficará em `/api/v1/**`; páginas Thymeleaf devem permanecer separadas sob `/sitio/**` ou rotas web equivalentes. Não exponha entidades JPA diretamente na API. Use DTOs específicos de request/response, preferencialmente `record` quando imutabilidade fizer sentido. Evite mass assignment.

Use semântica HTTP correta: GET consulta, POST cria/executa ação, PUT/PATCH altera e DELETE remove. Nunca use GET para alterar estado.

## Thymeleaf e frontend

Continue com Thymeleaf para a interface web. Não introduza React, Angular, Vue ou SPA sem necessidade futura explícita. HTML não deve conter grandes blocos de CSS ou JavaScript. Use fragments reutilizáveis, CSS global, CSS por domínio e JavaScript por domínio somente quando necessário.

## Segurança

Todo código novo deve considerar validação de entrada, menor privilégio, proteção contra IDOR, mass assignment, CSRF em interface de sessão, output escaping, secrets fora do Git, logs sem credenciais e erros sem stack trace para usuário final. A interface MVC usa Spring Security com sessão e form login; não adicione JWT, OAuth Server ou Keycloak sem necessidade concreta.

Rotas administrativas devem ser protegidas no backend por perfil `ADMIN`, e não apenas ocultas no menu. Operações mutáveis devem usar métodos HTTP mutáveis com token CSRF. Formulários sensíveis devem usar DTOs específicos, nunca binding direto para entidade de usuário.

## Banco de dados e Flyway

Flyway é o mecanismo permanente de evolução do schema. Hibernate deve validar o mapeamento com `ddl-auto=validate`. Não use `ddl-auto=update` como prática permanente.

Migrations ficam em `src/main/resources/db/migration` e seguem `V<versao>__<descricao>.sql`. Não edite migration já aplicada; crie uma nova. Não use `DROP` destrutivo sem autorização explícita e plano de preservação de dados.

## Testes

Projete regra de negócio para teste. Priorize testes unitários de domínio/service, controller/API, segurança, repository quando houver consulta relevante e integração para fluxos críticos. Todo bug corrigido deve ganhar teste de regressão quando possível. Antes de concluir tarefa relevante, execute `.\mvnw.cmd clean verify`.
