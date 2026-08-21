IF OBJECT_ID(N'dbo.estoque_categorias', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_categorias (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        descricao VARCHAR(255) NULL,
        ativa BIT NOT NULL CONSTRAINT df_estoque_categorias_ativa DEFAULT 1,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_categorias PRIMARY KEY (id),
        CONSTRAINT uk_estoque_categorias_nome UNIQUE (nome)
    );
END
GO

IF OBJECT_ID(N'dbo.estoque_unidades_medida', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_unidades_medida (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(80) NOT NULL,
        sigla VARCHAR(20) NOT NULL,
        tipo VARCHAR(40) NULL,
        ativa BIT NOT NULL CONSTRAINT df_estoque_unidades_medida_ativa DEFAULT 1,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_unidades_medida PRIMARY KEY (id),
        CONSTRAINT uk_estoque_unidades_medida_sigla UNIQUE (sigla)
    );
END
GO

IF OBJECT_ID(N'dbo.estoque_locais', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_locais (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        descricao VARCHAR(255) NULL,
        ativo BIT NOT NULL CONSTRAINT df_estoque_locais_ativo DEFAULT 1,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_locais PRIMARY KEY (id),
        CONSTRAINT uk_estoque_locais_nome UNIQUE (nome)
    );
END
GO

IF OBJECT_ID(N'dbo.estoque_itens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_itens (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(140) NOT NULL,
        descricao VARCHAR(500) NULL,
        categoria_id BIGINT NOT NULL,
        unidade_medida_id BIGINT NOT NULL,
        estoque_minimo DECIMAL(19,4) NULL,
        ativo BIT NOT NULL CONSTRAINT df_estoque_itens_ativo DEFAULT 1,
        controla_lote BIT NOT NULL CONSTRAINT df_estoque_itens_controla_lote DEFAULT 0,
        controla_validade BIT NOT NULL CONSTRAINT df_estoque_itens_controla_validade DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_itens PRIMARY KEY (id),
        CONSTRAINT fk_estoque_itens_categoria FOREIGN KEY (categoria_id) REFERENCES dbo.estoque_categorias (id),
        CONSTRAINT fk_estoque_itens_unidade FOREIGN KEY (unidade_medida_id) REFERENCES dbo.estoque_unidades_medida (id),
        CONSTRAINT ck_estoque_itens_minimo CHECK (estoque_minimo IS NULL OR estoque_minimo >= 0),
        CONSTRAINT ck_estoque_itens_validade_lote CHECK (controla_validade = 0 OR controla_lote = 1)
    );
END
GO

IF OBJECT_ID(N'dbo.estoque_lotes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_lotes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        item_id BIGINT NOT NULL,
        codigo VARCHAR(80) NOT NULL,
        validade DATE NULL,
        observacao VARCHAR(500) NULL,
        ativo BIT NOT NULL CONSTRAINT df_estoque_lotes_ativo DEFAULT 1,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_lotes PRIMARY KEY (id),
        CONSTRAINT fk_estoque_lotes_item FOREIGN KEY (item_id) REFERENCES dbo.estoque_itens (id),
        CONSTRAINT uk_estoque_lotes_item_codigo UNIQUE (item_id, codigo)
    );
END
GO

IF OBJECT_ID(N'dbo.estoque_movimentos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.estoque_movimentos (
        id BIGINT IDENTITY(1,1) NOT NULL,
        item_id BIGINT NOT NULL,
        tipo VARCHAR(30) NOT NULL,
        quantidade DECIMAL(19,4) NOT NULL,
        local_origem_id BIGINT NULL,
        local_destino_id BIGINT NULL,
        lote_id BIGINT NULL,
        custo_unitario DECIMAL(19,4) NULL,
        custo_total DECIMAL(19,4) NULL,
        observacao VARCHAR(500) NULL,
        data_movimento DATETIME2 NOT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_estoque_movimentos PRIMARY KEY (id),
        CONSTRAINT fk_estoque_movimentos_item FOREIGN KEY (item_id) REFERENCES dbo.estoque_itens (id),
        CONSTRAINT fk_estoque_movimentos_local_origem FOREIGN KEY (local_origem_id) REFERENCES dbo.estoque_locais (id),
        CONSTRAINT fk_estoque_movimentos_local_destino FOREIGN KEY (local_destino_id) REFERENCES dbo.estoque_locais (id),
        CONSTRAINT fk_estoque_movimentos_lote FOREIGN KEY (lote_id) REFERENCES dbo.estoque_lotes (id),
        CONSTRAINT ck_estoque_movimentos_tipo CHECK (tipo IN ('ENTRADA', 'CONSUMO', 'PERDA', 'AJUSTE_ENTRADA', 'AJUSTE_SAIDA', 'TRANSFERENCIA', 'DESCARTE')),
        CONSTRAINT ck_estoque_movimentos_quantidade CHECK (quantidade > 0),
        CONSTRAINT ck_estoque_movimentos_custos CHECK (
            (custo_unitario IS NULL OR custo_unitario >= 0)
            AND (custo_total IS NULL OR custo_total >= 0)
        ),
        CONSTRAINT ck_estoque_movimentos_direcao CHECK (
            (tipo IN ('ENTRADA', 'AJUSTE_ENTRADA') AND local_origem_id IS NULL AND local_destino_id IS NOT NULL)
            OR (tipo IN ('CONSUMO', 'PERDA', 'AJUSTE_SAIDA', 'DESCARTE') AND local_origem_id IS NOT NULL AND local_destino_id IS NULL)
            OR (tipo = 'TRANSFERENCIA' AND local_origem_id IS NOT NULL AND local_destino_id IS NOT NULL AND local_origem_id <> local_destino_id)
        )
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_itens_categoria' AND object_id = OBJECT_ID(N'dbo.estoque_itens'))
BEGIN
    CREATE INDEX ix_estoque_itens_categoria ON dbo.estoque_itens (categoria_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_movimentos_item_data' AND object_id = OBJECT_ID(N'dbo.estoque_movimentos'))
BEGIN
    CREATE INDEX ix_estoque_movimentos_item_data ON dbo.estoque_movimentos (item_id, data_movimento DESC);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_movimentos_origem' AND object_id = OBJECT_ID(N'dbo.estoque_movimentos'))
BEGIN
    CREATE INDEX ix_estoque_movimentos_origem ON dbo.estoque_movimentos (local_origem_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_movimentos_destino' AND object_id = OBJECT_ID(N'dbo.estoque_movimentos'))
BEGIN
    CREATE INDEX ix_estoque_movimentos_destino ON dbo.estoque_movimentos (local_destino_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_estoque_lotes_validade' AND object_id = OBJECT_ID(N'dbo.estoque_lotes'))
BEGIN
    CREATE INDEX ix_estoque_lotes_validade ON dbo.estoque_lotes (validade);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.estoque_categorias WHERE nome = 'Geral')
BEGIN
    INSERT INTO dbo.estoque_categorias (nome, descricao, ativa, criado_em, criado_por)
    VALUES ('Geral', 'Categoria inicial para itens de estoque.', 1, SYSUTCDATETIME(), 'flyway');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.estoque_unidades_medida WHERE sigla = 'KG')
BEGIN
    INSERT INTO dbo.estoque_unidades_medida (nome, sigla, tipo, ativa, criado_em, criado_por)
    VALUES
        ('Quilograma', 'KG', 'MASSA', 1, SYSUTCDATETIME(), 'flyway'),
        ('Grama', 'G', 'MASSA', 1, SYSUTCDATETIME(), 'flyway'),
        ('Litro', 'L', 'VOLUME', 1, SYSUTCDATETIME(), 'flyway'),
        ('Mililitro', 'ML', 'VOLUME', 1, SYSUTCDATETIME(), 'flyway'),
        ('Unidade', 'UN', 'CONTAGEM', 1, SYSUTCDATETIME(), 'flyway'),
        ('Saco', 'SACO', 'CONTAGEM', 1, SYSUTCDATETIME(), 'flyway'),
        ('Caixa', 'CX', 'CONTAGEM', 1, SYSUTCDATETIME(), 'flyway'),
        ('Metro', 'M', 'COMPRIMENTO', 1, SYSUTCDATETIME(), 'flyway');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.estoque_locais WHERE nome = 'Deposito')
BEGIN
    INSERT INTO dbo.estoque_locais (nome, descricao, ativo, criado_em, criado_por)
    VALUES
        ('Deposito', 'Local inicial para armazenamento geral.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Galinheiro', 'Local de estoque vinculado ao manejo das aves.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Pinteiro', 'Local de estoque vinculado ao pinteiro.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Piscicultura', 'Local de estoque vinculado aos tanques e arraçoamento.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Suinos', 'Local de estoque vinculado aos suinos.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Casa', 'Local de estoque doméstico e despensa.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Oficina', 'Local de estoque de materiais de manutenção.', 1, SYSUTCDATETIME(), 'flyway'),
        ('Agricultura', 'Local de estoque de insumos agrícolas.', 1, SYSUTCDATETIME(), 'flyway');
END
GO
