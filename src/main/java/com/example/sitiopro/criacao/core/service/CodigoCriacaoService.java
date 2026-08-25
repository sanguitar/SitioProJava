package com.example.sitiopro.criacao.core.service;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Year;
import java.util.Locale;

@Service
public class CodigoCriacaoService {

    private static final Logger log = LoggerFactory.getLogger(CodigoCriacaoService.class);
    private static final int LOCK_TIMEOUT_MILLIS = 10_000;

    private final EntityManager entityManager;
    private final Clock clock;

    public CodigoCriacaoService(EntityManager entityManager, Clock clock) {
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Transactional
    public String proximoLoteAves() {
        return proximo("LOTE_AVES", "AV");
    }

    @Transactional
    public String proximaIncubacaoAves() {
        return proximo("INCUBACAO_AVES", "INC");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquearIdempotencia(String escopo, String chave) {
        adquirirLock("SITIOPRO:CRIACAO:IDEMP:" + escopo + ":" + chave);
    }

    private String proximo(String tipo, String prefixo) {
        int ano = Year.now(clock).getValue();
        adquirirLock("SITIOPRO:CRIACAO:CODIGO:" + tipo + ":" + ano);
        Long valor = entityManager.unwrap(Session.class).doReturningWork(conexao -> {
            try (PreparedStatement statement = conexao.prepareStatement("""
                    SET NOCOUNT ON;
                    IF EXISTS (
                        SELECT 1
                        FROM dbo.criacao_codigo_sequencias WITH (UPDLOCK, HOLDLOCK)
                        WHERE tipo = ? AND ano = ?
                    )
                        UPDATE dbo.criacao_codigo_sequencias
                        SET ultimo_valor = ultimo_valor + 1
                        WHERE tipo = ? AND ano = ?;
                    ELSE
                        INSERT INTO dbo.criacao_codigo_sequencias (tipo, ano, ultimo_valor)
                        VALUES (?, ?, 1);

                    SELECT ultimo_valor
                    FROM dbo.criacao_codigo_sequencias
                    WHERE tipo = ? AND ano = ?;
                    """)) {
                statement.setString(1, tipo);
                statement.setInt(2, ano);
                statement.setString(3, tipo);
                statement.setInt(4, ano);
                statement.setString(5, tipo);
                statement.setInt(6, ano);
                statement.setString(7, tipo);
                statement.setInt(8, ano);
                try (ResultSet resultado = statement.executeQuery()) {
                    return resultado.next() ? resultado.getLong(1) : null;
                }
            }
        });
        if (valor == null || valor < 1) {
            throw new IllegalStateException("SQL Server não retornou um sequencial operacional válido.");
        }
        return String.format(Locale.ROOT, "%s-%04d-%04d", prefixo, ano, valor);
    }

    private void adquirirLock(String recurso) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("A reserva do sequencial exige uma transação ativa.");
        }
        entityManager.unwrap(Session.class).doWork(conexao -> {
            int resultado = executarLock(conexao, recurso, false);
            if (resultado < 0) {
                throw new IllegalStateException(
                        "Não foi possível reservar o sequencial operacional no SQL Server: " + resultado);
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    liberarLock(conexao, recurso);
                }
            });
        });
    }

    private int executarLock(Connection conexao, String recurso, boolean liberar) throws SQLException {
        String procedure = liberar ? "sys.sp_releaseapplock" : "sys.sp_getapplock";
        String parametros = liberar
                ? "@Resource = ?, @LockOwner = 'Session'"
                : "@Resource = ?, @LockMode = 'Exclusive', @LockOwner = 'Session', @LockTimeout = ?";
        String sql = "SET NOCOUNT ON; DECLARE @resultado INT; EXEC @resultado = " + procedure
                + " " + parametros + "; SELECT CAST(@resultado AS INT);";
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, recurso);
            if (!liberar) statement.setInt(2, LOCK_TIMEOUT_MILLIS);
            try (ResultSet resposta = statement.executeQuery()) {
                if (!resposta.next()) {
                    throw new IllegalStateException("SQL Server não retornou o resultado do application lock.");
                }
                return resposta.getInt(1);
            }
        }
    }

    private void liberarLock(Connection conexao, String recurso) {
        try {
            int resultado = executarLock(conexao, recurso, true);
            if (resultado < 0) {
                log.error("SQL Server não liberou um application lock de criações: {}", resultado);
            }
        } catch (SQLException | RuntimeException ex) {
            log.error("Falha ao liberar um application lock de criações: {}", ex.getClass().getName());
        }
    }
}
