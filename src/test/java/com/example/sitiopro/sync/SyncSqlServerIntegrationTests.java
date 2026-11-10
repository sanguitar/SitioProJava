package com.example.sitiopro.sync;

import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false",
        "sitiopro.tarefas.scheduler-enabled=false"
})
class SyncSqlServerIntegrationTests {
    @Container
    static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.flyway.user", SQLSERVER::getUsername);
        registry.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired SyncService service;
    @Autowired UsuarioRepository usuarios;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    private Usuario usuario;
    private Long recursoId;

    @BeforeEach
    void preparar() {
        usuario = usuarios.findByLogin("sync_integracao").orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setNome("Sync Integração");
            novo.setLogin("sync_integracao");
            novo.setSenhaHash("{noop}teste-somente-integracao");
            novo.setPerfil(PerfilUsuario.ADMIN);
            novo.setAtivo(true);
            return usuarios.save(novo);
        });
        Long propriedade = jdbc.queryForObject(
                "SELECT TOP 1 id FROM dbo.propriedades WHERE principal=1", Long.class);
        recursoId = jdbc.queryForObject("""
                INSERT INTO dbo.propriedade_recursos_hidricos
                    (propriedade_id,nome,tipo,ativo,capacidade_litros,volume_atual_litros,versao)
                OUTPUT INSERTED.id VALUES (?,'Caixa Sync ' + CONVERT(varchar(36),NEWID()),
                    'CAIXA_DAGUA',1,1000,500,0)
                """, Long.class, propriedade);
    }

    @Test
    void v35AV37CriamIdentidadeIdempotenciaChangeLogETriggers() {
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM dbo.flyway_schema_history
                WHERE version IN ('35','36','37') AND success=1
                """, Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.columns
                WHERE name='public_id' AND object_id IN (OBJECT_ID('dbo.tarefas'),OBJECT_ID('dbo.agua_operacoes'))
                """, Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.tables WHERE name IN ('api_idempotency','sync_change_log')
                """, Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.triggers
                WHERE name IN ('tr_tarefas_sync_change','tr_agua_medicao_sync_change')
                """, Integer.class)).isEqualTo(2);
    }

    @Test
    void bootstrapCriacaoAtualizacaoPullCursorEPaginacaoFuncionam() {
        long cursorInicial = service.bootstrap().cursor();
        UUID device = UUID.randomUUID();
        UUID tarefaId = UUID.randomUUID();
        UUID operacaoCriacao = UUID.randomUUID();

        var criacao = service.sincronizar(request(device, cursorInicial, 1,
                tarefa(operacaoCriacao, tarefaId, null, "Tarefa offline", StatusTarefa.PENDENTE)), ator());
        assertThat(criacao.results()).singleElement().satisfies(resultado -> {
            assertThat(resultado.entityId()).isEqualTo(tarefaId);
            assertThat(resultado.version()).isZero();
        });
        assertThat(criacao.changes()).hasSize(1);
        assertThat(criacao.hasMore()).isFalse();

        var atualizacao = service.sincronizar(request(device, criacao.nextCursor(), 1,
                tarefa(UUID.randomUUID(), tarefaId, 0L, "Tarefa atualizada", StatusTarefa.EM_ANDAMENTO)), ator());
        assertThat(atualizacao.results()).singleElement()
                .satisfies(resultado -> assertThat(resultado.version()).isGreaterThan(0));
        assertThat(atualizacao.changes()).hasSize(1);
        assertThat(atualizacao.hasMore()).isTrue();
        assertThat(atualizacao.nextCursor()).isGreaterThan(criacao.nextCursor());
        var proximaPagina = service.pull(atualizacao.nextCursor(), 1);
        assertThat(proximaPagina.changes()).hasSize(1);
        assertThat(proximaPagina.nextCursor()).isGreaterThan(atualizacao.nextCursor());
        assertThat(service.bootstrap().entities()).anySatisfy(change -> {
            if (change.entityId().equals(tarefaId)) {
                assertThat(change.payload().path("titulo").asText()).isEqualTo("Tarefa atualizada");
            }
        });
    }

    @Test
    void operacaoDuplicadaRetornaRespostaAnteriorEPayloadDivergenteDa409() {
        UUID device = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        UUID tarefaId = UUID.randomUUID();
        SyncDtos.SyncRequest original = request(device, 0, 500,
                tarefa(operation, tarefaId, null, "Idempotente", StatusTarefa.PENDENTE));

        var primeira = service.sincronizar(original, ator());
        var repetida = service.sincronizar(original, ator());
        assertThat(repetida.results()).singleElement().satisfies(resultado -> {
            assertThat(resultado.operationId()).isEqualTo(primeira.results().getFirst().operationId());
            assertThat(resultado.entityId()).isEqualTo(primeira.results().getFirst().entityId());
            assertThat(resultado.version()).isEqualTo(primeira.results().getFirst().version());
            assertThat(resultado.payload().toString())
                    .isEqualTo(primeira.results().getFirst().payload().toString());
        });
        assertThat(contarPorUuid("tarefas", tarefaId)).isOne();
        assertThat(contarIdempotencias(operation)).isOne();

        assertThatThrownBy(() -> service.sincronizar(request(device, 0, 500,
                tarefa(operation, tarefaId, primeira.results().getFirst().version(),
                        "Conteúdo divergente", StatusTarefa.PENDENTE)), ator()))
                .isInstanceOfSatisfying(SyncException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getCode()).isEqualTo("OPERATION_ID_REUTILIZADA");
                });
    }

    @Test
    void versaoDesatualizadaRetornaConflitoServerWinsSemSobrescrever() {
        UUID device = UUID.randomUUID();
        UUID tarefaId = UUID.randomUUID();
        var criada = service.sincronizar(request(device, 0, 500,
                tarefa(UUID.randomUUID(), tarefaId, null, "Versão original", StatusTarefa.PENDENTE)), ator());
        long versao = criada.results().getFirst().version();
        service.sincronizar(request(device, criada.nextCursor(), 500,
                tarefa(UUID.randomUUID(), tarefaId, versao, "Versão vencedora", StatusTarefa.EM_ANDAMENTO)), ator());

        var obsoleta = service.sincronizar(request(device, 0, 500,
                tarefa(UUID.randomUUID(), tarefaId, versao, "Versão obsoleta", StatusTarefa.PENDENTE)), ator());
        assertThat(obsoleta.results()).isEmpty();
        assertThat(obsoleta.conflicts()).singleElement().satisfies(conflito -> {
            assertThat(conflito.resolution()).isEqualTo("SERVER_WINS");
            assertThat(conflito.currentVersion()).isGreaterThan(versao);
            assertThat(conflito.serverSnapshot().path("titulo").asText()).isEqualTo("Versão vencedora");
        });
    }

    @Test
    void medicaoAguaUsaUuidRetrySeguroEChangeLog() {
        UUID device = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        UUID medicaoId = UUID.randomUUID();
        long cursor = service.bootstrap().cursor();
        SyncDtos.SyncCommand medicao = agua(operation, medicaoId, Instant.now().minusSeconds(60), "321.125");

        var primeira = service.sincronizar(request(device, cursor, 500, medicao), ator());
        var repetida = service.sincronizar(request(device, cursor, 500, medicao), ator());
        assertThat(repetida.results()).singleElement().satisfies(resultado -> {
            assertThat(resultado.operationId()).isEqualTo(operation);
            assertThat(resultado.entityId()).isEqualTo(medicaoId);
            assertThat(resultado.version()).isEqualTo(primeira.results().getFirst().version());
            assertThat(resultado.payload().path("quantidadeLitros").decimalValue())
                    .isEqualByComparingTo(primeira.results().getFirst().payload()
                            .path("quantidadeLitros").decimalValue());
        });
        assertThat(contarPorUuid("agua_operacoes", medicaoId)).isOne();
        assertThat(primeira.changes()).anySatisfy(change -> {
            if (change.entityId().equals(medicaoId)) {
                assertThat(change.entityType()).isEqualTo(SyncDtos.EntityType.AGUA_MEDICAO);
                assertThat(change.payload().path("quantidadeLitros").decimalValue())
                        .isEqualByComparingTo("321.125");
            }
        });
    }

    @Test
    void falhaFazRollbackCompletoEPermiteRetryDaOperationId() {
        UUID device = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        UUID medicaoId = UUID.randomUUID();
        SyncDtos.SyncCommand invalida = agua(operation, medicaoId, Instant.now().plusSeconds(3600), "200");

        assertThatThrownBy(() -> service.sincronizar(request(device, 0, 500, invalida), ator()))
                .isInstanceOf(RuntimeException.class);
        assertThat(contarPorUuid("agua_operacoes", medicaoId)).isZero();
        assertThat(contarIdempotencias(operation)).isZero();

        var retry = service.sincronizar(request(device, 0, 500,
                agua(operation, medicaoId, Instant.now().minusSeconds(60), "200")), ator());
        assertThat(retry.results()).hasSize(1);
        assertThat(contarPorUuid("agua_operacoes", medicaoId)).isOne();
        assertThat(contarIdempotencias(operation)).isOne();
    }

    private SyncDtos.SyncRequest request(UUID device, long cursor, int limit, SyncDtos.SyncCommand... commands) {
        return new SyncDtos.SyncRequest(device, cursor, List.of(commands), limit);
    }

    private SyncDtos.SyncCommand tarefa(UUID operation, UUID entity, Long version,
            String titulo, StatusTarefa status) {
        JsonNode payload = objectMapper.valueToTree(new SyncDtos.TarefaPayload(titulo, "Criada offline",
                PrioridadeTarefa.NORMAL, null, usuario.getId(), TipoRecorrencia.NENHUMA, null, status));
        return new SyncDtos.SyncCommand(operation, SyncDtos.EntityType.TAREFA, entity, version,
                SyncDtos.Operation.UPSERT, payload);
    }

    private SyncDtos.SyncCommand agua(UUID operation, UUID entity, Instant data, String volume) {
        JsonNode payload = objectMapper.valueToTree(new SyncDtos.AguaMedicaoPayload(
                recursoId, data, new BigDecimal(volume), "Medição offline"));
        return new SyncDtos.SyncCommand(operation, SyncDtos.EntityType.AGUA_MEDICAO, entity, null,
                SyncDtos.Operation.UPSERT, payload);
    }

    private Integer contarPorUuid(String tabela, UUID id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM dbo." + tabela
                + " WHERE public_id=CAST(? AS UNIQUEIDENTIFIER)", Integer.class, id.toString());
    }

    private Integer contarIdempotencias(UUID operation) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM dbo.api_idempotency "
                + "WHERE operation_id=CAST(? AS UNIQUEIDENTIFIER)", Integer.class, operation.toString());
    }

    private UsuarioAtor ator() {
        return new UsuarioAtor(usuario.getId(), usuario.getLogin(), true);
    }
}
