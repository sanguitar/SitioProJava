ALTER TABLE dbo.propriedade_recursos_hidricos ADD
    volume_atual_litros DECIMAL(18,3) NULL,
    nivel_minimo_alerta_litros DECIMAL(18,3) NULL,
    intervalo_inspecao_dias INT NOT NULL
        CONSTRAINT df_recursos_hidricos_intervalo_inspecao DEFAULT 7,
    ultima_inspecao DATETIME2 NULL,
    proxima_inspecao DATETIME2 NULL;
GO

UPDATE dbo.propriedade_recursos_hidricos
SET proxima_inspecao = DATEADD(DAY, intervalo_inspecao_dias, SYSDATETIME())
WHERE ativo = 1 AND proxima_inspecao IS NULL;

ALTER TABLE dbo.propriedade_recursos_hidricos ADD
    CONSTRAINT ck_recursos_hidricos_volume_atual CHECK (
        volume_atual_litros IS NULL
        OR (volume_atual_litros >= 0
            AND (capacidade_litros IS NULL OR volume_atual_litros <= capacidade_litros))
    ),
    CONSTRAINT ck_recursos_hidricos_nivel_minimo CHECK (
        nivel_minimo_alerta_litros IS NULL
        OR (nivel_minimo_alerta_litros >= 0
            AND (capacidade_litros IS NULL OR nivel_minimo_alerta_litros <= capacidade_litros))
    ),
    CONSTRAINT ck_recursos_hidricos_intervalo_inspecao CHECK (
        intervalo_inspecao_dias BETWEEN 1 AND 365
    ),
    CONSTRAINT ck_recursos_hidricos_inspecao CHECK (
        proxima_inspecao IS NULL OR ultima_inspecao IS NULL OR proxima_inspecao >= ultima_inspecao
    );

CREATE INDEX ix_recursos_hidricos_inspecao
    ON dbo.propriedade_recursos_hidricos(ativo, proxima_inspecao)
    WHERE proxima_inspecao IS NOT NULL;

CREATE TABLE dbo.agua_operacoes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_agua_operacoes PRIMARY KEY,
    recurso_origem_id BIGINT NOT NULL,
    recurso_destino_id BIGINT NULL,
    tipo VARCHAR(30) NOT NULL,
    data_operacao DATETIME2 NOT NULL,
    quantidade_litros DECIMAL(18,3) NULL,
    volume_origem_antes_litros DECIMAL(18,3) NULL,
    volume_origem_depois_litros DECIMAL(18,3) NULL,
    volume_destino_antes_litros DECIMAL(18,3) NULL,
    volume_destino_depois_litros DECIMAL(18,3) NULL,
    observacao VARCHAR(1000) NULL,
    chave_idempotencia VARCHAR(100) NOT NULL,
    versao BIGINT NOT NULL CONSTRAINT df_agua_operacoes_versao DEFAULT 0,
    criado_em DATETIME2 NULL,
    criado_por VARCHAR(100) NULL,
    alterado_em DATETIME2 NULL,
    alterado_por VARCHAR(100) NULL,
    CONSTRAINT fk_agua_operacoes_origem FOREIGN KEY (recurso_origem_id)
        REFERENCES dbo.propriedade_recursos_hidricos(id),
    CONSTRAINT fk_agua_operacoes_destino FOREIGN KEY (recurso_destino_id)
        REFERENCES dbo.propriedade_recursos_hidricos(id),
    CONSTRAINT uq_agua_operacoes_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT ck_agua_operacoes_tipo CHECK (tipo IN
        ('MEDICAO_NIVEL','ABASTECIMENTO','CONSUMO','RETIRADA','TRANSFERENCIA','INSPECAO')),
    CONSTRAINT ck_agua_operacoes_quantidade CHECK (
        (tipo = 'INSPECAO' AND quantidade_litros IS NULL)
        OR (tipo = 'MEDICAO_NIVEL' AND quantidade_litros IS NOT NULL AND quantidade_litros >= 0)
        OR (tipo IN ('ABASTECIMENTO','CONSUMO','RETIRADA','TRANSFERENCIA')
            AND quantidade_litros IS NOT NULL AND quantidade_litros > 0)
    ),
    CONSTRAINT ck_agua_operacoes_destino CHECK (
        (tipo = 'TRANSFERENCIA' AND recurso_destino_id IS NOT NULL
            AND recurso_destino_id <> recurso_origem_id)
        OR (tipo <> 'TRANSFERENCIA' AND recurso_destino_id IS NULL)
    ),
    CONSTRAINT ck_agua_operacoes_volumes CHECK (
        (volume_origem_antes_litros IS NULL OR volume_origem_antes_litros >= 0)
        AND (volume_origem_depois_litros IS NULL OR volume_origem_depois_litros >= 0)
        AND (volume_destino_antes_litros IS NULL OR volume_destino_antes_litros >= 0)
        AND (volume_destino_depois_litros IS NULL OR volume_destino_depois_litros >= 0)
    )
);

CREATE INDEX ix_agua_operacoes_origem_data
    ON dbo.agua_operacoes(recurso_origem_id, data_operacao DESC, id DESC);
CREATE INDEX ix_agua_operacoes_destino_data
    ON dbo.agua_operacoes(recurso_destino_id, data_operacao DESC, id DESC)
    WHERE recurso_destino_id IS NOT NULL;
CREATE INDEX ix_agua_operacoes_data
    ON dbo.agua_operacoes(data_operacao DESC, id DESC);

ALTER TABLE dbo.tarefas DROP CONSTRAINT ck_tarefas_modulo;
ALTER TABLE dbo.tarefas ADD CONSTRAINT ck_tarefas_modulo CHECK (modulo_origem IS NULL OR modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA','MANUTENCAO','AGUA'));

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_modulo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_modulo CHECK (modulo_origem IN
    ('TAREFAS','ESTOQUE','INTEGRACOES','CLIMA','COMPRAS','CRIACOES','AGRICULTURA','MANUTENCAO','AGUA'));

ALTER TABLE dbo.alertas DROP CONSTRAINT ck_alertas_tipo;
ALTER TABLE dbo.alertas ADD CONSTRAINT ck_alertas_tipo CHECK (tipo IN
    ('ESTOQUE_ABAIXO_MINIMO','LOTE_PROXIMO_VENCIMENTO','LOTE_VENCIDO','INTEGRACAO_DESATUALIZADA',
     'INTEGRACAO_COM_FALHA','CHUVA_INTENSA_24H','CRIACAO_MORTALIDADE_ALTA',
     'CRIACAO_INCUBACAO_ECLOSAO_PROXIMA','CRIACAO_INCUBACAO_ATRASADA',
     'CRIACAO_SUINOS_CHECAGEM_PENDENTE','CRIACAO_SUINOS_PARTO_ATRASADO',
     'CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO','AGRICULTURA_OCORRENCIA_FITOSSANITARIA',
     'CRIACAO_PEIXES_QUALIDADE_AGUA','CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE','MANUTENCAO_VENCIDA',
     'AGUA_NIVEL_BAIXO','AGUA_INSPECAO_VENCIDA'));
