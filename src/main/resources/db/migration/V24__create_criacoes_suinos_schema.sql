ALTER TABLE dbo.criacao_codigo_sequencias DROP CONSTRAINT CK_criacao_codigo_sequencias_tipo;
ALTER TABLE dbo.criacao_codigo_sequencias ADD CONSTRAINT CK_criacao_codigo_sequencias_tipo
    CHECK (tipo IN ('LOTE_AVES', 'INCUBACAO_AVES', 'LOTE_SUINOS'));

CREATE TABLE dbo.suinos_lotes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_suinos_lotes PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL CONSTRAINT UQ_suinos_lotes_codigo UNIQUE,
    categoria VARCHAR(30) NOT NULL,
    data_entrada DATE NOT NULL,
    data_nascimento DATE NULL,
    origem VARCHAR(200) NOT NULL,
    quantidade_inicial INT NOT NULL,
    quantidade_atual INT NOT NULL,
    peso_medio DECIMAL(19,4) NULL,
    instalacao_atual_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_suinos_lotes_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_suinos_lotes_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_suinos_lotes_instalacao FOREIGN KEY (instalacao_atual_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT CK_suinos_lotes_categoria CHECK (categoria IN ('LEITAO','CRESCIMENTO','TERMINACAO','MATRIZ','REPRODUTOR')),
    CONSTRAINT CK_suinos_lotes_status CHECK (status IN ('ATIVO','ENCERRADO')),
    CONSTRAINT CK_suinos_lotes_quantidades CHECK (quantidade_inicial > 0 AND quantidade_atual >= 0),
    CONSTRAINT CK_suinos_lotes_peso CHECK (peso_medio IS NULL OR peso_medio > 0),
    CONSTRAINT CK_suinos_lotes_datas CHECK (data_nascimento IS NULL OR data_nascimento <= data_entrada)
);

CREATE INDEX IX_suinos_lotes_status_instalacao ON dbo.suinos_lotes(status, instalacao_atual_id);

CREATE TABLE dbo.suinos_eventos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_suinos_eventos PRIMARY KEY,
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
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_suinos_eventos_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_suinos_eventos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_suinos_eventos_lote FOREIGN KEY (lote_id) REFERENCES dbo.suinos_lotes(id),
    CONSTRAINT FK_suinos_eventos_instalacao_origem FOREIGN KEY (instalacao_origem_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT FK_suinos_eventos_instalacao_destino FOREIGN KEY (instalacao_destino_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT FK_suinos_eventos_item FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens(id),
    CONSTRAINT FK_suinos_eventos_local FOREIGN KEY (local_estoque_id) REFERENCES dbo.estoque_locais(id),
    CONSTRAINT FK_suinos_eventos_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT UQ_suinos_eventos_movimento UNIQUE (movimento_estoque_id),
    CONSTRAINT CK_suinos_eventos_tipo CHECK (tipo IN ('ENTRADA_INICIAL','ENTRADA_ANIMAIS','MORTALIDADE','PERDA','TRANSFERENCIA','PESAGEM','ALIMENTACAO')),
    CONSTRAINT CK_suinos_eventos_quantidade CHECK (quantidade IS NULL OR quantidade > 0),
    CONSTRAINT CK_suinos_eventos_valor CHECK (valor_decimal IS NULL OR valor_decimal > 0)
);

CREATE INDEX IX_suinos_eventos_lote_data ON dbo.suinos_eventos(lote_id, data_evento DESC, id DESC);
CREATE INDEX IX_suinos_eventos_tipo_data ON dbo.suinos_eventos(tipo, data_evento DESC);
