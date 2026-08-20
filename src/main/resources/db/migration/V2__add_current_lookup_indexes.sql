IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_producao_categoria_id'
      AND object_id = OBJECT_ID(N'dbo.producao')
)
BEGIN
    CREATE INDEX ix_producao_categoria_id ON dbo.producao (categoria_id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_abastecimentos_veiculo_id'
      AND object_id = OBJECT_ID(N'dbo.abastecimentos')
)
BEGIN
    CREATE INDEX ix_abastecimentos_veiculo_id ON dbo.abastecimentos (veiculo_id);
END
GO
