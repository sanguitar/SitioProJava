# Reset e seed do banco DEV

Estes scripts apagam dados operacionais somente do SQL Server local usado pelo
Docker Compose e recriam um catálogo de desenvolvimento. Migrations, schema,
catálogos externos necessários e o administrador `sanderson` são preservados.

Pré-requisitos:

- stack local em execução;
- container `app` com `SPRING_PROFILES_ACTIVE=dev`;
- `DB_HOST=sqlserver` e `DB_NAME=sitio_db`;
- administrador `sanderson` ativo com perfil `ADMIN`;
- schema Flyway na V13.

Execute na raiz do projeto:

```powershell
.\scripts\dev\reset-dev-data.ps1 -Confirmation "THIS WILL DELETE LOCAL DEV DATA"
```

O wrapper valida o runtime, copia os SQLs para o container do SQL Server, para
temporariamente a aplicação, executa reset e seed e inicia a aplicação novamente.
Ele usa `MSSQL_SA_PASSWORD` já disponível dentro do container e não lê nem imprime
senhas. Não use estes scripts em produção.

O seed é idempotente por chave natural. Ele pode ser reaplicado isoladamente com
`sqlcmd`, desde que `ExpectedDatabase=sitio_db` seja informado. O catálogo nasce
sem saldos e sem compras, permitindo testes manuais sem histórico artificial.
