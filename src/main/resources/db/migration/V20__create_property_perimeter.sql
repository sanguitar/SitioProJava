CREATE TABLE dbo.propriedade_perimetros (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_propriedade_perimetros PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    status_crs VARCHAR(20) NOT NULL CONSTRAINT df_perimetro_crs DEFAULT 'NAO_CONFIRMADO',
    crs VARCHAR(120) NULL,
    datum VARCHAR(120) NULL,
    observacao VARCHAR(1000) NULL,
    versao BIGINT NOT NULL CONSTRAINT df_perimetro_versao DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_perimetro_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT uq_perimetro_propriedade UNIQUE (propriedade_id),
    CONSTRAINT ck_perimetro_status_crs CHECK (status_crs IN ('NAO_CONFIRMADO','CONFIRMADO')),
    CONSTRAINT ck_perimetro_crs_confirmado CHECK (status_crs <> 'CONFIRMADO' OR (crs IS NOT NULL AND LEN(LTRIM(RTRIM(crs))) > 0))
);

CREATE TABLE dbo.propriedade_perimetro_vertices (
    perimetro_id BIGINT NOT NULL,
    ordem INT NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    marco VARCHAR(120) NULL,
    observacao VARCHAR(1000) NULL,
    CONSTRAINT pk_perimetro_vertices PRIMARY KEY (perimetro_id, ordem),
    CONSTRAINT fk_vertice_perimetro FOREIGN KEY (perimetro_id) REFERENCES dbo.propriedade_perimetros(id),
    CONSTRAINT uq_perimetro_coordenadas UNIQUE (perimetro_id, latitude, longitude),
    CONSTRAINT ck_vertice_ordem CHECK (ordem BETWEEN 1 AND 500),
    CONSTRAINT ck_vertice_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_vertice_longitude CHECK (longitude BETWEEN -180 AND 180)
);
