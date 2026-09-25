package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.criacao.aves.service.AvesAlertasService;
import com.example.sitiopro.criacao.suinos.service.SuinosAlertasService;
import com.example.sitiopro.criacao.suinos.service.SuinosSanidadeAlertasService;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.LoteEstoqueResumo;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegrasAlertasServiceTests {

    @Mock
    private EstoqueMovimentoService estoqueService;
    @Mock
    private IntegracaoPainelService integracaoService;
    @Mock
    private ClimaConsultaService climaService;
    @Mock
    private AlertaService alertaService;
    @Mock
    private AvesAlertasService avesAlertasService;
    @Mock
    private SuinosAlertasService suinosAlertasService;
    @Mock
    private SuinosSanidadeAlertasService suinosSanidadeAlertasService;

    private RegrasAlertasService service;

    @BeforeEach
    void preparar() {
        TarefasAlertasProperties properties = new TarefasAlertasProperties();
        properties.setLoteProximoVencimentoDias(15);
        properties.setChuva24hLimiteMm(new BigDecimal("50"));
        service = new RegrasAlertasService(estoqueService, integracaoService, climaService,
                alertaService, properties, avesAlertasService, suinosAlertasService,
                suinosSanidadeAlertasService);
        lenient().when(estoqueService.listarItensComSaldo()).thenReturn(List.of());
        lenient().when(estoqueService.listarLotesProximosVencimento(15)).thenReturn(List.of());
        lenient().when(estoqueService.listarLotesVencidos()).thenReturn(List.of());
        lenient().when(integracaoService.resumo()).thenReturn(new IntegracaoPainelResumo(0, 0, 0, 0, Map.of()));
        lenient().when(climaService.resumo()).thenReturn(ClimaResumo.naoSincronizado());
    }

    @Test
    void produzCondicoesParaEstoqueIntegracoesEClima() {
        when(estoqueService.listarItensComSaldo()).thenReturn(List.of(new ItemEstoqueResumo(
                1L, "Ração", "Insumos", "KG", new BigDecimal("2"), new BigDecimal("10"),
                true, true, null, null)));
        when(estoqueService.listarLotesProximosVencimento(15)).thenReturn(List.of(new LoteEstoqueResumo(
                2L, "Milho", "L-2", LocalDate.of(2026, 8, 30), new BigDecimal("5"), "KG")));
        when(estoqueService.listarLotesVencidos()).thenReturn(List.of(new LoteEstoqueResumo(
                3L, "Vacina", "L-3", LocalDate.of(2026, 8, 20), BigDecimal.ONE, "UN")));
        IntegracaoFonteResumo stale = fonte("open-meteo", StatusOperacionalIntegracao.DESATUALIZADO);
        IntegracaoFonteResumo falha = fonte("agrofit", StatusOperacionalIntegracao.FALHA);
        when(integracaoService.resumo()).thenReturn(new IntegracaoPainelResumo(
                0, 1, 0, 1, Map.of("Campo", List.of(stale, falha))));
        when(climaService.resumo()).thenReturn(new ClimaResumo(true, false, new BigDecimal("28"), 80,
                new BigDecimal("70"), null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now(), "America/Manaus", "open-meteo"));

        service.avaliar();

        ArgumentCaptor<TipoAlerta> tipos = ArgumentCaptor.forClass(TipoAlerta.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CondicaoAlerta>> condicoes = ArgumentCaptor.forClass(List.class);
        verify(alertaService, atLeastOnce()).sincronizar(any(ModuloOrigem.class), tipos.capture(), condicoes.capture());
        Map<TipoAlerta, Integer> quantidades = new java.util.EnumMap<>(TipoAlerta.class);
        for (int i = 0; i < tipos.getAllValues().size(); i++) {
            quantidades.put(tipos.getAllValues().get(i), condicoes.getAllValues().get(i).size());
        }
        assertThat(quantidades).containsEntry(TipoAlerta.ESTOQUE_ABAIXO_MINIMO, 1)
                .containsEntry(TipoAlerta.LOTE_PROXIMO_VENCIMENTO, 1)
                .containsEntry(TipoAlerta.LOTE_VENCIDO, 1)
                .containsEntry(TipoAlerta.INTEGRACAO_DESATUALIZADA, 1)
                .containsEntry(TipoAlerta.INTEGRACAO_COM_FALHA, 1)
                .containsEntry(TipoAlerta.CHUVA_INTENSA_24H, 1);
    }

    @Test
    void ausenciaDeDadosClimaticosNaoInventaAlerta() {
        service.avaliar();

        verify(alertaService, never()).sincronizar(ModuloOrigem.CLIMA,
                TipoAlerta.CHUVA_INTENSA_24H, List.of());
    }

    private IntegracaoFonteResumo fonte(String slug, StatusOperacionalIntegracao status) {
        return new IntegracaoFonteResumo(slug, slug, "Campo", "Fonte", true, true, true,
                false, true, "CONFIGURADA", status, null, LocalDateTime.now(), null, null);
    }
}
