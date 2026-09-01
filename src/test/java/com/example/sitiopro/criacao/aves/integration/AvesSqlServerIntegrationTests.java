package com.example.sitiopro.criacao.aves.integration;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.aves.dto.TransferirLoteAvesRequest;
import com.example.sitiopro.criacao.aves.service.ManejoAvesService;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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
    @Autowired private CodigoCriacaoService codigoService;
    @Autowired private ManejoAvesService manejoService;

    @Test
    void flywayCriaSchemaDeAvesEHibernateValidaMapeamentos() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.tables WHERE name IN (
                  'criacao_instalacoes', 'aves_lotes', 'aves_eventos', 'aves_mortalidades',
                  'aves_alimentacoes', 'aves_pesagens', 'aves_posturas', 'aves_transferencias',
                  'aves_incubacoes', 'aves_incubacao_acompanhamentos', 'criacao_codigo_sequencias')
                """, Integer.class);
        Integer migration = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.flyway_schema_history
                WHERE version IN ('10', '11', '14') AND success = 1
                """, Integer.class);

        assertThat(tabelas).isEqualTo(11);
        assertThat(migration).isEqualTo(3);
    }

    @Test
    void geraCodigosConcorrentesSemDuplicidadeOuMaximoDeId() throws Exception {
        List<String> lotes = gerarEmParalelo(8, codigoService::proximoLoteAves);
        List<String> incubacoes = gerarEmParalelo(8, codigoService::proximaIncubacaoAves);

        assertThat(lotes).hasSize(8).doesNotHaveDuplicates()
                .allMatch(codigo -> codigo.matches("AV-\\d{4}-\\d{4}"));
        assertThat(incubacoes).hasSize(8).doesNotHaveDuplicates()
                .allMatch(codigo -> codigo.matches("INC-\\d{4}-\\d{4}"));
        assertThat(lotes).containsExactlyInAnyOrder(
                "AV-2026-0001", "AV-2026-0002", "AV-2026-0003", "AV-2026-0004",
                "AV-2026-0005", "AV-2026-0006", "AV-2026-0007", "AV-2026-0008");
        assertThat(incubacoes).containsExactlyInAnyOrder(
                "INC-2026-0001", "INC-2026-0002", "INC-2026-0003", "INC-2026-0004",
                "INC-2026-0005", "INC-2026-0006", "INC-2026-0007", "INC-2026-0008");
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
                (codigo, instalacao_id, metodo, especie, data_inicio, quantidade_ovos, data_prevista_eclosao, status,
                 pintinhos_eclodidos, ovos_perdidos, data_eclosao, chave_idempotencia)
                VALUES (?, ?, 'CHOCADEIRA', 'GALINHA', '2026-08-01', 10, '2026-08-22', 'FINALIZADA', 9, 2,
                        '2026-08-22', ?)
                """, "SQL-INC-" + System.nanoTime(), instalacaoId, "sql-inc-key-" + System.nanoTime()))
                .hasStackTraceContaining("ck_aves_incubacoes_ovos");
    }

    @Test
    void v14CriaAcompanhamentosComConstraintsEChaveAutomaticaDeTarefa() {
        Integer colunas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns
                WHERE object_id = OBJECT_ID('dbo.aves_incubacoes')
                  AND name IN ('metodo', 'especie', 'postura_origem_id',
                               'observacao_finalizacao', 'motivo_ajuste_previsao')
                """, Integer.class);
        Integer chaveTarefa = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.columns
                WHERE object_id = OBJECT_ID('dbo.tarefas') AND name = 'chave_automacao'
                """, Integer.class);
        Integer checks = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.check_constraints
                WHERE name IN ('ck_aves_incubacoes_metodo', 'ck_aves_incubacoes_especie',
                               'ck_aves_inc_acomp_tipo', 'ck_aves_inc_acomp_quantidades',
                               'ck_aves_inc_acomp_medicoes')
                """, Integer.class);
        Integer indices = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM sys.indexes
                WHERE name IN ('ix_aves_inc_acomp_incubacao_data', 'ux_tarefas_chave_automacao')
                """, Integer.class);

        assertThat(colunas).isEqualTo(5);
        assertThat(chaveTarefa).isEqualTo(1);
        assertThat(checks).isEqualTo(5);
        assertThat(indices).isEqualTo(2);
    }

    @Test
    void transferenciasConcorrentesNaoExcedemCapacidadeDaInstalacao() throws Exception {
        long origemA = criarInstalacao("Origem A " + System.nanoTime(), "GALINHEIRO", 100);
        long origemB = criarInstalacao("Origem B " + System.nanoTime(), "GALINHEIRO", 100);
        long destino = criarInstalacao("Destino limitado " + System.nanoTime(), "PIQUETE", 10);
        long loteA = criarLote(origemA, 6);
        long loteB = criarLote(origemB, 6);

        AtomicInteger indice = new AtomicInteger();
        List<Boolean> resultados = gerarEmParalelo(2, () -> {
            long loteId = indice.getAndIncrement() == 0 ? loteA : loteB;
            TransferirLoteAvesRequest request = new TransferirLoteAvesRequest();
            request.setInstalacaoDestinoId(destino);
            request.setChaveIdempotencia("transfer-audit-" + loteId + "-" + System.nanoTime());
            try {
                manejoService.transferir(loteId, request, new UsuarioAtor(null, "auditoria", true));
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        });

        Integer ocupacao = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(quantidade_atual), 0)
                FROM dbo.aves_lotes
                WHERE instalacao_atual_id = ? AND status = 'ATIVO'
                """, Integer.class, destino);
        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(ocupacao).isEqualTo(6);
    }

    private long criarInstalacao(String nome, String tipo) {
        return criarInstalacao(nome, tipo, 100);
    }

    private long criarInstalacao(String nome, String tipo, int capacidade) {
        jdbcTemplate.update("""
                INSERT INTO dbo.criacao_instalacoes (nome, tipo, capacidade, ativo)
                VALUES (?, ?, ?, 1)
                """, nome, tipo, capacidade);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dbo.criacao_instalacoes WHERE nome = ?", Long.class, nome);
    }

    private long criarLote(long instalacaoId, int quantidade) {
        String codigo = "SQL-LOTE-" + System.nanoTime();
        String chave = "sql-lote-key-" + System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO dbo.aves_lotes
                (codigo, especie, finalidade, origem, data_entrada, quantidade_inicial, quantidade_atual,
                 sexo, instalacao_atual_id, status, chave_idempotencia)
                VALUES (?, 'GALINHA', 'POSTURA', 'Auditoria', '2026-08-24', ?, ?,
                        'FEMEAS', ?, 'ATIVO', ?)
                """, codigo, quantidade, quantidade, instalacaoId, chave);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dbo.aves_lotes WHERE chave_idempotencia = ?", Long.class, chave);
    }

    private <T> List<T> gerarEmParalelo(int quantidade, Supplier<T> gerador) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(quantidade);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<T>> futuros = new ArrayList<>();
            for (int indice = 0; indice < quantidade; indice++) {
                futuros.add(executor.submit(() -> {
                    inicio.await();
                    return gerador.get();
                }));
            }
            inicio.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) resultados.add(futuro.get());
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }
}
