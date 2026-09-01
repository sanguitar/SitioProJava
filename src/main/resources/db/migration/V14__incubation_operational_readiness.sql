ALTER TABLE dbo.aves_incubacoes ADD
    metodo VARCHAR(30) NULL,
    especie VARCHAR(30) NULL,
    postura_origem_id BIGINT NULL,
    observacao_finalizacao VARCHAR(1000) NULL,
    motivo_ajuste_previsao VARCHAR(500) NULL;
GO

UPDATE incubacao
SET metodo = 'CHOCADEIRA',
    especie = COALESCE(lote.especie, 'GALINHA')
FROM dbo.aves_incubacoes incubacao
LEFT JOIN dbo.aves_lotes lote ON lote.id = incubacao.lote_reprodutor_id;
GO

ALTER TABLE dbo.aves_incubacoes ALTER COLUMN metodo VARCHAR(30) NOT NULL;
ALTER TABLE dbo.aves_incubacoes ALTER COLUMN especie VARCHAR(30) NOT NULL;
GO

ALTER TABLE dbo.aves_incubacoes ADD
    CONSTRAINT fk_aves_incubacoes_postura_origem
        FOREIGN KEY (postura_origem_id) REFERENCES dbo.aves_posturas (id),
    CONSTRAINT ck_aves_incubacoes_metodo
        CHECK (metodo IN ('CHOCADEIRA', 'GALINHA_CHOCA')),
    CONSTRAINT ck_aves_incubacoes_especie
        CHECK (especie IN ('GALINHA', 'CODORNA', 'PATO', 'PERU', 'GANSO', 'OUTRA'));
GO

CREATE TABLE dbo.aves_incubacao_acompanhamentos (
    id BIGINT IDENTITY(1,1) NOT NULL,
    incubacao_id BIGINT NOT NULL,
    data_hora DATETIME2 NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    quantidade_avaliada INT NULL,
    ovos_ferteis INT NULL,
    ovos_sem_desenvolvimento INT NULL,
    perdas INT NULL,
    temperatura DECIMAL(5,2) NULL,
    umidade DECIMAL(5,2) NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT df_aves_inc_acomp_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT pk_aves_incubacao_acompanhamentos PRIMARY KEY (id),
    CONSTRAINT uk_aves_inc_acomp_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT fk_aves_inc_acomp_incubacao
        FOREIGN KEY (incubacao_id) REFERENCES dbo.aves_incubacoes (id),
    CONSTRAINT ck_aves_inc_acomp_tipo CHECK (tipo IN (
        'VERIFICACAO_GERAL', 'OVOSCOPIA', 'PERDA_RETIRADA',
        'TEMPERATURA_UMIDADE', 'OUTRO'
    )),
    CONSTRAINT ck_aves_inc_acomp_quantidades CHECK (
        (quantidade_avaliada IS NULL OR quantidade_avaliada >= 0)
        AND (ovos_ferteis IS NULL OR ovos_ferteis >= 0)
        AND (ovos_sem_desenvolvimento IS NULL OR ovos_sem_desenvolvimento >= 0)
        AND (perdas IS NULL OR perdas >= 0)
        AND (
            quantidade_avaliada IS NULL
            OR COALESCE(ovos_ferteis, 0) + COALESCE(ovos_sem_desenvolvimento, 0)
                + COALESCE(perdas, 0) <= quantidade_avaliada
        )
    ),
    CONSTRAINT ck_aves_inc_acomp_medicoes CHECK (
        (temperatura IS NULL OR temperatura BETWEEN 0 AND 100)
        AND (umidade IS NULL OR umidade BETWEEN 0 AND 100)
        AND (
            (tipo = 'TEMPERATURA_UMIDADE' AND (temperatura IS NOT NULL OR umidade IS NOT NULL))
            OR (tipo <> 'TEMPERATURA_UMIDADE' AND temperatura IS NULL AND umidade IS NULL)
        )
    )
);
GO

CREATE INDEX ix_aves_inc_acomp_incubacao_data
    ON dbo.aves_incubacao_acompanhamentos (incubacao_id, data_hora DESC, id DESC);
GO

ALTER TABLE dbo.tarefas ADD chave_automacao VARCHAR(160) NULL;
GO

CREATE UNIQUE INDEX ux_tarefas_chave_automacao
    ON dbo.tarefas (chave_automacao)
    WHERE chave_automacao IS NOT NULL;
GO

