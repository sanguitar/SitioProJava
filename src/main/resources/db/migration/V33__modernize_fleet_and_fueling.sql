ALTER TABLE dbo.veiculos ALTER COLUMN km_atual DECIMAL(19,2) NULL;
ALTER TABLE dbo.veiculos ALTER COLUMN valor_fipe DECIMAL(19,2) NULL;
ALTER TABLE dbo.fipe_cache ALTER COLUMN valor DECIMAL(19,2) NULL;
ALTER TABLE dbo.abastecimentos ALTER COLUMN litros DECIMAL(19,3) NULL;
ALTER TABLE dbo.abastecimentos ALTER COLUMN preco_por_litro DECIMAL(19,4) NULL;
ALTER TABLE dbo.abastecimentos ALTER COLUMN valor_total DECIMAL(19,2) NULL;
ALTER TABLE dbo.abastecimentos ALTER COLUMN km_no_ato DECIMAL(19,2) NULL;
GO

ALTER TABLE dbo.veiculos ADD
    tipo_medidor VARCHAR(30) NULL,
    horimetro_atual DECIMAL(19,2) NULL,
    ativo_patrimonial_id BIGINT NULL,
    chave_idempotencia VARCHAR(100) NULL,
    versao BIGINT NOT NULL CONSTRAINT DF_veiculos_versao DEFAULT 0;

ALTER TABLE dbo.abastecimentos ADD
    horimetro_no_ato DECIMAL(19,2) NULL,
    chave_idempotencia VARCHAR(100) NULL,
    versao BIGINT NOT NULL CONSTRAINT DF_abastecimentos_versao DEFAULT 0;
GO

UPDATE dbo.veiculos
SET tipo_medidor = 'QUILOMETRAGEM',
    chave_idempotencia = CONCAT('LEGADO-VEICULO-', id)
WHERE tipo_medidor IS NULL OR chave_idempotencia IS NULL;

UPDATE dbo.abastecimentos
SET chave_idempotencia = CONCAT('LEGADO-ABASTECIMENTO-', id),
    valor_total = ROUND(litros * preco_por_litro, 2)
WHERE chave_idempotencia IS NULL;

ALTER TABLE dbo.veiculos ALTER COLUMN tipo_medidor VARCHAR(30) NOT NULL;
ALTER TABLE dbo.veiculos ALTER COLUMN chave_idempotencia VARCHAR(100) NOT NULL;
ALTER TABLE dbo.abastecimentos ALTER COLUMN chave_idempotencia VARCHAR(100) NOT NULL;
GO

ALTER TABLE dbo.veiculos ADD
    CONSTRAINT FK_veiculos_ativo_patrimonial FOREIGN KEY (ativo_patrimonial_id)
        REFERENCES dbo.patrimonio_ativos(id),
    CONSTRAINT CK_veiculos_tipo_medidor CHECK (tipo_medidor IN ('QUILOMETRAGEM','HORIMETRO')),
    CONSTRAINT CK_veiculos_km_atual CHECK (km_atual IS NULL OR km_atual >= 0),
    CONSTRAINT CK_veiculos_horimetro_atual CHECK (horimetro_atual IS NULL OR horimetro_atual >= 0),
    CONSTRAINT CK_veiculos_valor_fipe CHECK (valor_fipe IS NULL OR valor_fipe >= 0),
    CONSTRAINT UQ_veiculos_idempotencia UNIQUE (chave_idempotencia);

CREATE UNIQUE INDEX UX_veiculos_ativo_patrimonial
    ON dbo.veiculos(ativo_patrimonial_id) WHERE ativo_patrimonial_id IS NOT NULL;

ALTER TABLE dbo.abastecimentos ADD
    CONSTRAINT CK_abastecimentos_litros CHECK (litros IS NOT NULL AND litros > 0),
    CONSTRAINT CK_abastecimentos_preco CHECK (preco_por_litro IS NOT NULL AND preco_por_litro > 0),
    CONSTRAINT CK_abastecimentos_valor_total CHECK (valor_total IS NOT NULL AND valor_total >= 0),
    CONSTRAINT CK_abastecimentos_km CHECK (km_no_ato IS NULL OR km_no_ato >= 0),
    CONSTRAINT CK_abastecimentos_horimetro CHECK (horimetro_no_ato IS NULL OR horimetro_no_ato >= 0),
    CONSTRAINT CK_abastecimentos_medidor CHECK (
        (km_no_ato IS NULL AND horimetro_no_ato IS NULL)
        OR (km_no_ato IS NOT NULL AND horimetro_no_ato IS NULL)
        OR (km_no_ato IS NULL AND horimetro_no_ato IS NOT NULL)
    ),
    CONSTRAINT UQ_abastecimentos_idempotencia UNIQUE (chave_idempotencia);

CREATE INDEX IX_abastecimentos_data_id ON dbo.abastecimentos([data] DESC, id DESC);
CREATE INDEX IX_abastecimentos_veiculo_data ON dbo.abastecimentos(veiculo_id, [data] DESC, id DESC);
