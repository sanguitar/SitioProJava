package com.example.sitiopro.tarefas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class SqlServerApplicationLock {

    private final JdbcTemplate jdbcTemplate;

    public SqlServerApplicationLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> Optional<T> executar(String recurso, Supplier<T> operacao) {
        return jdbcTemplate.execute((ConnectionCallback<Optional<T>>) conexao -> {
            int resultado = adquirir(conexao, recurso);
            if (resultado >= -3 && resultado < 0) {
                return Optional.empty();
            }
            if (resultado < 0) {
                throw new IllegalStateException(
                        "SQL Server rejeitou os parâmetros do application lock: " + resultado);
            }
            try {
                return Optional.ofNullable(operacao.get());
            } finally {
                liberar(conexao, recurso);
            }
        });
    }

    private int adquirir(java.sql.Connection conexao, String recurso) throws SQLException {
        return executar(conexao, """
                DECLARE @resultado INT;
                EXEC @resultado = sys.sp_getapplock
                    @Resource = ?,
                    @LockMode = 'Exclusive',
                    @LockOwner = 'Session',
                    @LockTimeout = 0;
                SELECT CAST(@resultado AS INT);
                """, recurso);
    }

    private void liberar(java.sql.Connection conexao, String recurso) throws SQLException {
        int resultado = executar(conexao, """
                DECLARE @resultado INT;
                EXEC @resultado = sys.sp_releaseapplock
                    @Resource = ?,
                    @LockOwner = 'Session';
                SELECT CAST(@resultado AS INT);
                """, recurso);
        if (resultado < 0) {
            throw new IllegalStateException("SQL Server não liberou o application lock: " + resultado);
        }
    }

    private int executar(java.sql.Connection conexao, String sql, String recurso) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, recurso);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalStateException("SQL Server não retornou o resultado do application lock.");
                }
                return resultado.getInt(1);
            }
        }
    }
}
