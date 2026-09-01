package com.example.sitiopro.producao.integration;

import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.service.ProducaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

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
class ProducaoSqlServerIntegrationTests {

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
    private CategoriaEstoqueRepository categoriaRepository;

    @Autowired
    private EstoqueCatalogoService estoqueCatalogoService;

    @Autowired
    private ProducaoService producaoService;

    @Test
    void bancoComDuasCategoriasAtivasPermiteSalvarEReabrirCadastro() {
        CategoriaEstoque graos = novaCategoria("Grãos cadastro rural", true);
        CategoriaEstoque inativa = novaCategoria("Categoria rural inativa", false);

        List<CategoriaEstoque> ativas = estoqueCatalogoService.listarCategoriasAtivas();
        assertThat(ativas).extracting(CategoriaEstoque::getNome)
                .containsExactly("Geral", "Grãos cadastro rural")
                .doesNotContain(inativa.getNome());

        Producao salva = producaoService.salvar(form(graos.getId()));
        Long categoriaPersistida = jdbcTemplate.queryForObject(
                "SELECT estoque_categoria_id FROM dbo.producao WHERE id = ?",
                Long.class, salva.getId());

        assertThat(categoriaPersistida).isEqualTo(graos.getId());
        assertThat(producaoService.formularioEdicao(salva.getId()).getCategoriaId()).isEqualTo(graos.getId());

        CategoriaEstoque geral = ativas.getFirst();
        ProducaoForm edicao = form(geral.getId());
        edicao.setId(salva.getId());
        producaoService.salvar(edicao);

        Long categoriaAlterada = jdbcTemplate.queryForObject(
                "SELECT estoque_categoria_id FROM dbo.producao WHERE id = ?",
                Long.class, salva.getId());
        assertThat(categoriaAlterada).isEqualTo(geral.getId());
        assertThat(producaoService.formularioEdicao(salva.getId()).getCategoriaId()).isEqualTo(geral.getId());
    }

    @Test
    @Transactional
    void bancoRejeitaCategoriaInexistenteEInativaAntesDeSalvar() {
        CategoriaEstoque inativa = novaCategoria("Categoria bloqueada", false);

        assertThatThrownBy(() -> producaoService.salvar(form(Long.MAX_VALUE)))
                .isInstanceOf(EstoqueOperacaoException.class)
                .satisfies(ex -> assertThat(((EstoqueOperacaoException) ex).getCode())
                        .isEqualTo("CATEGORIA_INVALIDA"));
        assertThatThrownBy(() -> producaoService.salvar(form(inativa.getId())))
                .isInstanceOf(EstoqueOperacaoException.class)
                .satisfies(ex -> assertThat(((EstoqueOperacaoException) ex).getCode())
                        .isEqualTo("CATEGORIA_INVALIDA"));
    }

    @Test
    @Transactional
    void registroLegadoContinuaAcessivelSemInventarCategoriaDeEstoque() {
        jdbcTemplate.update("INSERT INTO dbo.categorias (nome) VALUES (?)", "Categoria legada");
        Long categoriaLegadaId = jdbcTemplate.queryForObject(
                "SELECT id FROM dbo.categorias WHERE nome = ?", Long.class, "Categoria legada");
        jdbcTemplate.update("""
                INSERT INTO dbo.producao (item, quantidade, unidade, status, categoria_id)
                VALUES (?, ?, ?, ?, ?)
                """, "Registro legado", 5, "unidade", "Estoque", categoriaLegadaId);
        Long producaoId = jdbcTemplate.queryForObject(
                "SELECT id FROM dbo.producao WHERE item = ?", Long.class, "Registro legado");

        ProducaoForm form = producaoService.formularioEdicao(producaoId);

        assertThat(form.getItem()).isEqualTo("Registro legado");
        assertThat(form.getCategoriaId()).isNull();
    }

    private CategoriaEstoque novaCategoria(String nome, boolean ativa) {
        CategoriaEstoque categoria = new CategoriaEstoque();
        categoria.setNome(nome);
        categoria.setDescricao("Fixture de integração do cadastro rural.");
        categoria.setAtiva(ativa);
        return categoriaRepository.save(categoria);
    }

    private ProducaoForm form(Long categoriaId) {
        ProducaoForm form = new ProducaoForm();
        form.setCategoriaId(categoriaId);
        form.setItem("Milho integração");
        form.setQuantidade(20);
        form.setUnidade("saca");
        form.setStatus("Estoque");
        return form;
    }
}
