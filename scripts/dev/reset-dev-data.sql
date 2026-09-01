SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;

IF N'$(DevResetConfirmation)' <> N'LOCAL_DEV_RESET_CONFIRMED'
    THROW 51000, N'Reset DEV recusado: confirmacao explicita ausente.', 1;

IF DB_NAME() <> N'$(ExpectedDatabase)'
    THROW 51001, N'Reset DEV recusado: banco conectado nao e o banco local esperado.', 1;

IF OBJECT_ID(N'dbo.flyway_schema_history', N'U') IS NULL
    THROW 51002, N'Reset DEV recusado: historico Flyway nao encontrado.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.flyway_schema_history
    WHERE version = '13' AND success = 1
)
    THROW 51003, N'Reset DEV recusado: schema nao esta na V13.', 1;

IF NOT EXISTS (
    SELECT 1
    FROM dbo.usuarios
    WHERE login = 'sanderson' AND perfil = 'ADMIN' AND ativo = 1
)
    THROW 51004, N'Reset DEV recusado: administrador principal ativo nao encontrado.', 1;

DECLARE @admin_hash VARCHAR(255) = (
    SELECT senha_hash FROM dbo.usuarios WHERE login = 'sanderson'
);
DECLARE @flyway_count INT = (SELECT COUNT(*) FROM dbo.flyway_schema_history);

BEGIN TRY
    BEGIN TRANSACTION;

    DELETE FROM dbo.aves_alimentacoes;
    DELETE FROM dbo.aves_mortalidades;
    DELETE FROM dbo.aves_pesagens;
    DELETE FROM dbo.aves_posturas;
    DELETE FROM dbo.aves_transferencias;
    DELETE FROM dbo.aves_incubacoes;
    DELETE FROM dbo.aves_eventos;
    DELETE FROM dbo.aves_lotes;
    DELETE FROM dbo.criacao_instalacoes;
    DELETE FROM dbo.criacao_codigo_sequencias;

    DELETE FROM dbo.tarefa_alerta_eventos;
    UPDATE dbo.tarefas
       SET recorrencia_id = NULL,
           ocorrencia_programada_em = NULL
     WHERE recorrencia_id IS NOT NULL;
    DELETE FROM dbo.alertas;
    DELETE FROM dbo.tarefa_recorrencias;
    DELETE FROM dbo.tarefas;

    DELETE FROM dbo.itens_compra;
    DELETE FROM dbo.compras;
    DELETE FROM dbo.fornecedores;

    DELETE FROM dbo.producao;
    DELETE FROM dbo.estoque_movimentos;
    DELETE FROM dbo.estoque_lotes;
    DELETE FROM dbo.estoque_itens;
    DELETE FROM dbo.estoque_categorias;
    DELETE FROM dbo.estoque_unidades_medida;
    DELETE FROM dbo.estoque_locais;
    DELETE FROM dbo.categorias;

    DELETE FROM dbo.abastecimentos;
    DELETE FROM dbo.veiculos;
    DELETE FROM dbo.fipe_cache;

    DELETE FROM dbo.integracao_execucoes;
    DELETE FROM dbo.previsoes_climaticas;
    UPDATE dbo.integracao_estados
       SET ultimo_sucesso_em = NULL,
           ultima_tentativa_em = NULL,
           em_execucao = 0,
           execucao_iniciada_em = NULL,
           checkpoint_valor = NULL,
           etag = NULL,
           last_modified = NULL,
           versao = 0;

    DELETE FROM dbo.usuarios WHERE login <> 'sanderson';

    DBCC CHECKIDENT ('dbo.aves_alimentacoes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_mortalidades', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_pesagens', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_posturas', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_transferencias', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_incubacoes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_eventos', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.aves_lotes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.criacao_instalacoes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.tarefa_alerta_eventos', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.alertas', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.tarefa_recorrencias', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.tarefas', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.itens_compra', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.compras', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.fornecedores', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.producao', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_movimentos', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_lotes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_itens', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_categorias', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_unidades_medida', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.estoque_locais', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.categorias', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.abastecimentos', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.veiculos', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.integracao_execucoes', RESEED, 0) WITH NO_INFOMSGS;
    DBCC CHECKIDENT ('dbo.previsoes_climaticas', RESEED, 0) WITH NO_INFOMSGS;

    IF (SELECT COUNT(*) FROM dbo.usuarios) <> 1
        THROW 51005, N'Reset DEV abortado: usuarios inesperados permaneceram.', 1;

    IF NOT EXISTS (
        SELECT 1 FROM dbo.usuarios
        WHERE login = 'sanderson'
          AND perfil = 'ADMIN'
          AND ativo = 1
          AND senha_hash = @admin_hash
    )
        THROW 51006, N'Reset DEV abortado: administrador principal foi alterado.', 1;

    IF (SELECT COUNT(*) FROM dbo.flyway_schema_history) <> @flyway_count
        THROW 51007, N'Reset DEV abortado: historico Flyway foi alterado.', 1;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT
    DB_NAME() AS banco,
    (SELECT COUNT(*) FROM dbo.usuarios) AS usuarios_preservados,
    (SELECT COUNT(*) FROM dbo.flyway_schema_history) AS migrations_preservadas,
    (SELECT COUNT(*) FROM dbo.agrofit_culturas) AS culturas_externas_preservadas;
