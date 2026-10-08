package com.example.sitiopro.manutencao.integration;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.*;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name="openMeteoRestClient",classes=RestClient.class)
@MockBean(name="agrofitRestClient",classes=RestClient.class)
@MockBean(classes=SistemaSaudeService.class)
@SpringBootTest(properties={"spring.profiles.active=test","spring.jpa.hibernate.ddl-auto=validate","spring.flyway.enabled=true","sitiopro.initial-admin.enabled=false"})
class ManutencaoSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER=new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r){r.add("spring.datasource.url",SQLSERVER::getJdbcUrl);r.add("spring.datasource.username",SQLSERVER::getUsername);r.add("spring.datasource.password",SQLSERVER::getPassword);r.add("spring.flyway.user",SQLSERVER::getUsername);r.add("spring.flyway.password",SQLSERVER::getPassword);}
    @Autowired JdbcTemplate jdbc; @Autowired CodigoCriacaoService codigos;

    @Test void v30CriaSchemaConstraintsIndicesEValidaHibernate(){
        Integer migration=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='30' AND success=1",Integer.class);
        Integer tabelas=jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name IN ('patrimonio_ativos','manutencao_registros','manutencao_consumos')",Integer.class);
        Integer checks=jdbc.queryForObject("SELECT COUNT(*) FROM sys.check_constraints WHERE name IN ('CK_patrimonio_ativos_tipo','CK_patrimonio_ativos_status','CK_manutencao_registros_tipo','CK_manutencao_registros_custo','CK_manutencao_consumos_quantidade')",Integer.class);
        assertThat(migration).isOne();assertThat(tabelas).isEqualTo(3);assertThat(checks).isEqualTo(5);
    }

    @Test void codigoPatrimonialUsaSequenciaPropria(){
        assertThat(codigos.proximoAtivoPatrimonial()).matches("PAT-\\d{4}-0001");
        assertThat(codigos.proximoAtivoPatrimonial()).matches("PAT-\\d{4}-0002");
    }

    @Test void bancoRejeitaTipoCustoEConsumoInvalidos(){
        long ativo=jdbc.queryForObject("INSERT INTO dbo.patrimonio_ativos(codigo,nome,tipo,status,chave_idempotencia,versao) OUTPUT INSERTED.id VALUES ('PAT-SQL','Bomba','BOMBA','ATIVO','pat-sql',0)",Long.class);
        assertThatThrownBy(()->jdbc.update("INSERT INTO dbo.manutencao_registros(ativo_id,tipo,data_manutencao,descricao,responsavel_nome,custo,chave_idempotencia,versao) VALUES (?,'INVALIDO',SYSDATETIME(),'Teste','Operador',0,'man-tipo',0)",ativo)).hasStackTraceContaining("CK_manutencao_registros_tipo");
        assertThatThrownBy(()->jdbc.update("INSERT INTO dbo.manutencao_registros(ativo_id,tipo,data_manutencao,descricao,responsavel_nome,custo,chave_idempotencia,versao) VALUES (?,'PREVENTIVA',SYSDATETIME(),'Teste','Operador',-1,'man-custo',0)",ativo)).hasStackTraceContaining("CK_manutencao_registros_custo");
    }

    @Test void v31CriaPlanosLeiturasVinculoEConstraints(){
        Integer migration=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='31' AND success=1",Integer.class);
        Integer tabelas=jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name IN ('manutencao_planos_preventivos','manutencao_leituras_medidores')",Integer.class);
        Integer checks=jdbc.queryForObject("SELECT COUNT(*) FROM sys.check_constraints WHERE name IN ('CK_manutencao_planos_tipo','CK_manutencao_planos_intervalo','CK_manutencao_planos_referencia','CK_manutencao_leituras_tipo','CK_manutencao_leituras_valores','CK_manutencao_leituras_ajuste')",Integer.class);
        assertThat(migration).isOne();assertThat(tabelas).isEqualTo(2);assertThat(checks).isEqualTo(6);

        long ativo=jdbc.queryForObject("INSERT INTO dbo.patrimonio_ativos(codigo,nome,tipo,status,chave_idempotencia,versao) OUTPUT INSERTED.id VALUES ('PAT-V31','Trator','MAQUINA','ATIVO','pat-v31',0)",Long.class);
        long plano=jdbc.queryForObject("INSERT INTO dbo.manutencao_planos_preventivos(ativo_id,nome,tipo_periodicidade,intervalo,data_referencia,proxima_data,ativo,ciclo_atual,chave_idempotencia,versao) OUTPUT INSERTED.id VALUES (?,'Revisão','DIAS',30,'2026-10-01','2026-10-31',1,1,'plano-v31',0)",Long.class,ativo);
        jdbc.update("INSERT INTO dbo.manutencao_leituras_medidores(ativo_id,data_leitura,horimetro,tipo_leitura,chave_idempotencia,versao) VALUES (?,'2026-10-06',125.50,'OPERACIONAL','leitura-v31',0)",ativo);
        jdbc.update("INSERT INTO dbo.manutencao_registros(ativo_id,plano_preventivo_id,tipo,data_manutencao,descricao,responsavel_nome,custo,chave_idempotencia,versao) VALUES (?,?,'PREVENTIVA','2026-10-06','Revisão','Operador',0,'registro-v31',0)",ativo,plano);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.manutencao_registros WHERE plano_preventivo_id=?",Integer.class,plano)).isOne();
        assertThatThrownBy(()->jdbc.update("INSERT INTO dbo.manutencao_leituras_medidores(ativo_id,data_leitura,horimetro,tipo_leitura,chave_idempotencia,versao) VALUES (?,'2026-10-06',-1,'OPERACIONAL','leitura-v31-invalida',0)",ativo)).hasStackTraceContaining("CK_manutencao_leituras_valores");
    }
}
