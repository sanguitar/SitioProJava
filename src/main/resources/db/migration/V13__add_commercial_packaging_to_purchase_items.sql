IF COL_LENGTH(N'dbo.itens_compra', N'quantidade_volumes') IS NULL
BEGIN
    ALTER TABLE dbo.itens_compra
        ADD quantidade_volumes DECIMAL(19,4) NULL,
            tipo_embalagem VARCHAR(20) NULL,
            conteudo_por_volume DECIMAL(19,4) NULL,
            preco_por_volume DECIMAL(19,4) NULL,
            unidade_base VARCHAR(20) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = N'ck_itens_compra_apresentacao_comercial'
      AND parent_object_id = OBJECT_ID(N'dbo.itens_compra')
)
BEGIN
    ALTER TABLE dbo.itens_compra
        ADD CONSTRAINT ck_itens_compra_apresentacao_comercial CHECK (
            (
                quantidade_volumes IS NULL
                AND tipo_embalagem IS NULL
                AND conteudo_por_volume IS NULL
                AND preco_por_volume IS NULL
                AND unidade_base IS NULL
            )
            OR
            (
                quantidade_volumes > 0
                AND tipo_embalagem IN ('UNIDADE', 'PACOTE', 'SACO', 'CAIXA', 'FARDO', 'BALDE', 'GALAO', 'ROLO', 'OUTRO')
                AND conteudo_por_volume > 0
                AND preco_por_volume >= 0
                AND LEN(LTRIM(RTRIM(unidade_base))) > 0
            )
        );
END
GO
