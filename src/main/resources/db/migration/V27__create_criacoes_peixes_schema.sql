ALTER TABLE dbo.criacao_instalacoes DROP CONSTRAINT ck_criacao_instalacoes_tipo;
ALTER TABLE dbo.criacao_instalacoes ADD CONSTRAINT ck_criacao_instalacoes_tipo
    CHECK (tipo IN ('INCUBADORA','CRIADOURO_PINTINHOS','GALINHEIRO','PIQUETE','TANQUE_PISCICULTURA','OUTRO'));

ALTER TABLE dbo.criacao_codigo_sequencias DROP CONSTRAINT CK_criacao_codigo_sequencias_tipo;
ALTER TABLE dbo.criacao_codigo_sequencias ADD CONSTRAINT CK_criacao_codigo_sequencias_tipo
    CHECK (tipo IN ('LOTE_AVES','INCUBACAO_AVES','LOTE_SUINOS','ANIMAL_SUINOS','REPRODUCAO_SUINOS','LOTE_PEIXES'));

CREATE TABLE dbo.peixes_lotes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_peixes_lotes PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL CONSTRAINT UQ_peixes_lotes_codigo UNIQUE,
    especie VARCHAR(120) NOT NULL,
    quantidade_inicial INT NOT NULL,
    quantidade_atual INT NOT NULL,
    data_entrada DATE NOT NULL,
    origem VARCHAR(200) NOT NULL,
    peso_medio DECIMAL(19,4) NULL,
    instalacao_atual_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_peixes_lotes_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_peixes_lotes_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_peixes_lotes_instalacao FOREIGN KEY (instalacao_atual_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT CK_peixes_lotes_quantidades CHECK (quantidade_inicial > 0 AND quantidade_atual >= 0),
    CONSTRAINT CK_peixes_lotes_peso CHECK (peso_medio IS NULL OR peso_medio > 0),
    CONSTRAINT CK_peixes_lotes_status CHECK (status IN ('ATIVO','ENCERRADO'))
);

CREATE INDEX IX_peixes_lotes_status_instalacao ON dbo.peixes_lotes(status, instalacao_atual_id);

CREATE TABLE dbo.peixes_eventos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_peixes_eventos PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    quantidade INT NULL,
    valor_decimal DECIMAL(19,4) NULL,
    data_evento DATETIME2 NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    observacao VARCHAR(1000) NULL,
    instalacao_origem_id BIGINT NULL,
    instalacao_destino_id BIGINT NULL,
    item_estoque_id BIGINT NULL,
    local_estoque_id BIGINT NULL,
    movimento_estoque_id BIGINT NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_peixes_eventos_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_peixes_eventos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_peixes_eventos_lote FOREIGN KEY (lote_id) REFERENCES dbo.peixes_lotes(id),
    CONSTRAINT FK_peixes_eventos_instalacao_origem FOREIGN KEY (instalacao_origem_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT FK_peixes_eventos_instalacao_destino FOREIGN KEY (instalacao_destino_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT FK_peixes_eventos_item FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens(id),
    CONSTRAINT FK_peixes_eventos_local FOREIGN KEY (local_estoque_id) REFERENCES dbo.estoque_locais(id),
    CONSTRAINT FK_peixes_eventos_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT UQ_peixes_eventos_movimento UNIQUE (movimento_estoque_id),
    CONSTRAINT CK_peixes_eventos_tipo CHECK (tipo IN ('ENTRADA_INICIAL','ENTRADA_PEIXES','MORTALIDADE','PERDA','TRANSFERENCIA','BIOMETRIA','ALIMENTACAO')),
    CONSTRAINT CK_peixes_eventos_quantidade CHECK (quantidade IS NULL OR quantidade > 0),
    CONSTRAINT CK_peixes_eventos_valor CHECK (valor_decimal IS NULL OR valor_decimal > 0)
);

CREATE INDEX IX_peixes_eventos_lote_data ON dbo.peixes_eventos(lote_id, data_evento DESC, id DESC);
CREATE INDEX IX_peixes_eventos_tipo_data ON dbo.peixes_eventos(tipo, data_evento DESC);
