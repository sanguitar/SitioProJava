ALTER TABLE dbo.propriedade_perimetro_vertices DROP CONSTRAINT uq_perimetro_coordenadas;
ALTER TABLE dbo.propriedade_perimetro_vertices ALTER COLUMN latitude DECIMAL(12,9) NOT NULL;
ALTER TABLE dbo.propriedade_perimetro_vertices ALTER COLUMN longitude DECIMAL(12,9) NOT NULL;
ALTER TABLE dbo.propriedade_perimetro_vertices ADD altitude_geodesica_m DECIMAL(8,2) NULL;
ALTER TABLE dbo.propriedade_perimetro_vertices ADD CONSTRAINT uq_perimetro_coordenadas UNIQUE (perimetro_id, latitude, longitude);
GO

ALTER TABLE dbo.propriedade_perimetros ADD
    sistema_geodesico VARCHAR(80) NULL,
    crs_epsg INT NULL,
    area_documental_ha DECIMAL(14,4) NULL,
    perimetro_documental_m DECIMAL(14,2) NULL,
    area_calculada_m2 DECIMAL(18,4) NULL,
    perimetro_calculado_m DECIMAL(18,4) NULL,
    poligono_geography geography NULL;
GO

ALTER TABLE dbo.propriedade_perimetros ADD CONSTRAINT ck_perimetro_crs_epsg
    CHECK (crs_epsg IS NULL OR crs_epsg > 0);
ALTER TABLE dbo.propriedade_perimetros ADD CONSTRAINT ck_perimetro_area_documental
    CHECK (area_documental_ha IS NULL OR area_documental_ha > 0);
ALTER TABLE dbo.propriedade_perimetros ADD CONSTRAINT ck_perimetro_documental_m
    CHECK (perimetro_documental_m IS NULL OR perimetro_documental_m > 0);
ALTER TABLE dbo.propriedade_perimetros ADD CONSTRAINT ck_perimetro_calculos_conferencia
    CHECK ((area_calculada_m2 IS NULL OR area_calculada_m2 > 0)
        AND (perimetro_calculado_m IS NULL OR perimetro_calculado_m > 0));
GO

ALTER TABLE dbo.propriedade_talhoes ADD
    geometria_status_crs VARCHAR(20) NOT NULL CONSTRAINT df_talhoes_geometria_status_crs DEFAULT 'NAO_CONFIRMADO',
    geometria_crs_epsg INT NULL,
    geometria_geography geography NULL;
GO

ALTER TABLE dbo.propriedade_talhoes ADD CONSTRAINT ck_talhoes_geometria_status_crs
    CHECK (geometria_status_crs IN ('NAO_CONFIRMADO','CONFIRMADO'));
ALTER TABLE dbo.propriedade_talhoes ADD CONSTRAINT ck_talhoes_geometria_crs_epsg
    CHECK (geometria_crs_epsg IS NULL OR geometria_crs_epsg > 0);
GO

DECLARE @propriedade_id BIGINT = (SELECT TOP (1) id FROM dbo.propriedades WHERE principal = 1 ORDER BY id);
IF @propriedade_id IS NULL
BEGIN
    INSERT INTO dbo.propriedades (nome, principal, criado_em, criado_por, alterado_em, alterado_por)
    VALUES ('Sítio Guaratinguetá', 1, SYSUTCDATETIME(), 'flyway-v21', SYSUTCDATETIME(), 'flyway-v21');
    SET @propriedade_id = SCOPE_IDENTITY();
END;

DECLARE @perimetro TABLE (id BIGINT);
MERGE dbo.propriedade_perimetros AS destino
USING (SELECT @propriedade_id AS propriedade_id) AS origem
    ON destino.propriedade_id = origem.propriedade_id
WHEN MATCHED THEN UPDATE SET
    status_crs = 'CONFIRMADO',
    crs = 'EPSG:4674',
    datum = 'SIRGAS 2000',
    sistema_geodesico = 'SIRGAS 2000',
    crs_epsg = 4674,
    area_documental_ha = 1.8955,
    perimetro_documental_m = 919.71,
    observacao = 'CRS confirmado documentalmente. Area e perimetro documentais preservados separadamente dos calculos GIS.',
    alterado_em = SYSUTCDATETIME(),
    alterado_por = 'flyway-v21'
WHEN NOT MATCHED THEN INSERT
    (propriedade_id, status_crs, crs, datum, sistema_geodesico, crs_epsg,
     area_documental_ha, perimetro_documental_m, observacao, criado_em, criado_por, alterado_em, alterado_por)
    VALUES
    (@propriedade_id, 'CONFIRMADO', 'EPSG:4674', 'SIRGAS 2000', 'SIRGAS 2000', 4674,
     1.8955, 919.71,
     'CRS confirmado documentalmente. Area e perimetro documentais preservados separadamente dos calculos GIS.',
     SYSUTCDATETIME(), 'flyway-v21', SYSUTCDATETIME(), 'flyway-v21')
OUTPUT inserted.id INTO @perimetro;

DECLARE @perimetro_id BIGINT = (SELECT TOP (1) id FROM @perimetro);
DELETE FROM dbo.propriedade_perimetro_vertices WHERE perimetro_id = @perimetro_id;

INSERT INTO dbo.propriedade_perimetro_vertices
    (perimetro_id, ordem, latitude, longitude, altitude_geodesica_m, marco, observacao)
VALUES
    (@perimetro_id, 1, -8.346821111, -63.871070000, 90.30, 'DZCZ-M-0205', 'Vertice oficial do memorial.'),
    (@perimetro_id, 2, -8.350538889, -63.871139722, 89.88, 'DZCZ-M-0168', 'Vertice oficial do memorial.'),
    (@perimetro_id, 3, -8.350611389, -63.871590833, 88.97, 'DZCZ-M-0167', 'Vertice oficial do memorial.'),
    (@perimetro_id, 4, -8.346856389, -63.871454722, 90.28, 'DZCZ-M-0170', 'Vertice oficial do memorial.');

DECLARE @wkt NVARCHAR(MAX) = 'POLYGON((-63.871070000 -8.346821111 90.30, -63.871139722 -8.350538889 89.88, -63.871590833 -8.350611389 88.97, -63.871454722 -8.346856389 90.28, -63.871070000 -8.346821111 90.30))';
DECLARE @geografia geography = geography::STGeomFromText(@wkt, 4674);
IF @geografia.STIsValid() <> 1
    THROW 51022, 'Poligono SIRGAS 2000 invalido.', 1;

DECLARE @geografia_operacional geography = @geografia.ReorientObject();
IF @geografia_operacional.STIsValid() <> 1
    THROW 51023, 'Poligono SIRGAS 2000 reorientado invalido.', 1;

UPDATE dbo.propriedade_perimetros
SET poligono_geography = @geografia_operacional,
    area_calculada_m2 = ROUND(@geografia_operacional.STArea(), 4),
    perimetro_calculado_m = ROUND(@geografia_operacional.STLength(), 4)
WHERE id = @perimetro_id;
GO
