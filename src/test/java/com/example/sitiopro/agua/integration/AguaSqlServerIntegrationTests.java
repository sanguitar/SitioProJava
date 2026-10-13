package com.example.sitiopro.agua.integration;

import com.example.sitiopro.observability.service.SistemaSaudeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.*;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {"spring.profiles.active=test", "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true", "sitiopro.initial-admin.enabled=false", "sitiopro.tarefas.scheduler-enabled=false"})
class AguaSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        r.add("spring.datasource.username", SQLSERVER::getUsername);
        r.add("spring.datasource.password", SQLSERVER::getPassword);
        r.add("spring.flyway.user", SQLSERVER::getUsername);
        r.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired JdbcTemplate jdbc;

    @Test
    void v32CriaOperacoesEstadoIndicesConstraintsEValidaHibernate() {
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='32' AND success=1", Integer.class)).isOne();
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys.tables WHERE name='agua_operacoes'", Integer.class)).isOne();
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.columns
                WHERE object_id=OBJECT_ID('dbo.propriedade_recursos_hidricos')
                  AND name IN ('volume_atual_litros','nivel_minimo_alerta_litros','intervalo_inspecao_dias',
                               'ultima_inspecao','proxima_inspecao')
                """, Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.check_constraints
                WHERE name IN ('ck_recursos_hidricos_volume_atual','ck_recursos_hidricos_nivel_minimo',
                               'ck_agua_operacoes_tipo','ck_agua_operacoes_quantidade','ck_agua_operacoes_destino')
                """, Integer.class)).isEqualTo(5);
    }

    @Test
    void bancoPreservaPrecisaoIdempotenciaEFormatoDaTransferencia() {
        Long propriedade = jdbc.queryForObject("SELECT TOP 1 id FROM dbo.propriedades WHERE principal=1", Long.class);
        long origem = jdbc.queryForObject("""
                INSERT INTO dbo.propriedade_recursos_hidricos
                    (propriedade_id,nome,tipo,ativo,capacidade_litros,volume_atual_litros,versao)
                OUTPUT INSERTED.id VALUES (?,'Caixa SQL 1','CAIXA_DAGUA',1,1000,500.125,0)
                """, Long.class, propriedade);
        long destino = jdbc.queryForObject("""
                INSERT INTO dbo.propriedade_recursos_hidricos
                    (propriedade_id,nome,tipo,ativo,capacidade_litros,volume_atual_litros,versao)
                OUTPUT INSERTED.id VALUES (?,'Caixa SQL 2','CAIXA_DAGUA',1,1000,100.250,0)
                """, Long.class, propriedade);
        jdbc.update("""
                INSERT INTO dbo.agua_operacoes
                    (recurso_origem_id,recurso_destino_id,tipo,data_operacao,quantidade_litros,
                     volume_origem_antes_litros,volume_origem_depois_litros,
                     volume_destino_antes_litros,volume_destino_depois_litros,chave_idempotencia,versao)
                VALUES (?,?,'TRANSFERENCIA',SYSDATETIME(),25.125,500.125,475.000,100.250,125.375,'agua-sql-1',0)
                """, origem, destino);
        assertThat(jdbc.queryForObject(
                "SELECT quantidade_litros FROM dbo.agua_operacoes WHERE chave_idempotencia='agua-sql-1'",
                java.math.BigDecimal.class)).isEqualByComparingTo("25.125");
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO dbo.agua_operacoes
                    (recurso_origem_id,tipo,data_operacao,quantidade_litros,chave_idempotencia,versao)
                VALUES (?,'TRANSFERENCIA',SYSDATETIME(),10,'agua-sql-invalida',0)
                """, origem)).hasStackTraceContaining("ck_agua_operacoes_destino");
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO dbo.agua_operacoes
                    (recurso_origem_id,tipo,data_operacao,quantidade_litros,chave_idempotencia,versao)
                VALUES (?,'CONSUMO',SYSDATETIME(),1,'agua-sql-1',0)
                """, origem)).hasStackTraceContaining("uq_agua_operacoes_idempotencia");
    }
}
