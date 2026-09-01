package com.example.sitiopro.compras.integration;

import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.entity.TipoEmbalagem;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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
        Integer colunasApresentacao = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.columns
                WHERE object_id = OBJECT_ID(N'dbo.itens_compra')
                  AND name IN ('quantidade_volumes', 'tipo_embalagem', 'conteudo_por_volume',
                               'preco_por_volume', 'unidade_base')
                """, Integer.class);
        Integer constraintApresentacao = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys.check_constraints
                WHERE parent_object_id = OBJECT_ID(N'dbo.itens_compra')
                  AND name = 'ck_itens_compra_apresentacao_comercial'
                """, Integer.class);

        assertThat(tabelas).isEqualTo(3);
        assertThat(colunasOrigem).isEqualTo(3);
        assertThat(indiceIdempotencia).isEqualTo(1);
        assertThat(colunasApresentacao).isEqualTo(5);
        assertThat(constraintApresentacao).isEqualTo(1);
    }

    @Test
    void apresentacaoComercialPersisteEEntraNoEstoqueEmUnidadeBase() {
        CategoriaEstoque categoria = categoriaRepository.findByAtivaTrueOrderByNomeAsc().getFirst();
        UnidadeMedida unidadeKg = unidadeRepository.findByAtivaTrueOrderByNomeAsc().stream()
                .filter(unidade -> "KG".equals(unidade.getSigla()))
                .findFirst()
                .orElseThrow();
        LocalEstoque local = localRepository.findByAtivoTrueOrderByNomeAsc().getFirst();

        Long fornecedorId = criarFornecedor("Fornecedor embalagem ");
        Long itemId = criarItem("Ração embalagem ", categoria, unidadeKg, false, false);
        CompraRequest compraRequest = new CompraRequest();
        compraRequest.setFornecedorId(fornecedorId);
        compraRequest.setDataCompra(LocalDate.now());
        Long compraId = compraService.criarCompra(compraRequest).id();

        var rascunho = compraService.adicionarItem(compraId,
                apresentacaoRequest(itemId, TipoEmbalagem.SACO, "2", "60", "135", local.getId()));
        var confirmada = compraService.confirmarCompra(compraId);

        assertThat(rascunho.itens().getFirst().quantidadeEstoque()).isEqualByComparingTo("120");
        assertThat(rascunho.itens().getFirst().valorTotal()).isEqualByComparingTo("270");
        assertThat(confirmada.total()).isEqualByComparingTo("270");
        assertThat(movimentoService.saldoItemTotal(itemId)).isEqualByComparingTo("120");
        assertThat(movimentoService.ultimoPreco(itemId)).isEqualByComparingTo("2.25");

        Map<String, Object> persistido = jdbcTemplate.queryForMap("""
                SELECT quantidade_volumes, tipo_embalagem, conteudo_por_volume, preco_por_volume,
                       unidade_base, quantidade, custo_unitario, subtotal
                FROM dbo.itens_compra
                WHERE compra_id = ?
                """, compraId);
        assertThat(persistido.get("tipo_embalagem")).isEqualTo("SACO");
        assertThat((BigDecimal) persistido.get("quantidade_volumes")).isEqualByComparingTo("2");
        assertThat((BigDecimal) persistido.get("conteudo_por_volume")).isEqualByComparingTo("60");
        assertThat((BigDecimal) persistido.get("preco_por_volume")).isEqualByComparingTo("135");
        assertThat(persistido.get("unidade_base")).isEqualTo("KG");
        assertThat((BigDecimal) persistido.get("quantidade")).isEqualByComparingTo("120");
        assertThat((BigDecimal) persistido.get("subtotal")).isEqualByComparingTo("270");
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
        Integer itensLegados = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.itens_compra
                WHERE compra_id = ?
                  AND quantidade_volumes IS NULL
                  AND tipo_embalagem IS NULL
                  AND conteudo_por_volume IS NULL
                  AND preco_por_volume IS NULL
                  AND unidade_base IS NULL
                """, Integer.class, compraId);
        assertThat(movimentosVinculados).isEqualTo(2);
        assertThat(itensLegados).isEqualTo(2);
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

    private ItemCompraRequest apresentacaoRequest(Long itemId, TipoEmbalagem tipo, String volumes,
            String conteudo, String preco, Long localId) {
        ItemCompraRequest request = new ItemCompraRequest();
        request.setItemEstoqueId(itemId);
        request.setTipoEmbalagem(tipo);
        request.setQuantidadeVolumes(new BigDecimal(volumes));
        request.setConteudoPorVolume(new BigDecimal(conteudo));
        request.setPrecoPorVolume(new BigDecimal(preco));
        request.setLocalDestinoId(localId);
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
