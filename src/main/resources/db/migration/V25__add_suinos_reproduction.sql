ALTER TABLE dbo.criacao_codigo_sequencias DROP CONSTRAINT CK_criacao_codigo_sequencias_tipo;
ALTER TABLE dbo.criacao_codigo_sequencias ADD CONSTRAINT CK_criacao_codigo_sequencias_tipo
    CHECK (tipo IN ('LOTE_AVES', 'INCUBACAO_AVES', 'LOTE_SUINOS', 'ANIMAL_SUINOS', 'REPRODUCAO_SUINOS'));

CREATE TABLE dbo.suinos_animais_reprodutivos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_suinos_animais_reprodutivos PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL CONSTRAINT UQ_suinos_animais_reprodutivos_codigo UNIQUE,
    lote_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    identificacao VARCHAR(120) NULL,
    data_nascimento DATE NULL,
    peso_atual DECIMAL(19,4) NULL,
    status VARCHAR(20) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_suinos_animais_reprodutivos_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_suinos_animais_reprodutivos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_suinos_animais_reprodutivos_lote FOREIGN KEY (lote_id) REFERENCES dbo.suinos_lotes(id),
    CONSTRAINT CK_suinos_animais_reprodutivos_tipo CHECK (tipo IN ('MATRIZ','REPRODUTOR')),
    CONSTRAINT CK_suinos_animais_reprodutivos_status CHECK (status IN ('ATIVO','INATIVO')),
    CONSTRAINT CK_suinos_animais_reprodutivos_peso CHECK (peso_atual IS NULL OR peso_atual > 0)
);

CREATE INDEX IX_suinos_animais_reprodutivos_lote_status
    ON dbo.suinos_animais_reprodutivos(lote_id, status, tipo);

CREATE TABLE dbo.suinos_ciclos_reprodutivos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_suinos_ciclos_reprodutivos PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL CONSTRAINT UQ_suinos_ciclos_reprodutivos_codigo UNIQUE,
    matriz_id BIGINT NOT NULL,
    reprodutor_id BIGINT NULL,
    metodo VARCHAR(20) NOT NULL,
    data_cobertura DATE NOT NULL,
    data_prevista_checagem DATE NOT NULL,
    data_checagem DATE NULL,
    data_prevista_parto DATE NOT NULL,
    data_parto DATE NULL,
    nascidos_vivos INT NULL,
    natimortos INT NULL,
    perdas_parto INT NULL,
    lote_leitoes_id BIGINT NULL,
    data_prevista_desmame DATE NULL,
    data_desmame DATE NULL,
    peso_medio_desmame DECIMAL(19,4) NULL,
    status VARCHAR(30) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_suinos_ciclos_idempotencia UNIQUE,
    chave_parto VARCHAR(100) NULL CONSTRAINT UQ_suinos_ciclos_parto UNIQUE,
    chave_desmame VARCHAR(100) NULL CONSTRAINT UQ_suinos_ciclos_desmame UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_suinos_ciclos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_suinos_ciclos_matriz FOREIGN KEY (matriz_id) REFERENCES dbo.suinos_animais_reprodutivos(id),
    CONSTRAINT FK_suinos_ciclos_reprodutor FOREIGN KEY (reprodutor_id) REFERENCES dbo.suinos_animais_reprodutivos(id),
    CONSTRAINT FK_suinos_ciclos_lote_leitoes FOREIGN KEY (lote_leitoes_id) REFERENCES dbo.suinos_lotes(id),
    CONSTRAINT CK_suinos_ciclos_metodo CHECK (metodo IN ('COBERTURA','INSEMINACAO')),
    CONSTRAINT CK_suinos_ciclos_status CHECK (status IN ('AGUARDANDO_CHECAGEM','GESTANTE','NAO_CONFIRMADA','PARTO_REALIZADO','DESMAMADO','ENCERRADO')),
    CONSTRAINT CK_suinos_ciclos_datas CHECK (
        data_prevista_checagem >= data_cobertura AND data_prevista_parto > data_cobertura
        AND (data_checagem IS NULL OR data_checagem >= data_cobertura)
        AND (data_parto IS NULL OR data_parto >= data_cobertura)
        AND (data_desmame IS NULL OR (data_parto IS NOT NULL AND data_desmame >= data_parto))
    ),
    CONSTRAINT CK_suinos_ciclos_resultado_parto CHECK (
        (nascidos_vivos IS NULL OR nascidos_vivos >= 0)
        AND (natimortos IS NULL OR natimortos >= 0)
        AND (perdas_parto IS NULL OR perdas_parto >= 0)
    ),
    CONSTRAINT CK_suinos_ciclos_peso_desmame CHECK (peso_medio_desmame IS NULL OR peso_medio_desmame > 0)
);

CREATE INDEX IX_suinos_ciclos_matriz_data
    ON dbo.suinos_ciclos_reprodutivos(matriz_id, data_cobertura DESC, id DESC);
CREATE INDEX IX_suinos_ciclos_status_prazos
    ON dbo.suinos_ciclos_reprodutivos(status, data_prevista_checagem, data_prevista_parto);
