CREATE TABLE dbo.peixes_qualidade_agua_configuracao (
    id INT NOT NULL CONSTRAINT PK_peixes_qualidade_agua_configuracao PRIMARY KEY,
    temperatura_min DECIMAL(8,4) NOT NULL,
    temperatura_max DECIMAL(8,4) NOT NULL,
    ph_min DECIMAL(8,4) NOT NULL,
    ph_max DECIMAL(8,4) NOT NULL,
    oxigenio_min DECIMAL(8,4) NOT NULL,
    transparencia_min_cm DECIMAL(10,4) NOT NULL,
    amonia_max DECIMAL(8,4) NOT NULL,
    nitrito_max DECIMAL(8,4) NOT NULL,
    intervalo_medicao_dias INT NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT DF_peixes_qualidade_config_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT CK_peixes_qualidade_config_id CHECK (id = 1),
    CONSTRAINT CK_peixes_qualidade_config_temperatura CHECK (temperatura_min < temperatura_max),
    CONSTRAINT CK_peixes_qualidade_config_ph CHECK (ph_min >= 0 AND ph_max <= 14 AND ph_min < ph_max),
    CONSTRAINT CK_peixes_qualidade_config_limites CHECK (oxigenio_min >= 0 AND transparencia_min_cm >= 0 AND amonia_max >= 0 AND nitrito_max >= 0),
    CONSTRAINT CK_peixes_qualidade_config_intervalo CHECK (intervalo_medicao_dias BETWEEN 1 AND 365)
);

INSERT INTO dbo.peixes_qualidade_agua_configuracao
    (id, temperatura_min, temperatura_max, ph_min, ph_max, oxigenio_min,
     transparencia_min_cm, amonia_max, nitrito_max, intervalo_medicao_dias, versao)
VALUES (1, 24.0000, 32.0000, 6.5000, 8.5000, 5.0000, 30.0000, 0.5000, 0.2000, 7, 0);

CREATE TABLE dbo.peixes_medicoes_qualidade_agua (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_peixes_medicoes_qualidade_agua PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    instalacao_id BIGINT NOT NULL,
    medido_em DATETIME2 NOT NULL,
    temperatura DECIMAL(8,4) NOT NULL,
    ph DECIMAL(8,4) NOT NULL,
    oxigenio_dissolvido DECIMAL(8,4) NOT NULL,
    transparencia_cm DECIMAL(10,4) NULL,
    amonia DECIMAL(8,4) NULL,
    nitrito DECIMAL(8,4) NULL,
    responsavel VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT DF_peixes_medicoes_qualidade_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_peixes_medicoes_qualidade_lote FOREIGN KEY (lote_id) REFERENCES dbo.peixes_lotes(id),
    CONSTRAINT FK_peixes_medicoes_qualidade_instalacao FOREIGN KEY (instalacao_id) REFERENCES dbo.criacao_instalacoes(id),
    CONSTRAINT UQ_peixes_medicoes_qualidade_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT CK_peixes_medicoes_qualidade_temperatura CHECK (temperatura BETWEEN -5 AND 50),
    CONSTRAINT CK_peixes_medicoes_qualidade_ph CHECK (ph BETWEEN 0 AND 14),
    CONSTRAINT CK_peixes_medicoes_qualidade_valores CHECK (oxigenio_dissolvido >= 0 AND (transparencia_cm IS NULL OR transparencia_cm >= 0) AND (amonia IS NULL OR amonia >= 0) AND (nitrito IS NULL OR nitrito >= 0))
);

CREATE INDEX IX_peixes_medicoes_qualidade_lote_data
    ON dbo.peixes_medicoes_qualidade_agua(lote_id, medido_em DESC, id DESC);
CREATE INDEX IX_peixes_medicoes_qualidade_instalacao_data
    ON dbo.peixes_medicoes_qualidade_agua(instalacao_id, medido_em DESC, id DESC);

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_tipo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_tipo CHECK (tipo IN
    ('ESTOQUE_ABAIXO_MINIMO','LOTE_PROXIMO_VENCIMENTO','LOTE_VENCIDO','INTEGRACAO_DESATUALIZADA',
     'INTEGRACAO_COM_FALHA','CHUVA_INTENSA_24H','CRIACAO_MORTALIDADE_ALTA',
     'CRIACAO_INCUBACAO_ECLOSAO_PROXIMA','CRIACAO_INCUBACAO_ATRASADA',
     'CRIACAO_SUINOS_CHECAGEM_PENDENTE','CRIACAO_SUINOS_PARTO_ATRASADO',
     'CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO','AGRICULTURA_OCORRENCIA_FITOSSANITARIA',
     'CRIACAO_PEIXES_QUALIDADE_AGUA'));
