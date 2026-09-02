package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.servico;
import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.MortalidadeAvesRepository;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvesAlertasServiceTests {
    @Mock private LoteAvesRepository loteRepository;
    @Mock private MortalidadeAvesRepository mortalidadeRepository;
    @Mock private IncubacaoAvesRepository incubacaoRepository;
    @Mock private AlertaService alertaService;
    private AvesAlertasService service;
    private ConfiguracaoOperacionalService configuracao;

    @BeforeEach
    void preparar() {
        configuracao = servico();
        service = new AvesAlertasService(loteRepository, mortalidadeRepository, incubacaoRepository,
                alertaService, new AvesProperties(), configuracao,
                Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void antecedenciaPersistidaAmpliaJanelaDeEclosao() {
        when(configuracao.obter()).thenReturn(new ConfiguracaoOperacionalLeitura(
                "Teste", "UTC", null, null, 21, 5, 0, null, null));
        when(incubacaoRepository.findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO))
                .thenReturn(List.of(incubacao(20L, "INC-20", LocalDate.of(2026, 8, 29)),
                        incubacao(21L, "INC-21", LocalDate.of(2026, 8, 30))));
        service.avaliar();
        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_INCUBACAO_ECLOSAO_PROXIMA),
                argThat(c -> c.size() == 1 && c.getFirst().referenciaOrigem().equals("INCUBACAO:20")));
    }

    @Test
    void mortalidadeAcimaDoThresholdProduzChaveDeterministica() {
        LoteAves lote = lote(7L, 100);
        when(loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO)).thenReturn(List.of(lote));
        when(mortalidadeRepository.somarDesde(eq(7L), any())).thenReturn(6L);
        when(incubacaoRepository.findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO))
                .thenReturn(List.of());

        service.avaliar();

        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES), eq(TipoAlerta.CRIACAO_MORTALIDADE_ALTA),
                argThat(condicoes -> condicoes.size() == 1
                        && condicoes.getFirst().chaveDeduplicacao().equals("CRIACAO:AVES:LOTE:7:MORTALIDADE_ALTA")));
    }

    @Test
    void ausenciaDaCondicaoEnviaListaVaziaParaResolverAlertaAnterior() {
        LoteAves lote = lote(7L, 100);
        when(loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO)).thenReturn(List.of(lote));
        when(mortalidadeRepository.somarDesde(eq(7L), any())).thenReturn(2L);
        when(incubacaoRepository.findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO))
                .thenReturn(List.of());

        service.avaliar();

        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES), eq(TipoAlerta.CRIACAO_MORTALIDADE_ALTA),
                argThat(List::isEmpty));
    }

    @Test
    void separaEclosaoProximaDeIncubacaoAtrasada() {
        when(loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO)).thenReturn(List.of());
        IncubacaoAves proxima = incubacao(11L, "INC-11", LocalDate.of(2026, 8, 25));
        IncubacaoAves atrasada = incubacao(12L, "INC-12", LocalDate.of(2026, 8, 23));
        when(incubacaoRepository.findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO))
                .thenReturn(List.of(proxima, atrasada));

        service.avaliar();

        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_INCUBACAO_ECLOSAO_PROXIMA),
                argThat(c -> c.size() == 1 && c.getFirst().referenciaOrigem().equals("INCUBACAO:11")));
        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_INCUBACAO_ATRASADA),
                argThat(c -> c.size() == 1 && c.getFirst().referenciaOrigem().equals("INCUBACAO:12")));
    }

    @Test
    void semIncubacaoAbertaResolveAlertasDeEclosaoAnteriores() {
        when(loteRepository.findByStatusOrderByCodigoAsc(StatusLoteAves.ATIVO)).thenReturn(List.of());
        when(incubacaoRepository.findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves.EM_INCUBACAO))
                .thenReturn(List.of());

        service.avaliar();

        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_INCUBACAO_ECLOSAO_PROXIMA), argThat(List::isEmpty));
        verify(alertaService).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_INCUBACAO_ATRASADA), argThat(List::isEmpty));
    }

    private LoteAves lote(Long id, int quantidadeInicial) {
        LoteAves lote = new LoteAves();
        ReflectionTestUtils.setField(lote, "id", id);
        lote.setCodigo("AV-" + id);
        lote.setQuantidadeInicial(quantidadeInicial);
        lote.setStatus(StatusLoteAves.ATIVO);
        return lote;
    }

    private IncubacaoAves incubacao(Long id, String codigo, LocalDate previsao) {
        IncubacaoAves incubacao = new IncubacaoAves();
        ReflectionTestUtils.setField(incubacao, "id", id);
        incubacao.setCodigo(codigo);
        incubacao.setDataPrevistaEclosao(previsao);
        incubacao.setStatus(StatusIncubacaoAves.EM_INCUBACAO);
        return incubacao;
    }
}
