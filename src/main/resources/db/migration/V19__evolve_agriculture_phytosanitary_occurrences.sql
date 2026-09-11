ALTER TABLE dbo.agricultura_ocorrencias ADD
    titulo VARCHAR(180) NULL,
    status VARCHAR(25) NOT NULL CONSTRAINT df_agricultura_ocorrencias_status DEFAULT 'ABERTA',
    resolucao VARCHAR(1500) NULL,
    encerrada_em DATETIME2 NULL,
    versao BIGINT NOT NULL CONSTRAINT df_agricultura_ocorrencias_versao DEFAULT 0;
GO

ALTER TABLE dbo.agricultura_ocorrencias DROP CONSTRAINT ck_agricultura_ocorrencias_tipo;
GO

UPDATE dbo.agricultura_ocorrencias
SET titulo = LEFT(descricao, 180),
    tipo = CASE tipo
        WHEN 'CLIMATICA' THEN 'DANO_CLIMATICO'
        WHEN 'PRAGA' THEN 'PRAGA'
        WHEN 'DOENCA' THEN 'DOENCA'
        ELSE 'OUTRO'
    END;
ALTER TABLE dbo.agricultura_ocorrencias ALTER COLUMN titulo VARCHAR(180) NOT NULL;
GO

ALTER TABLE dbo.agricultura_ocorrencias ADD
    CONSTRAINT ck_agricultura_ocorrencias_tipo CHECK (tipo IN
        ('PRAGA','DOENCA','DEFICIENCIA','DANO_CLIMATICO','PLANTA_DANINHA','OUTRO')),
    CONSTRAINT ck_agricultura_ocorrencias_status CHECK (status IN ('ABERTA','EM_ACOMPANHAMENTO','ENCERRADA')),
    CONSTRAINT ck_agricultura_ocorrencias_titulo CHECK (LEN(LTRIM(RTRIM(titulo))) > 0),
    CONSTRAINT ck_agricultura_ocorrencias_encerramento CHECK (
        (status = 'ENCERRADA' AND encerrada_em IS NOT NULL AND resolucao IS NOT NULL
            AND LEN(LTRIM(RTRIM(resolucao))) > 0)
        OR (status <> 'ENCERRADA' AND encerrada_em IS NULL AND resolucao IS NULL));
CREATE INDEX ix_agricultura_ocorrencias_status_severidade
    ON dbo.agricultura_ocorrencias(status, severidade, cultivo_id);
GO

CREATE TABLE dbo.agricultura_ocorrencia_agrofit_referencias (
    ocorrencia_id BIGINT NOT NULL,
    agrofit_cultura_id BIGINT NOT NULL,
    CONSTRAINT pk_agricultura_ocorrencia_agrofit PRIMARY KEY (ocorrencia_id, agrofit_cultura_id),
    CONSTRAINT fk_agricultura_ocorrencia_agrofit_ocorrencia FOREIGN KEY (ocorrencia_id)
        REFERENCES dbo.agricultura_ocorrencias(id),
    CONSTRAINT fk_agricultura_ocorrencia_agrofit_cultura FOREIGN KEY (agrofit_cultura_id)
        REFERENCES dbo.agrofit_culturas(id)
);
CREATE INDEX ix_agricultura_ocorrencia_agrofit_cultura
    ON dbo.agricultura_ocorrencia_agrofit_referencias(agrofit_cultura_id, ocorrencia_id);
GO

CREATE TABLE dbo.agricultura_ocorrencia_historicos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_ocorrencia_historicos PRIMARY KEY,
    ocorrencia_id BIGINT NOT NULL,
    data_hora DATETIME2 NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    severidade VARCHAR(15) NOT NULL,
    status VARCHAR(25) NOT NULL,
    descricao VARCHAR(1500) NOT NULL,
    chave_idempotencia VARCHAR(80) NOT NULL,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_ocorrencia_historicos_ocorrencia FOREIGN KEY (ocorrencia_id)
        REFERENCES dbo.agricultura_ocorrencias(id),
    CONSTRAINT ck_agricultura_ocorrencia_historicos_tipo CHECK (tipo IN ('REGISTRO','ATUALIZACAO','ENCERRAMENTO')),
    CONSTRAINT ck_agricultura_ocorrencia_historicos_severidade CHECK (severidade IN ('BAIXA','MEDIA','ALTA','CRITICA')),
    CONSTRAINT ck_agricultura_ocorrencia_historicos_status CHECK (status IN ('ABERTA','EM_ACOMPANHAMENTO','ENCERRADA')),
    CONSTRAINT ck_agricultura_ocorrencia_historicos_texto CHECK (
        LEN(LTRIM(RTRIM(descricao))) > 0 AND LEN(LTRIM(RTRIM(chave_idempotencia))) >= 8)
);
CREATE INDEX ix_agricultura_ocorrencia_historicos_ocorrencia
    ON dbo.agricultura_ocorrencia_historicos(ocorrencia_id, data_hora, id);
CREATE UNIQUE INDEX ux_agricultura_ocorrencia_historicos_idempotencia
    ON dbo.agricultura_ocorrencia_historicos(ocorrencia_id, chave_idempotencia);
GO

INSERT INTO dbo.agricultura_ocorrencia_historicos
    (ocorrencia_id, data_hora, tipo, severidade, status, descricao, chave_idempotencia,
     criado_em, criado_por, alterado_em, alterado_por)
SELECT id, data_hora, 'REGISTRO', severidade, 'ABERTA', 'Ocorrencia migrada para o historico fitossanitario.',
       CONCAT('MIGRACAO-V19-', id), criado_em, criado_por, alterado_em, alterado_por
FROM dbo.agricultura_ocorrencias;
GO

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_tipo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_tipo CHECK (tipo IN
    ('ESTOQUE_ABAIXO_MINIMO','LOTE_PROXIMO_VENCIMENTO','LOTE_VENCIDO',
     'INTEGRACAO_DESATUALIZADA','INTEGRACAO_COM_FALHA','CHUVA_INTENSA_24H',
     'CRIACAO_MORTALIDADE_ALTA','CRIACAO_INCUBACAO_ECLOSAO_PROXIMA',
     'CRIACAO_INCUBACAO_ATRASADA','AGRICULTURA_OCORRENCIA_FITOSSANITARIA'));
GO
