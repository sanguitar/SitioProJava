CREATE TABLE dbo.manutencao_planos_preventivos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_manutencao_planos_preventivos PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(500) NULL,
    tipo_periodicidade VARCHAR(30) NOT NULL,
    intervalo DECIMAL(19,2) NOT NULL,
    data_referencia DATETIME2 NULL,
    proxima_data DATETIME2 NULL,
    valor_referencia DECIMAL(19,2) NULL,
    proximo_valor DECIMAL(19,2) NULL,
    ativo BIT NOT NULL CONSTRAINT DF_manutencao_planos_ativo DEFAULT 1,
    ciclo_atual INT NOT NULL CONSTRAINT DF_manutencao_planos_ciclo DEFAULT 1,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_manutencao_planos_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_manutencao_planos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_manutencao_planos_ativo FOREIGN KEY (ativo_id) REFERENCES dbo.patrimonio_ativos(id),
    CONSTRAINT CK_manutencao_planos_tipo CHECK (tipo_periodicidade IN ('DIAS','HORIMETRO','QUILOMETRAGEM')),
    CONSTRAINT CK_manutencao_planos_intervalo CHECK (intervalo > 0),
    CONSTRAINT CK_manutencao_planos_ciclo CHECK (ciclo_atual > 0),
    CONSTRAINT CK_manutencao_planos_referencia CHECK (
        (tipo_periodicidade = 'DIAS' AND data_referencia IS NOT NULL AND proxima_data IS NOT NULL
            AND valor_referencia IS NULL AND proximo_valor IS NULL AND intervalo = FLOOR(intervalo))
        OR
        (tipo_periodicidade IN ('HORIMETRO','QUILOMETRAGEM') AND data_referencia IS NULL AND proxima_data IS NULL
            AND valor_referencia IS NOT NULL AND proximo_valor IS NOT NULL)
    )
);

CREATE INDEX IX_manutencao_planos_ativo ON dbo.manutencao_planos_preventivos(ativo_id, ativo, nome);
CREATE INDEX IX_manutencao_planos_proxima_data ON dbo.manutencao_planos_preventivos(ativo, proxima_data)
    WHERE proxima_data IS NOT NULL;

CREATE TABLE dbo.manutencao_leituras_medidores (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_manutencao_leituras_medidores PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    data_leitura DATETIME2 NOT NULL,
    horimetro DECIMAL(19,2) NULL,
    quilometragem DECIMAL(19,2) NULL,
    tipo_leitura VARCHAR(30) NOT NULL,
    justificativa_ajuste VARCHAR(500) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_manutencao_leituras_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_manutencao_leituras_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_manutencao_leituras_ativo FOREIGN KEY (ativo_id) REFERENCES dbo.patrimonio_ativos(id),
    CONSTRAINT CK_manutencao_leituras_tipo CHECK (tipo_leitura IN ('OPERACIONAL','AJUSTE_ADMINISTRATIVO')),
    CONSTRAINT CK_manutencao_leituras_valores CHECK (
        (horimetro IS NOT NULL OR quilometragem IS NOT NULL)
        AND (horimetro IS NULL OR horimetro >= 0)
        AND (quilometragem IS NULL OR quilometragem >= 0)
    ),
    CONSTRAINT CK_manutencao_leituras_ajuste CHECK (
        (tipo_leitura = 'OPERACIONAL' AND justificativa_ajuste IS NULL)
        OR (tipo_leitura = 'AJUSTE_ADMINISTRATIVO' AND justificativa_ajuste IS NOT NULL)
    )
);

CREATE INDEX IX_manutencao_leituras_ativo_data
    ON dbo.manutencao_leituras_medidores(ativo_id, data_leitura DESC, id DESC);

ALTER TABLE dbo.manutencao_registros ADD plano_preventivo_id BIGINT NULL;
GO
ALTER TABLE dbo.manutencao_registros ADD CONSTRAINT FK_manutencao_registros_plano
    FOREIGN KEY (plano_preventivo_id) REFERENCES dbo.manutencao_planos_preventivos(id);
CREATE INDEX IX_manutencao_registros_plano ON dbo.manutencao_registros(plano_preventivo_id)
    WHERE plano_preventivo_id IS NOT NULL;
