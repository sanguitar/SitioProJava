package com.example.sitiopro.dashboard.service;

import com.example.sitiopro.compras.dto.CompraResumo;
import com.example.sitiopro.compras.dto.ComprasDashboardResumo;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.dashboard.dto.EstadoClimaDashboard;
import com.example.sitiopro.dashboard.dto.NivelAtencao;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.LoteEstoqueResumo;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.dto.PrevisaoClimaticaResponse;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import com.example.sitiopro.tarefas.dto.TarefasAlertasPainelResumo;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 8, 24, 8, 0);

    @Mock
    private ResumoOperacionalService resumoOperacionalService;

    @Mock
    private EstoqueMovimentoService estoqueService;

    @Mock
    private CompraService compraService;

    @Mock
    private ClimaConsultaService climaService;

    @Mock
    private IntegracaoPainelService integracaoService;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneId.of("America/Manaus"));
        service = new DashboardService(resumoOperacionalService, estoqueService, compraService,
                climaService, integracaoService, clock);

        when(resumoOperacionalService.resumoPainel(5)).thenReturn(operacaoVazia());
        when(estoqueService.listarItensComSaldo()).thenReturn(List.of());
        when(estoqueService.listarLotesVencidos()).thenReturn(List.of());
        when(estoqueService.listarLotesProximosVencimento(30)).thenReturn(List.of());
        when(compraService.montarResumo()).thenReturn(
                new ComprasDashboardResumo(0, BigDecimal.ZERO, 0, 0, null, List.of()));
        when(climaService.resumo()).thenReturn(ClimaResumo.naoSincronizado());
        when(integracaoService.resumo()).thenReturn(new IntegracaoPainelResumo(0, 0, 0, 0, Map.of()));
    }

    @Test
    void operacaoSemPendenciasRetornaEstadoNormalESemDadosClimaticos() {
        var resumo = service.montarResumo();

        assertThat(resumo.nivelAtencao()).isEqualTo(NivelAtencao.NORMAL);
        assertThat(resumo.clima().estado()).isEqualTo(EstadoClimaDashboard.SEM_DADOS);
        assertThat(resumo.geradoEm()).isEqualTo(AGORA);
        assertThat(resumo.tarefas().proximas()).isEmpty();
        assertThat(resumo.alertas().principais()).isEmpty();
        verify(climaService, never()).previsao(24);
    }

    @Test
    void tarefaEAlertaCriticosRecebemPrioridadeMaxima() {
        TarefaResumo tarefa = new TarefaResumo(
                10L, "Restabelecer irrigação", StatusTarefa.PENDENTE, PrioridadeTarefa.CRITICA,
                AGORA.minusHours(2), 2L, "Operador", OrigemTarefa.MANUAL, ModuloOrigem.TAREFAS, true);
        AlertaResumo alerta = new AlertaResumo(
                20L, "Reservatório em nível crítico", SeveridadeAlerta.CRITICA, StatusAlerta.ATIVO,
                ModuloOrigem.INTEGRACOES, TipoAlerta.INTEGRACAO_COM_FALHA, "RESERVATORIO:1",
                AGORA.minusMinutes(30),
                AGORA.minusMinutes(30), null);
        when(resumoOperacionalService.resumoPainel(5)).thenReturn(new TarefasAlertasPainelResumo(
                new TarefasAlertasPainelResumo.Tarefas(0, 1, 1, 0, List.of(tarefa)),
                new TarefasAlertasPainelResumo.Alertas(1, 1, 0, List.of(alerta))));

        var resumo = service.montarResumo();

        assertThat(resumo.nivelAtencao()).isEqualTo(NivelAtencao.CRITICA);
        assertThat(resumo.tarefas().proximas()).singleElement()
                .satisfies(item -> {
                    assertThat(item.id()).isEqualTo(10L);
                    assertThat(item.vencida()).isTrue();
                });
        assertThat(resumo.alertas().principais()).singleElement()
                .extracting(item -> item.id())
                .isEqualTo(20L);
    }

    @Test
    void consolidaEstoqueEComprasSemExporEntidades() {
        ItemEstoqueResumo item = new ItemEstoqueResumo(
                30L, "Ração postura", "Insumos", "KG", new BigDecimal("12"),
                new BigDecimal("40"), true, true, new BigDecimal("3.70"), new BigDecimal("3.65"));
        LoteEstoqueResumo lote = new LoteEstoqueResumo(
                40L, "Vacina", "L-2026", LocalDate.of(2026, 8, 20), BigDecimal.ONE, "UN");
        CompraResumo compra = new CompraResumo(
                50L, 5L, "Agro Vale", LocalDate.of(2026, 8, 23), "NF-50", StatusCompra.CONFIRMADA,
                "Confirmada", new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100"), 1, AGORA.minusDays(1), "admin");
        when(estoqueService.listarItensComSaldo()).thenReturn(List.of(item));
        when(estoqueService.listarLotesVencidos()).thenReturn(List.of(lote));
        when(compraService.montarResumo()).thenReturn(
                new ComprasDashboardResumo(3, new BigDecimal("350"), 2, 4, compra, List.of(compra)));

        var resumo = service.montarResumo();

        assertThat(resumo.nivelAtencao()).isEqualTo(NivelAtencao.ALTA);
        assertThat(resumo.estoque().itensAbaixoMinimo()).isEqualTo(1);
        assertThat(resumo.estoque().lotesVencidos()).isEqualTo(1);
        assertThat(resumo.estoque().itensCriticos()).singleElement()
                .extracting(itemResumo -> itemResumo.nome())
                .isEqualTo("Ração postura");
        assertThat(resumo.compras().valorConfirmadoNoMes()).isEqualByComparingTo("350");
        assertThat(resumo.compras().ultimaConfirmada().id()).isEqualTo(50L);
    }

    @Test
    void climaDesatualizadoUsaSomenteUltimoDadoLocalEPrevisaoPersistida() {
        when(climaService.resumo()).thenReturn(new ClimaResumo(
                true, true, new BigDecimal("28.4"), 71, new BigDecimal("5.8"),
                new BigDecimal("12.5"), null, null, null, 61, AGORA, AGORA.minusHours(4),
                "America/Manaus", "open-meteo"));
        when(climaService.previsao(24)).thenReturn(List.of(
                previsao(35), previsao(80), previsao(null)));

        var resumo = service.montarResumo();

        assertThat(resumo.clima().estado()).isEqualTo(EstadoClimaDashboard.DESATUALIZADO);
        assertThat(resumo.clima().condicao()).isEqualTo("Chuva");
        assertThat(resumo.clima().probabilidadePrecipitacao()).isEqualTo(80);
        assertThat(resumo.nivelAtencao()).isEqualTo(NivelAtencao.NORMAL);
        verify(climaService).previsao(24);
    }

    @Test
    void climaAtualizadoRetornaEstadoNormal() {
        when(climaService.resumo()).thenReturn(new ClimaResumo(
                true, false, new BigDecimal("27.2"), 68, new BigDecimal("0.4"),
                new BigDecimal("8.1"), null, null, null, 1, AGORA, AGORA.minusMinutes(15),
                "America/Manaus", "open-meteo"));
        when(climaService.previsao(24)).thenReturn(List.of(previsao(20)));

        var resumo = service.montarResumo();

        assertThat(resumo.clima().estado()).isEqualTo(EstadoClimaDashboard.NORMAL);
        assertThat(resumo.clima().temperatura()).isEqualByComparingTo("27.2");
        assertThat(resumo.clima().condicao()).isEqualTo("Parcialmente nublado");
    }

    @Test
    void falhaDeIntegracaoElevaAtencaoSemDispararSincronizacao() {
        IntegracaoFonteResumo falha = integracao(
                "open-meteo", "Open-Meteo", StatusOperacionalIntegracao.FALHA, AGORA.minusMinutes(20));
        IntegracaoFonteResumo operacional = integracao(
                "embrapa-agrofit", "Embrapa Agrofit", StatusOperacionalIntegracao.OPERACIONAL,
                AGORA.minusHours(1));
        when(integracaoService.resumo()).thenReturn(
                new IntegracaoPainelResumo(1, 0, 0, 1, Map.of("Dados externos", List.of(falha, operacional))));

        var resumo = service.montarResumo();

        assertThat(resumo.nivelAtencao()).isEqualTo(NivelAtencao.ALTA);
        assertThat(resumo.integracoes().implementadas()).isEqualTo(2);
        assertThat(resumo.integracoes().comFalha()).isEqualTo(1);
        assertThat(resumo.integracoes().problemas()).singleElement()
                .extracting(item -> item.slug())
                .isEqualTo("open-meteo");
    }

    private TarefasAlertasPainelResumo operacaoVazia() {
        return new TarefasAlertasPainelResumo(
                new TarefasAlertasPainelResumo.Tarefas(0, 0, 0, 0, List.of()),
                new TarefasAlertasPainelResumo.Alertas(0, 0, 0, List.of()));
    }

    private PrevisaoClimaticaResponse previsao(Integer probabilidade) {
        return new PrevisaoClimaticaResponse(
                AGORA, null, null, null, probabilidade, null, null, null, null, null,
                AGORA.minusMinutes(10), "open-meteo");
    }

    private IntegracaoFonteResumo integracao(String slug, String nome, StatusOperacionalIntegracao status,
            LocalDateTime ultimaTentativa) {
        return new IntegracaoFonteResumo(
                slug, nome, "Dados externos", "Fonte operacional", true, true, true,
                false, true, "NÃO EXIGIDA", status, null, ultimaTentativa, null, null);
    }
}
