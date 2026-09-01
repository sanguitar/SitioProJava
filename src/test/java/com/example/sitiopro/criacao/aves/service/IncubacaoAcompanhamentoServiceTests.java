package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.RegistrarAcompanhamentoIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.entity.AcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.repository.AcompanhamentoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncubacaoAcompanhamentoServiceTests {

    @Mock private IncubacaoAvesRepository incubacaoRepository;
    @Mock private AcompanhamentoIncubacaoAvesRepository repository;
    @Mock private CodigoCriacaoService codigoService;

    private IncubacaoAcompanhamentoService service;
    private IncubacaoAves incubacao;

    @BeforeEach
    void preparar() {
        service = new IncubacaoAcompanhamentoService(incubacaoRepository, repository, codigoService,
                Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneOffset.UTC));
        incubacao = incubacao(MetodoIncubacaoAves.CHOCADEIRA);
    }

    @Test
    void registraOvoscopiaSemAlterarQuantidadeInicial() {
        RegistrarAcompanhamentoIncubacaoAvesRequest request = request(TipoAcompanhamentoIncubacaoAves.OVOSCOPIA);
        request.setQuantidadeAvaliada(20);
        request.setOvosFerteis(16);
        request.setOvosSemDesenvolvimento(2);
        request.setPerdas(2);
        prepararPersistencia(request);

        var resumo = service.registrar(1L, request);

        assertThat(resumo.ovosFerteis()).isEqualTo(16);
        assertThat(resumo.ovosSemDesenvolvimento()).isEqualTo(2);
        assertThat(resumo.perdas()).isEqualTo(2);
        assertThat(incubacao.getQuantidadeOvos()).isEqualTo(20);
        verify(codigoService).bloquearIdempotencia("ACOMPANHAMENTO_INCUBACAO_AVES", "acomp-1");
    }

    @Test
    void registraTemperaturaEUmidadeEmBigDecimalNaChocadeira() {
        RegistrarAcompanhamentoIncubacaoAvesRequest request = request(
                TipoAcompanhamentoIncubacaoAves.TEMPERATURA_UMIDADE);
        request.setTemperatura(new BigDecimal("37.50"));
        request.setUmidade(new BigDecimal("55.25"));
        prepararPersistencia(request);

        var resumo = service.registrar(1L, request);

        assertThat(resumo.temperatura()).isEqualByComparingTo("37.50");
        assertThat(resumo.umidade()).isEqualByComparingTo("55.25");
    }

    @Test
    void galinhaChocaAceitaVerificacaoMasRecusaMedicaoDeEquipamento() {
        incubacao = incubacao(MetodoIncubacaoAves.GALINHA_CHOCA);
        RegistrarAcompanhamentoIncubacaoAvesRequest medicao = request(
                TipoAcompanhamentoIncubacaoAves.TEMPERATURA_UMIDADE);
        medicao.setTemperatura(new BigDecimal("37.50"));
        when(repository.findByChaveIdempotencia("acomp-1")).thenReturn(Optional.empty());
        when(incubacaoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));

        assertThatThrownBy(() -> service.registrar(1L, medicao))
                .isInstanceOf(AvesOperacaoException.class)
                .extracting("code").isEqualTo("MEDICAO_NAO_APLICAVEL");
        verify(repository, never()).save(any());
    }

    @Test
    void retryDaMesmaChaveRetornaRegistroSemDuplicar() {
        RegistrarAcompanhamentoIncubacaoAvesRequest request = request(
                TipoAcompanhamentoIncubacaoAves.VERIFICACAO_GERAL);
        AcompanhamentoIncubacaoAves existente = new AcompanhamentoIncubacaoAves();
        ReflectionTestUtils.setField(existente, "id", 10L);
        existente.setIncubacao(incubacao);
        existente.setTipo(request.getTipo());
        existente.setDataHora(request.getDataHora());
        existente.setChaveIdempotencia("acomp-1");
        when(repository.findByChaveIdempotencia("acomp-1")).thenReturn(Optional.of(existente));

        assertThat(service.registrar(1L, request).id()).isEqualTo(10L);

        verify(incubacaoRepository, never()).buscarParaAtualizacao(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void detalheNaoPermiteAcessarRegistroDeOutraIncubacao() {
        IncubacaoAves outraIncubacao = incubacao(MetodoIncubacaoAves.CHOCADEIRA);
        ReflectionTestUtils.setField(outraIncubacao, "id", 2L);
        AcompanhamentoIncubacaoAves acompanhamento = new AcompanhamentoIncubacaoAves();
        ReflectionTestUtils.setField(acompanhamento, "id", 10L);
        acompanhamento.setIncubacao(outraIncubacao);
        when(repository.findById(10L)).thenReturn(Optional.of(acompanhamento));

        assertThatThrownBy(() -> service.detalhar(1L, 10L))
                .isInstanceOf(AvesOperacaoException.class)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
    }

    private void prepararPersistencia(RegistrarAcompanhamentoIncubacaoAvesRequest request) {
        when(repository.findByChaveIdempotencia(request.getChaveIdempotencia())).thenReturn(Optional.empty());
        when(incubacaoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));
        when(repository.save(any())).thenAnswer(invocation -> {
            AcompanhamentoIncubacaoAves item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", 10L);
            return item;
        });
    }

    private RegistrarAcompanhamentoIncubacaoAvesRequest request(TipoAcompanhamentoIncubacaoAves tipo) {
        RegistrarAcompanhamentoIncubacaoAvesRequest request = new RegistrarAcompanhamentoIncubacaoAvesRequest();
        request.setTipo(tipo);
        request.setDataHora(LocalDateTime.of(2026, 9, 5, 8, 0));
        request.setChaveIdempotencia("acomp-1");
        return request;
    }

    private IncubacaoAves incubacao(MetodoIncubacaoAves metodo) {
        IncubacaoAves item = new IncubacaoAves();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setCodigo("INC-2026-0001");
        item.setMetodo(metodo);
        item.setEspecie(EspecieAves.GALINHA);
        item.setDataInicio(LocalDate.of(2026, 9, 1));
        item.setDataPrevistaEclosao(LocalDate.of(2026, 9, 22));
        item.setQuantidadeOvos(20);
        item.setStatus(StatusIncubacaoAves.EM_INCUBACAO);
        return item;
    }
}
