ALTER TABLE dbo.tarefas
    ADD public_id UNIQUEIDENTIFIER NULL;

EXEC(N'UPDATE dbo.tarefas SET public_id = NEWID() WHERE public_id IS NULL');
EXEC(N'ALTER TABLE dbo.tarefas ALTER COLUMN public_id UNIQUEIDENTIFIER NOT NULL');
EXEC(N'ALTER TABLE dbo.tarefas ADD CONSTRAINT df_tarefas_public_id DEFAULT NEWSEQUENTIALID() FOR public_id');
EXEC(N'ALTER TABLE dbo.tarefas ADD CONSTRAINT uq_tarefas_public_id UNIQUE (public_id)');

ALTER TABLE dbo.agua_operacoes
    ADD public_id UNIQUEIDENTIFIER NULL;

EXEC(N'UPDATE dbo.agua_operacoes SET public_id = NEWID() WHERE public_id IS NULL');
EXEC(N'ALTER TABLE dbo.agua_operacoes ALTER COLUMN public_id UNIQUEIDENTIFIER NOT NULL');
EXEC(N'ALTER TABLE dbo.agua_operacoes ADD CONSTRAINT df_agua_operacoes_public_id DEFAULT NEWSEQUENTIALID() FOR public_id');
EXEC(N'ALTER TABLE dbo.agua_operacoes ADD CONSTRAINT uq_agua_operacoes_public_id UNIQUE (public_id)');
