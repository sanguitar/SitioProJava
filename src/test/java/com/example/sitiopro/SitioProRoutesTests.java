package com.example.sitiopro;

import com.example.sitiopro.administracao.configuracao.controller.ConfiguracaoOperacionalController;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.abastecimento.controller.AbastecimentoController;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.categoria.controller.CategoriaController;
import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.compras.controller.ComprasController;
import com.example.sitiopro.criacao.aves.dto.AvesResumo;
import com.example.sitiopro.criacao.aves.service.AvesResumoService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAvesService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAcompanhamentoService;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.aves.service.LoteAvesService;
import com.example.sitiopro.criacao.aves.service.ManejoAvesService;
import com.example.sitiopro.criacao.aves.web.CriacoesController;
import com.example.sitiopro.criacao.aves.web.IncubacoesAvesController;
import com.example.sitiopro.criacao.aves.web.InstalacoesAvesController;
import com.example.sitiopro.criacao.aves.web.LotesAvesController;
import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.CompraFiltro;
import com.example.sitiopro.compras.dto.CompraResumo;
import com.example.sitiopro.compras.dto.ComprasDashboardResumo;
import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.compras.service.FornecedorService;
import com.example.sitiopro.dashboard.api.DashboardApiController;
import com.example.sitiopro.dashboard.controller.DashboardController;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.integracao.controller.IntegracaoAdminController;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.estoque.api.EstoqueApiController;
import com.example.sitiopro.estoque.controller.EstoqueController;
import com.example.sitiopro.estoque.dto.EstoqueDashboardResumo;
import com.example.sitiopro.estoque.dto.ItemEstoqueDetalhe;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueResponse;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.frota.controller.VeiculoController;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.observability.controller.SistemaSaudeController;
import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.planejamento.controller.AdministracaoPlanejamentoController;
import com.example.sitiopro.planejamento.controller.AgriculturaPlanejamentoController;
import com.example.sitiopro.planejamento.controller.AguaPlanejamentoController;
import com.example.sitiopro.planejamento.controller.CriacoesPlanejamentoController;
import com.example.sitiopro.planejamento.controller.PlanejamentoRedirectController;
import com.example.sitiopro.planejamento.controller.PropriedadePlanejamentoController;
import com.example.sitiopro.planejamento.controller.VeiculosPlanejamentoController;
import com.example.sitiopro.producao.controller.ProducaoController;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.service.ProducaoService;
import com.example.sitiopro.tarefas.controller.AlertaController;
import com.example.sitiopro.tarefas.controller.TarefaController;
import com.example.sitiopro.tarefas.dto.AlertaDetalhe;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaResumoOperacional;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.usuario.controller.UsuarioController;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static com.example.sitiopro.DashboardTestFixture.vazio;
import static com.example.sitiopro.DashboardTestFixture.comDestaques;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {
        DashboardController.class,
        DashboardApiController.class,
        ProducaoController.class,
        CategoriaController.class,
        VeiculoController.class,
        AbastecimentoController.class,
        EstoqueController.class,
        EstoqueApiController.class,
        ComprasController.class,
        TarefaController.class,
        AlertaController.class,
        CriacoesPlanejamentoController.class,
        CriacoesController.class,
        InstalacoesAvesController.class,
        LotesAvesController.class,
        IncubacoesAvesController.class,
        AgriculturaPlanejamentoController.class,
        AguaPlanejamentoController.class,
        PropriedadePlanejamentoController.class,
        VeiculosPlanejamentoController.class,
        AdministracaoPlanejamentoController.class,
        ConfiguracaoOperacionalController.class,
        PlanejamentoRedirectController.class,
        UsuarioController.class,
        SistemaSaudeController.class,
        IntegracaoAdminController.class
})
@WithMockUser(roles = "ADMIN")
class SitioProRoutesTests {

    @MockBean
    private ConfiguracaoOperacionalService configuracaoOperacionalService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private ProducaoService producaoService;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private VeiculoService veiculoService;

    @MockBean
    private AbastecimentoService abastecimentoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EstoqueCatalogoService estoqueCatalogoService;

    @MockBean
    private EstoqueMovimentoService estoqueMovimentoService;

    @MockBean
    private SistemaSaudeService sistemaSaudeService;

    @MockBean
    private CompraService compraService;

    @MockBean
    private FornecedorService fornecedorService;

    @MockBean
    private IntegracaoPainelService integracaoPainelService;

    @MockBean
    private IntegracaoOrquestrador integracaoOrquestrador;

    @MockBean
    private TarefaService tarefaService;

    @MockBean
    private AlertaService alertaService;

    @MockBean
    private ResumoOperacionalService resumoOperacionalService;

    @MockBean private AvesResumoService avesResumoService;
    @MockBean private InstalacaoCriacaoService instalacaoCriacaoService;
    @MockBean private LoteAvesService loteAvesService;
    @MockBean private ManejoAvesService manejoAvesService;
    @MockBean private IncubacaoAvesService incubacaoAvesService;
    @MockBean private IncubacaoAcompanhamentoService incubacaoAcompanhamentoService;
    @MockBean private Clock clock;

    @BeforeEach
    void configurarMocks() {
        when(clock.instant()).thenReturn(Instant.parse("2026-08-24T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(dashboardService.montarResumo()).thenReturn(vazio());
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novoFormulario()).thenReturn(new ProducaoForm());
        when(veiculoService.listarTodos()).thenReturn(List.of());
        when(usuarioService.listarTodos()).thenReturn(List.of());
        when(estoqueMovimentoService.montarResumo()).thenReturn(new EstoqueDashboardResumo(
                0, 0, 0, BigDecimal.ZERO, List.of(), List.of(), List.of()));
        when(estoqueMovimentoService.listarItensComSaldo()).thenReturn(List.of());
        when(estoqueMovimentoService.listarMovimentos(anyInt(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(estoqueMovimentoService.buscarMovimento(1L)).thenReturn(new MovimentoEstoqueResponse(
                1L, 1L, "Ração postura", TipoMovimentoEstoque.ENTRADA, "Entrada",
                BigDecimal.TEN, "KG", null, "Depósito", null, null, null,
                null, LocalDateTime.now(), "operador", null, null, null));
        when(estoqueMovimentoService.listarLotesVencidos()).thenReturn(List.of());
        when(estoqueMovimentoService.listarLotesProximosVencimento(anyInt())).thenReturn(List.of());
        ItemEstoqueResumo itemResumo = new ItemEstoqueResumo(1L, "Ração postura", "Geral", "KG",
                BigDecimal.ZERO, null, true, false, null, null);
        when(estoqueMovimentoService.detalharItem(1L)).thenReturn(new ItemEstoqueDetalhe(
                itemResumo, null, false, false, List.of(), List.of()));
        when(estoqueCatalogoService.listarCategoriasAtivas()).thenReturn(List.of());
        when(estoqueCatalogoService.listarUnidadesAtivas()).thenReturn(List.of());
        when(estoqueCatalogoService.listarItensAtivos()).thenReturn(List.of());
        when(estoqueCatalogoService.listarLocaisAtivos()).thenReturn(List.of());
        when(estoqueCatalogoService.listarLocais()).thenReturn(List.of());
        when(estoqueCatalogoService.listarCategorias()).thenReturn(List.of());
        FornecedorResumo fornecedor = new FornecedorResumo(1L, "Agro Vale", null, null, null, true);
        CompraDetalhe compra = new CompraDetalhe(1L, fornecedor, LocalDate.now(), "NF-1", null,
                StatusCompra.RASCUNHO, "Rascunho", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, List.of(), null, null, null, null, null, null);
        when(fornecedorService.listarAtivos()).thenReturn(List.of(fornecedor));
        when(fornecedorService.listarTodos()).thenReturn(List.of(fornecedor));
        when(fornecedorService.detalhar(1L)).thenReturn(fornecedor);
        FornecedorRequest fornecedorRequest = new FornecedorRequest();
        fornecedorRequest.setNome("Agro Vale");
        fornecedorRequest.setAtivo(true);
        when(fornecedorService.formulario(1L)).thenReturn(fornecedorRequest);
        when(compraService.montarResumo()).thenReturn(new ComprasDashboardResumo(
                0, BigDecimal.ZERO, 0, 1, null, List.of()));
        when(compraService.listar(any(CompraFiltro.class))).thenReturn(List.of());
        when(compraService.detalhar(1L)).thenReturn(compra);
        IntegracaoFonteResumo integracao = new IntegracaoFonteResumo(
                "open-meteo", "Open-Meteo", "Clima", "Previsão local", true,
                false, false, false, false, "NÃO EXIGIDA",
                StatusOperacionalIntegracao.DESABILITADO,
                null, null, null, null);
        when(integracaoPainelService.resumo()).thenReturn(new IntegracaoPainelResumo(0, 0, 0, 0, Map.of()));
        when(integracaoPainelService.detalhar(any())).thenReturn(integracao);
        when(integracaoPainelService.historico(any())).thenReturn(List.of());
        when(sistemaSaudeService.resumo()).thenReturn(new SistemaSaudeResumo(
                "UP", "UP", Duration.ofMinutes(5), "0.0.1-SNAPSHOT", "test",
                "DESABILITADA", "test-request"));
        when(tarefaService.listar(any())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(tarefaService.listarResponsaveisAtivos()).thenReturn(List.of());
        when(tarefaService.detalhar(1L)).thenReturn(tarefaDetalhe());
        when(alertaService.listar(any())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(alertaService.detalhar(1L)).thenReturn(alertaDetalhe());
        when(resumoOperacionalService.resumo()).thenReturn(new TarefaResumoOperacional(0, 0, 0, 0, 0));
        when(avesResumoService.resumo()).thenReturn(new AvesResumo(0, 0, 0, 0, 0, 0, 0, LocalDateTime.now()));
        when(loteAvesService.listar(org.mockito.ArgumentMatchers.nullable(com.example.sitiopro.criacao.aves.entity.StatusLoteAves.class),
                org.mockito.ArgumentMatchers.nullable(String.class), anyInt(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(loteAvesService.listarAtivos()).thenReturn(List.of());
        when(incubacaoAvesService.listar(anyInt(), anyInt())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(instalacaoCriacaoService.listar(anyInt(), anyInt())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(instalacaoCriacaoService.listarAtivas()).thenReturn(List.of());
        when(instalacaoCriacaoService.listarIncubadorasAtivas()).thenReturn(List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/sitio/painel",
            "/sitio/cadastro",
            "/sitio/configuracoes",
            "/sitio/frota",
            "/sitio/frota/novo",
            "/sitio/abastecimento/novo",
            "/sitio/admin/usuarios",
            "/sitio/admin/usuarios/novo",
            "/sitio/estoque",
            "/sitio/estoque/itens",
            "/sitio/estoque/itens/novo",
            "/sitio/estoque/itens/1",
            "/sitio/estoque/movimentacoes",
            "/sitio/estoque/movimentacoes/1",
            "/sitio/estoque/movimentacoes/nova",
            "/sitio/estoque/locais",
            "/sitio/estoque/categorias",
            "/sitio/estoque/inventario",
            "/sitio/compras",
            "/sitio/compras/nova",
            "/sitio/compras/1",
            "/sitio/compras/fornecedores",
            "/sitio/compras/fornecedores/novo",
            "/sitio/compras/fornecedores/1",
            "/sitio/tarefas",
            "/sitio/tarefas/nova",
            "/sitio/tarefas/1",
            "/sitio/alertas",
            "/sitio/alertas/1",
            "/sitio/admin/saude",
            "/sitio/admin/integracoes",
            "/sitio/admin/integracoes/open-meteo",
            "/sitio/admin/integracoes/embrapa-agrofit"
            ,"/sitio/criacoes"
            ,"/sitio/criacoes/aves"
            ,"/sitio/aves"
            ,"/sitio/criacoes/aves/lotes"
            ,"/sitio/criacoes/aves/lotes/novo"
            ,"/sitio/criacoes/aves/incubacoes"
            ,"/sitio/criacoes/aves/incubacoes/nova"
            ,"/sitio/criacoes/aves/instalacoes"
            ,"/sitio/criacoes/aves/instalacoes/nova"
    })
    void rotasFuncionaisExistentesContinuamRespondendo(String rota) throws Exception {
        mockMvc.perform(get(rota))
                .andExpect(status().isOk());
    }

    @Test
    void detalheCompraExibeApresentacaoComercialSemCamposDerivadosManipulaveis() throws Exception {
        ItemEstoque racao = itemEstoque(10L, "Ração postura", "KG", true);
        when(estoqueCatalogoService.listarItensAtivos()).thenReturn(List.of(racao));

        mockMvc.perform(get("/sitio/compras/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Somente itens ativos aparecem nesta lista.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"quantidadeVolumes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"tipoEmbalagem\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"conteudoPorVolume\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"precoPorVolume\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-unidade=\"KG\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"unidadeBase\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"quantidade\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"custoUnitario\""))));
    }

    @Test
    void cadastroRuralCarregaCategoriasAtivasNoModelENoSelect() throws Exception {
        CategoriaEstoque geral = categoriaEstoque(1L, "Geral", true);
        CategoriaEstoque graos = categoriaEstoque(2L, "Grãos", true);
        when(estoqueCatalogoService.listarCategoriasAtivas()).thenReturn(List.of(geral, graos));

        mockMvc.perform(get("/sitio/cadastro"))
                .andExpect(status().isOk())
                .andExpect(view().name("producao/cadastro"))
                .andExpect(model().attribute("categorias", List.of(geral, graos)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"1\">Geral</option>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"2\">Grãos</option>")));
    }

    @Test
    void cadastroRuralValidoEnviaSomenteDtoComCategoriaId() throws Exception {
        mockMvc.perform(post("/sitio/salvar")
                        .with(csrf())
                        .param("categoriaId", "2")
                        .param("item", "Milho em grão")
                        .param("quantidade", "30")
                        .param("unidade", "saca")
                        .param("status", "Estoque"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/painel"));

        verify(producaoService).salvar(argThat(form -> form.getId() == null
                && Long.valueOf(2L).equals(form.getCategoriaId())
                && "Milho em grão".equals(form.getItem())));
    }

    @Test
    void cadastroRuralSemCategoriaReexibeFormularioComOpcoes() throws Exception {
        CategoriaEstoque geral = categoriaEstoque(1L, "Geral", true);
        when(estoqueCatalogoService.listarCategoriasAtivas()).thenReturn(List.of(geral));

        mockMvc.perform(post("/sitio/salvar")
                        .with(csrf())
                        .param("categoria", "1")
                        .param("categoria.id", "1")
                        .param("estoqueCategoria", "1")
                        .param("estoqueCategoria.id", "1")
                        .param("item", "Milho em grão")
                        .param("quantidade", "30")
                        .param("unidade", "saca")
                        .param("status", "Estoque"))
                .andExpect(status().isOk())
                .andExpect(view().name("producao/cadastro"))
                .andExpect(model().attributeHasFieldErrors("producaoForm", "categoriaId"))
                .andExpect(model().attribute("categorias", List.of(geral)))
                .andExpect(model().attribute("producaoForm", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.hasProperty("item", org.hamcrest.Matchers.is("Milho em grão")),
                        org.hamcrest.Matchers.hasProperty("quantidade", org.hamcrest.Matchers.is(30)))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"1\">Geral</option>")));

        verify(producaoService, never()).salvar(any(ProducaoForm.class));
    }

    @Test
    void cadastroRuralRejeitaCategoriaManipuladaEMantemOpcoes() throws Exception {
        CategoriaEstoque geral = categoriaEstoque(1L, "Geral", true);
        when(estoqueCatalogoService.listarCategoriasAtivas()).thenReturn(List.of(geral));
        when(producaoService.salvar(any(ProducaoForm.class))).thenThrow(new EstoqueOperacaoException(
                "CATEGORIA_INVALIDA", "Categoria de estoque não encontrada ou inativa."));

        mockMvc.perform(post("/sitio/salvar")
                        .with(csrf())
                        .param("categoriaId", "99999")
                        .param("item", "Milho em grão")
                        .param("quantidade", "30")
                        .param("unidade", "saca")
                        .param("status", "Estoque"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("producaoForm", "categoriaId"))
                .andExpect(model().attribute("categorias", List.of(geral)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Categoria de estoque não encontrada ou inativa.")));
    }

    @Test
    void edicaoDoCadastroRuralMantemCategoriaAtualSelecionada() throws Exception {
        CategoriaEstoque geral = categoriaEstoque(1L, "Geral", true);
        CategoriaEstoque graos = categoriaEstoque(2L, "Grãos", true);
        ProducaoForm form = new ProducaoForm();
        form.setId(7L);
        form.setCategoriaId(2L);
        form.setItem("Milho");
        form.setQuantidade(10);
        form.setUnidade("saca");
        form.setStatus("Estoque");
        when(producaoService.formularioEdicao(7L)).thenReturn(form);
        when(estoqueCatalogoService.listarCategoriasAtivas()).thenReturn(List.of(geral, graos));

        mockMvc.perform(get("/sitio/editar/7"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<option value=\"2\" selected=\"selected\">Grãos</option>")));
    }

    @Test
    void historicoEstoqueRenderizaNavegacaoEPreservaTamanho() throws Exception {
        when(estoqueMovimentoService.listarMovimentos(1, 20))
                .thenReturn(new PaginaResponse<>(List.of(movimentoEstoque()), 1, 20, 45, 3));

        mockMvc.perform(get("/sitio/estoque/movimentacoes")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Página 2 de 3")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "href=\"/sitio/estoque/movimentacoes?page=0&amp;size=20\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "href=\"/sitio/estoque/movimentacoes?page=2&amp;size=20\"")));
    }

    @Test
    void apiEstoqueRetornaPaginaComTamanhoPadraoEMetadados() throws Exception {
        when(estoqueMovimentoService.listarMovimentos(0, 20))
                .thenReturn(new PaginaResponse<>(List.of(movimentoEstoque()), 0, 20, 41, 3));

        mockMvc.perform(get("/api/v1/estoque/movimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].id").value(1))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalElementos").value(41))
                .andExpect(jsonPath("$.totalPaginas").value(3));
    }

    @Test
    void painelOperacionalRenderizaPrioridadesEEstadosVazios() throws Exception {
        mockMvc.perform(get("/sitio/painel"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("O que precisa da sua")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sem alertas ativos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("dashboard-climate-empty")));
    }

    @Test
    void apiPainelRetornaReadModelOperacional() throws Exception {
        mockMvc.perform(get("/api/v1/painel/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelAtencao").value("NORMAL"))
                .andExpect(jsonPath("$.tarefas.vencidas").value(0))
                .andExpect(jsonPath("$.clima.estado").value("SEM_DADOS"))
                .andExpect(jsonPath("$.tendencias.postura7Dias.temDados").value(false))
                .andExpect(jsonPath("$.tendencias.compras6Meses.temDados").value(false));
    }

    @Test
    void painelOperacionalRenderizaLinhasComDados() throws Exception {
        when(dashboardService.montarResumo()).thenReturn(comDestaques());

        mockMvc.perform(get("/sitio/painel"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/sitio/alertas/20")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/sitio/tarefas/10")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1 tarefa aberta")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Sem alertas ativos"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("0 em andamento"))));
    }

    @Test
    void formulariosDeCriacaoExibemCodigoSomenteComoGeradoPeloBackend() throws Exception {
        mockMvc.perform(get("/sitio/criacoes/aves/lotes/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Gerado automaticamente ao cadastrar")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"codigo\""))));

        mockMvc.perform(get("/sitio/criacoes/aves/incubacoes/nova"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Gerado automaticamente ao iniciar")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("name=\"codigo\""))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"novo", "detalhe", "historico"})
    void atalhosAntigosDeConfiguracoesRedirecionamParaTelaUnica(String acao) throws Exception {
        mockMvc.perform(get("/sitio/admin/configuracoes/" + acao))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/sitio/admin/configuracoes"));
    }

    @Test
    void configuracoesOperacionaisRenderizamTelaFuncional() throws Exception {
        when(configuracaoOperacionalService.obter()).thenReturn(
                com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.padrao());
        mockMvc.perform(get("/sitio/admin/configuracoes")).andExpect(status().isOk())
                .andExpect(view().name("admin/configuracoes"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dias padrão de incubação de galinha")));
    }

    @ParameterizedTest
    @MethodSource("rotasPlanejadas")
    void rotasPlanejadasRenderizamPlaceholder(String rota) throws Exception {
        mockMvc.perform(get(rota))
                .andExpect(status().isOk());
    }

    static Stream<String> rotasPlanejadas() {
        List<String> basesComFluxoPadrao = List.of(
                "/sitio/suinos",
                "/sitio/piscicultura",
                "/sitio/agricultura/areas",
                "/sitio/agricultura/culturas",
                "/sitio/agricultura/plantios",
                "/sitio/agricultura/adubacao",
                "/sitio/agricultura/irrigacao",
                "/sitio/agricultura/tratamentos",
                "/sitio/agricultura/colheitas",
                "/sitio/agua",
                "/sitio/agua/reservatorios",
                "/sitio/agua/bombas",
                "/sitio/agua/irrigacao",
                "/sitio/agua/registros",
                "/sitio/agua/manutencoes",
                "/sitio/casa",
                "/sitio/despensa",
                "/sitio/manutencao",
                "/sitio/ar-condicionado",
                "/sitio/dedetizacao",
                "/sitio/reformas",
                "/sitio/deterioracoes",
                "/sitio/patrimonio",
                "/sitio/seguranca",
                "/sitio/admin/centros-custo",
                "/sitio/admin/unidades-medida",
                "/sitio/admin/propriedade"
        );

        Stream<String> fluxosPadrao = basesComFluxoPadrao.stream()
                .flatMap(base -> Stream.of(base, base + "/novo", base + "/detalhe", base + "/historico"));

        return Stream.concat(fluxosPadrao, Stream.of(
                "/sitio/frota/detalhe",
                "/sitio/frota/historico",
                "/sitio/abastecimentos",
                "/sitio/abastecimentos/detalhe",
                "/sitio/abastecimentos/historico",
                "/sitio/admin/roadmap"
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/gestao/estoque",
            "/sitio/estoque/novo",
            "/sitio/estoque/detalhe",
            "/sitio/estoque/historico",
            "/sitio/compras/novo",
            "/sitio/compras/detalhe",
            "/sitio/compras/historico",
            "/criacoes/aves/chocadeira",
            "/sitio/aves/chocadeira",
            "/sitio/aves/pinteiro",
            "/sitio/aves/galinheiro",
            "/agricultura/plantios",
            "/agua/irrigacao",
            "/propriedade/seguranca-cameras",
            "/administracao/usuarios",
            "/configuracoes/roadmap",
            "/sitio/abastecimento",
            "/sitio/abastecimentos/novo"
    })
    void rotasAntigasOuAliasesRedirecionam(String rota) throws Exception {
        mockMvc.perform(get(rota))
                .andExpect(status().is3xxRedirection());
    }

    private TarefaDetalhe tarefaDetalhe() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 24, 10, 0);
        return new TarefaDetalhe(1L, "Verificar caixa d'água", null, StatusTarefa.PENDENTE,
                PrioridadeTarefa.NORMAL, agora, null, agora.plusDays(1), null,
                null, null, 1L, "Administrador", OrigemTarefa.MANUAL, null, null,
                TipoRecorrencia.NENHUMA, null, null, false, true, 0, false, List.of());
    }

    private MovimentoEstoqueResponse movimentoEstoque() {
        return new MovimentoEstoqueResponse(
                1L, 1L, "Ração postura", TipoMovimentoEstoque.ENTRADA, "Entrada",
                BigDecimal.TEN, "KG", null, "Depósito", null, null, null,
                null, LocalDateTime.of(2026, 8, 25, 12, 0), "operador", null, null, null);
    }

    private AlertaDetalhe alertaDetalhe() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 24, 10, 0);
        return new AlertaDetalhe(1L, "Ração abaixo do mínimo", "Saldo abaixo do mínimo.",
                SeveridadeAlerta.ALTA, StatusAlerta.ATIVO, ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, "ITEM:1", "ESTOQUE:ITEM:1:ABAIXO_MINIMO",
                agora, agora, null, null, null, Map.of("saldo", 1), null, 0, List.of());
    }

    private CategoriaEstoque categoriaEstoque(Long id, String nome, boolean ativa) {
        CategoriaEstoque categoria = new CategoriaEstoque();
        ReflectionTestUtils.setField(categoria, "id", id);
        categoria.setNome(nome);
        categoria.setAtiva(ativa);
        return categoria;
    }

    private ItemEstoque itemEstoque(Long id, String nome, String unidadeSigla, boolean ativo) {
        UnidadeMedida unidade = new UnidadeMedida();
        ReflectionTestUtils.setField(unidade, "id", id);
        unidade.setNome(unidadeSigla);
        unidade.setSigla(unidadeSigla);
        unidade.setAtiva(true);
        ItemEstoque item = new ItemEstoque();
        ReflectionTestUtils.setField(item, "id", id);
        item.setNome(nome);
        item.setUnidadeMedida(unidade);
        item.setCategoria(categoriaEstoque(id, "Geral", true));
        item.setAtivo(ativo);
        return item;
    }
}
