CREATE TABLE dbo.sync_change_log (
    change_id BIGINT IDENTITY(1,1) NOT NULL,
    entity_type VARCHAR(30) NOT NULL,
    entity_public_id UNIQUEIDENTIFIER NOT NULL,
    operation VARCHAR(10) NOT NULL,
    entity_version BIGINT NOT NULL,
    occurred_at DATETIMEOFFSET(6) NOT NULL CONSTRAINT df_sync_change_log_occurred_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_sync_change_log PRIMARY KEY (change_id),
    CONSTRAINT ck_sync_change_log_entity_type CHECK (entity_type IN ('TAREFA', 'AGUA_MEDICAO')),
    CONSTRAINT ck_sync_change_log_operation CHECK (operation IN ('UPSERT', 'DELETE')),
    CONSTRAINT ck_sync_change_log_version CHECK (entity_version >= 0)
);

CREATE INDEX ix_sync_change_log_entity
    ON dbo.sync_change_log(entity_type, entity_public_id, change_id DESC);

EXEC(N'
CREATE TRIGGER dbo.tr_tarefas_sync_change
ON dbo.tarefas
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.sync_change_log
        (entity_type, entity_public_id, operation, entity_version, occurred_at)
    SELECT ''TAREFA'', i.public_id, ''UPSERT'', i.versao, SYSUTCDATETIME()
    FROM inserted i;
END
');

EXEC(N'
CREATE TRIGGER dbo.tr_agua_medicao_sync_change
ON dbo.agua_operacoes
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.sync_change_log
        (entity_type, entity_public_id, operation, entity_version, occurred_at)
    SELECT ''AGUA_MEDICAO'', i.public_id, ''UPSERT'', i.versao, SYSUTCDATETIME()
    FROM inserted i
    WHERE i.tipo = ''MEDICAO_NIVEL'';
END
');
