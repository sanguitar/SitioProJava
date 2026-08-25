IF OBJECT_ID(N'dbo.criacao_codigo_sequencias', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.criacao_codigo_sequencias (
        tipo VARCHAR(30) NOT NULL,
        ano SMALLINT NOT NULL,
        ultimo_valor BIGINT NOT NULL,
        CONSTRAINT pk_criacao_codigo_sequencias PRIMARY KEY (tipo, ano),
        CONSTRAINT ck_criacao_codigo_sequencias_tipo
            CHECK (tipo IN ('LOTE_AVES', 'INCUBACAO_AVES')),
        CONSTRAINT ck_criacao_codigo_sequencias_ano CHECK (ano BETWEEN 2000 AND 9999),
        CONSTRAINT ck_criacao_codigo_sequencias_valor CHECK (ultimo_valor > 0)
    );
END
GO

;WITH codigos_existentes AS (
    SELECT
        'LOTE_AVES' AS tipo,
        TRY_CONVERT(SMALLINT, SUBSTRING(codigo, 4, 4)) AS ano,
        TRY_CONVERT(BIGINT, SUBSTRING(codigo, 9, 30)) AS valor
    FROM dbo.aves_lotes
    WHERE codigo LIKE 'AV-[0-9][0-9][0-9][0-9]-%'

    UNION ALL

    SELECT
        'INCUBACAO_AVES' AS tipo,
        TRY_CONVERT(SMALLINT, SUBSTRING(codigo, 5, 4)) AS ano,
        TRY_CONVERT(BIGINT, SUBSTRING(codigo, 10, 30)) AS valor
    FROM dbo.aves_incubacoes
    WHERE codigo LIKE 'INC-[0-9][0-9][0-9][0-9]-%'
), maiores_valores AS (
    SELECT tipo, ano, MAX(valor) AS ultimo_valor
    FROM codigos_existentes
    WHERE ano IS NOT NULL AND valor IS NOT NULL AND valor > 0
    GROUP BY tipo, ano
)
INSERT INTO dbo.criacao_codigo_sequencias (tipo, ano, ultimo_valor)
SELECT origem.tipo, origem.ano, origem.ultimo_valor
FROM maiores_valores origem
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.criacao_codigo_sequencias atual
    WHERE atual.tipo = origem.tipo AND atual.ano = origem.ano
);
GO
