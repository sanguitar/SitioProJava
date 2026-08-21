# SitioPro - instruções permanentes para Codex

Trabalhe sempre sobre o estado atual do repositório e leia o contexto antes de alterar código. Preserve funcionalidades, views, placeholders, testes, Docker e SQL Server existentes.

## Regras essenciais

- Priorize documentação oficial: Spring, Java/JDK, Microsoft SQL Server, Docker, Flyway e especificações oficiais.
- Mantenha Java 21, Spring Boot, Thymeleaf, SQL Server e Docker Compose. Não introduza frontend SPA, filas, Redis, Elasticsearch, Kubernetes ou microserviços sem requisito explícito.
- Organize por domínio/feature e crie apenas as camadas necessárias. Não crie pacote vazio nem abstração sem problema concreto.
- Use constructor injection. Controllers não acessam repositories diretamente. Services concentram regras e transações.
- Não use `double`/`float` para dinheiro em código novo; prefira `BigDecimal`.
- Não exponha entidades JPA diretamente em futura API REST. APIs futuras devem ficar em `/api/v1/**`; páginas Thymeleaf permanecem separadas.
- Use Spring Security MVC com sessão/form login e CSRF habilitado. Não faça mutação via GET. Considere validação, IDOR, mass assignment, escaping, logout por POST e secrets fora do Git.
- Thymeleaf não deve conter grandes blocos de CSS/JS. Use fragments, CSS global, CSS por domínio e JS só quando necessário.
- Observabilidade deve usar logs estruturados úteis, preservar `request.id`/`trace.id`, nunca registrar secrets e não substituir auditoria persistente do SQL Server.
- Flyway é responsável pelo schema; Hibernate deve validar (`ddl-auto=validate`). Não editar migrations já aplicadas. `sa` fica restrito a bootstrap/administração do SQL Server; a aplicação usa usuário técnico com menor privilégio.
- Antes de concluir tarefa relevante, execute `.\mvnw.cmd clean verify` e mantenha testes existentes passando.

Detalhes: consulte `docs/engineering-standards.md`.
