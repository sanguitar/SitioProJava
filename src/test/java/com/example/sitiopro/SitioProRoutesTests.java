package com.example.sitiopro;

import com.example.sitiopro.abastecimento.controller.AbastecimentoController;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.categoria.controller.CategoriaController;
import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.dashboard.controller.DashboardController;
import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.frota.controller.VeiculoController;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.planejamento.controller.PlanejamentoController;
import com.example.sitiopro.producao.controller.ProducaoController;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.service.ProducaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

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
        PlanejamentoController.class
})
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

    @BeforeEach
    void configurarMocks() {
        DashboardResumo resumo = new DashboardResumo(new PageImpl<>(List.of()), List.of(), "[]", "[]", 0, 0, 0);

        when(dashboardService.montarResumo(nullable(Long.class), anyInt())).thenReturn(resumo);
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novo()).thenReturn(new Producao());
        when(veiculoService.listarTodos()).thenReturn(List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/sitio/painel",
            "/sitio/cadastro",
            "/sitio/configuracoes",
            "/sitio/frota",
            "/sitio/frota/novo",
            "/sitio/abastecimento/novo"
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
                "/gestao/estoque",
                "/gestao/compras",
                "/gestao/tarefas",
                "/criacoes/aves",
                "/criacoes/aves/chocadeira",
                "/criacoes/aves/pinteiro",
                "/criacoes/aves/galinheiro",
                "/criacoes/suinos",
                "/criacoes/piscicultura",
                "/agricultura/areas-talhoes",
                "/agricultura/culturas",
                "/agricultura/plantios",
                "/agricultura/adubacao",
                "/agricultura/irrigacao",
                "/agricultura/tratamentos",
                "/agricultura/colheitas",
                "/agua/reservatorios",
                "/agua/bombas",
                "/agua/irrigacao",
                "/agua/registros",
                "/agua/manutencoes",
                "/propriedade/casa",
                "/propriedade/despensa",
                "/propriedade/manutencao",
                "/propriedade/ar-condicionado",
                "/propriedade/dedetizacao",
                "/propriedade/reformas",
                "/propriedade/deterioracoes",
                "/propriedade/patrimonio",
                "/propriedade/seguranca-cameras",
                "/administracao/usuarios",
                "/administracao/configuracoes",
                "/administracao/centros-custo",
                "/administracao/unidades-medida",
                "/administracao/dados-propriedade"
        );

        Stream<String> fluxosPadrao = basesComFluxoPadrao.stream()
                .flatMap(base -> Stream.of(base, base + "/novo", base + "/detalhe", base + "/historico"));

        return Stream.concat(fluxosPadrao, Stream.of(
                "/sitio/frota/detalhe",
                "/sitio/frota/historico",
                "/sitio/abastecimento",
                "/sitio/abastecimento/detalhe",
                "/sitio/abastecimento/historico",
                "/configuracoes/roadmap"
        ));
    }
}
