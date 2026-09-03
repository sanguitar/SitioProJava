CREATE TABLE dbo.propriedades (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedades PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    municipio VARCHAR(120) NULL,
    uf VARCHAR(2) NULL,
    area_total_ha DECIMAL(14,4) NULL,
    latitude_central DECIMAL(9,6) NULL,
    longitude_central DECIMAL(9,6) NULL,
    observacao VARCHAR(1000) NULL,
    ativo BIT NOT NULL CONSTRAINT df_propriedades_ativo DEFAULT 1,
    principal BIT NOT NULL CONSTRAINT df_propriedades_principal DEFAULT 0,
    revisao_localizacao BIGINT NOT NULL CONSTRAINT df_propriedades_localizacao DEFAULT 0,
    versao BIGINT NOT NULL CONSTRAINT df_propriedades_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT ck_propriedades_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ck_propriedades_area CHECK (area_total_ha IS NULL OR area_total_ha > 0),
    CONSTRAINT ck_propriedades_uf CHECK (uf IS NULL OR uf IN
        ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO')),
    CONSTRAINT ck_propriedades_latitude CHECK (latitude_central IS NULL OR latitude_central BETWEEN -90 AND 90),
    CONSTRAINT ck_propriedades_longitude CHECK (longitude_central IS NULL OR longitude_central BETWEEN -180 AND 180),
    CONSTRAINT ck_propriedades_coordenadas CHECK (
        (latitude_central IS NULL AND longitude_central IS NULL)
        OR (latitude_central IS NOT NULL AND longitude_central IS NOT NULL))
);
CREATE UNIQUE INDEX ux_propriedades_principal ON dbo.propriedades(principal) WHERE principal = 1;
GO

-- Preserve the existing physical data before removing the former source of truth.
INSERT INTO dbo.propriedades
    (nome, latitude_central, longitude_central, principal, criado_em, criado_por, alterado_em, alterado_por)
SELECT nome_propriedade, latitude, longitude, 1, criado_em, criado_por, alterado_em, alterado_por
FROM dbo.configuracoes_operacionais;
ALTER TABLE dbo.configuracoes_operacionais ADD propriedade_id BIGINT NULL;
GO
UPDATE c SET propriedade_id = p.id
FROM dbo.configuracoes_operacionais c CROSS JOIN dbo.propriedades p
WHERE p.principal = 1;

IF EXISTS (
    SELECT 1 FROM dbo.configuracoes_operacionais c
    LEFT JOIN dbo.propriedades p ON p.id = c.propriedade_id
    WHERE p.id IS NULL OR p.nome <> c.nome_propriedade
        OR ISNULL(p.latitude_central, 999) <> ISNULL(c.latitude, 999)
        OR ISNULL(p.longitude_central, 999) <> ISNULL(c.longitude, 999)
)
    THROW 51000, 'Physical settings backfill failed; original data preserved.', 1;

ALTER TABLE dbo.configuracoes_operacionais ALTER COLUMN propriedade_id BIGINT NOT NULL;
ALTER TABLE dbo.configuracoes_operacionais ADD CONSTRAINT fk_config_operacionais_propriedade
    FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id);
ALTER TABLE dbo.configuracoes_operacionais DROP CONSTRAINT
    ck_config_operacionais_nome, ck_config_operacionais_latitude,
    ck_config_operacionais_longitude, ck_config_operacionais_coordenadas;
ALTER TABLE dbo.configuracoes_operacionais DROP COLUMN nome_propriedade, latitude, longitude;
GO

CREATE TABLE dbo.propriedade_areas (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_areas PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_propriedade_areas_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    tipo VARCHAR(30) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_propriedade_areas_ativo DEFAULT 1,
    area_ha DECIMAL(14,4) NULL,
    CONSTRAINT fk_propriedade_areas_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ck_propriedade_areas_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ux_propriedade_areas_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ck_propriedade_areas_tipo CHECK (tipo IN ('AGRICOLA','CRIACAO','PASTAGEM','RESIDENCIAL','INFRAESTRUTURA','VEGETACAO','PRESERVACAO','OUTRO')),
    CONSTRAINT ck_propriedade_areas_area CHECK (area_ha IS NULL OR area_ha > 0),
    CONSTRAINT ux_propriedade_areas_propriedade UNIQUE (id, propriedade_id)
);
CREATE INDEX ix_propriedade_areas_propriedade ON dbo.propriedade_areas(propriedade_id, nome);
GO

CREATE TABLE dbo.propriedade_talhoes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_talhoes PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_propriedade_talhoes_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    area_id BIGINT NULL,
    codigo AS ('TL-' + CASE WHEN id < 10000 THEN RIGHT('0000' + CONVERT(VARCHAR(20), id), 4) ELSE CONVERT(VARCHAR(20), id) END) PERSISTED,
    status VARCHAR(20) NOT NULL,
    area_ha DECIMAL(14,4) NOT NULL,
    CONSTRAINT fk_propriedade_talhoes_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ck_propriedade_talhoes_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ux_propriedade_talhoes_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ck_propriedade_talhoes_status CHECK (status IN ('ATIVO','INATIVO')),
    CONSTRAINT ck_propriedade_talhoes_area CHECK (area_ha IS NULL OR area_ha > 0),
    CONSTRAINT fk_propriedade_talhoes_area FOREIGN KEY (area_id, propriedade_id) REFERENCES dbo.propriedade_areas(id, propriedade_id)
);
CREATE INDEX ix_propriedade_talhoes_propriedade ON dbo.propriedade_talhoes(propriedade_id, nome);
CREATE INDEX ix_propriedade_talhoes_area ON dbo.propriedade_talhoes(area_id, propriedade_id);
CREATE UNIQUE INDEX ux_propriedade_talhoes_codigo ON dbo.propriedade_talhoes(codigo);
GO

CREATE TABLE dbo.propriedade_piquetes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_piquetes PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_propriedade_piquetes_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    area_id BIGINT NULL,
    codigo AS ('PQ-' + CASE WHEN id < 10000 THEN RIGHT('0000' + CONVERT(VARCHAR(20), id), 4) ELSE CONVERT(VARCHAR(20), id) END) PERSISTED,
    status VARCHAR(20) NOT NULL,
    area_ha DECIMAL(14,4) NULL,
    CONSTRAINT fk_propriedade_piquetes_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ck_propriedade_piquetes_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ux_propriedade_piquetes_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ck_propriedade_piquetes_status CHECK (status IN ('ATIVO','INATIVO')),
    CONSTRAINT ck_propriedade_piquetes_area CHECK (area_ha IS NULL OR area_ha > 0),
    CONSTRAINT fk_propriedade_piquetes_area FOREIGN KEY (area_id, propriedade_id) REFERENCES dbo.propriedade_areas(id, propriedade_id)
);
CREATE INDEX ix_propriedade_piquetes_propriedade ON dbo.propriedade_piquetes(propriedade_id, nome);
CREATE INDEX ix_propriedade_piquetes_area ON dbo.propriedade_piquetes(area_id, propriedade_id);
CREATE UNIQUE INDEX ux_propriedade_piquetes_codigo ON dbo.propriedade_piquetes(codigo);
GO

CREATE TABLE dbo.propriedade_estruturas (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_estruturas PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_propriedade_estruturas_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    area_id BIGINT NULL,
    tipo VARCHAR(30) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_propriedade_estruturas_ativo DEFAULT 1,
    capacidade DECIMAL(18,3) NULL,
    unidade_capacidade VARCHAR(30) NULL,
    CONSTRAINT fk_propriedade_estruturas_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ck_propriedade_estruturas_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ux_propriedade_estruturas_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ck_propriedade_estruturas_tipo CHECK (tipo IN ('CASA','GALINHEIRO','CHOCADEIRA','GALPAO','DEPOSITO','ALMOXARIFADO','CURRAL','CHIQUEIRO','TANQUE_PEIXES','ESTUFA','OFICINA','OUTRO')),
    CONSTRAINT ck_propriedade_estruturas_capacidade CHECK (
        (capacidade IS NULL AND unidade_capacidade IS NULL)
        OR (capacidade IS NOT NULL AND capacidade > 0 AND unidade_capacidade IS NOT NULL AND LEN(LTRIM(RTRIM(unidade_capacidade))) > 0)),
    CONSTRAINT fk_propriedade_estruturas_area FOREIGN KEY (area_id, propriedade_id) REFERENCES dbo.propriedade_areas(id, propriedade_id)
);
CREATE INDEX ix_propriedade_estruturas_propriedade ON dbo.propriedade_estruturas(propriedade_id, nome);
CREATE INDEX ix_propriedade_estruturas_area ON dbo.propriedade_estruturas(area_id, propriedade_id);
GO

CREATE TABLE dbo.propriedade_recursos_hidricos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_recursos_hidricos PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_propriedade_recursos_hidricos_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    area_id BIGINT NULL,
    tipo VARCHAR(30) NOT NULL,
    ativo BIT NOT NULL CONSTRAINT df_propriedade_recursos_hidricos_ativo DEFAULT 1,
    capacidade_litros DECIMAL(18,3) NULL,
    CONSTRAINT fk_propriedade_recursos_hidricos_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ck_propriedade_recursos_hidricos_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ux_propriedade_recursos_hidricos_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ck_propriedade_recursos_hidricos_tipo CHECK (tipo IN ('POCO','ACUDE','NASCENTE','CAIXA_DAGUA','CISTERNA','RESERVATORIO','TANQUE','OUTRO')),
    CONSTRAINT ck_propriedade_recursos_hidricos_capacidade CHECK (capacidade_litros IS NULL OR capacidade_litros > 0),
    CONSTRAINT fk_propriedade_recursos_hidricos_area FOREIGN KEY (area_id, propriedade_id) REFERENCES dbo.propriedade_areas(id, propriedade_id)
);
CREATE INDEX ix_propriedade_recursos_hidricos_propriedade ON dbo.propriedade_recursos_hidricos(propriedade_id, nome);
CREATE INDEX ix_propriedade_recursos_hidricos_area ON dbo.propriedade_recursos_hidricos(area_id, propriedade_id);
GO

ALTER TABLE dbo.criacao_instalacoes ADD estrutura_id BIGINT NULL;
ALTER TABLE dbo.criacao_instalacoes ADD CONSTRAINT fk_criacao_instalacoes_estrutura
    FOREIGN KEY (estrutura_id) REFERENCES dbo.propriedade_estruturas(id);
CREATE INDEX ix_criacao_instalacoes_estrutura ON dbo.criacao_instalacoes(estrutura_id);
GO
