package com.example.sitiopro.integracao.integration;

import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false",
        "sitiopro.integracoes.open-meteo.enabled=false",
        "sitiopro.integracoes.embrapa-agrofit.enabled=false"
})
class IntegracoesSqlServerIntegrationTests {

    @Container
    static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense();

    @DynamicPropertySource
    static void sqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.flyway.user", SQLSERVER::getUsername);
        registry.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PrevisaoClimaticaRepository previsaoRepository;

    @Test
    void flywayCriaSchemaEEstadosIniciaisDasIntegracoes() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.tables
                WHERE name IN ('integracao_estados', 'integracao_execucoes',
                               'previsoes_climaticas', 'agrofit_culturas')
                """, Integer.class);
        Integer estados = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.integracao_estados
                WHERE fonte IN ('OPEN_METEO', 'EMBRAPA_AGROFIT')
                """, Integer.class);

        assertThat(tabelas).isEqualTo(4);
        assertThat(estados).isEqualTo(2);
    }

    @Test
    void chaveClimaticaImpedeDuplicacaoDaMesmaHora() {
        LocalDateTime data = LocalDateTime.of(2026, 8, 21, 12, 0);
        previsaoRepository.saveAndFlush(previsao(data, "28.4"));

        assertThatThrownBy(() -> previsaoRepository.saveAndFlush(previsao(data, "29.0")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private PrevisaoClimatica previsao(LocalDateTime data, String temperatura) {
        PrevisaoClimatica previsao = new PrevisaoClimatica(
                FonteIntegracao.OPEN_METEO, "teste", "UTC", data);
        previsao.atualizar(
                new BigDecimal(temperatura), 70, BigDecimal.ZERO, 0,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                new BigDecimal("0.2"), 0, LocalDateTime.now(), "UTC");
        return previsao;
    }
}
