IF OBJECT_ID(N'dbo.tarefas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tarefas (
        id BIGINT IDENTITY(1,1) NOT NULL,
        titulo VARCHAR(160) NOT NULL,
        descricao VARCHAR(2000) NULL,
        status VARCHAR(20) NOT NULL,
        prioridade VARCHAR(20) NOT NULL,
        data_inicio DATETIME2 NULL,
        data_vencimento DATETIME2 NULL,
        data_conclusao DATETIME2 NULL,
        responsavel_id BIGINT NULL,
        criado_por_usuario_id BIGINT NOT NULL,
        origem VARCHAR(20) NOT NULL,
        modulo_origem VARCHAR(30) NULL,
        referencia_origem VARCHAR(160) NULL,
        recorrencia_id BIGINT NULL,
        ocorrencia_programada_em DATETIME2 NULL,
        ativo BIT NOT NULL CONSTRAINT df_tarefas_ativo DEFAULT 1,
        versao BIGINT NOT NULL CONSTRAINT df_tarefas_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_tarefas PRIMARY KEY (id),
        CONSTRAINT fk_tarefas_responsavel FOREIGN KEY (responsavel_id) REFERENCES dbo.usuarios (id),
        CONSTRAINT fk_tarefas_criador FOREIGN KEY (criado_por_usuario_id) REFERENCES dbo.usuarios (id),
        CONSTRAINT ck_tarefas_status CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA')),
        CONSTRAINT ck_tarefas_prioridade CHECK (prioridade IN ('BAIXA', 'NORMAL', 'ALTA', 'CRITICA')),
        CONSTRAINT ck_tarefas_origem CHECK (origem IN ('MANUAL', 'AUTOMATICA')),
        CONSTRAINT ck_tarefas_modulo CHECK (modulo_origem IS NULL OR modulo_origem IN ('TAREFAS', 'ESTOQUE', 'INTEGRACOES', 'CLIMA', 'COMPRAS')),
        CONSTRAINT ck_tarefas_conclusao CHECK (
            (status = 'CONCLUIDA' AND data_conclusao IS NOT NULL)
            OR (status <> 'CONCLUIDA' AND data_conclusao IS NULL)
        ),
        CONSTRAINT ck_tarefas_origem_contexto CHECK (
            (origem = 'MANUAL' AND modulo_origem IS NULL AND referencia_origem IS NULL)
            OR (origem = 'AUTOMATICA' AND modulo_origem IS NOT NULL AND referencia_origem IS NOT NULL)
        ),
        CONSTRAINT ck_tarefas_ocorrencia CHECK (
            (recorrencia_id IS NULL AND ocorrencia_programada_em IS NULL)
            OR (recorrencia_id IS NOT NULL AND ocorrencia_programada_em IS NOT NULL)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.tarefa_recorrencias', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tarefa_recorrencias (
        id BIGINT IDENTITY(1,1) NOT NULL,
        tarefa_modelo_id BIGINT NOT NULL,
        tipo VARCHAR(30) NOT NULL,
        intervalo_dias INT NULL,
        proxima_ocorrencia_em DATETIME2 NOT NULL,
        ativa BIT NOT NULL CONSTRAINT df_tarefa_recorrencias_ativa DEFAULT 1,
        versao BIGINT NOT NULL CONSTRAINT df_tarefa_recorrencias_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_tarefa_recorrencias PRIMARY KEY (id),
        CONSTRAINT uk_tarefa_recorrencias_modelo UNIQUE (tarefa_modelo_id),
        CONSTRAINT fk_tarefa_recorrencias_modelo FOREIGN KEY (tarefa_modelo_id) REFERENCES dbo.tarefas (id),
        CONSTRAINT ck_tarefa_recorrencias_tipo CHECK (tipo IN ('DIARIA', 'SEMANAL', 'MENSAL', 'INTERVALO_DIAS')),
        CONSTRAINT ck_tarefa_recorrencias_intervalo CHECK (
            (tipo = 'INTERVALO_DIAS' AND intervalo_dias IS NOT NULL AND intervalo_dias > 0)
            OR (tipo <> 'INTERVALO_DIAS' AND intervalo_dias IS NULL)
        )
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'fk_tarefas_recorrencia'
      AND parent_object_id = OBJECT_ID(N'dbo.tarefas')
)
BEGIN
    ALTER TABLE dbo.tarefas
        ADD CONSTRAINT fk_tarefas_recorrencia
        FOREIGN KEY (recorrencia_id) REFERENCES dbo.tarefa_recorrencias (id);
END
GO

IF OBJECT_ID(N'dbo.alertas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.alertas (
        id BIGINT IDENTITY(1,1) NOT NULL,
        titulo VARCHAR(180) NOT NULL,
        descricao VARCHAR(2000) NULL,
        severidade VARCHAR(20) NOT NULL,
        status VARCHAR(20) NOT NULL,
        modulo_origem VARCHAR(30) NOT NULL,
        tipo VARCHAR(50) NOT NULL,
        referencia_origem VARCHAR(160) NOT NULL,
        chave_deduplicacao VARCHAR(220) NOT NULL,
        detectado_em DATETIME2 NOT NULL,
        atualizado_em DATETIME2 NOT NULL,
        resolvido_em DATETIME2 NULL,
        reconhecido_em DATETIME2 NULL,
        reconhecido_por_id BIGINT NULL,
        dados_contexto VARCHAR(1000) NULL,
        tarefa_id BIGINT NULL,
        versao BIGINT NOT NULL CONSTRAINT df_alertas_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_alertas PRIMARY KEY (id),
        CONSTRAINT fk_alertas_reconhecido_por FOREIGN KEY (reconhecido_por_id) REFERENCES dbo.usuarios (id),
        CONSTRAINT fk_alertas_tarefa FOREIGN KEY (tarefa_id) REFERENCES dbo.tarefas (id),
        CONSTRAINT ck_alertas_severidade CHECK (severidade IN ('INFO', 'ATENCAO', 'ALTA', 'CRITICA')),
        CONSTRAINT ck_alertas_status CHECK (status IN ('ATIVO', 'RECONHECIDO', 'RESOLVIDO')),
        CONSTRAINT ck_alertas_modulo CHECK (modulo_origem IN ('TAREFAS', 'ESTOQUE', 'INTEGRACOES', 'CLIMA', 'COMPRAS')),
        CONSTRAINT ck_alertas_tipo CHECK (tipo IN ('ESTOQUE_ABAIXO_MINIMO', 'LOTE_PROXIMO_VENCIMENTO', 'LOTE_VENCIDO', 'INTEGRACAO_DESATUALIZADA', 'INTEGRACAO_COM_FALHA', 'CHUVA_INTENSA_24H')),
        CONSTRAINT ck_alertas_resolucao CHECK (
            (status = 'RESOLVIDO' AND resolvido_em IS NOT NULL)
            OR (status <> 'RESOLVIDO' AND resolvido_em IS NULL)
        ),
        CONSTRAINT ck_alertas_reconhecimento CHECK (
            (reconhecido_em IS NULL AND reconhecido_por_id IS NULL)
            OR (reconhecido_em IS NOT NULL AND reconhecido_por_id IS NOT NULL)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.tarefa_alerta_eventos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tarefa_alerta_eventos (
        id BIGINT IDENTITY(1,1) NOT NULL,
        tarefa_id BIGINT NULL,
        alerta_id BIGINT NULL,
        tipo VARCHAR(40) NOT NULL,
        ocorrido_em DATETIME2 NOT NULL,
        ator VARCHAR(80) NOT NULL,
        usuario_id BIGINT NULL,
        detalhe VARCHAR(500) NULL,
        CONSTRAINT pk_tarefa_alerta_eventos PRIMARY KEY (id),
        CONSTRAINT fk_tarefa_alerta_eventos_tarefa FOREIGN KEY (tarefa_id) REFERENCES dbo.tarefas (id),
        CONSTRAINT fk_tarefa_alerta_eventos_alerta FOREIGN KEY (alerta_id) REFERENCES dbo.alertas (id),
        CONSTRAINT fk_tarefa_alerta_eventos_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios (id),
        CONSTRAINT ck_tarefa_alerta_eventos_alvo CHECK (
            (tarefa_id IS NOT NULL AND alerta_id IS NULL)
            OR (tarefa_id IS NULL AND alerta_id IS NOT NULL)
        ),
        CONSTRAINT ck_tarefa_alerta_eventos_tipo CHECK (tipo IN (
            'TAREFA_CRIADA', 'TAREFA_EDITADA', 'TAREFA_INICIADA', 'TAREFA_CONCLUIDA',
            'TAREFA_CANCELADA', 'RECORRENCIA_GERADA', 'RECORRENCIA_DESATIVADA',
            'ALERTA_DETECTADO', 'ALERTA_ATUALIZADO', 'ALERTA_RECONHECIDO',
            'ALERTA_RESOLVIDO', 'ALERTA_TAREFA_CRIADA'
        ))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_tarefas_status_vencimento' AND object_id = OBJECT_ID(N'dbo.tarefas'))
BEGIN
    CREATE INDEX ix_tarefas_status_vencimento ON dbo.tarefas (status, data_vencimento, prioridade);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_tarefas_responsavel_status' AND object_id = OBJECT_ID(N'dbo.tarefas'))
BEGIN
    CREATE INDEX ix_tarefas_responsavel_status ON dbo.tarefas (responsavel_id, status, data_vencimento);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_tarefas_recorrencia_ocorrencia' AND object_id = OBJECT_ID(N'dbo.tarefas'))
BEGIN
    CREATE UNIQUE INDEX ux_tarefas_recorrencia_ocorrencia
        ON dbo.tarefas (recorrencia_id, ocorrencia_programada_em)
        WHERE recorrencia_id IS NOT NULL AND ocorrencia_programada_em IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_tarefa_recorrencias_proxima' AND object_id = OBJECT_ID(N'dbo.tarefa_recorrencias'))
BEGIN
    CREATE INDEX ix_tarefa_recorrencias_proxima
        ON dbo.tarefa_recorrencias (ativa, proxima_ocorrencia_em);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_alertas_chave_aberta' AND object_id = OBJECT_ID(N'dbo.alertas'))
BEGIN
    CREATE UNIQUE INDEX ux_alertas_chave_aberta
        ON dbo.alertas (chave_deduplicacao)
        WHERE resolvido_em IS NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_alertas_tarefa' AND object_id = OBJECT_ID(N'dbo.alertas'))
BEGIN
    CREATE UNIQUE INDEX ux_alertas_tarefa
        ON dbo.alertas (tarefa_id)
        WHERE tarefa_id IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_alertas_status_severidade' AND object_id = OBJECT_ID(N'dbo.alertas'))
BEGIN
    CREATE INDEX ix_alertas_status_severidade
        ON dbo.alertas (status, severidade, detectado_em DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_alertas_modulo_tipo' AND object_id = OBJECT_ID(N'dbo.alertas'))
BEGIN
    CREATE INDEX ix_alertas_modulo_tipo
        ON dbo.alertas (modulo_origem, tipo, status);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_tarefa_alerta_eventos_tarefa' AND object_id = OBJECT_ID(N'dbo.tarefa_alerta_eventos'))
BEGIN
    CREATE INDEX ix_tarefa_alerta_eventos_tarefa
        ON dbo.tarefa_alerta_eventos (tarefa_id, ocorrido_em DESC, id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_tarefa_alerta_eventos_alerta' AND object_id = OBJECT_ID(N'dbo.tarefa_alerta_eventos'))
BEGIN
    CREATE INDEX ix_tarefa_alerta_eventos_alerta
        ON dbo.tarefa_alerta_eventos (alerta_id, ocorrido_em DESC, id DESC);
END
GO
