package com.example.sitiopro.dashboard.service;

import com.example.sitiopro.compras.repository.CompraRepository;
import com.example.sitiopro.criacao.aves.repository.AlimentacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.dashboard.dto.DashboardTendenciasResumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardTendenciasService {

    private static final DateTimeFormatter ROTULO_DIA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter ROTULO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private final RegistroPosturaAvesRepository posturaRepository;
    private final AlimentacaoAvesRepository alimentacaoRepository;
    private final CompraRepository compraRepository;
    private final Clock clock;

    public DashboardTendenciasService(RegistroPosturaAvesRepository posturaRepository,
            AlimentacaoAvesRepository alimentacaoRepository, CompraRepository compraRepository, Clock clock) {
        this.posturaRepository = posturaRepository;
        this.alimentacaoRepository = alimentacaoRepository;
        this.compraRepository = compraRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardTendenciasResumo montar() {
        LocalDate hoje = LocalDate.now(clock);
        return new DashboardTendenciasResumo(
                postura(hoje.minusDays(6), hoje),
                consumo(hoje.minusDays(6), hoje),
                compras(YearMonth.from(hoje).minusMonths(5), YearMonth.from(hoje)));
    }

    private DashboardTendenciasResumo.PosturaSerie postura(LocalDate inicio, LocalDate fim) {
        Map<LocalDate, RegistroPosturaAvesRepository.PosturaDiaria> agregados = posturaRepository
                .agregarPorDia(inicio, fim).stream()
                .collect(java.util.stream.Collectors.toMap(
                        RegistroPosturaAvesRepository.PosturaDiaria::getData,
                        item -> item,
                        (primeiro, segundo) -> primeiro,
                        LinkedHashMap::new));
        List<DiaPostura> dias = inicio.datesUntil(fim.plusDays(1))
                .map(data -> postura(data, agregados.get(data)))
                .toList();
        long maximo = dias.stream().mapToLong(DiaPostura::total).max().orElse(0);
        List<DashboardTendenciasResumo.PosturaPonto> pontos = dias.stream()
                .map(dia -> new DashboardTendenciasResumo.PosturaPonto(
                        dia.data(), ROTULO_DIA.format(dia.data()), dia.inteiros(), dia.perdas(), dia.total(),
                        percentual(dia.total(), maximo), percentual(dia.inteiros(), dia.total())))
                .toList();
        long total = dias.stream().mapToLong(DiaPostura::total).sum();
        return new DashboardTendenciasResumo.PosturaSerie(total, total > 0, pontos);
    }

    private DiaPostura postura(LocalDate data, RegistroPosturaAvesRepository.PosturaDiaria item) {
        long inteiros = item == null ? 0 : valor(item.getInteiros());
        long perdas = item == null ? 0 : valor(item.getQuebrados()) + valor(item.getDescartados());
        return new DiaPostura(data, inteiros, perdas, inteiros + perdas);
    }

    private List<DashboardTendenciasResumo.ConsumoSerie> consumo(LocalDate inicio, LocalDate fim) {
        Map<String, Map<LocalDate, BigDecimal>> porUnidade = new TreeMap<>();
        for (AlimentacaoAvesRepository.ConsumoDiario item
                : alimentacaoRepository.agregarPorDiaEUnidade(inicio, fim.plusDays(1))) {
            porUnidade.computeIfAbsent(item.getUnidade(), ignorada -> new LinkedHashMap<>())
                    .put(item.getData(), decimal(item.getQuantidade()));
        }
        return porUnidade.entrySet().stream()
                .map(entry -> consumo(entry.getKey(), entry.getValue(), inicio, fim))
                .sorted(Comparator.comparing(DashboardTendenciasResumo.ConsumoSerie::unidade))
                .toList();
    }

    private DashboardTendenciasResumo.ConsumoSerie consumo(String unidade, Map<LocalDate, BigDecimal> agregados,
            LocalDate inicio, LocalDate fim) {
        List<ValorData> valores = inicio.datesUntil(fim.plusDays(1))
                .map(data -> new ValorData(data, agregados.getOrDefault(data, BigDecimal.ZERO)))
                .toList();
        BigDecimal maximo = maximo(valores.stream().map(ValorData::valor).toList());
        List<DashboardTendenciasResumo.ValorPonto> pontos = valores.stream()
                .map(item -> new DashboardTendenciasResumo.ValorPonto(
                        item.data().toString(), ROTULO_DIA.format(item.data()), item.valor(),
                        percentual(item.valor(), maximo)))
                .toList();
        BigDecimal total = valores.stream().map(ValorData::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardTendenciasResumo.ConsumoSerie(unidade, total, total.signum() > 0, pontos);
    }

    private DashboardTendenciasResumo.ComprasSerie compras(YearMonth inicio, YearMonth fim) {
        Map<YearMonth, BigDecimal> agregados = new LinkedHashMap<>();
        for (CompraRepository.CompraMensal item : compraRepository.agregarConfirmadasPorMes(
                inicio.atDay(1), fim.atEndOfMonth())) {
            agregados.put(YearMonth.from(item.getMes()), decimal(item.getValor()));
        }
        List<ValorMes> meses = new ArrayList<>();
        for (YearMonth atual = inicio; !atual.isAfter(fim); atual = atual.plusMonths(1)) {
            meses.add(new ValorMes(atual, agregados.getOrDefault(atual, BigDecimal.ZERO)));
        }
        BigDecimal maximo = maximo(meses.stream().map(ValorMes::valor).toList());
        List<DashboardTendenciasResumo.ValorPonto> pontos = meses.stream()
                .map(item -> new DashboardTendenciasResumo.ValorPonto(
                        item.mes().toString(), ROTULO_MES.format(item.mes().atDay(1)), item.valor(),
                        percentual(item.valor(), maximo)))
                .toList();
        BigDecimal total = meses.stream().map(ValorMes::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardTendenciasResumo.ComprasSerie(total, total.signum() > 0, pontos);
    }

    private BigDecimal maximo(List<BigDecimal> valores) {
        return valores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private int percentual(BigDecimal valor, BigDecimal maximo) {
        if (valor == null || valor.signum() <= 0 || maximo == null || maximo.signum() <= 0) return 0;
        return Math.max(1, valor.multiply(BigDecimal.valueOf(100))
                .divide(maximo, 0, RoundingMode.HALF_UP).intValue());
    }

    private int percentual(long valor, long maximo) {
        if (valor <= 0 || maximo <= 0) return 0;
        return Math.max(1, BigDecimal.valueOf(valor).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(maximo), 0, RoundingMode.HALF_UP).intValue());
    }

    private long valor(Long valor) { return valor == null ? 0 : valor; }
    private BigDecimal decimal(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor; }

    private record DiaPostura(LocalDate data, long inteiros, long perdas, long total) {}
    private record ValorData(LocalDate data, BigDecimal valor) {}
    private record ValorMes(YearMonth mes, BigDecimal valor) {}
}
