package com.example.sitiopro.criacao.peixes.integration;
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
@Testcontainers(disabledWithoutDocker=true)
@MockBean(name="openMeteoRestClient",classes=RestClient.class) @MockBean(name="agrofitRestClient",classes=RestClient.class) @MockBean(classes=SistemaSaudeService.class)
@SpringBootTest(properties={"spring.profiles.active=test","spring.jpa.hibernate.ddl-auto=validate","spring.flyway.enabled=true","sitiopro.initial-admin.enabled=false"})
class PeixesSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER=new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r){r.add("spring.datasource.url",SQLSERVER::getJdbcUrl);r.add("spring.datasource.username",SQLSERVER::getUsername);r.add("spring.datasource.password",SQLSERVER::getPassword);r.add("spring.flyway.user",SQLSERVER::getUsername);r.add("spring.flyway.password",SQLSERVER::getPassword);}
    @Autowired JdbcTemplate jdbc; @Autowired CodigoCriacaoService codigos;
    @Test void v27CriaSchemaEMapeamentosValidos(){Integer tabelas=jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name IN ('peixes_lotes','peixes_eventos')",Integer.class);Integer migration=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='27' AND success=1",Integer.class);assertThat(tabelas).isEqualTo(2);assertThat(migration).isOne();}
    @Test void codigoPeixesUsaSequenciaPropria(){assertThat(codigos.proximoLotePeixes()).matches("PX-\\d{4}-0001");assertThat(codigos.proximoLotePeixes()).matches("PX-\\d{4}-0002");}
    @Test void v27ProtegeTiposIdempotenciaMovimentoEPrecisao(){Integer checks=jdbc.queryForObject("SELECT COUNT(*) FROM sys.check_constraints WHERE name IN ('CK_peixes_lotes_quantidades','CK_peixes_lotes_peso','CK_peixes_lotes_status','CK_peixes_eventos_tipo','CK_peixes_eventos_quantidade','CK_peixes_eventos_valor')",Integer.class);Integer uniques=jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes WHERE object_id IN (OBJECT_ID('dbo.peixes_lotes'),OBJECT_ID('dbo.peixes_eventos')) AND name IN ('UQ_peixes_lotes_idempotencia','UQ_peixes_eventos_idempotencia','UQ_peixes_eventos_movimento')",Integer.class);assertThat(checks).isEqualTo(6);assertThat(uniques).isEqualTo(3);}
    @Test void tanquePisciculturaEPermitidoEStatusInvalidoRejeitado(){long tanque=jdbc.queryForObject("INSERT INTO dbo.criacao_instalacoes(nome,tipo,ativo,versao) OUTPUT INSERTED.id VALUES ('Tanque SQL','TANQUE_PISCICULTURA',1,0)",Long.class);assertThatThrownBy(()->jdbc.update("INSERT INTO dbo.peixes_lotes(codigo,especie,quantidade_inicial,quantidade_atual,data_entrada,origem,instalacao_atual_id,status,chave_idempotencia,versao) VALUES ('PX-X','Tambaqui',10,10,'2026-09-25','Teste',?,'INVALIDO','x',0)",tanque)).hasStackTraceContaining("CK_peixes_lotes_status");}
    @Test void v28CriaQualidadeAguaComDefaultsPrecisaoEIdempotencia(){Integer tabelas=jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name IN ('peixes_medicoes_qualidade_agua','peixes_qualidade_agua_configuracao')",Integer.class);Integer migration=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version='28' AND success=1",Integer.class);Integer config=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.peixes_qualidade_agua_configuracao WHERE id=1 AND temperatura_min=24.0000 AND ph_min=6.5000 AND intervalo_medicao_dias=7",Integer.class);Integer unique=jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.peixes_medicoes_qualidade_agua') AND name='UQ_peixes_medicoes_qualidade_idempotencia'",Integer.class);assertThat(tabelas).isEqualTo(2);assertThat(migration).isOne();assertThat(config).isOne();assertThat(unique).isOne();}
    @Test void v28ConstraintsRejeitamPhEIntervaloInvalidos(){assertThatThrownBy(()->jdbc.update("UPDATE dbo.peixes_qualidade_agua_configuracao SET ph_min=9,ph_max=8 WHERE id=1")).hasStackTraceContaining("CK_peixes_qualidade_config_ph");assertThatThrownBy(()->jdbc.update("UPDATE dbo.peixes_qualidade_agua_configuracao SET intervalo_medicao_dias=0 WHERE id=1")).hasStackTraceContaining("CK_peixes_qualidade_config_intervalo");}
}
