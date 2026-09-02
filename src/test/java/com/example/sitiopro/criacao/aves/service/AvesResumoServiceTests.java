package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.MortalidadeAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.servico;

@ExtendWith(MockitoExtension.class)
class AvesResumoServiceTests {

    @Mock private LoteAvesRepository loteRepository;
    @Mock private IncubacaoAvesRepository incubacaoRepository;
    @Mock private RegistroPosturaAvesRepository posturaRepository;
    @Mock private MortalidadeAvesRepository mortalidadeRepository;
    @Mock private AlertaService alertaService;

    private AvesResumoService service;
    private LocalDate hoje;

    @BeforeEach
    void preparar() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneOffset.UTC);
        hoje = LocalDate.of(2026, 9, 5);
        service = new AvesResumoService(loteRepository, incubacaoRepository, posturaRepository,
                mortalidadeRepository, alertaService, new AvesProperties(), servico(), clock);
    }

    @Test
    void resumeOvosProximaEclosaoEPintinhosRecentes() {
        IncubacaoAves proxima = new IncubacaoAves();
        proxima.setCodigo("INC-2026-0007");
        proxima.setDataPrevistaEclosao(hoje.plusDays(12));
        when(loteRepository.countByStatus(StatusLoteAves.ATIVO)).thenReturn(3L);
        when(loteRepository.somarQuantidadePorStatus(StatusLoteAves.ATIVO)).thenReturn(80L);
        when(incubacaoRepository.countByStatus(StatusIncubacaoAves.EM_INCUBACAO)).thenReturn(1L);
        when(incubacaoRepository.countByStatusAndDataPrevistaEclosaoBetween(
                StatusIncubacaoAves.EM_INCUBACAO, hoje, hoje.plusDays(2))).thenReturn(0L);
        when(alertaService.contarAbertos(ModuloOrigem.CRIACOES)).thenReturn(2L);
        when(posturaRepository.somarInteirosNaData(hoje)).thenReturn(14L);
        when(mortalidadeRepository.somarTotalDesde(LocalDateTime.of(2026, 8, 29, 12, 0))).thenReturn(1L);
        when(incubacaoRepository.somarOvosPorStatus(StatusIncubacaoAves.EM_INCUBACAO)).thenReturn(20L);
        when(incubacaoRepository.findFirstByStatusOrderByDataPrevistaEclosaoAscIdAsc(
                StatusIncubacaoAves.EM_INCUBACAO)).thenReturn(Optional.of(proxima));
        when(incubacaoRepository.somarPintinhosDesde(StatusIncubacaoAves.FINALIZADA,
                hoje.minusDays(30))).thenReturn(16L);

        var resumo = service.resumo();

        assertThat(resumo.incubacoesAtivas()).isEqualTo(1);
        assertThat(resumo.ovosEmIncubacao()).isEqualTo(20);
        assertThat(resumo.proximaIncubacaoCodigo()).isEqualTo("INC-2026-0007");
        assertThat(resumo.proximaEclosao()).isEqualTo(hoje.plusDays(12));
        assertThat(resumo.diasProximaEclosao()).isEqualTo(12);
        assertThat(resumo.pintinhosRecentes()).isEqualTo(16);
    }

    @Test
    void semIncubacoesRetornaEstadoVazioCoerente() {
        when(loteRepository.somarQuantidadePorStatus(StatusLoteAves.ATIVO)).thenReturn(0L);
        when(posturaRepository.somarInteirosNaData(hoje)).thenReturn(0L);
        when(mortalidadeRepository.somarTotalDesde(LocalDateTime.of(2026, 8, 29, 12, 0))).thenReturn(0L);
        when(incubacaoRepository.somarOvosPorStatus(StatusIncubacaoAves.EM_INCUBACAO)).thenReturn(0L);
        when(incubacaoRepository.findFirstByStatusOrderByDataPrevistaEclosaoAscIdAsc(
                StatusIncubacaoAves.EM_INCUBACAO)).thenReturn(Optional.empty());
        when(incubacaoRepository.somarPintinhosDesde(StatusIncubacaoAves.FINALIZADA,
                hoje.minusDays(30))).thenReturn(0L);

        var resumo = service.resumo();

        assertThat(resumo.ovosEmIncubacao()).isZero();
        assertThat(resumo.proximaEclosao()).isNull();
        assertThat(resumo.diasProximaEclosao()).isNull();
        assertThat(resumo.pintinhosRecentes()).isZero();
    }
}
