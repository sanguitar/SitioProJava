package com.example.sitiopro.tarefas.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlServerApplicationLockTests {

    @Test
    void executaOperacaoProtegidaELiberaLockDaMesmaSessao() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection conexao = mock(Connection.class);
        PreparedStatement aquisicao = mock(PreparedStatement.class);
        PreparedStatement liberacao = mock(PreparedStatement.class);
        ResultSet resultadoAquisicao = resultado(0);
        ResultSet resultadoLiberacao = resultado(0);
        when(conexao.prepareStatement(org.mockito.ArgumentMatchers.contains("sp_getapplock")))
                .thenReturn(aquisicao);
        when(conexao.prepareStatement(org.mockito.ArgumentMatchers.contains("sp_releaseapplock")))
                .thenReturn(liberacao);
        when(aquisicao.executeQuery()).thenReturn(resultadoAquisicao);
        when(liberacao.executeQuery()).thenReturn(resultadoLiberacao);
        when(jdbcTemplate.execute(org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.ConnectionCallback<java.util.Optional<String>>>any()))
                .thenAnswer(invocacao -> invocacao
                        .<org.springframework.jdbc.core.ConnectionCallback<java.util.Optional<String>>>getArgument(0)
                        .doInConnection(conexao));
        SqlServerApplicationLock lock = new SqlServerApplicationLock(jdbcTemplate);

        assertThat(lock.executar("livre", () -> "feito")).contains("feito");
        verify(aquisicao).setString(1, "livre");
        verify(liberacao).setString(1, "livre");
    }

    @Test
    void naoExecutaOperacaoQuandoOutraInstanciaPossuiOLock() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection conexao = mock(Connection.class);
        PreparedStatement aquisicao = mock(PreparedStatement.class);
        ResultSet resultadoAquisicao = resultado(-1);
        when(conexao.prepareStatement(org.mockito.ArgumentMatchers.contains("sp_getapplock")))
                .thenReturn(aquisicao);
        when(aquisicao.executeQuery()).thenReturn(resultadoAquisicao);
        when(jdbcTemplate.execute(org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.ConnectionCallback<java.util.Optional<String>>>any()))
                .thenAnswer(invocacao -> invocacao
                        .<org.springframework.jdbc.core.ConnectionCallback<java.util.Optional<String>>>getArgument(0)
                        .doInConnection(conexao));

        assertThat(new SqlServerApplicationLock(jdbcTemplate).executar("ocupado", () -> "não deve executar"))
                .isEmpty();
    }

    private ResultSet resultado(int codigo) throws Exception {
        ResultSet resultado = mock(ResultSet.class);
        when(resultado.next()).thenReturn(true);
        when(resultado.getInt(1)).thenReturn(codigo);
        return resultado;
    }
}
