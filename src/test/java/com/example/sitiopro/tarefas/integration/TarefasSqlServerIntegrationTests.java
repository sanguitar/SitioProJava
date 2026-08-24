package com.example.sitiopro.tarefas.integration;

import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.AutomacaoTarefasService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false",
        "sitiopro.tarefas.scheduler-enabled=false"
})
class TarefasSqlServerIntegrationTests {

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

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TarefaService tarefaService;
    @Autowired
    private AlertaService alertaService;
    @Autowired
    private AutomacaoTarefasService automacaoService;

    private Usuario usuario;

    @BeforeEach
    void prepararUsuario() {
        usuario = usuarioRepository.findByLogin("tarefas_integracao").orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setNome("Tarefas Integração");
            novo.setLogin("tarefas_integracao");
            novo.setSenhaHash("{noop}teste-somente-integracao");
            novo.setPerfil(PerfilUsuario.ADMIN);
            novo.setAtivo(true);
            return usuarioRepository.save(novo);
        });
    }

    @Test
    void flywayCriaSchemaConstraintsEIndicesDoModulo() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.tables
                WHERE name IN ('tarefas', 'tarefa_recorrencias', 'alertas', 'tarefa_alerta_eventos')
                """, Integer.class);
        Integer indicesUnicos = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.indexes
                WHERE name IN ('ux_tarefas_recorrencia_ocorrencia', 'ux_alertas_chave_aberta')
                """, Integer.class);

        assertThat(tabelas).isEqualTo(4);
        assertThat(indicesUnicos).isEqualTo(2);
    }

    @Test
    void recorrenciaPersistidaPermaneceIdempotenteEmExecucoesRepetidas() {
        TarefaRequest request = new TarefaRequest();
        request.setTitulo("Recorrência integração " + System.nanoTime());
        request.setPrioridade(PrioridadeTarefa.NORMAL);
        request.setResponsavelId(usuario.getId());
        request.setDataVencimento(LocalDateTime.now().minusDays(2));
        request.setRecorrencia(TipoRecorrencia.DIARIA);
        var modelo = tarefaService.criar(request, ator());

        int primeiraExecucao = automacaoService.gerarRecorrencias();
        Integer antes = contarOcorrencias(modelo.id());
        int segundaExecucao = automacaoService.gerarRecorrencias();
        Integer depois = contarOcorrencias(modelo.id());

        assertThat(primeiraExecucao).isGreaterThanOrEqualTo(1);
        assertThat(segundaExecucao).isZero();
        assertThat(depois).isEqualTo(antes);
    }

    @Test
    void alertaAbertoEDeduplicadoEReaparecimentoCriaNovoHistorico() {
        String chave = "ESTOQUE:ITEM:INTEGRACAO:" + System.nanoTime();
        CondicaoAlerta condicao = new CondicaoAlerta(chave, "Estoque mínimo de integração",
                "Saldo abaixo do mínimo.", SeveridadeAlerta.ALTA, "ITEM:INTEGRACAO",
                Map.of("saldo", BigDecimal.ONE));

        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(condicao));
        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(condicao));
        assertThat(contarAlertas(chave)).isEqualTo(1);

        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of());
        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.ESTOQUE_ABAIXO_MINIMO, List.of(condicao));

        assertThat(contarAlertas(chave)).isEqualTo(2);
        assertThat(contarAlertasAbertos(chave)).isEqualTo(1);
    }

    private Integer contarOcorrencias(Long tarefaModeloId) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.tarefas t
                JOIN dbo.tarefa_recorrencias r ON r.id = t.recorrencia_id
                WHERE r.tarefa_modelo_id = ?
                """, Integer.class, tarefaModeloId);
    }

    private Integer contarAlertas(String chave) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.alertas WHERE chave_deduplicacao = ?", Integer.class, chave);
    }

    private Integer contarAlertasAbertos(String chave) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.alertas
                WHERE chave_deduplicacao = ? AND resolvido_em IS NULL
                """, Integer.class, chave);
    }

    private UsuarioAtor ator() {
        return new UsuarioAtor(usuario.getId(), usuario.getLogin(), true);
    }
}
