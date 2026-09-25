package com.example.sitiopro.criacao.suinos.integration;

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
@MockBean(name="openMeteoRestClient",classes=RestClient.class)
@MockBean(name="agrofitRestClient",classes=RestClient.class)
@MockBean(classes=SistemaSaudeService.class)
@SpringBootTest(properties={"spring.profiles.active=test","spring.jpa.hibernate.ddl-auto=validate","spring.flyway.enabled=true","sitiopro.initial-admin.enabled=false"})
class SuinosSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER=new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r){r.add("spring.datasource.url",SQLSERVER::getJdbcUrl);r.add("spring.datasource.username",SQLSERVER::getUsername);r.add("spring.datasource.password",SQLSERVER::getPassword);r.add("spring.flyway.user",SQLSERVER::getUsername);r.add("spring.flyway.password",SQLSERVER::getPassword);}
    @Autowired JdbcTemplate jdbc; @Autowired CodigoCriacaoService codigos;
    @Test void v24AV26CriamSchemaEMapeamentosValidos(){Integer tabelas=jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name IN ('suinos_lotes','suinos_eventos','suinos_animais_reprodutivos','suinos_ciclos_reprodutivos','suinos_registros_sanitarios')",Integer.class);Integer migrations=jdbc.queryForObject("SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE version IN ('24','25','26') AND success=1",Integer.class);assertThat(tabelas).isEqualTo(5);assertThat(migrations).isEqualTo(3);}
    @Test void codigoSuinoUsaSequenciaPropria(){assertThat(codigos.proximoLoteSuinos()).matches("SU-\\d{4}-0001");assertThat(codigos.proximoLoteSuinos()).matches("SU-\\d{4}-0002");}
    @Test void codigosReprodutivosUsamSequenciasProprias(){assertThat(codigos.proximoAnimalSuinos()).matches("SR-\\d{4}-0001");assertThat(codigos.proximaReproducaoSuinos()).matches("GES-\\d{4}-0001");}
    @Test void v26ProtegeAlvoTipoConsumoEIdempotencia(){Integer constraints=jdbc.queryForObject("SELECT COUNT(*) FROM sys.check_constraints WHERE name IN ('CK_suinos_registros_alvo','CK_suinos_registros_tipo','CK_suinos_registros_consumo','CK_suinos_registros_proxima_acao')",Integer.class);Integer indices=jdbc.queryForObject("SELECT COUNT(*) FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.suinos_registros_sanitarios') AND name IN ('IX_suinos_registros_lote_data','IX_suinos_registros_animal_data','IX_suinos_registros_proxima_acao','UQ_suinos_registros_idempotencia')",Integer.class);assertThat(constraints).isEqualTo(4);assertThat(indices).isEqualTo(4);}
    @Test void constraintsRecusamCategoriaECoordenacaoInvalidas(){long instalacao=jdbc.queryForObject("INSERT INTO dbo.criacao_instalacoes(nome,tipo,ativo,versao) OUTPUT INSERTED.id VALUES ('Pocilga SQL','OUTRO',1,0)",Long.class);assertThatThrownBy(()->jdbc.update("INSERT INTO dbo.suinos_lotes(codigo,categoria,data_entrada,origem,quantidade_inicial,quantidade_atual,instalacao_atual_id,status,chave_idempotencia,versao) VALUES ('SU-X','INVALIDA','2026-09-24','Teste',1,1,?,'ATIVO','x',0)",instalacao)).hasStackTraceContaining("CK_suinos_lotes_categoria");}
}
