IF COL_LENGTH('dbo.categorias', 'criado_em') IS NULL
BEGIN
    ALTER TABLE dbo.categorias ADD criado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.categorias', 'criado_por') IS NULL
BEGIN
    ALTER TABLE dbo.categorias ADD criado_por VARCHAR(100) NULL;
END
GO
IF COL_LENGTH('dbo.categorias', 'alterado_em') IS NULL
BEGIN
    ALTER TABLE dbo.categorias ADD alterado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.categorias', 'alterado_por') IS NULL
BEGIN
    ALTER TABLE dbo.categorias ADD alterado_por VARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.producao', 'criado_em') IS NULL
BEGIN
    ALTER TABLE dbo.producao ADD criado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.producao', 'criado_por') IS NULL
BEGIN
    ALTER TABLE dbo.producao ADD criado_por VARCHAR(100) NULL;
END
GO
IF COL_LENGTH('dbo.producao', 'alterado_em') IS NULL
BEGIN
    ALTER TABLE dbo.producao ADD alterado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.producao', 'alterado_por') IS NULL
BEGIN
    ALTER TABLE dbo.producao ADD alterado_por VARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.veiculos', 'criado_em') IS NULL
BEGIN
    ALTER TABLE dbo.veiculos ADD criado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.veiculos', 'criado_por') IS NULL
BEGIN
    ALTER TABLE dbo.veiculos ADD criado_por VARCHAR(100) NULL;
END
GO
IF COL_LENGTH('dbo.veiculos', 'alterado_em') IS NULL
BEGIN
    ALTER TABLE dbo.veiculos ADD alterado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.veiculos', 'alterado_por') IS NULL
BEGIN
    ALTER TABLE dbo.veiculos ADD alterado_por VARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.abastecimentos', 'criado_em') IS NULL
BEGIN
    ALTER TABLE dbo.abastecimentos ADD criado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.abastecimentos', 'criado_por') IS NULL
BEGIN
    ALTER TABLE dbo.abastecimentos ADD criado_por VARCHAR(100) NULL;
END
GO
IF COL_LENGTH('dbo.abastecimentos', 'alterado_em') IS NULL
BEGIN
    ALTER TABLE dbo.abastecimentos ADD alterado_em DATETIME2 NULL;
END
GO
IF COL_LENGTH('dbo.abastecimentos', 'alterado_por') IS NULL
BEGIN
    ALTER TABLE dbo.abastecimentos ADD alterado_por VARCHAR(100) NULL;
END
GO
