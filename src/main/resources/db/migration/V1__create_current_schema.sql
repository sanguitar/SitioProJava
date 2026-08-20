CREATE TABLE dbo.categorias (
    id BIGINT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(255) NULL,
    icone VARCHAR(255) NULL,
    cor_hex VARCHAR(255) NULL,
    CONSTRAINT pk_categorias PRIMARY KEY (id)
);
GO

CREATE TABLE dbo.veiculos (
    id BIGINT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    placa VARCHAR(20) NULL,
    tipo VARCHAR(30) NOT NULL,
    marca_fipe VARCHAR(255) NULL,
    modelo_fipe VARCHAR(255) NULL,
    ano_modelo INT NULL,
    km_atual FLOAT(53) NULL,
    situacao VARCHAR(30) NOT NULL,
    valor_fipe FLOAT(53) NULL,
    ultima_consulta_fipe VARCHAR(255) NULL,
    icone VARCHAR(255) NULL,
    CONSTRAINT pk_veiculos PRIMARY KEY (id)
);
GO

CREATE TABLE dbo.fipe_cache (
    id INT NOT NULL,
    tipo INT NULL,
    marca VARCHAR(255) NULL,
    modelo VARCHAR(255) NULL,
    ano_modelo VARCHAR(255) NULL,
    valor FLOAT(53) NULL,
    historico_json NVARCHAR(MAX) NULL,
    CONSTRAINT pk_fipe_cache PRIMARY KEY (id)
);
GO

CREATE TABLE dbo.producao (
    id BIGINT IDENTITY(1,1) NOT NULL,
    item VARCHAR(255) NULL,
    quantidade INT NULL,
    unidade VARCHAR(255) NULL,
    status VARCHAR(255) NULL,
    categoria_id BIGINT NULL,
    CONSTRAINT pk_producao PRIMARY KEY (id),
    CONSTRAINT fk_producao_categorias FOREIGN KEY (categoria_id) REFERENCES dbo.categorias (id)
);
GO

CREATE TABLE dbo.abastecimentos (
    id BIGINT IDENTITY(1,1) NOT NULL,
    veiculo_id BIGINT NOT NULL,
    [data] DATE NOT NULL,
    litros FLOAT(53) NULL,
    preco_por_litro FLOAT(53) NULL,
    valor_total FLOAT(53) NULL,
    km_no_ato FLOAT(53) NULL,
    [local] VARCHAR(255) NOT NULL,
    CONSTRAINT pk_abastecimentos PRIMARY KEY (id),
    CONSTRAINT fk_abastecimentos_veiculos FOREIGN KEY (veiculo_id) REFERENCES dbo.veiculos (id)
);
GO
