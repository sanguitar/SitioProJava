SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;

IF DB_NAME() <> N'$(ExpectedDatabase)'
    THROW 51100, N'Seed DEV recusado: banco conectado nao e o banco local esperado.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.flyway_schema_history
    WHERE version = '13' AND success = 1
)
    THROW 51101, N'Seed DEV recusado: schema nao esta na V13.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.usuarios
    WHERE login = 'sanderson' AND perfil = 'ADMIN' AND ativo = 1
)
    THROW 51102, N'Seed DEV recusado: administrador principal ativo nao encontrado.', 1;

DECLARE @Categorias TABLE (
    nome VARCHAR(100) PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

INSERT INTO @Categorias (nome, descricao) VALUES
    ('Grãos e cereais', 'Alimentos secos e cereais de consumo humano ou animal.'),
    ('Massas e farinhas', 'Massas, farinhas e derivados para a despensa.'),
    ('Mercearia', 'Itens gerais e conservas de mercearia.'),
    ('Temperos', 'Temperos e condimentos culinários.'),
    ('Bebidas', 'Bebidas não alcoólicas.'),
    ('Laticínios e refrigerados', 'Leites e derivados refrigerados.'),
    ('Hortifruti', 'Frutas, verduras e legumes.'),
    ('Higiene pessoal', 'Produtos comuns de higiene pessoal.'),
    ('Limpeza', 'Produtos e materiais de limpeza doméstica.'),
    ('Rações para aves', 'Rações destinadas às diferentes fases das aves.'),
    ('Rações para suínos', 'Rações destinadas às diferentes fases dos suínos.'),
    ('Rações para peixes', 'Rações destinadas à piscicultura.'),
    ('Nutrição animal', 'Farelos e suplementos comuns de nutrição animal.'),
    ('Sementes e mudas', 'Sementes e materiais simples de plantio.'),
    ('Fertilizantes e solo', 'Fertilizantes, corretivos e substratos.'),
    ('Manejo e veterinária', 'Materiais simples e não controlados de manejo.'),
    ('Ferragens', 'Ferragens e materiais gerais de manutenção.'),
    ('Elétrica', 'Materiais elétricos de manutenção.'),
    ('Hidráulica', 'Materiais hidráulicos de manutenção.'),
    ('Lubrificantes', 'Lubrificantes para máquinas e equipamentos.'),
    ('Consumíveis', 'Consumíveis gerais usados na propriedade.'),
    ('Outros', 'Itens que ainda não possuem categoria específica.');

DECLARE @Unidades TABLE (
    nome VARCHAR(80) NOT NULL,
    sigla VARCHAR(20) PRIMARY KEY,
    tipo VARCHAR(40) NOT NULL
);

INSERT INTO @Unidades (nome, sigla, tipo) VALUES
    ('Unidade', 'UN', 'CONTAGEM'),
    ('Quilograma', 'KG', 'MASSA'),
    ('Grama', 'G', 'MASSA'),
    ('Litro', 'L', 'VOLUME'),
    ('Mililitro', 'ML', 'VOLUME'),
    ('Metro', 'M', 'COMPRIMENTO'),
    ('Metro quadrado', 'M2', 'AREA'),
    ('Metro cúbico', 'M3', 'VOLUME'),
    ('Pacote', 'PACOTE', 'CONTAGEM'),
    ('Caixa', 'CX', 'CONTAGEM'),
    ('Saco', 'SACO', 'CONTAGEM'),
    ('Rolo', 'ROLO', 'CONTAGEM');

DECLARE @Locais TABLE (
    nome VARCHAR(100) PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

INSERT INTO @Locais (nome, descricao) VALUES
    ('Despensa da casa', 'Armazenamento de alimentos e itens de uso doméstico.'),
    ('Depósito geral', 'Armazenamento geral da propriedade.'),
    ('Depósito de ração', 'Armazenamento seco e separado para rações.'),
    ('Galpão', 'Materiais e insumos de uso rural.'),
    ('Almoxarifado', 'Ferramentas, ferragens e consumíveis.'),
    ('Geladeira', 'Itens domésticos mantidos refrigerados.'),
    ('Freezer', 'Itens domésticos mantidos congelados.'),
    ('Galinheiro', 'Insumos disponíveis próximos ao galinheiro.'),
    ('Pinteiro', 'Insumos disponíveis próximos ao pinteiro.'),
    ('Piscicultura', 'Insumos de manejo dos tanques.'),
    ('Suínos', 'Insumos de manejo dos suínos.'),
    ('Oficina', 'Lubrificantes e materiais de manutenção.'),
    ('Agricultura', 'Sementes, fertilizantes e materiais agrícolas.');

DECLARE @Itens TABLE (
    nome VARCHAR(140) PRIMARY KEY,
    categoria VARCHAR(100) NOT NULL,
    unidade VARCHAR(20) NOT NULL
);

INSERT INTO @Itens (nome, categoria, unidade) VALUES
    ('Arroz', 'Grãos e cereais', 'KG'),
    ('Feijão carioca', 'Grãos e cereais', 'KG'),
    ('Feijão preto', 'Grãos e cereais', 'KG'),
    ('Milho em grão', 'Grãos e cereais', 'KG'),
    ('Canjica', 'Grãos e cereais', 'KG'),
    ('Lentilha', 'Grãos e cereais', 'KG'),
    ('Ervilha seca', 'Grãos e cereais', 'KG'),
    ('Grão-de-bico', 'Grãos e cereais', 'KG'),
    ('Aveia em flocos', 'Grãos e cereais', 'KG'),
    ('Macarrão espaguete', 'Massas e farinhas', 'KG'),
    ('Macarrão parafuso', 'Massas e farinhas', 'KG'),
    ('Farinha de trigo', 'Massas e farinhas', 'KG'),
    ('Farinha de mandioca', 'Massas e farinhas', 'KG'),
    ('Farinha de milho', 'Massas e farinhas', 'KG'),
    ('Fubá', 'Massas e farinhas', 'KG'),
    ('Tapioca', 'Massas e farinhas', 'KG'),
    ('Amido de milho', 'Massas e farinhas', 'KG'),
    ('Açúcar', 'Mercearia', 'KG'),
    ('Sal', 'Mercearia', 'KG'),
    ('Café', 'Mercearia', 'KG'),
    ('Óleo de soja', 'Mercearia', 'L'),
    ('Azeite', 'Mercearia', 'L'),
    ('Vinagre', 'Mercearia', 'L'),
    ('Molho de tomate', 'Mercearia', 'UN'),
    ('Extrato de tomate', 'Mercearia', 'UN'),
    ('Milho verde em conserva', 'Mercearia', 'UN'),
    ('Ervilha em conserva', 'Mercearia', 'UN'),
    ('Sardinha em conserva', 'Mercearia', 'UN'),
    ('Atum em conserva', 'Mercearia', 'UN'),
    ('Biscoito salgado', 'Mercearia', 'KG'),
    ('Biscoito doce', 'Mercearia', 'KG'),
    ('Fermento químico', 'Mercearia', 'G'),
    ('Achocolatado', 'Mercearia', 'KG'),
    ('Leite condensado', 'Mercearia', 'UN'),
    ('Creme de leite', 'Mercearia', 'UN'),
    ('Maionese', 'Mercearia', 'UN'),
    ('Alho', 'Temperos', 'KG'),
    ('Cebola', 'Temperos', 'KG'),
    ('Colorau', 'Temperos', 'G'),
    ('Pimenta-do-reino', 'Temperos', 'G'),
    ('Cominho', 'Temperos', 'G'),
    ('Orégano', 'Temperos', 'G'),
    ('Cúrcuma', 'Temperos', 'G'),
    ('Louro', 'Temperos', 'G'),
    ('Páprica', 'Temperos', 'G'),
    ('Noz-moscada', 'Temperos', 'G'),
    ('Canela', 'Temperos', 'G'),
    ('Cravo', 'Temperos', 'G'),
    ('Água mineral', 'Bebidas', 'L'),
    ('Suco de frutas', 'Bebidas', 'L'),
    ('Refrigerante', 'Bebidas', 'L'),
    ('Água de coco', 'Bebidas', 'L'),
    ('Chá', 'Bebidas', 'G'),
    ('Polpa de fruta', 'Bebidas', 'KG'),
    ('Leite', 'Laticínios e refrigerados', 'L'),
    ('Leite em pó', 'Laticínios e refrigerados', 'KG'),
    ('Queijo', 'Laticínios e refrigerados', 'KG'),
    ('Manteiga', 'Laticínios e refrigerados', 'KG'),
    ('Margarina', 'Laticínios e refrigerados', 'KG'),
    ('Iogurte', 'Laticínios e refrigerados', 'L'),
    ('Batata', 'Hortifruti', 'KG'),
    ('Cenoura', 'Hortifruti', 'KG'),
    ('Tomate', 'Hortifruti', 'KG'),
    ('Abóbora', 'Hortifruti', 'KG'),
    ('Alface', 'Hortifruti', 'UN'),
    ('Sabonete', 'Higiene pessoal', 'UN'),
    ('Shampoo', 'Higiene pessoal', 'L'),
    ('Condicionador', 'Higiene pessoal', 'L'),
    ('Creme dental', 'Higiene pessoal', 'UN'),
    ('Escova dental', 'Higiene pessoal', 'UN'),
    ('Papel higiênico', 'Higiene pessoal', 'ROLO'),
    ('Desodorante', 'Higiene pessoal', 'UN'),
    ('Absorvente', 'Higiene pessoal', 'PACOTE'),
    ('Algodão', 'Higiene pessoal', 'PACOTE'),
    ('Fio dental', 'Higiene pessoal', 'UN'),
    ('Aparelho de barbear descartável', 'Higiene pessoal', 'UN'),
    ('Cotonete', 'Higiene pessoal', 'PACOTE'),
    ('Detergente', 'Limpeza', 'L'),
    ('Sabão em pó', 'Limpeza', 'KG'),
    ('Sabão em barra', 'Limpeza', 'UN'),
    ('Água sanitária', 'Limpeza', 'L'),
    ('Desinfetante doméstico', 'Limpeza', 'L'),
    ('Amaciante', 'Limpeza', 'L'),
    ('Esponja', 'Limpeza', 'UN'),
    ('Saco de lixo', 'Limpeza', 'PACOTE'),
    ('Papel toalha', 'Limpeza', 'ROLO'),
    ('Álcool de limpeza', 'Limpeza', 'L'),
    ('Limpador multiuso', 'Limpeza', 'L'),
    ('Limpador de vidros', 'Limpeza', 'L'),
    ('Vassoura', 'Limpeza', 'UN'),
    ('Rodo', 'Limpeza', 'UN'),
    ('Pano de limpeza', 'Limpeza', 'UN'),
    ('Escova de limpeza', 'Limpeza', 'UN'),
    ('Ração inicial para pintinhos', 'Rações para aves', 'KG'),
    ('Ração crescimento aves', 'Rações para aves', 'KG'),
    ('Ração postura', 'Rações para aves', 'KG'),
    ('Ração engorda aves', 'Rações para aves', 'KG'),
    ('Ração suínos inicial', 'Rações para suínos', 'KG'),
    ('Ração suínos crescimento', 'Rações para suínos', 'KG'),
    ('Ração suínos terminação', 'Rações para suínos', 'KG'),
    ('Ração para peixes', 'Rações para peixes', 'KG'),
    ('Farelo de soja', 'Nutrição animal', 'KG'),
    ('Farelo de trigo', 'Nutrição animal', 'KG'),
    ('Suplemento mineral', 'Nutrição animal', 'KG'),
    ('Sal mineral', 'Nutrição animal', 'KG'),
    ('Semente de milho', 'Sementes e mudas', 'G'),
    ('Semente de feijão', 'Sementes e mudas', 'G'),
    ('Maniva de mandioca', 'Sementes e mudas', 'UN'),
    ('Semente de arroz', 'Sementes e mudas', 'G'),
    ('Semente de abóbora', 'Sementes e mudas', 'G'),
    ('Semente de melancia', 'Sementes e mudas', 'G'),
    ('Semente de tomate', 'Sementes e mudas', 'G'),
    ('Semente de alface', 'Sementes e mudas', 'G'),
    ('Semente de cebolinha', 'Sementes e mudas', 'G'),
    ('Semente de coentro', 'Sementes e mudas', 'G'),
    ('Semente de pimentão', 'Sementes e mudas', 'G'),
    ('Semente de quiabo', 'Sementes e mudas', 'G'),
    ('Semente de pepino', 'Sementes e mudas', 'G'),
    ('Calcário agrícola', 'Fertilizantes e solo', 'KG'),
    ('Adubo NPK 04-14-08', 'Fertilizantes e solo', 'KG'),
    ('Adubo NPK 10-10-10', 'Fertilizantes e solo', 'KG'),
    ('Adubo NPK 20-05-20', 'Fertilizantes e solo', 'KG'),
    ('Ureia', 'Fertilizantes e solo', 'KG'),
    ('Superfosfato simples', 'Fertilizantes e solo', 'KG'),
    ('Cloreto de potássio', 'Fertilizantes e solo', 'KG'),
    ('Substrato', 'Fertilizantes e solo', 'KG'),
    ('Composto orgânico', 'Fertilizantes e solo', 'KG'),
    ('Cal hidratada', 'Manejo e veterinária', 'KG'),
    ('Desinfetante para instalações', 'Manejo e veterinária', 'L'),
    ('Iodo', 'Manejo e veterinária', 'L'),
    ('Luvas descartáveis', 'Manejo e veterinária', 'PACOTE'),
    ('Seringa descartável', 'Manejo e veterinária', 'UN'),
    ('Agulha descartável', 'Manejo e veterinária', 'UN'),
    ('Óleo lubrificante', 'Lubrificantes', 'L'),
    ('Graxa', 'Lubrificantes', 'KG'),
    ('Fita isolante', 'Elétrica', 'ROLO'),
    ('Fita veda rosca', 'Hidráulica', 'ROLO'),
    ('Parafusos', 'Ferragens', 'UN'),
    ('Pregos', 'Ferragens', 'KG'),
    ('Arame', 'Ferragens', 'M'),
    ('Abraçadeiras', 'Consumíveis', 'UN'),
    ('Lâmpada LED', 'Elétrica', 'UN'),
    ('Mangueira', 'Hidráulica', 'M'),
    ('Conexão hidráulica', 'Hidráulica', 'UN'),
    ('Fusível', 'Elétrica', 'UN');

DECLARE @Fornecedores TABLE (
    nome VARCHAR(140) PRIMARY KEY
);

INSERT INTO @Fornecedores (nome) VALUES
    ('Casa das Rações'),
    ('Agropecuária Central'),
    ('Mercado Central'),
    ('Atacadão Rural'),
    ('Casa do Produtor'),
    ('Ferragens do Campo'),
    ('Distribuidora de Alimentos'),
    ('Hortifruti Local');

BEGIN TRY
    BEGIN TRANSACTION;

    UPDATE destino
       SET descricao = origem.descricao,
           ativa = 1,
           alterado_em = SYSUTCDATETIME(),
           alterado_por = 'dev-seed'
      FROM dbo.estoque_categorias destino
      JOIN @Categorias origem ON origem.nome = destino.nome;

    INSERT INTO dbo.estoque_categorias (nome, descricao, ativa, criado_em, criado_por)
    SELECT origem.nome, origem.descricao, 1, SYSUTCDATETIME(), 'dev-seed'
      FROM @Categorias origem
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.estoque_categorias destino WHERE destino.nome = origem.nome
     );

    UPDATE destino
       SET nome = origem.nome,
           tipo = origem.tipo,
           ativa = 1,
           alterado_em = SYSUTCDATETIME(),
           alterado_por = 'dev-seed'
      FROM dbo.estoque_unidades_medida destino
      JOIN @Unidades origem ON origem.sigla = destino.sigla;

    INSERT INTO dbo.estoque_unidades_medida (nome, sigla, tipo, ativa, criado_em, criado_por)
    SELECT origem.nome, origem.sigla, origem.tipo, 1, SYSUTCDATETIME(), 'dev-seed'
      FROM @Unidades origem
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.estoque_unidades_medida destino WHERE destino.sigla = origem.sigla
     );

    UPDATE destino
       SET descricao = origem.descricao,
           ativo = 1,
           alterado_em = SYSUTCDATETIME(),
           alterado_por = 'dev-seed'
      FROM dbo.estoque_locais destino
      JOIN @Locais origem ON origem.nome = destino.nome;

    INSERT INTO dbo.estoque_locais (nome, descricao, ativo, criado_em, criado_por)
    SELECT origem.nome, origem.descricao, 1, SYSUTCDATETIME(), 'dev-seed'
      FROM @Locais origem
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.estoque_locais destino WHERE destino.nome = origem.nome
     );

    UPDATE destino
       SET descricao = 'Item genérico para demonstração no ambiente DEV.',
           categoria_id = categoria.id,
           unidade_medida_id = unidade.id,
           estoque_minimo = NULL,
           ativo = 1,
           controla_lote = 0,
           controla_validade = 0,
           alterado_em = SYSUTCDATETIME(),
           alterado_por = 'dev-seed'
      FROM dbo.estoque_itens destino
      JOIN @Itens origem ON origem.nome = destino.nome
      JOIN dbo.estoque_categorias categoria ON categoria.nome = origem.categoria
      JOIN dbo.estoque_unidades_medida unidade ON unidade.sigla = origem.unidade;

    INSERT INTO dbo.estoque_itens (
        nome, descricao, categoria_id, unidade_medida_id, estoque_minimo,
        ativo, controla_lote, controla_validade, criado_em, criado_por
    )
    SELECT
        origem.nome,
        'Item genérico para demonstração no ambiente DEV.',
        categoria.id,
        unidade.id,
        NULL,
        1,
        0,
        0,
        SYSUTCDATETIME(),
        'dev-seed'
      FROM @Itens origem
      JOIN dbo.estoque_categorias categoria ON categoria.nome = origem.categoria
      JOIN dbo.estoque_unidades_medida unidade ON unidade.sigla = origem.unidade
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.estoque_itens destino WHERE destino.nome = origem.nome
     );

    UPDATE destino
       SET documento = NULL,
           telefone = NULL,
           email = NULL,
           observacao = 'Fornecedor fictício para testes no ambiente DEV.',
           ativo = 1,
           alterado_em = SYSUTCDATETIME(),
           alterado_por = 'dev-seed'
      FROM dbo.fornecedores destino
      JOIN @Fornecedores origem ON origem.nome = destino.nome;

    INSERT INTO dbo.fornecedores (
        nome, documento, telefone, email, observacao, ativo, criado_em, criado_por
    )
    SELECT
        origem.nome,
        NULL,
        NULL,
        NULL,
        'Fornecedor fictício para testes no ambiente DEV.',
        1,
        SYSUTCDATETIME(),
        'dev-seed'
      FROM @Fornecedores origem
     WHERE NOT EXISTS (
         SELECT 1 FROM dbo.fornecedores destino WHERE destino.nome = origem.nome
     );

    IF EXISTS (
        SELECT origem.nome
          FROM @Categorias origem
          LEFT JOIN dbo.estoque_categorias destino ON destino.nome = origem.nome AND destino.ativa = 1
         WHERE destino.id IS NULL
    )
        THROW 51103, N'Seed DEV abortado: categoria ausente ou inativa.', 1;

    IF EXISTS (
        SELECT origem.sigla
          FROM @Unidades origem
          LEFT JOIN dbo.estoque_unidades_medida destino ON destino.sigla = origem.sigla AND destino.ativa = 1
         WHERE destino.id IS NULL
    )
        THROW 51104, N'Seed DEV abortado: unidade ausente ou inativa.', 1;

    IF EXISTS (
        SELECT origem.nome
          FROM @Itens origem
          LEFT JOIN dbo.estoque_itens destino ON destino.nome = origem.nome AND destino.ativo = 1
         GROUP BY origem.nome
        HAVING COUNT(destino.id) <> 1
    )
        THROW 51105, N'Seed DEV abortado: item ausente, inativo ou duplicado.', 1;

    IF EXISTS (
        SELECT origem.nome
          FROM @Fornecedores origem
          LEFT JOIN dbo.fornecedores destino ON destino.nome = origem.nome AND destino.ativo = 1
         GROUP BY origem.nome
        HAVING COUNT(destino.id) <> 1
    )
        THROW 51106, N'Seed DEV abortado: fornecedor ausente, inativo ou duplicado.', 1;

    IF (
        SELECT COUNT(*)
        FROM dbo.estoque_itens
        WHERE ativo = 1 AND nome IN ('Arroz', 'Milho em grão', 'Ração postura')
    ) <> 3
        THROW 51107, N'Seed DEV abortado: itens essenciais de Compras nao estao ativos.', 1;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT
    (SELECT COUNT(*) FROM @Categorias) AS categorias_seed,
    (SELECT COUNT(*) FROM @Unidades) AS unidades_seed,
    (SELECT COUNT(*) FROM @Locais) AS locais_seed,
    (SELECT COUNT(*) FROM @Itens) AS itens_seed,
    (SELECT COUNT(*) FROM @Fornecedores) AS fornecedores_seed,
    (SELECT COUNT(*) FROM dbo.estoque_movimentos) AS movimentos_iniciais,
    (SELECT COUNT(*) FROM dbo.compras) AS compras_iniciais;
