IF COL_LENGTH(N'dbo.producao', N'estoque_categoria_id') IS NULL
BEGIN
    ALTER TABLE dbo.producao ADD estoque_categoria_id BIGINT NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = N'fk_producao_estoque_categorias'
      AND parent_object_id = OBJECT_ID(N'dbo.producao')
)
BEGIN
    ALTER TABLE dbo.producao
        ADD CONSTRAINT fk_producao_estoque_categorias
        FOREIGN KEY (estoque_categoria_id) REFERENCES dbo.estoque_categorias (id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_producao_estoque_categoria'
      AND object_id = OBJECT_ID(N'dbo.producao')
)
BEGIN
    CREATE INDEX ix_producao_estoque_categoria ON dbo.producao (estoque_categoria_id);
END
GO
