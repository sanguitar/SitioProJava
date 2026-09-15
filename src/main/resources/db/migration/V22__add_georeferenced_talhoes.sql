ALTER TABLE dbo.propriedade_talhoes ADD area_gis_m2 DECIMAL(18,4) NULL;
GO

ALTER TABLE dbo.propriedade_talhoes ADD CONSTRAINT ck_talhoes_area_gis
    CHECK (area_gis_m2 IS NULL OR area_gis_m2 > 0);
GO

CREATE TABLE dbo.propriedade_talhao_vertices (
    talhao_id BIGINT NOT NULL,
    ordem INT NOT NULL,
    latitude DECIMAL(12,9) NOT NULL,
    longitude DECIMAL(12,9) NOT NULL,
    altitude_geodesica_m DECIMAL(8,2) NULL,
    marco VARCHAR(80) NULL,
    observacao VARCHAR(500) NULL,
    CONSTRAINT pk_propriedade_talhao_vertices PRIMARY KEY (talhao_id, ordem),
    CONSTRAINT fk_talhao_vertices_talhao FOREIGN KEY (talhao_id) REFERENCES dbo.propriedade_talhoes(id) ON DELETE CASCADE,
    CONSTRAINT ck_talhao_vertices_ordem CHECK (ordem BETWEEN 1 AND 500),
    CONSTRAINT ck_talhao_vertices_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_talhao_vertices_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT uq_talhao_vertices_coordenadas UNIQUE (talhao_id, latitude, longitude)
);
GO

CREATE INDEX ix_talhao_vertices_talhao_ordem ON dbo.propriedade_talhao_vertices(talhao_id, ordem);
GO
