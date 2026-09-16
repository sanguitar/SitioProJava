CREATE TABLE dbo.aves_incubacao_ovos (
    id BIGINT IDENTITY(1,1) NOT NULL,
    incubacao_id BIGINT NOT NULL,
    numero INT NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT df_aves_inc_ovos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT pk_aves_incubacao_ovos PRIMARY KEY (id),
    CONSTRAINT uk_aves_inc_ovos_incubacao_numero UNIQUE (incubacao_id, numero),
    CONSTRAINT fk_aves_inc_ovos_incubacao
        FOREIGN KEY (incubacao_id) REFERENCES dbo.aves_incubacoes (id),
    CONSTRAINT ck_aves_inc_ovos_numero CHECK (numero > 0)
);
GO

CREATE TABLE dbo.aves_incubacao_ovoscopias (
    id BIGINT IDENTITY(1,1) NOT NULL,
    incubacao_id BIGINT NOT NULL,
    data_ovoscopia DATE NOT NULL,
    dia_incubacao INT NOT NULL,
    proxima_verificacao DATE NULL,
    responsavel VARCHAR(120) NULL,
    observacao_geral VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT df_aves_inc_ovoscopias_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT pk_aves_incubacao_ovoscopias PRIMARY KEY (id),
    CONSTRAINT uk_aves_inc_ovoscopias_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT fk_aves_inc_ovoscopias_incubacao
        FOREIGN KEY (incubacao_id) REFERENCES dbo.aves_incubacoes (id),
    CONSTRAINT ck_aves_inc_ovoscopias_dia CHECK (dia_incubacao >= 1)
);
GO

CREATE TABLE dbo.aves_incubacao_ovoscopia_itens (
    id BIGINT IDENTITY(1,1) NOT NULL,
    ovoscopia_id BIGINT NOT NULL,
    ovo_id BIGINT NOT NULL,
    achado VARCHAR(40) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_aves_inc_ovoscopia_itens_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT pk_aves_incubacao_ovoscopia_itens PRIMARY KEY (id),
    CONSTRAINT uk_aves_inc_ovoscopia_item_ovo UNIQUE (ovoscopia_id, ovo_id),
    CONSTRAINT fk_aves_inc_ovoscopia_itens_ovoscopia
        FOREIGN KEY (ovoscopia_id) REFERENCES dbo.aves_incubacao_ovoscopias (id),
    CONSTRAINT fk_aves_inc_ovoscopia_itens_ovo
        FOREIGN KEY (ovo_id) REFERENCES dbo.aves_incubacao_ovos (id),
    CONSTRAINT ck_aves_inc_ovoscopia_itens_achado CHECK (achado IN (
        'DESENVOLVIMENTO_VISIVEL',
        'SEM_DESENVOLVIMENTO_VISIVEL',
        'RACHADURA',
        'DUVIDA',
        'REAVALIAR',
        'PERDA_RETIRADA'
    ))
);
GO

;WITH numeros AS (
    SELECT TOP (10000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS numero
    FROM sys.all_objects a
    CROSS JOIN sys.all_objects b
)
INSERT INTO dbo.aves_incubacao_ovos (incubacao_id, numero)
SELECT incubacao.id, numeros.numero
FROM dbo.aves_incubacoes incubacao
JOIN numeros ON numeros.numero <= incubacao.quantidade_ovos
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.aves_incubacao_ovos ovo
    WHERE ovo.incubacao_id = incubacao.id
);
GO

CREATE INDEX ix_aves_inc_ovos_incubacao
    ON dbo.aves_incubacao_ovos (incubacao_id, numero);
GO

CREATE INDEX ix_aves_inc_ovoscopias_incubacao_data
    ON dbo.aves_incubacao_ovoscopias (incubacao_id, data_ovoscopia DESC, id DESC);
GO

