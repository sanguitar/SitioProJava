CREATE TABLE dbo.suinos_registros_sanitarios (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_suinos_registros_sanitarios PRIMARY KEY,
    lote_id BIGINT NULL,
    animal_reprodutivo_id BIGINT NULL,
    tipo VARCHAR(30) NOT NULL,
    data_procedimento DATE NOT NULL,
    procedimento_produto VARCHAR(200) NOT NULL,
    motivo VARCHAR(500) NULL,
    responsavel_nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    proxima_acao VARCHAR(250) NULL,
    proxima_acao_data DATE NULL,
    proxima_acao_concluida BIT NOT NULL CONSTRAINT DF_suinos_registros_proxima_concluida DEFAULT 0,
    proxima_acao_concluida_em DATETIME2 NULL,
    custo DECIMAL(19,2) NULL,
    item_estoque_id BIGINT NULL,
    local_estoque_id BIGINT NULL,
    quantidade_consumida DECIMAL(19,4) NULL,
    lote_estoque_codigo VARCHAR(100) NULL,
    movimento_estoque_id BIGINT NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_suinos_registros_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_suinos_registros_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_suinos_registros_lote FOREIGN KEY (lote_id) REFERENCES dbo.suinos_lotes(id),
    CONSTRAINT FK_suinos_registros_animal FOREIGN KEY (animal_reprodutivo_id) REFERENCES dbo.suinos_animais_reprodutivos(id),
    CONSTRAINT FK_suinos_registros_item FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens(id),
    CONSTRAINT FK_suinos_registros_local FOREIGN KEY (local_estoque_id) REFERENCES dbo.estoque_locais(id),
    CONSTRAINT FK_suinos_registros_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT UQ_suinos_registros_movimento UNIQUE (movimento_estoque_id),
    CONSTRAINT CK_suinos_registros_alvo CHECK (
        (lote_id IS NOT NULL AND animal_reprodutivo_id IS NULL)
        OR (lote_id IS NULL AND animal_reprodutivo_id IS NOT NULL)
    ),
    CONSTRAINT CK_suinos_registros_tipo CHECK (
        tipo IN ('VACINACAO','VERMIFUGACAO','TRATAMENTO','EXAME','OCORRENCIA','OUTRO')
    ),
    CONSTRAINT CK_suinos_registros_proxima_acao CHECK (
        (proxima_acao IS NULL AND proxima_acao_data IS NULL)
        OR (proxima_acao IS NOT NULL AND proxima_acao_data IS NOT NULL)
    ),
    CONSTRAINT CK_suinos_registros_conclusao CHECK (
        (proxima_acao_concluida = 0 AND proxima_acao_concluida_em IS NULL)
        OR (proxima_acao_concluida = 1 AND proxima_acao_concluida_em IS NOT NULL)
    ),
    CONSTRAINT CK_suinos_registros_custo CHECK (custo IS NULL OR custo >= 0),
    CONSTRAINT CK_suinos_registros_consumo CHECK (
        quantidade_consumida IS NULL
        OR (quantidade_consumida > 0 AND item_estoque_id IS NOT NULL AND local_estoque_id IS NOT NULL)
    )
);

CREATE INDEX IX_suinos_registros_lote_data
    ON dbo.suinos_registros_sanitarios(lote_id, data_procedimento DESC, id DESC);
CREATE INDEX IX_suinos_registros_animal_data
    ON dbo.suinos_registros_sanitarios(animal_reprodutivo_id, data_procedimento DESC, id DESC);
CREATE INDEX IX_suinos_registros_proxima_acao
    ON dbo.suinos_registros_sanitarios(proxima_acao_concluida, proxima_acao_data)
    WHERE proxima_acao_data IS NOT NULL;
