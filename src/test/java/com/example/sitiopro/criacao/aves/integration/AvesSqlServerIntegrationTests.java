package com.example.sitiopro.criacao.aves.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false"
})
class AvesSqlServerIntegrationTests {
    @Container
    static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void sqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.flyway.user", SQLSERVER::getUsername);
        registry.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCriaSchemaDeAvesEHibernateValidaMapeamentos() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.tables WHERE name IN (
                  'criacao_instalacoes', 'aves_lotes', 'aves_eventos', 'aves_mortalidades',
                  'aves_alimentacoes', 'aves_pesagens', 'aves_posturas', 'aves_transferencias',
                  'aves_incubacoes')
                """, Integer.class);
        Integer migration = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.flyway_schema_history
                WHERE version = '10' AND success = 1
                """, Integer.class);

        assertThat(tabelas).isEqualTo(9);
        assertThat(migration).isEqualTo(1);
    }

    @Test
    void constraintImpedeQuantidadeAtualMaiorQueInicial() {
        long instalacaoId = criarInstalacao("SQL lote " + System.nanoTime(), "GALINHEIRO");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO dbo.aves_lotes
                (codigo, especie, finalidade, origem, data_entrada, quantidade_inicial, quantidade_atual,
                 sexo, instalacao_atual_id, status, chave_idempotencia)
                VALUES (?, 'GALINHA', 'POSTURA', 'Teste SQL', '2026-08-24', 10, 11,
                        'FEMEAS', ?, 'ATIVO', ?)
                """, "SQL-AV-" + System.nanoTime(), instalacaoId, "sql-key-" + System.nanoTime()))
                .hasStackTraceContaining("ck_aves_lotes_quantidades");
    }

    @Test
    void constraintImpedeResultadoDeIncubacaoMaiorQueOvos() {
        long instalacaoId = criarInstalacao("SQL incubadora " + System.nanoTime(), "INCUBADORA");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO dbo.aves_incubacoes
                (codigo, instalacao_id, data_inicio, quantidade_ovos, data_prevista_eclosao, status,
                 pintinhos_eclodidos, ovos_perdidos, data_eclosao, chave_idempotencia)
                VALUES (?, ?, '2026-08-01', 10, '2026-08-22', 'FINALIZADA', 9, 2,
                        '2026-08-22', ?)
                """, "SQL-INC-" + System.nanoTime(), instalacaoId, "sql-inc-key-" + System.nanoTime()))
                .hasStackTraceContaining("ck_aves_incubacoes_ovos");
    }

    private long criarInstalacao(String nome, String tipo) {
        jdbcTemplate.update("""
                INSERT INTO dbo.criacao_instalacoes (nome, tipo, capacidade, ativo)
                VALUES (?, ?, 100, 1)
                """, nome, tipo);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dbo.criacao_instalacoes WHERE nome = ?", Long.class, nome);
    }
}
