ALTER TABLE dbo.previsoes_climaticas
    DROP CONSTRAINT ck_previsoes_climaticas_percentuais;
GO

ALTER TABLE dbo.previsoes_climaticas
    ALTER COLUMN umidade_relativa INT NULL;
GO

ALTER TABLE dbo.previsoes_climaticas
    ALTER COLUMN probabilidade_precipitacao INT NULL;
GO

ALTER TABLE dbo.previsoes_climaticas
    ALTER COLUMN codigo_tempo INT NULL;
GO

ALTER TABLE dbo.previsoes_climaticas
    ADD CONSTRAINT ck_previsoes_climaticas_percentuais CHECK (
        (umidade_relativa IS NULL OR umidade_relativa BETWEEN 0 AND 100)
        AND (probabilidade_precipitacao IS NULL OR probabilidade_precipitacao BETWEEN 0 AND 100)
    );
GO
