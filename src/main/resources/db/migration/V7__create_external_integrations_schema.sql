IF OBJECT_ID(N'dbo.integracao_estados', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.integracao_estados (
        fonte VARCHAR(40) NOT NULL,
        ultimo_sucesso_em DATETIME2 NULL,
        ultima_tentativa_em DATETIME2 NULL,
        em_execucao BIT NOT NULL CONSTRAINT df_integracao_estados_em_execucao DEFAULT 0,
        execucao_iniciada_em DATETIME2 NULL,
        checkpoint_valor VARCHAR(500) NULL,
        etag VARCHAR(255) NULL,
        last_modified VARCHAR(255) NULL,
        versao BIGINT NOT NULL CONSTRAINT df_integracao_estados_versao DEFAULT 0,
        CONSTRAINT pk_integracao_estados PRIMARY KEY (fonte),
        CONSTRAINT ck_integracao_estados_execucao CHECK (
            (em_execucao = 1 AND execucao_iniciada_em IS NOT NULL)
            OR (em_execucao = 0 AND execucao_iniciada_em IS NULL)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.integracao_execucoes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.integracao_execucoes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        fonte VARCHAR(40) NOT NULL,
        iniciado_em DATETIME2 NOT NULL,
        finalizado_em DATETIME2 NULL,
        status VARCHAR(20) NOT NULL,
        registros_lidos INT NOT NULL CONSTRAINT df_integracao_execucoes_lidos DEFAULT 0,
        registros_inseridos INT NOT NULL CONSTRAINT df_integracao_execucoes_inseridos DEFAULT 0,
        registros_atualizados INT NOT NULL CONSTRAINT df_integracao_execucoes_atualizados DEFAULT 0,
        registros_ignorados INT NOT NULL CONSTRAINT df_integracao_execucoes_ignorados DEFAULT 0,
        erro_codigo VARCHAR(80) NULL,
        erro_resumo VARCHAR(500) NULL,
        trace_id VARCHAR(64) NULL,
        CONSTRAINT pk_integracao_execucoes PRIMARY KEY (id),
        CONSTRAINT fk_integracao_execucoes_estado FOREIGN KEY (fonte) REFERENCES dbo.integracao_estados (fonte),
        CONSTRAINT ck_integracao_execucoes_status CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILURE')),
        CONSTRAINT ck_integracao_execucoes_contadores CHECK (
            registros_lidos >= 0
            AND registros_inseridos >= 0
            AND registros_atualizados >= 0
            AND registros_ignorados >= 0
        ),
        CONSTRAINT ck_integracao_execucoes_finalizacao CHECK (
            (status = 'RUNNING' AND finalizado_em IS NULL)
            OR (status IN ('SUCCESS', 'FAILURE') AND finalizado_em IS NOT NULL)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.previsoes_climaticas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.previsoes_climaticas (
        id BIGINT IDENTITY(1,1) NOT NULL,
        fonte VARCHAR(40) NOT NULL,
        contexto VARCHAR(80) NOT NULL,
        timezone VARCHAR(80) NOT NULL,
        data_hora_previsao DATETIME2 NOT NULL,
        temperatura DECIMAL(7,2) NULL,
        umidade_relativa SMALLINT NULL,
        precipitacao DECIMAL(9,3) NULL,
        probabilidade_precipitacao SMALLINT NULL,
        velocidade_vento DECIMAL(9,2) NULL,
        rajada_vento DECIMAL(9,2) NULL,
        et0 DECIMAL(9,4) NULL,
        umidade_solo DECIMAL(8,5) NULL,
        codigo_tempo SMALLINT NULL,
        obtido_em DATETIME2 NOT NULL,
        CONSTRAINT pk_previsoes_climaticas PRIMARY KEY (id),
        CONSTRAINT uk_previsoes_climaticas_fonte_contexto_data UNIQUE (fonte, contexto, data_hora_previsao),
        CONSTRAINT ck_previsoes_climaticas_percentuais CHECK (
            (umidade_relativa IS NULL OR umidade_relativa BETWEEN 0 AND 100)
            AND (probabilidade_precipitacao IS NULL OR probabilidade_precipitacao BETWEEN 0 AND 100)
        ),
        CONSTRAINT ck_previsoes_climaticas_valores CHECK (
            (precipitacao IS NULL OR precipitacao >= 0)
            AND (velocidade_vento IS NULL OR velocidade_vento >= 0)
            AND (rajada_vento IS NULL OR rajada_vento >= 0)
            AND (et0 IS NULL OR et0 >= 0)
            AND (umidade_solo IS NULL OR umidade_solo >= 0)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.agrofit_culturas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.agrofit_culturas (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(180) NOT NULL,
        nome_normalizado VARCHAR(180) NOT NULL,
        obtido_em DATETIME2 NOT NULL,
        fonte VARCHAR(40) NOT NULL,
        CONSTRAINT pk_agrofit_culturas PRIMARY KEY (id),
        CONSTRAINT uk_agrofit_culturas_nome_normalizado UNIQUE (nome_normalizado)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_integracao_execucoes_fonte_inicio' AND object_id = OBJECT_ID(N'dbo.integracao_execucoes'))
BEGIN
    CREATE INDEX ix_integracao_execucoes_fonte_inicio
        ON dbo.integracao_execucoes (fonte, iniciado_em DESC, id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_previsoes_climaticas_contexto_data' AND object_id = OBJECT_ID(N'dbo.previsoes_climaticas'))
BEGIN
    CREATE INDEX ix_previsoes_climaticas_contexto_data
        ON dbo.previsoes_climaticas (contexto, data_hora_previsao);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.integracao_estados WHERE fonte = 'OPEN_METEO')
BEGIN
    INSERT INTO dbo.integracao_estados (fonte, em_execucao, versao)
    VALUES ('OPEN_METEO', 0, 0);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.integracao_estados WHERE fonte = 'EMBRAPA_AGROFIT')
BEGIN
    INSERT INTO dbo.integracao_estados (fonte, em_execucao, versao)
    VALUES ('EMBRAPA_AGROFIT', 0, 0);
END
GO
