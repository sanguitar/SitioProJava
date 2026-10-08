ALTER TABLE dbo.criacao_codigo_sequencias DROP CONSTRAINT CK_criacao_codigo_sequencias_tipo;
ALTER TABLE dbo.criacao_codigo_sequencias ADD CONSTRAINT CK_criacao_codigo_sequencias_tipo
    CHECK (tipo IN ('LOTE_AVES','INCUBACAO_AVES','LOTE_SUINOS','ANIMAL_SUINOS',
        'REPRODUCAO_SUINOS','LOTE_PEIXES','PATRIMONIO_ATIVO'));

CREATE TABLE dbo.patrimonio_ativos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_patrimonio_ativos PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL CONSTRAINT UQ_patrimonio_ativos_codigo UNIQUE,
    nome VARCHAR(160) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    marca VARCHAR(100) NULL,
    modelo VARCHAR(100) NULL,
    numero_serie VARCHAR(120) NULL,
    data_aquisicao DATE NULL,
    valor_aquisicao DECIMAL(19,2) NULL,
    localizacao VARCHAR(160) NULL,
    estrutura_id BIGINT NULL,
    status VARCHAR(30) NOT NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_patrimonio_ativos_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_patrimonio_ativos_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_patrimonio_ativos_estrutura FOREIGN KEY (estrutura_id) REFERENCES dbo.propriedade_estruturas(id),
    CONSTRAINT CK_patrimonio_ativos_tipo CHECK (tipo IN
        ('MAQUINA','EQUIPAMENTO','FERRAMENTA','BOMBA','MOTOR','GERADOR','IMPLEMENTO','ELETRODOMESTICO','OUTRO')),
    CONSTRAINT CK_patrimonio_ativos_status CHECK (status IN ('ATIVO','EM_MANUTENCAO','INATIVO','BAIXADO')),
    CONSTRAINT CK_patrimonio_ativos_valor CHECK (valor_aquisicao IS NULL OR valor_aquisicao >= 0)
);

CREATE INDEX IX_patrimonio_ativos_status_nome ON dbo.patrimonio_ativos(status, nome);
CREATE INDEX IX_patrimonio_ativos_estrutura ON dbo.patrimonio_ativos(estrutura_id) WHERE estrutura_id IS NOT NULL;

CREATE TABLE dbo.manutencao_registros (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_manutencao_registros PRIMARY KEY,
    ativo_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    data_manutencao DATETIME2 NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    responsavel_nome VARCHAR(120) NOT NULL,
    horimetro DECIMAL(19,2) NULL,
    quilometragem DECIMAL(19,2) NULL,
    custo DECIMAL(19,2) NOT NULL CONSTRAINT DF_manutencao_registros_custo DEFAULT 0,
    proxima_manutencao DATETIME2 NULL,
    proxima_manutencao_concluida BIT NOT NULL CONSTRAINT DF_manutencao_proxima_concluida DEFAULT 0,
    proxima_manutencao_concluida_em DATETIME2 NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT UQ_manutencao_registros_idempotencia UNIQUE,
    versao BIGINT NOT NULL CONSTRAINT DF_manutencao_registros_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT FK_manutencao_registros_ativo FOREIGN KEY (ativo_id) REFERENCES dbo.patrimonio_ativos(id),
    CONSTRAINT CK_manutencao_registros_tipo CHECK (tipo IN
        ('PREVENTIVA','CORRETIVA','INSPECAO','LUBRIFICACAO','TROCA_PECA','OUTRO')),
    CONSTRAINT CK_manutencao_registros_horimetro CHECK (horimetro IS NULL OR horimetro >= 0),
    CONSTRAINT CK_manutencao_registros_quilometragem CHECK (quilometragem IS NULL OR quilometragem >= 0),
    CONSTRAINT CK_manutencao_registros_custo CHECK (custo >= 0),
    CONSTRAINT CK_manutencao_registros_proxima CHECK
        (proxima_manutencao IS NULL OR proxima_manutencao >= data_manutencao),
    CONSTRAINT CK_manutencao_registros_conclusao CHECK (
        (proxima_manutencao_concluida = 0 AND proxima_manutencao_concluida_em IS NULL)
        OR (proxima_manutencao_concluida = 1 AND proxima_manutencao_concluida_em IS NOT NULL)
    )
);

CREATE INDEX IX_manutencao_registros_ativo_data
    ON dbo.manutencao_registros(ativo_id, data_manutencao DESC, id DESC);
CREATE INDEX IX_manutencao_registros_proxima
    ON dbo.manutencao_registros(proxima_manutencao_concluida, proxima_manutencao)
    WHERE proxima_manutencao IS NOT NULL;

CREATE TABLE dbo.manutencao_consumos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_manutencao_consumos PRIMARY KEY,
    manutencao_id BIGINT NOT NULL,
    item_estoque_id BIGINT NOT NULL,
    local_estoque_id BIGINT NOT NULL,
    quantidade DECIMAL(19,4) NOT NULL,
    lote_estoque_codigo VARCHAR(100) NULL,
    movimento_estoque_id BIGINT NOT NULL,
    CONSTRAINT FK_manutencao_consumos_manutencao FOREIGN KEY (manutencao_id) REFERENCES dbo.manutencao_registros(id),
    CONSTRAINT FK_manutencao_consumos_item FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens(id),
    CONSTRAINT FK_manutencao_consumos_local FOREIGN KEY (local_estoque_id) REFERENCES dbo.estoque_locais(id),
    CONSTRAINT FK_manutencao_consumos_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT UQ_manutencao_consumos_movimento UNIQUE (movimento_estoque_id),
    CONSTRAINT CK_manutencao_consumos_quantidade CHECK (quantidade > 0)
);

CREATE INDEX IX_manutencao_consumos_manutencao ON dbo.manutencao_consumos(manutencao_id, id);

ALTER TABLE dbo.tarefas DROP CONSTRAINT ck_tarefas_modulo;
ALTER TABLE dbo.tarefas ADD CONSTRAINT ck_tarefas_modulo CHECK (modulo_origem IS NULL OR modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA','MANUTENCAO'));

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_modulo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_modulo CHECK (modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA','MANUTENCAO'));

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_tipo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_tipo CHECK (tipo IN
    ('ESTOQUE_ABAIXO_MINIMO','LOTE_PROXIMO_VENCIMENTO','LOTE_VENCIDO','INTEGRACAO_DESATUALIZADA',
     'INTEGRACAO_COM_FALHA','CHUVA_INTENSA_24H','CRIACAO_MORTALIDADE_ALTA',
     'CRIACAO_INCUBACAO_ECLOSAO_PROXIMA','CRIACAO_INCUBACAO_ATRASADA',
     'CRIACAO_SUINOS_CHECAGEM_PENDENTE','CRIACAO_SUINOS_PARTO_ATRASADO',
     'CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO','AGRICULTURA_OCORRENCIA_FITOSSANITARIA',
     'CRIACAO_PEIXES_QUALIDADE_AGUA','CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE','MANUTENCAO_VENCIDA'));
