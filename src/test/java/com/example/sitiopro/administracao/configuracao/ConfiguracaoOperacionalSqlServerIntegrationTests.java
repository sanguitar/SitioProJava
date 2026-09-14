package com.example.sitiopro.administracao.configuracao;

import com.example.sitiopro.administracao.configuracao.config.ConfiguracaoOperacionalInicialProperties;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.service.IncubacaoAvesService;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test", "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false",
        "sitiopro.configuracao-operacional.inicial.nome-propriedade=Sítio SQL",
        "sitiopro.configuracao-operacional.inicial.timezone=America/Porto_Velho",
        "sitiopro.configuracao-operacional.inicial.latitude=-8.123456",
        "sitiopro.configuracao-operacional.inicial.longitude=-63.123456",
        "sitiopro.configuracao-operacional.inicial.dias-padrao-incubacao=21",
        "sitiopro.configuracao-operacional.inicial.antecedencia-alerta-eclosao-dias=2"
})
class ConfiguracaoOperacionalSqlServerIntegrationTests {
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

    @Autowired private ConfiguracaoOperacionalService service;
    @Autowired private ConfiguracaoOperacionalInicialProperties iniciais;
    @Autowired private IncubacaoAvesService incubacaoService;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void v15ValidaComHibernateEInicializaUmUnicoRegistro() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1 AND version = '15'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM configuracoes_operacionais", Integer.class)).isEqualTo(1);
        var config = service.obter();
        assertThat(config.nomePropriedade()).isEqualTo("Sítio Guaratinguetá");
        assertThat(config.latitude()).isNull();
        assertThat(config.longitude()).isNull();
        assertThat(config.timezone()).isEqualTo("America/Porto_Velho");
        service.inicializar();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM configuracoes_operacionais", Integer.class)).isEqualTo(1);
    }

    @Test
    @Transactional
    void atualizacaoPersisteAuditoriaEIncubacaoUsaDiasDoBanco() {
        Usuario usuario = new Usuario();
        usuario.setLogin("admin-teste");
        usuario.setNome("Admin teste");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        try {
            var form = service.obter().paraFormulario();
            form.setNomePropriedade("Sítio alterado");
            form.setDiasPadraoIncubacao(23);
            form.setAntecedenciaAlertaEclosaoDias(4);
            form.setLatitude(new BigDecimal("-9.123456"));
            form.setLongitude(new BigDecimal("-63.654321"));
            service.atualizar(form);
            assertThat(jdbc.queryForObject("SELECT dias_padrao_incubacao FROM configuracoes_operacionais WHERE id=1",
                    Integer.class)).isEqualTo(23);
            assertThat(jdbc.queryForObject("SELECT alterado_por FROM configuracoes_operacionais WHERE id=1",
                    String.class)).isEqualTo("admin-teste");
            assertThat(service.obter().alteradoEm()).isNotNull();
            LocalDate inicio = LocalDate.of(2026, 9, 2);
            assertThat(incubacaoService.previsaoPadrao(EspecieAves.GALINHA, inicio)).isEqualTo(inicio.plusDays(23));
            assertThat(service.obter().revisaoLocalizacao()).isEqualTo(1);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @Transactional
    void reinicializacaoNaoSobrescreveValoresAdministrados() {
        var form = service.obter().paraFormulario();
        form.setDiasPadraoIncubacao(24);
        service.atualizar(form);
        int original = iniciais.getDiasPadraoIncubacao();
        try {
            iniciais.setDiasPadraoIncubacao(99);
            service.inicializar();
            assertThat(service.obter().diasPadraoIncubacao()).isEqualTo(24);
        } finally {
            iniciais.setDiasPadraoIncubacao(original);
        }
    }

    @Test
    void sqlRejeitaCoordenadasInvalidasEMultiplosRegistros() {
        assertThatThrownBy(() -> jdbc.update("UPDATE propriedades SET latitude_central=91 WHERE principal=1"))
                .hasStackTraceContaining("ck_propriedades_latitude");
        assertThatThrownBy(() -> jdbc.update(
                "UPDATE propriedades SET latitude_central=-8, longitude_central=NULL WHERE principal=1"))
                .hasStackTraceContaining("ck_propriedades_coordenadas");
        assertThatThrownBy(() -> jdbc.update("UPDATE configuracoes_operacionais SET id=2 WHERE id=1"))
                .hasStackTraceContaining("ck_config_operacionais_registro_unico");
        assertThatThrownBy(() -> jdbc.update("UPDATE configuracoes_operacionais SET dias_padrao_incubacao=0 WHERE id=1"))
                .hasStackTraceContaining("ck_config_operacionais_");
    }
}
