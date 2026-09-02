CREATE TABLE dbo.configuracoes_operacionais (
    id INT NOT NULL,
    nome_propriedade VARCHAR(120) NOT NULL,
    timezone VARCHAR(80) NOT NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    dias_padrao_incubacao INT NOT NULL,
    antecedencia_alerta_eclosao_dias INT NOT NULL,
    revisao_localizacao BIGINT NOT NULL CONSTRAINT df_config_operacionais_localizacao DEFAULT 0,
    versao BIGINT NOT NULL CONSTRAINT df_config_operacionais_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT pk_configuracoes_operacionais PRIMARY KEY (id),
    CONSTRAINT ck_config_operacionais_registro_unico CHECK (id = 1),
    CONSTRAINT ck_config_operacionais_nome CHECK (LEN(LTRIM(RTRIM(nome_propriedade))) > 0),
    CONSTRAINT ck_config_operacionais_timezone CHECK (LEN(LTRIM(RTRIM(timezone))) > 0),
    CONSTRAINT ck_config_operacionais_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_config_operacionais_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_config_operacionais_coordenadas CHECK (
        (latitude IS NULL AND longitude IS NULL)
        OR (latitude IS NOT NULL AND longitude IS NOT NULL)
    ),
    CONSTRAINT ck_config_operacionais_incubacao CHECK (dias_padrao_incubacao BETWEEN 1 AND 120),
    CONSTRAINT ck_config_operacionais_alerta_eclosao CHECK (
        antecedencia_alerta_eclosao_dias BETWEEN 0 AND 30
        AND antecedencia_alerta_eclosao_dias <= dias_padrao_incubacao
    )
);
GO
