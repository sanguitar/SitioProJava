package com.example.sitiopro.estoque.integration;

import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueResponse;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
class EstoqueSqlServerIntegrationTests {

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
    private CategoriaEstoqueRepository categoriaRepository;

    @Autowired
    private UnidadeMedidaRepository unidadeRepository;

    @Autowired
    private LocalEstoqueRepository localRepository;

    @Autowired
    private EstoqueCatalogoService catalogoService;

    @Autowired
    private EstoqueMovimentoService movimentoService;

    @Test
    void flywayCriaSchemaEstoqueNoSqlServer() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.tables
                WHERE name IN (
                  'estoque_categorias',
                  'estoque_unidades_medida',
                  'estoque_locais',
                  'estoque_itens',
                  'estoque_lotes',
                  'estoque_movimentos'
                )
                """, Integer.class);
        Integer constraintQuantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.check_constraints
                WHERE name = 'ck_estoque_movimentos_quantidade'
                """, Integer.class);

        assertThat(tabelas).isEqualTo(6);
        assertThat(constraintQuantidade).isEqualTo(1);
    }

    @Test
    void repositoriesEServicesPersistemItemEMovimentoNoSqlServer() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        ItemEstoqueRequest itemRequest = new ItemEstoqueRequest();
        itemRequest.setNome("Ração integração " + System.nanoTime());
        itemRequest.setCategoriaId(categoria.getId());
        itemRequest.setUnidadeMedidaId(unidade.getId());
        itemRequest.setEstoqueMinimo(new BigDecimal("80"));
        Long itemId = catalogoService.criarItem(itemRequest).getId();

        MovimentoEstoqueRequest movimentoRequest = new MovimentoEstoqueRequest();
        movimentoRequest.setItemId(itemId);
        movimentoRequest.setTipo(TipoMovimentoEstoque.ENTRADA);
        movimentoRequest.setQuantidade(new BigDecimal("100"));
        movimentoRequest.setLocalDestinoId(local.getId());
        movimentoRequest.setCustoUnitario(new BigDecimal("3.50"));
        movimentoService.registrarMovimento(movimentoRequest, true);

        assertThat(movimentoService.saldoItemTotal(itemId)).isEqualByComparingTo("100");
        assertThat(movimentoService.ultimoPreco(itemId)).isEqualByComparingTo("3.5000");
    }

    @Test
    void historicoEOrdenadoEPaginadoNoSqlServer() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        ItemEstoqueRequest itemRequest = new ItemEstoqueRequest();
        itemRequest.setNome("Ração paginação " + System.nanoTime());
        itemRequest.setCategoriaId(categoria.getId());
        itemRequest.setUnidadeMedidaId(unidade.getId());
        Long itemId = catalogoService.criarItem(itemRequest).getId();
        LocalDateTime inicio = LocalDateTime.of(2099, 1, 1, 0, 0);

        for (int indice = 0; indice < 25; indice++) {
            MovimentoEstoqueRequest request = movimento(
                    itemId, local.getId(), TipoMovimentoEstoque.ENTRADA, BigDecimal.ONE);
            request.setDataMovimento(inicio.plusMinutes(indice));
            movimentoService.registrarMovimento(request, false);
        }

        PaginaResponse<MovimentoEstoqueResponse> primeira = movimentoService.listarMovimentos(0, 20);
        PaginaResponse<MovimentoEstoqueResponse> segunda = movimentoService.listarMovimentos(1, 20);

        assertThat(primeira.conteudo()).hasSize(20);
        assertThat(primeira.conteudo()).extracting(MovimentoEstoqueResponse::dataMovimento)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(5, 24)
                        .mapToObj(indice -> inicio.plusMinutes(indice))
                        .sorted(java.util.Comparator.reverseOrder())
                        .toList());
        assertThat(segunda.conteudo().subList(0, 5)).extracting(MovimentoEstoqueResponse::dataMovimento)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(0, 4)
                        .mapToObj(indice -> inicio.plusMinutes(indice))
                        .sorted(java.util.Comparator.reverseOrder())
                        .toList());
        assertThat(primeira.totalElementos()).isGreaterThanOrEqualTo(25);
        assertThat(segunda.pagina()).isEqualTo(1);
    }

    @Test
    void constraintSqlServerRejeitaQuantidadeZero() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        ItemEstoqueRequest itemRequest = new ItemEstoqueRequest();
        itemRequest.setNome("Ração constraint " + System.nanoTime());
        itemRequest.setCategoriaId(categoria.getId());
        itemRequest.setUnidadeMedidaId(unidade.getId());
        Long itemId = catalogoService.criarItem(itemRequest).getId();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO dbo.estoque_movimentos
                (item_id, tipo, quantidade, local_destino_id, data_movimento)
                VALUES (?, 'ENTRADA', 0, ?, SYSUTCDATETIME())
                """, itemId, local.getId()))
                .hasMessageContaining("ck_estoque_movimentos_quantidade");
    }

    @Test
    void consumosConcorrentesNaoPermitemSaldoNegativo() throws Exception {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        ItemEstoqueRequest itemRequest = new ItemEstoqueRequest();
        itemRequest.setNome("Ração concorrência " + System.nanoTime());
        itemRequest.setCategoriaId(categoria.getId());
        itemRequest.setUnidadeMedidaId(unidade.getId());
        Long itemId = catalogoService.criarItem(itemRequest).getId();

        movimentoService.registrarMovimento(movimento(
                itemId, local.getId(), TipoMovimentoEstoque.ENTRADA, new BigDecimal("10")), false);

        List<Boolean> resultados = executarEmParalelo(2, () -> {
            try {
                movimentoService.registrarMovimento(movimento(
                        itemId, local.getId(), TipoMovimentoEstoque.CONSUMO, new BigDecimal("8")), false);
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        });

        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(movimentoService.saldoItemTotal(itemId)).isEqualByComparingTo("2");
    }

    private MovimentoEstoqueRequest movimento(Long itemId, Long localId,
            TipoMovimentoEstoque tipo, BigDecimal quantidade) {
        MovimentoEstoqueRequest request = new MovimentoEstoqueRequest();
        request.setItemId(itemId);
        request.setTipo(tipo);
        request.setQuantidade(quantidade);
        if (tipo == TipoMovimentoEstoque.ENTRADA) {
            request.setLocalDestinoId(localId);
        } else {
            request.setLocalOrigemId(localId);
        }
        return request;
    }

    private <T> List<T> executarEmParalelo(int quantidade, java.util.concurrent.Callable<T> operacao)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(quantidade);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<T>> futuros = new ArrayList<>();
            for (int indice = 0; indice < quantidade; indice++) {
                futuros.add(executor.submit(() -> {
                    inicio.await();
                    return operacao.call();
                }));
            }
            inicio.countDown();
            List<T> resultados = new ArrayList<>();
            for (Future<T> futuro : futuros) {
                resultados.add(futuro.get());
            }
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }
}
