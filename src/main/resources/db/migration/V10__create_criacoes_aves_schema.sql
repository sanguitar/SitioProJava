IF OBJECT_ID(N'dbo.criacao_instalacoes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.criacao_instalacoes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nome VARCHAR(120) NOT NULL,
        tipo VARCHAR(40) NOT NULL,
        descricao VARCHAR(500) NULL,
        capacidade INT NULL,
        ativo BIT NOT NULL CONSTRAINT df_criacao_instalacoes_ativo DEFAULT 1,
        versao BIGINT NOT NULL CONSTRAINT df_criacao_instalacoes_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_criacao_instalacoes PRIMARY KEY (id),
        CONSTRAINT uk_criacao_instalacoes_nome UNIQUE (nome),
        CONSTRAINT ck_criacao_instalacoes_tipo CHECK (tipo IN ('INCUBADORA', 'CRIADOURO_PINTINHOS', 'GALINHEIRO', 'PIQUETE', 'OUTRO')),
        CONSTRAINT ck_criacao_instalacoes_capacidade CHECK (capacidade IS NULL OR capacidade > 0)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_lotes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_lotes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        codigo VARCHAR(80) NOT NULL,
        nome VARCHAR(120) NULL,
        especie VARCHAR(30) NOT NULL,
        finalidade VARCHAR(30) NOT NULL,
        linhagem VARCHAR(120) NULL,
        origem VARCHAR(200) NOT NULL,
        data_entrada DATE NOT NULL,
        data_nascimento DATE NULL,
        quantidade_inicial INT NOT NULL,
        quantidade_atual INT NOT NULL,
        sexo VARCHAR(30) NOT NULL,
        instalacao_atual_id BIGINT NOT NULL,
        status VARCHAR(30) NOT NULL,
        observacoes VARCHAR(1000) NULL,
        custo_inicial DECIMAL(19,4) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        versao BIGINT NOT NULL CONSTRAINT df_aves_lotes_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_lotes PRIMARY KEY (id),
        CONSTRAINT uk_aves_lotes_codigo UNIQUE (codigo),
        CONSTRAINT uk_aves_lotes_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT fk_aves_lotes_instalacao FOREIGN KEY (instalacao_atual_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT ck_aves_lotes_especie CHECK (especie IN ('GALINHA', 'CODORNA', 'PATO', 'PERU', 'GANSO', 'OUTRA')),
        CONSTRAINT ck_aves_lotes_finalidade CHECK (finalidade IN ('POSTURA', 'CORTE', 'REPRODUCAO', 'MISTA')),
        CONSTRAINT ck_aves_lotes_sexo CHECK (sexo IN ('FEMEAS', 'MACHOS', 'MISTO', 'NAO_DEFINIDO')),
        CONSTRAINT ck_aves_lotes_status CHECK (status IN ('ATIVO', 'ENCERRADO', 'VENDIDO', 'ABATIDO')),
        CONSTRAINT ck_aves_lotes_quantidades CHECK (quantidade_inicial > 0 AND quantidade_atual >= 0 AND quantidade_atual <= quantidade_inicial),
        CONSTRAINT ck_aves_lotes_custo CHECK (custo_inicial IS NULL OR custo_inicial >= 0)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_eventos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_eventos (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        tipo VARCHAR(40) NOT NULL,
        quantidade INT NULL,
        data_evento DATETIME2 NOT NULL,
        origem VARCHAR(80) NOT NULL,
        usuario VARCHAR(100) NOT NULL,
        observacao VARCHAR(1000) NULL,
        referencia_externa VARCHAR(160) NULL,
        instalacao_origem_id BIGINT NULL,
        instalacao_destino_id BIGINT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_eventos PRIMARY KEY (id),
        CONSTRAINT fk_aves_eventos_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_eventos_instalacao_origem FOREIGN KEY (instalacao_origem_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT fk_aves_eventos_instalacao_destino FOREIGN KEY (instalacao_destino_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT ck_aves_eventos_tipo CHECK (tipo IN ('ENTRADA_INICIAL', 'ECLOSAO', 'MORTALIDADE', 'DESCARTE', 'VENDA', 'ABATE', 'TRANSFERENCIA', 'AJUSTE_ADMINISTRATIVO', 'ALIMENTACAO', 'PESAGEM', 'POSTURA', 'ENCERRAMENTO')),
        CONSTRAINT ck_aves_eventos_quantidade CHECK (quantidade IS NULL OR quantidade >= 0)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_mortalidades', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_mortalidades (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        quantidade INT NOT NULL,
        data_evento DATETIME2 NOT NULL,
        causa VARCHAR(200) NULL,
        observacao VARCHAR(1000) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        evento_id BIGINT NOT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_mortalidades PRIMARY KEY (id),
        CONSTRAINT uk_aves_mortalidades_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT uk_aves_mortalidades_evento UNIQUE (evento_id),
        CONSTRAINT fk_aves_mortalidades_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_mortalidades_evento FOREIGN KEY (evento_id) REFERENCES dbo.aves_eventos (id),
        CONSTRAINT ck_aves_mortalidades_quantidade CHECK (quantidade > 0)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_alimentacoes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_alimentacoes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        item_estoque_id BIGINT NOT NULL,
        local_estoque_id BIGINT NOT NULL,
        quantidade DECIMAL(19,4) NOT NULL,
        data_evento DATETIME2 NOT NULL,
        observacao VARCHAR(1000) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        movimento_estoque_id BIGINT NULL,
        custo_unitario_referencia DECIMAL(19,4) NULL,
        evento_id BIGINT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_alimentacoes PRIMARY KEY (id),
        CONSTRAINT uk_aves_alimentacoes_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT fk_aves_alimentacoes_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_alimentacoes_item FOREIGN KEY (item_estoque_id) REFERENCES dbo.estoque_itens (id),
        CONSTRAINT fk_aves_alimentacoes_local FOREIGN KEY (local_estoque_id) REFERENCES dbo.estoque_locais (id),
        CONSTRAINT fk_aves_alimentacoes_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos (id),
        CONSTRAINT fk_aves_alimentacoes_evento FOREIGN KEY (evento_id) REFERENCES dbo.aves_eventos (id),
        CONSTRAINT ck_aves_alimentacoes_quantidade CHECK (quantidade > 0),
        CONSTRAINT ck_aves_alimentacoes_custo CHECK (custo_unitario_referencia IS NULL OR custo_unitario_referencia >= 0)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'uk_aves_alimentacoes_movimento' AND object_id = OBJECT_ID(N'dbo.aves_alimentacoes'))
BEGIN
    CREATE UNIQUE INDEX uk_aves_alimentacoes_movimento ON dbo.aves_alimentacoes (movimento_estoque_id) WHERE movimento_estoque_id IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'uk_aves_alimentacoes_evento' AND object_id = OBJECT_ID(N'dbo.aves_alimentacoes'))
BEGIN
    CREATE UNIQUE INDEX uk_aves_alimentacoes_evento ON dbo.aves_alimentacoes (evento_id) WHERE evento_id IS NOT NULL;
END
GO

IF OBJECT_ID(N'dbo.aves_pesagens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_pesagens (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        data_evento DATETIME2 NOT NULL,
        quantidade_amostrada INT NULL,
        peso_medio DECIMAL(19,4) NOT NULL,
        peso_minimo DECIMAL(19,4) NULL,
        peso_maximo DECIMAL(19,4) NULL,
        observacao VARCHAR(1000) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        evento_id BIGINT NOT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_pesagens PRIMARY KEY (id),
        CONSTRAINT uk_aves_pesagens_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT uk_aves_pesagens_evento UNIQUE (evento_id),
        CONSTRAINT fk_aves_pesagens_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_pesagens_evento FOREIGN KEY (evento_id) REFERENCES dbo.aves_eventos (id),
        CONSTRAINT ck_aves_pesagens_amostra CHECK (quantidade_amostrada IS NULL OR quantidade_amostrada > 0),
        CONSTRAINT ck_aves_pesagens_pesos CHECK (peso_medio > 0 AND (peso_minimo IS NULL OR peso_minimo > 0) AND (peso_maximo IS NULL OR peso_maximo > 0) AND (peso_minimo IS NULL OR peso_minimo <= peso_medio) AND (peso_maximo IS NULL OR peso_maximo >= peso_medio))
    );
END
GO

IF OBJECT_ID(N'dbo.aves_posturas', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_posturas (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        data_coleta DATE NOT NULL,
        ovos_inteiros INT NOT NULL,
        ovos_quebrados INT NOT NULL CONSTRAINT df_aves_posturas_quebrados DEFAULT 0,
        ovos_descartados INT NOT NULL CONSTRAINT df_aves_posturas_descartados DEFAULT 0,
        observacao VARCHAR(1000) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        evento_id BIGINT NOT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_posturas PRIMARY KEY (id),
        CONSTRAINT uk_aves_posturas_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT uk_aves_posturas_evento UNIQUE (evento_id),
        CONSTRAINT fk_aves_posturas_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_posturas_evento FOREIGN KEY (evento_id) REFERENCES dbo.aves_eventos (id),
        CONSTRAINT ck_aves_posturas_quantidades CHECK (ovos_inteiros >= 0 AND ovos_quebrados >= 0 AND ovos_descartados >= 0 AND (ovos_inteiros + ovos_quebrados + ovos_descartados) > 0)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_transferencias', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_transferencias (
        id BIGINT IDENTITY(1,1) NOT NULL,
        lote_id BIGINT NOT NULL,
        instalacao_origem_id BIGINT NOT NULL,
        instalacao_destino_id BIGINT NOT NULL,
        data_evento DATETIME2 NOT NULL,
        usuario VARCHAR(100) NOT NULL,
        observacao VARCHAR(1000) NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        evento_id BIGINT NOT NULL,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_transferencias PRIMARY KEY (id),
        CONSTRAINT uk_aves_transferencias_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT uk_aves_transferencias_evento UNIQUE (evento_id),
        CONSTRAINT fk_aves_transferencias_lote FOREIGN KEY (lote_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_transferencias_origem FOREIGN KEY (instalacao_origem_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT fk_aves_transferencias_destino FOREIGN KEY (instalacao_destino_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT fk_aves_transferencias_evento FOREIGN KEY (evento_id) REFERENCES dbo.aves_eventos (id),
        CONSTRAINT ck_aves_transferencias_destino CHECK (instalacao_origem_id <> instalacao_destino_id)
    );
END
GO

IF OBJECT_ID(N'dbo.aves_incubacoes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.aves_incubacoes (
        id BIGINT IDENTITY(1,1) NOT NULL,
        codigo VARCHAR(80) NOT NULL,
        instalacao_id BIGINT NOT NULL,
        data_inicio DATE NOT NULL,
        quantidade_ovos INT NOT NULL,
        origem_ovos VARCHAR(200) NULL,
        lote_reprodutor_id BIGINT NULL,
        data_prevista_eclosao DATE NOT NULL,
        status VARCHAR(30) NOT NULL,
        observacao VARCHAR(1000) NULL,
        pintinhos_eclodidos INT NULL,
        ovos_perdidos INT NULL,
        data_eclosao DATE NULL,
        lote_resultante_id BIGINT NULL,
        chave_idempotencia VARCHAR(100) NOT NULL,
        versao BIGINT NOT NULL CONSTRAINT df_aves_incubacoes_versao DEFAULT 0,
        criado_em DATETIME2 NULL,
        criado_por VARCHAR(100) NULL,
        alterado_em DATETIME2 NULL,
        alterado_por VARCHAR(100) NULL,
        CONSTRAINT pk_aves_incubacoes PRIMARY KEY (id),
        CONSTRAINT uk_aves_incubacoes_codigo UNIQUE (codigo),
        CONSTRAINT uk_aves_incubacoes_idempotencia UNIQUE (chave_idempotencia),
        CONSTRAINT fk_aves_incubacoes_instalacao FOREIGN KEY (instalacao_id) REFERENCES dbo.criacao_instalacoes (id),
        CONSTRAINT fk_aves_incubacoes_lote_reprodutor FOREIGN KEY (lote_reprodutor_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT fk_aves_incubacoes_lote_resultante FOREIGN KEY (lote_resultante_id) REFERENCES dbo.aves_lotes (id),
        CONSTRAINT ck_aves_incubacoes_status CHECK (status IN ('EM_INCUBACAO', 'FINALIZADA', 'CANCELADA')),
        CONSTRAINT ck_aves_incubacoes_ovos CHECK (quantidade_ovos > 0 AND (pintinhos_eclodidos IS NULL OR pintinhos_eclodidos >= 0) AND (ovos_perdidos IS NULL OR ovos_perdidos >= 0) AND (pintinhos_eclodidos IS NULL OR ovos_perdidos IS NULL OR pintinhos_eclodidos + ovos_perdidos <= quantidade_ovos)),
        CONSTRAINT ck_aves_incubacoes_datas CHECK (data_prevista_eclosao >= data_inicio),
        CONSTRAINT ck_aves_incubacoes_finalizacao CHECK ((status = 'FINALIZADA' AND pintinhos_eclodidos IS NOT NULL AND ovos_perdidos IS NOT NULL AND data_eclosao IS NOT NULL) OR (status <> 'FINALIZADA' AND pintinhos_eclodidos IS NULL AND ovos_perdidos IS NULL AND data_eclosao IS NULL AND lote_resultante_id IS NULL))
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'uk_aves_incubacoes_lote_resultante' AND object_id = OBJECT_ID(N'dbo.aves_incubacoes'))
BEGIN
    CREATE UNIQUE INDEX uk_aves_incubacoes_lote_resultante ON dbo.aves_incubacoes (lote_resultante_id) WHERE lote_resultante_id IS NOT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_lotes_status_instalacao' AND object_id = OBJECT_ID(N'dbo.aves_lotes'))
    CREATE INDEX ix_aves_lotes_status_instalacao ON dbo.aves_lotes (status, instalacao_atual_id);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_eventos_lote_data' AND object_id = OBJECT_ID(N'dbo.aves_eventos'))
    CREATE INDEX ix_aves_eventos_lote_data ON dbo.aves_eventos (lote_id, data_evento DESC, id DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_alimentacoes_lote_data' AND object_id = OBJECT_ID(N'dbo.aves_alimentacoes'))
    CREATE INDEX ix_aves_alimentacoes_lote_data ON dbo.aves_alimentacoes (lote_id, data_evento DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_mortalidades_lote_data' AND object_id = OBJECT_ID(N'dbo.aves_mortalidades'))
    CREATE INDEX ix_aves_mortalidades_lote_data ON dbo.aves_mortalidades (lote_id, data_evento DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_posturas_lote_data' AND object_id = OBJECT_ID(N'dbo.aves_posturas'))
    CREATE INDEX ix_aves_posturas_lote_data ON dbo.aves_posturas (lote_id, data_coleta DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_aves_incubacoes_status_previsao' AND object_id = OBJECT_ID(N'dbo.aves_incubacoes'))
    CREATE INDEX ix_aves_incubacoes_status_previsao ON dbo.aves_incubacoes (status, data_prevista_eclosao);
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_tarefas_modulo' AND parent_object_id = OBJECT_ID(N'dbo.tarefas'))
    ALTER TABLE dbo.tarefas DROP CONSTRAINT ck_tarefas_modulo;
GO
ALTER TABLE dbo.tarefas ADD CONSTRAINT ck_tarefas_modulo CHECK (modulo_origem IS NULL OR modulo_origem IN ('TAREFAS', 'ESTOQUE', 'INTEGRACOES', 'CLIMA', 'COMPRAS', 'CRIACOES'));
GO
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_tarefas_origem_contexto' AND parent_object_id = OBJECT_ID(N'dbo.tarefas'))
    ALTER TABLE dbo.tarefas DROP CONSTRAINT ck_tarefas_origem_contexto;
GO
ALTER TABLE dbo.tarefas ADD CONSTRAINT ck_tarefas_origem_contexto CHECK ((modulo_origem IS NULL AND referencia_origem IS NULL) OR (modulo_origem IS NOT NULL AND referencia_origem IS NOT NULL));
GO
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_alertas_modulo' AND parent_object_id = OBJECT_ID(N'dbo.alertas'))
    ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_modulo;
GO
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_modulo CHECK (modulo_origem IN ('TAREFAS', 'ESTOQUE', 'INTEGRACOES', 'CLIMA', 'COMPRAS', 'CRIACOES'));
GO
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'ck_alertas_tipo' AND parent_object_id = OBJECT_ID(N'dbo.alertas'))
    ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_tipo;
GO
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_tipo CHECK (tipo IN ('ESTOQUE_ABAIXO_MINIMO', 'LOTE_PROXIMO_VENCIMENTO', 'LOTE_VENCIDO', 'INTEGRACAO_DESATUALIZADA', 'INTEGRACAO_COM_FALHA', 'CHUVA_INTENSA_24H', 'CRIACAO_MORTALIDADE_ALTA', 'CRIACAO_INCUBACAO_ECLOSAO_PROXIMA', 'CRIACAO_INCUBACAO_ATRASADA'));
GO
