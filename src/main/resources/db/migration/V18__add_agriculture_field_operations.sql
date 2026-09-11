ALTER TABLE dbo.agricultura_colheitas ADD
    destino VARCHAR(20) NOT NULL CONSTRAINT df_agricultura_colheitas_destino DEFAULT 'SEM_ESTOQUE',
    movimento_estoque_id BIGINT NULL,
    chave_idempotencia VARCHAR(80) NULL;
GO
ALTER TABLE dbo.agricultura_colheitas ADD
    CONSTRAINT fk_agricultura_colheitas_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT ck_agricultura_colheitas_destino CHECK (
        (destino = 'SEM_ESTOQUE' AND movimento_estoque_id IS NULL) OR
        (destino = 'ESTOQUE' AND movimento_estoque_id IS NOT NULL AND chave_idempotencia IS NOT NULL));
CREATE UNIQUE INDEX ux_agricultura_colheitas_idempotencia
    ON dbo.agricultura_colheitas(cultivo_id, chave_idempotencia) WHERE chave_idempotencia IS NOT NULL;
CREATE UNIQUE INDEX ux_agricultura_colheitas_movimento
    ON dbo.agricultura_colheitas(movimento_estoque_id) WHERE movimento_estoque_id IS NOT NULL;
GO

CREATE TABLE dbo.agricultura_adubacoes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_adubacoes PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data DATE NOT NULL,
    produto VARCHAR(180) NOT NULL, quantidade DECIMAL(18,4) NOT NULL, unidade VARCHAR(30) NOT NULL,
    area_aplicada_ha DECIMAL(14,4) NULL, metodo VARCHAR(120) NULL,
    origem VARCHAR(20) NOT NULL, descricao_origem VARCHAR(180) NULL,
    movimento_estoque_id BIGINT NULL, chave_idempotencia VARCHAR(80) NOT NULL,
    observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_adubacoes_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT fk_agricultura_adubacoes_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT ck_agricultura_adubacoes_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_agricultura_adubacoes_area CHECK (area_aplicada_ha IS NULL OR area_aplicada_ha > 0),
    CONSTRAINT ck_agricultura_adubacoes_texto CHECK (
        LEN(LTRIM(RTRIM(produto))) > 0 AND LEN(LTRIM(RTRIM(unidade))) > 0 AND LEN(LTRIM(RTRIM(chave_idempotencia))) >= 8),
    CONSTRAINT ck_agricultura_adubacoes_origem CHECK (
        (origem = 'ESTOQUE' AND movimento_estoque_id IS NOT NULL) OR
        (origem = 'EXTERNA' AND movimento_estoque_id IS NULL AND descricao_origem IS NOT NULL AND LEN(LTRIM(RTRIM(descricao_origem))) > 0))
);
CREATE INDEX ix_agricultura_adubacoes_cultivo ON dbo.agricultura_adubacoes(cultivo_id, data, id);
CREATE UNIQUE INDEX ux_agricultura_adubacoes_idempotencia ON dbo.agricultura_adubacoes(cultivo_id, chave_idempotencia);
CREATE UNIQUE INDEX ux_agricultura_adubacoes_movimento ON dbo.agricultura_adubacoes(movimento_estoque_id) WHERE movimento_estoque_id IS NOT NULL;
GO

CREATE TABLE dbo.agricultura_irrigacoes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_irrigacoes PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data_hora DATETIME2 NOT NULL,
    duracao_minutos INT NULL, volume_litros DECIMAL(18,4) NULL, metodo VARCHAR(120) NULL,
    chave_idempotencia VARCHAR(80) NOT NULL, observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_irrigacoes_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT ck_agricultura_irrigacoes_medicao CHECK (
        (duracao_minutos IS NOT NULL AND duracao_minutos BETWEEN 1 AND 10080) OR (volume_litros IS NOT NULL AND volume_litros > 0)),
    CONSTRAINT ck_agricultura_irrigacoes_volume CHECK (volume_litros IS NULL OR volume_litros > 0),
    CONSTRAINT ck_agricultura_irrigacoes_chave CHECK (LEN(LTRIM(RTRIM(chave_idempotencia))) >= 8)
);
CREATE INDEX ix_agricultura_irrigacoes_cultivo ON dbo.agricultura_irrigacoes(cultivo_id, data_hora, id);
CREATE UNIQUE INDEX ux_agricultura_irrigacoes_idempotencia ON dbo.agricultura_irrigacoes(cultivo_id, chave_idempotencia);
GO

CREATE TABLE dbo.agricultura_tratamentos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_tratamentos PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data DATE NOT NULL,
    finalidade VARCHAR(180) NOT NULL, produto_aplicado VARCHAR(180) NOT NULL,
    quantidade DECIMAL(18,4) NOT NULL, unidade VARCHAR(30) NOT NULL,
    area_tratada_ha DECIMAL(14,4) NULL, metodo VARCHAR(120) NULL,
    origem VARCHAR(20) NOT NULL, descricao_origem VARCHAR(180) NULL,
    movimento_estoque_id BIGINT NULL, chave_idempotencia VARCHAR(80) NOT NULL,
    observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_tratamentos_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT fk_agricultura_tratamentos_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT ck_agricultura_tratamentos_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_agricultura_tratamentos_area CHECK (area_tratada_ha IS NULL OR area_tratada_ha > 0),
    CONSTRAINT ck_agricultura_tratamentos_texto CHECK (
        LEN(LTRIM(RTRIM(finalidade))) > 0 AND LEN(LTRIM(RTRIM(produto_aplicado))) > 0 AND
        LEN(LTRIM(RTRIM(unidade))) > 0 AND LEN(LTRIM(RTRIM(chave_idempotencia))) >= 8),
    CONSTRAINT ck_agricultura_tratamentos_origem CHECK (
        (origem = 'ESTOQUE' AND movimento_estoque_id IS NOT NULL) OR
        (origem = 'EXTERNA' AND movimento_estoque_id IS NULL AND descricao_origem IS NOT NULL AND LEN(LTRIM(RTRIM(descricao_origem))) > 0))
);
CREATE INDEX ix_agricultura_tratamentos_cultivo ON dbo.agricultura_tratamentos(cultivo_id, data, id);
CREATE UNIQUE INDEX ux_agricultura_tratamentos_idempotencia ON dbo.agricultura_tratamentos(cultivo_id, chave_idempotencia);
CREATE UNIQUE INDEX ux_agricultura_tratamentos_movimento ON dbo.agricultura_tratamentos(movimento_estoque_id) WHERE movimento_estoque_id IS NOT NULL;
GO

CREATE TABLE dbo.agricultura_ocorrencias (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_ocorrencias PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data_hora DATETIME2 NOT NULL,
    tipo VARCHAR(25) NOT NULL, severidade VARCHAR(15) NOT NULL, descricao VARCHAR(1000) NOT NULL,
    area_afetada_ha DECIMAL(14,4) NULL, quantidade_perdida DECIMAL(18,4) NULL,
    unidade_perda VARCHAR(30) NULL, perda_total BIT NOT NULL DEFAULT 0,
    chave_idempotencia VARCHAR(80) NOT NULL, observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_ocorrencias_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT ck_agricultura_ocorrencias_tipo CHECK (tipo IN
        ('CLIMATICA','PRAGA','DOENCA','MANEJO','IRRIGACAO','INSUMO','EQUIPAMENTO','OUTRO')),
    CONSTRAINT ck_agricultura_ocorrencias_severidade CHECK (severidade IN ('BAIXA','MEDIA','ALTA','CRITICA')),
    CONSTRAINT ck_agricultura_ocorrencias_area CHECK (area_afetada_ha IS NULL OR area_afetada_ha > 0),
    CONSTRAINT ck_agricultura_ocorrencias_perda CHECK (
        (quantidade_perdida IS NULL AND unidade_perda IS NULL) OR
        (quantidade_perdida > 0 AND LEN(LTRIM(RTRIM(unidade_perda))) > 0)),
    CONSTRAINT ck_agricultura_ocorrencias_texto CHECK (
        LEN(LTRIM(RTRIM(descricao))) > 0 AND LEN(LTRIM(RTRIM(chave_idempotencia))) >= 8)
);
CREATE INDEX ix_agricultura_ocorrencias_cultivo ON dbo.agricultura_ocorrencias(cultivo_id, data_hora, id);
CREATE UNIQUE INDEX ux_agricultura_ocorrencias_idempotencia ON dbo.agricultura_ocorrencias(cultivo_id, chave_idempotencia);
GO
