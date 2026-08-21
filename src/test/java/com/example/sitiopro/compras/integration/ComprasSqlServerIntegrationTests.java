package com.example.sitiopro.compras.integration;

import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.compras.service.ComprasOperacaoException;
import com.example.sitiopro.compras.service.FornecedorService;
import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false"
})
class ComprasSqlServerIntegrationTests {

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
    private FornecedorService fornecedorService;

    @Autowired
    private CompraService compraService;

    @Autowired
    private EstoqueCatalogoService catalogoService;

    @Autowired
    private EstoqueMovimentoService movimentoService;

    @Test
    void flywayCriaSchemaComprasNoSqlServer() {
        Integer tabelas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.tables
                WHERE name IN ('fornecedores', 'compras', 'itens_compra')
                """, Integer.class);
        Integer colunasOrigem = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns
                WHERE object_id = OBJECT_ID(N'dbo.estoque_movimentos')
                  AND name IN ('origem_modulo', 'origem_referencia_id', 'origem_descricao')
                """, Integer.class);
        Integer indiceIdempotencia = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.indexes
                WHERE name = 'ux_itens_compra_movimento'
                  AND object_id = OBJECT_ID(N'dbo.itens_compra')
                """, Integer.class);

        assertThat(tabelas).isEqualTo(3);
        assertThat(colunasOrigem).isEqualTo(3);
        assertThat(indiceIdempotencia).isEqualTo(1);
    }

    @Test
    void compraConfirmadaGeraMovimentosESaldosNoSqlServer() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        Long fornecedorId = criarFornecedor("Fornecedor integração ");
        Long itemRacaoId = criarItem("Compra ração integração ", categoria, unidade, false, false);
        Long itemLoteId = criarItem("Compra lote integração ", categoria, unidade, true, true);

        CompraRequest compraRequest = new CompraRequest();
        compraRequest.setFornecedorId(fornecedorId);
        compraRequest.setDataCompra(LocalDate.now());
        compraRequest.setFrete(new BigDecimal("10.00"));
        compraRequest.setDesconto(new BigDecimal("1.00"));
        Long compraId = compraService.criarCompra(compraRequest).id();

        compraService.adicionarItem(compraId, itemRequest(itemRacaoId, "5", "3.50", local.getId(), null, null));
        compraService.adicionarItem(compraId, itemRequest(itemLoteId, "2", "9.00", local.getId(), "L-INT",
                LocalDate.now().plusMonths(6)));

        var confirmada = compraService.confirmarCompra(compraId);

        assertThat(confirmada.status()).isEqualTo(StatusCompra.CONFIRMADA);
        assertThat(confirmada.total()).isEqualByComparingTo("44.5000");
        assertThat(confirmada.itens()).extracting("movimentoEstoqueId").doesNotContainNull();
        assertThat(movimentoService.saldoItemTotal(itemRacaoId)).isEqualByComparingTo("5.0000");
        assertThat(movimentoService.ultimoPreco(itemRacaoId)).isEqualByComparingTo("3.5000");

        Integer movimentosVinculados = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.itens_compra
                WHERE compra_id = ?
                  AND movimento_estoque_id IS NOT NULL
                """, Integer.class, compraId);
        assertThat(movimentosVinculados).isEqualTo(2);
    }

    @Test
    void segundaConfirmacaoNaoDuplicaMovimentos() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidade = unidadeRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        Long fornecedorId = criarFornecedor("Fornecedor idempotência ");
        Long itemId = criarItem("Compra idempotência ", categoria, unidade, false, false);

        CompraRequest compraRequest = new CompraRequest();
        compraRequest.setFornecedorId(fornecedorId);
        compraRequest.setDataCompra(LocalDate.now());
        Long compraId = compraService.criarCompra(compraRequest).id();
        compraService.adicionarItem(compraId, itemRequest(itemId, "1", "2.00", local.getId(), null, null));

        compraService.confirmarCompra(compraId);

        Integer antes = contarMovimentosDaCompra(compraId);
        assertThatThrownBy(() -> compraService.confirmarCompra(compraId))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("COMPRA_JA_CONFIRMADA");
        Integer depois = contarMovimentosDaCompra(compraId);

        assertThat(depois).isEqualTo(antes);
    }

    private Long criarFornecedor(String prefixo) {
        FornecedorRequest request = new FornecedorRequest();
        request.setNome(prefixo + System.nanoTime());
        return fornecedorService.criar(request).id();
    }

    private Long criarItem(String prefixo, CategoriaEstoque categoria, UnidadeMedida unidade,
            boolean controlaLote, boolean controlaValidade) {
        ItemEstoqueRequest request = new ItemEstoqueRequest();
        request.setNome(prefixo + System.nanoTime());
        request.setCategoriaId(categoria.getId());
        request.setUnidadeMedidaId(unidade.getId());
        request.setControlaLote(controlaLote);
        request.setControlaValidade(controlaValidade);
        return catalogoService.criarItem(request).getId();
    }

    private ItemCompraRequest itemRequest(Long itemId, String quantidade, String custoUnitario, Long localId,
            String lote, LocalDate validade) {
        ItemCompraRequest request = new ItemCompraRequest();
        request.setItemEstoqueId(itemId);
        request.setQuantidade(new BigDecimal(quantidade));
        request.setCustoUnitario(new BigDecimal(custoUnitario));
        request.setLocalDestinoId(localId);
        request.setLoteCodigo(lote);
        request.setValidade(validade);
        return request;
    }

    private Integer contarMovimentosDaCompra(Long compraId) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.estoque_movimentos
                WHERE origem_modulo = 'compras'
                  AND origem_referencia_id = ?
                """, Integer.class, compraId);
    }
}
