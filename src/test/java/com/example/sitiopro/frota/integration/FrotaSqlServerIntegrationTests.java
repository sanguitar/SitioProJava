package com.example.sitiopro.frota.integration;

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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {"spring.profiles.active=test", "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true", "sitiopro.initial-admin.enabled=false", "sitiopro.tarefas.scheduler-enabled=false"})
class FrotaSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", SQLSERVER::getJdbcUrl); r.add("spring.datasource.username", SQLSERVER::getUsername);
        r.add("spring.datasource.password", SQLSERVER::getPassword); r.add("spring.flyway.user", SQLSERVER::getUsername);
        r.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired JdbcTemplate jdbc;

    @Test
    void v33ConverteDecimaisCriaVinculoVersionamentoEValidaHibernate() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='33' AND success=1", Integer.class)).isOne();
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.columns WHERE
                  (object_id=OBJECT_ID('dbo.veiculos') AND name IN ('km_atual','valor_fipe','horimetro_atual') AND system_type_id=106)
                  OR (object_id=OBJECT_ID('dbo.abastecimentos') AND name IN ('litros','preco_por_litro','valor_total','km_no_ato','horimetro_no_ato') AND system_type_id=106)
                  OR (object_id=OBJECT_ID('dbo.fipe_cache') AND name='valor' AND system_type_id=106)
                """, Integer.class)).isEqualTo(9);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.foreign_keys WHERE name='FK_veiculos_ativo_patrimonial'", Integer.class)).isOne();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes WHERE name IN ('UX_veiculos_ativo_patrimonial','IX_abastecimentos_veiculo_data')", Integer.class)).isEqualTo(2);
    }

    @Test
    void preservaPrecisaoERejeitaIdempotenciaDuplicada() {
        long veiculo = jdbc.queryForObject("""
                INSERT INTO dbo.veiculos(nome,tipo,situacao,tipo_medidor,chave_idempotencia,versao)
                OUTPUT INSERTED.id VALUES ('TRATOR SQL','3','DISPONIVEL','HORIMETRO','veiculo-sql-1',0)
                """, Long.class);
        jdbc.update("""
                INSERT INTO dbo.abastecimentos(veiculo_id,[data],litros,preco_por_litro,valor_total,
                  horimetro_no_ato,[local],chave_idempotencia,versao)
                VALUES (?,CAST(GETDATE() AS date),12.345,6.7891,83.81,25.50,'Posto','ab-sql-1',0)
                """, veiculo);
        assertThat(jdbc.queryForObject("SELECT litros FROM dbo.abastecimentos WHERE chave_idempotencia='ab-sql-1'", BigDecimal.class))
                .isEqualByComparingTo("12.345");
        assertThat(jdbc.queryForObject("SELECT preco_por_litro FROM dbo.abastecimentos WHERE chave_idempotencia='ab-sql-1'", BigDecimal.class))
                .isEqualByComparingTo("6.7891");
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO dbo.abastecimentos(veiculo_id,[data],litros,preco_por_litro,valor_total,
                  horimetro_no_ato,[local],chave_idempotencia,versao)
                VALUES (?,CAST(GETDATE() AS date),1,1,1,26,'Posto','ab-sql-1',0)
                """, veiculo)).hasStackTraceContaining("UQ_abastecimentos_idempotencia");
    }
}
