CREATE TABLE dbo.agricultura_safras (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_safras PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ano_inicio INT NOT NULL, ano_fim INT NOT NULL,
    data_inicio DATE NOT NULL, data_fim DATE NULL,
    status VARCHAR(25) NOT NULL,
    observacao VARCHAR(1000) NULL, versao BIGINT NOT NULL DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_safras_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT ux_agricultura_safras_nome UNIQUE (propriedade_id, nome),
    CONSTRAINT ux_agricultura_safras_propriedade UNIQUE (id, propriedade_id),
    CONSTRAINT ck_agricultura_safras_nome CHECK (LEN(LTRIM(RTRIM(nome))) > 0),
    CONSTRAINT ck_agricultura_safras_anos CHECK (ano_inicio BETWEEN 1900 AND 9999 AND ano_fim BETWEEN ano_inicio AND 9999),
    CONSTRAINT ck_agricultura_safras_datas CHECK (
        YEAR(data_inicio) BETWEEN ano_inicio AND ano_fim AND
        (data_fim IS NULL OR (data_fim >= data_inicio AND YEAR(data_fim) BETWEEN ano_inicio AND ano_fim))),
    CONSTRAINT ck_agricultura_safras_status CHECK (status IN ('PLANEJADA','EM_ANDAMENTO','ENCERRADA','CANCELADA'))
);
CREATE INDEX ix_agricultura_safras_status ON dbo.agricultura_safras(propriedade_id, status, data_inicio);
GO
CREATE TABLE dbo.agricultura_culturas (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_culturas PRIMARY KEY,
    nome_comum VARCHAR(180) NOT NULL,
    nome_cientifico VARCHAR(180) NULL,
    ciclo_dias_estimado INT NULL,
    ativo BIT NOT NULL DEFAULT 1,
    agrofit_cultura_id BIGINT NULL,
    observacao VARCHAR(1000) NULL, versao BIGINT NOT NULL DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT ux_agricultura_culturas_nome UNIQUE (nome_comum),
    CONSTRAINT ck_agricultura_culturas_nome CHECK (LEN(LTRIM(RTRIM(nome_comum))) > 0),
    CONSTRAINT ck_agricultura_culturas_ciclo CHECK (ciclo_dias_estimado IS NULL OR ciclo_dias_estimado BETWEEN 1 AND 3650),
    CONSTRAINT fk_agricultura_culturas_agrofit FOREIGN KEY (agrofit_cultura_id) REFERENCES dbo.agrofit_culturas(id)
);
CREATE INDEX ix_agricultura_culturas_agrofit ON dbo.agricultura_culturas(agrofit_cultura_id);
GO
-- Composite references keep the crop, season and official plot in the same property.
CREATE UNIQUE INDEX ux_agricultura_talhao_propriedade ON dbo.propriedade_talhoes(id, propriedade_id);
CREATE TABLE dbo.agricultura_cultivos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_cultivos PRIMARY KEY,
    propriedade_id BIGINT NOT NULL,
    safra_id BIGINT NOT NULL, talhao_id BIGINT NOT NULL, cultura_id BIGINT NOT NULL,
    area_cultivada_ha DECIMAL(14,4) NOT NULL,
    data_plantio DATE NOT NULL, previsao_colheita DATE NULL, data_colheita_real DATE NULL,
    status VARCHAR(25) NOT NULL,
    revisao_operacoes BIGINT NOT NULL DEFAULT 0,
    observacao VARCHAR(1000) NULL, versao BIGINT NOT NULL DEFAULT 0,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_cultivos_propriedade FOREIGN KEY (propriedade_id) REFERENCES dbo.propriedades(id),
    CONSTRAINT fk_agricultura_cultivos_safra FOREIGN KEY (safra_id, propriedade_id) REFERENCES dbo.agricultura_safras(id, propriedade_id),
    CONSTRAINT fk_agricultura_cultivos_talhao FOREIGN KEY (talhao_id, propriedade_id) REFERENCES dbo.propriedade_talhoes(id, propriedade_id),
    CONSTRAINT fk_agricultura_cultivos_cultura FOREIGN KEY (cultura_id) REFERENCES dbo.agricultura_culturas(id),
    CONSTRAINT ck_agricultura_cultivos_area CHECK (area_cultivada_ha > 0),
    CONSTRAINT ck_agricultura_cultivos_datas CHECK (
        (previsao_colheita IS NULL OR previsao_colheita >= data_plantio) AND
        (data_colheita_real IS NULL OR data_colheita_real >= data_plantio)),
    CONSTRAINT ck_agricultura_cultivos_status CHECK (status IN
        ('PLANEJADO','IMPLANTADO','EM_DESENVOLVIMENTO','PRONTO_COLHEITA','COLHIDO','PERDIDO','CANCELADO')),
    CONSTRAINT ck_agricultura_cultivos_colheita CHECK (
        (status = 'COLHIDO' AND data_colheita_real IS NOT NULL) OR (status <> 'COLHIDO' AND data_colheita_real IS NULL))
);
CREATE INDEX ix_agricultura_cultivos_safra ON dbo.agricultura_cultivos(safra_id, propriedade_id, status);
CREATE INDEX ix_agricultura_cultivos_talhao ON dbo.agricultura_cultivos(talhao_id, propriedade_id, status) INCLUDE (area_cultivada_ha);
CREATE INDEX ix_agricultura_cultivos_cultura ON dbo.agricultura_cultivos(cultura_id);
CREATE INDEX ix_agricultura_cultivos_painel ON dbo.agricultura_cultivos(propriedade_id, status, previsao_colheita);
GO
CREATE TABLE dbo.agricultura_plantios (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_plantios PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data DATE NOT NULL, metodo VARCHAR(80) NULL,
    quantidade DECIMAL(18,4) NOT NULL, unidade VARCHAR(30) NOT NULL,
    espacamento VARCHAR(120) NULL,
    origem VARCHAR(20) NOT NULL, descricao_origem VARCHAR(180) NULL,
    movimento_estoque_id BIGINT NULL, observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_plantios_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT fk_agricultura_plantios_movimento FOREIGN KEY (movimento_estoque_id) REFERENCES dbo.estoque_movimentos(id),
    CONSTRAINT ck_agricultura_plantios_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_agricultura_plantios_unidade CHECK (LEN(LTRIM(RTRIM(unidade))) > 0),
    CONSTRAINT ck_agricultura_plantios_origem CHECK (
        (origem = 'ESTOQUE' AND movimento_estoque_id IS NOT NULL) OR
        (origem = 'EXTERNA' AND movimento_estoque_id IS NULL AND descricao_origem IS NOT NULL AND LEN(LTRIM(RTRIM(descricao_origem))) > 0))
);
CREATE INDEX ix_agricultura_plantios_cultivo ON dbo.agricultura_plantios(cultivo_id, data, id);
CREATE UNIQUE INDEX ux_agricultura_plantios_movimento ON dbo.agricultura_plantios(movimento_estoque_id) WHERE movimento_estoque_id IS NOT NULL;
GO
CREATE TABLE dbo.agricultura_acompanhamentos (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_acompanhamentos PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data_hora DATETIME2 NOT NULL,
    tipo VARCHAR(25) NOT NULL, descricao VARCHAR(1000) NOT NULL, observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_acompanhamentos_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT ck_agricultura_acompanhamentos_tipo CHECK (tipo IN
        ('GERAL','GERMINACAO','DESENVOLVIMENTO','FLORACAO','FRUTIFICACAO','PRAGA','DOENCA','DEFICIENCIA','PERDA','OUTRO')),
    CONSTRAINT ck_agricultura_acompanhamentos_descricao CHECK (LEN(LTRIM(RTRIM(descricao))) > 0)
);
CREATE INDEX ix_agricultura_acompanhamentos_cultivo ON dbo.agricultura_acompanhamentos(cultivo_id, data_hora, id);
GO
CREATE TABLE dbo.agricultura_colheitas (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agricultura_colheitas PRIMARY KEY,
    cultivo_id BIGINT NOT NULL, data DATE NOT NULL,
    quantidade DECIMAL(18,4) NOT NULL, unidade VARCHAR(30) NOT NULL,
    classificacao VARCHAR(120) NULL, perdas DECIMAL(18,4) NULL,
    finaliza_cultivo BIT NOT NULL, observacao VARCHAR(1000) NULL,
    criado_em DATETIME2 NULL, criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL, alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agricultura_colheitas_cultivo FOREIGN KEY (cultivo_id) REFERENCES dbo.agricultura_cultivos(id),
    CONSTRAINT ck_agricultura_colheitas_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_agricultura_colheitas_perdas CHECK (perdas IS NULL OR perdas >= 0),
    CONSTRAINT ck_agricultura_colheitas_unidade CHECK (LEN(LTRIM(RTRIM(unidade))) > 0)
);
CREATE INDEX ix_agricultura_colheitas_cultivo ON dbo.agricultura_colheitas(cultivo_id, data, id);
CREATE UNIQUE INDEX ux_agricultura_colheitas_final ON dbo.agricultura_colheitas(cultivo_id) WHERE finaliza_cultivo = 1;
GO
ALTER TABLE dbo.tarefas DROP CONSTRAINT ck_tarefas_modulo;
ALTER TABLE dbo.tarefas ADD CONSTRAINT ck_tarefas_modulo CHECK (modulo_origem IS NULL OR modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA'));
ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_modulo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_modulo CHECK (modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA'));
