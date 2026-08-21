package com.example.sitiopro.estoque.integration;

import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
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
}
