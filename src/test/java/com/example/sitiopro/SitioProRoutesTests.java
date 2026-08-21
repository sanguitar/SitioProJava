package com.example.sitiopro;

import com.example.sitiopro.abastecimento.controller.AbastecimentoController;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.categoria.controller.CategoriaController;
import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.dashboard.controller.DashboardController;
import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.estoque.controller.EstoqueController;
import com.example.sitiopro.estoque.dto.EstoqueDashboardResumo;
import com.example.sitiopro.estoque.dto.ItemEstoqueDetalhe;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.frota.controller.VeiculoController;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.observability.controller.SistemaSaudeController;
import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.planejamento.controller.AdministracaoPlanejamentoController;
import com.example.sitiopro.planejamento.controller.AgriculturaPlanejamentoController;
import com.example.sitiopro.planejamento.controller.AguaPlanejamentoController;
import com.example.sitiopro.planejamento.controller.CriacoesPlanejamentoController;
import com.example.sitiopro.planejamento.controller.GestaoPlanejamentoController;
import com.example.sitiopro.planejamento.controller.PlanejamentoRedirectController;
import com.example.sitiopro.planejamento.controller.PropriedadePlanejamentoController;
import com.example.sitiopro.planejamento.controller.VeiculosPlanejamentoController;
import com.example.sitiopro.producao.controller.ProducaoController;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.service.ProducaoService;
import com.example.sitiopro.usuario.controller.UsuarioController;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        DashboardController.class,
        ProducaoController.class,
        CategoriaController.class,
        VeiculoController.class,
        AbastecimentoController.class,
        EstoqueController.class,
        GestaoPlanejamentoController.class,
        CriacoesPlanejamentoController.class,
        AgriculturaPlanejamentoController.class,
        AguaPlanejamentoController.class,
        PropriedadePlanejamentoController.class,
        VeiculosPlanejamentoController.class,
        AdministracaoPlanejamentoController.class,
        PlanejamentoRedirectController.class,
        UsuarioController.class,
        SistemaSaudeController.class
})
@WithMockUser(roles = "ADMIN")
class SitioProRoutesTests {

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

    @BeforeEach
    void configurarMocks() {
        DashboardResumo resumo = new DashboardResumo(new PageImpl<>(List.of()), List.of(), "[]", "[]", 0, 0, 0);

        when(dashboardService.montarResumo(nullable(Long.class), anyInt())).thenReturn(resumo);
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novo()).thenReturn(new Producao());
        when(veiculoService.listarTodos()).thenReturn(List.of());
        when(usuarioService.listarTodos()).thenReturn(List.of());
        when(estoqueMovimentoService.montarResumo()).thenReturn(new EstoqueDashboardResumo(
                0, 0, 0, BigDecimal.ZERO, List.of(), List.of(), List.of()));
        when(estoqueMovimentoService.listarItensComSaldo()).thenReturn(List.of());
        when(estoqueMovimentoService.listarMovimentos()).thenReturn(List.of());
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
        when(sistemaSaudeService.resumo()).thenReturn(new SistemaSaudeResumo(
                "UP", "UP", Duration.ofMinutes(5), "0.0.1-SNAPSHOT", "test",
                "DESABILITADA", "test-request"));
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
            "/sitio/estoque/movimentacoes/nova",
            "/sitio/estoque/locais",
            "/sitio/estoque/categorias",
            "/sitio/estoque/inventario",
            "/sitio/admin/saude"
    })
    void rotasFuncionaisExistentesContinuamRespondendo(String rota) throws Exception {
        mockMvc.perform(get(rota))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("rotasPlanejadas")
    void rotasPlanejadasRenderizamPlaceholder(String rota) throws Exception {
        mockMvc.perform(get(rota))
                .andExpect(status().isOk());
    }

    static Stream<String> rotasPlanejadas() {
        List<String> basesComFluxoPadrao = List.of(
                "/sitio/compras",
                "/sitio/tarefas",
                "/sitio/aves",
                "/sitio/aves/chocadeira",
                "/sitio/aves/pinteiro",
                "/sitio/aves/galinheiro",
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
                "/sitio/admin/configuracoes",
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
            "/criacoes/aves/chocadeira",
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
}
