IF COL_LENGTH(N'dbo.estoque_movimentos', N'origem_modulo') IS NULL
BEGIN
    ALTER TABLE dbo.estoque_movimentos
        ADD origem_modulo VARCHAR(40) NULL,
            origem_referencia_id BIGINT NULL,
            origem_descricao VARCHAR(200) NULL;
END
GO

IF OBJECT_ID(N'dbo.fornecedores', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.fornecedores (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(140) NOT NULL,
        documento VARCHAR(40) NULL,
        telefone VARCHAR(40) NULL,
        email VARCHAR(140) NULL,
        observacao VARCHAR(500) NULL,
        ativo BIT NOT NULL CONSTRAINT df_fornecedores_ativo DEFAULT 1,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_fornecedores PRIMARY KEY (id),
        CONSTRAINT uk_fornecedores_nome UNIQUE (nome)
    );
END
GO

IF OBJECT_ID(N'dbo.compras', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.compras (
        id BIGINT IDENTITY(1,1) NOT NULL,
        fornecedor_id BIGINT NOT NULL,
        data_compra DATE NOT NULL,
        status VARCHAR(20) NOT NULL CONSTRAINT df_compras_status DEFAULT 'RASCUNHO',
        numero_documento VARCHAR(80) NULL,
        observacao VARCHAR(500) NULL,
        frete DECIMAL(19,4) NOT NULL CONSTRAINT df_compras_frete DEFAULT 0,
        desconto DECIMAL(19,4) NOT NULL CONSTRAINT df_compras_desconto DEFAULT 0,
        subtotal DECIMAL(19,4) NOT NULL CONSTRAINT df_compras_subtotal DEFAULT 0,
        total DECIMAL(19,4) NOT NULL CONSTRAINT df_compras_total DEFAULT 0,
        confirmado_em DATETIME2 NULL,
        confirmado_por VARCHAR(100) NULL,
        versao BIGINT NOT NULL CONSTRAINT df_compras_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_compras PRIMARY KEY (id),
        CONSTRAINT fk_compras_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES dbo.fornecedores (id),
        CONSTRAINT ck_compras_status CHECK (status IN ('RASCUNHO', 'CONFIRMADA', 'CANCELADA')),
        CONSTRAINT ck_compras_valores CHECK (
            frete >= 0
            AND desconto >= 0
            AND subtotal >= 0
            AND total >= 0
        ),
        CONSTRAINT ck_compras_confirmacao CHECK (
            (status = 'CONFIRMADA' AND confirmado_em IS NOT NULL AND confirmado_por IS NOT NULL)
            OR (status <> 'CONFIRMADA')
        )
    );
END
GO

IF OBJECT_ID(N'dbo.itens_compra', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.itens_compra (
        id BIGINT IDENTITY(1,1) NOT NULL,
        compra_id BIGINT NOT NULL,
        item_estoque_id BIGINT NOT NULL,
        local_destino_id BIGINT NOT NULL,
        movimento_estoque_id BIGINT NULL,
        quantidade DECIMAL(19,4) NOT NULL,
        custo_unitario DECIMAL(19,4) NOT NULL,
        subtotal DECIMAL(19,4) NOT NULL,
        lote_codigo VARCHAR(80) NULL,
        validade DATE NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_itens_compra PRIMARY KEY (id),
        CONSTRAINT fk_itens_compra_compra FOREIGN KEY (compra_id) REFERENCES dbo.compras (id),
        CONSTRAINT fk_itens_compra_item_estoque FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens (id),
        CONSTRAINT fk_itens_compra_local_destino FOREIGN KEY (local_destino_id) REFERENCES dbo.estoque_locais (id),
        CONSTRAINT fk_itens_compra_movimento_estoque FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos (id),
        CONSTRAINT ck_itens_compra_quantidade CHECK (quantidade > 0),
        CONSTRAINT ck_itens_compra_valores CHECK (custo_unitario >= 0 AND subtotal >= 0)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_compras_status_data' AND object_id = OBJECT_ID(N'dbo.compras'))
BEGIN
    CREATE INDEX ix_compras_status_data ON dbo.compras (status, data_compra DESC, id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_compras_fornecedor_data' AND object_id = OBJECT_ID(N'dbo.compras'))
BEGIN
    CREATE INDEX ix_compras_fornecedor_data ON dbo.compras (fornecedor_id, data_compra DESC, id DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_itens_compra_compra' AND object_id = OBJECT_ID(N'dbo.itens_compra'))
BEGIN
    CREATE INDEX ix_itens_compra_compra ON dbo.itens_compra (compra_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_itens_compra_item' AND object_id = OBJECT_ID(N'dbo.itens_compra'))
BEGIN
    CREATE INDEX ix_itens_compra_item ON dbo.itens_compra (item_estoque_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_itens_compra_local' AND object_id = OBJECT_ID(N'dbo.itens_compra'))
BEGIN
    CREATE INDEX ix_itens_compra_local ON dbo.itens_compra (local_destino_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_itens_compra_movimento' AND object_id = OBJECT_ID(N'dbo.itens_compra'))
BEGIN
    CREATE UNIQUE INDEX ux_itens_compra_movimento
        ON dbo.itens_compra (movimento_estoque_id)
        WHERE movimento_estoque_id IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_movimentos_origem_ref' AND object_id = OBJECT_ID(N'dbo.estoque_movimentos'))
BEGIN
    CREATE INDEX ix_estoque_movimentos_origem_ref
        ON dbo.estoque_movimentos (origem_modulo, origem_referencia_id);
END
GO
