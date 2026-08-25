package com.example.sitiopro.dashboard.service;

import com.example.sitiopro.compras.repository.CompraRepository;
import com.example.sitiopro.criacao.aves.repository.AlimentacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardTendenciasServiceTests {

    @Mock private RegistroPosturaAvesRepository posturaRepository;
    @Mock private AlimentacaoAvesRepository alimentacaoRepository;
    @Mock private CompraRepository compraRepository;
    private DashboardTendenciasService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneId.of("America/Manaus"));
        service = new DashboardTendenciasService(
                posturaRepository, alimentacaoRepository, compraRepository, clock);
    }

    @Test
    void consolidaSeriesPreencheLacunasESeparaUnidades() {
        var posturas = List.of(
                postura(LocalDate.of(2026, 8, 20), 8, 1, 1),
                postura(LocalDate.of(2026, 8, 24), 12, 0, 0));
        var consumos = List.of(
                consumo(LocalDate.of(2026, 8, 18), "KG", "2.50"),
                consumo(LocalDate.of(2026, 8, 24), "KG", "1.50"),
                consumo(LocalDate.of(2026, 8, 23), "UN", "4"));
        var compras = List.of(
                compra(LocalDate.of(2026, 3, 1), "120.00"),
                compra(LocalDate.of(2026, 8, 1), "380.00"));

        when(posturaRepository.agregarPorDia(LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 24)))
                .thenReturn(posturas);
        when(alimentacaoRepository.agregarPorDiaEUnidade(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 25)))
                .thenReturn(consumos);
        when(compraRepository.agregarConfirmadasPorMes(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(compras);

        var resumo = service.montar();

        assertThat(resumo.postura7Dias().temDados()).isTrue();
        assertThat(resumo.postura7Dias().totalOvos()).isEqualTo(22);
        assertThat(resumo.postura7Dias().pontos()).hasSize(7);
        assertThat(resumo.postura7Dias().pontos().get(1).total()).isZero();
        assertThat(resumo.consumoRacao7Dias()).extracting(item -> item.unidade())
                .containsExactly("KG", "UN");
        assertThat(resumo.consumoRacao7Dias().getFirst().total()).isEqualByComparingTo("4.00");
        assertThat(resumo.consumoRacao7Dias().getFirst().pontos()).hasSize(7);
        assertThat(resumo.compras6Meses().total()).isEqualByComparingTo("500.00");
        assertThat(resumo.compras6Meses().pontos()).hasSize(6);
        assertThat(resumo.compras6Meses().pontos().get(1).valor()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void retornaSeriesVaziasComPeriodosEstaveis() {
        when(posturaRepository.agregarPorDia(LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 24)))
                .thenReturn(List.of());
        when(alimentacaoRepository.agregarPorDiaEUnidade(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 25))).thenReturn(List.of());
        when(compraRepository.agregarConfirmadasPorMes(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 8, 31))).thenReturn(List.of());

        var resumo = service.montar();

        assertThat(resumo.postura7Dias().temDados()).isFalse();
        assertThat(resumo.postura7Dias().pontos()).hasSize(7)
                .allSatisfy(ponto -> assertThat(ponto.total()).isZero());
        assertThat(resumo.consumoRacao7Dias()).isEmpty();
        assertThat(resumo.compras6Meses().temDados()).isFalse();
        assertThat(resumo.compras6Meses().pontos()).hasSize(6)
                .allSatisfy(ponto -> assertThat(ponto.valor()).isEqualByComparingTo(BigDecimal.ZERO));
        verify(posturaRepository).agregarPorDia(LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 24));
        verify(alimentacaoRepository).agregarPorDiaEUnidade(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 25));
        verify(compraRepository).agregarConfirmadasPorMes(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 8, 31));
    }

    private RegistroPosturaAvesRepository.PosturaDiaria postura(
            LocalDate data, long inteiros, long quebrados, long descartados) {
        var item = mock(RegistroPosturaAvesRepository.PosturaDiaria.class);
        when(item.getData()).thenReturn(data);
        when(item.getInteiros()).thenReturn(inteiros);
        when(item.getQuebrados()).thenReturn(quebrados);
        when(item.getDescartados()).thenReturn(descartados);
        return item;
    }

    private AlimentacaoAvesRepository.ConsumoDiario consumo(
            LocalDate data, String unidade, String quantidade) {
        var item = mock(AlimentacaoAvesRepository.ConsumoDiario.class);
        when(item.getData()).thenReturn(data);
        when(item.getUnidade()).thenReturn(unidade);
        when(item.getQuantidade()).thenReturn(new BigDecimal(quantidade));
        return item;
    }

    private CompraRepository.CompraMensal compra(LocalDate mes, String valor) {
        var item = mock(CompraRepository.CompraMensal.class);
        when(item.getMes()).thenReturn(mes);
        when(item.getValor()).thenReturn(new BigDecimal(valor));
        return item;
    }
}
