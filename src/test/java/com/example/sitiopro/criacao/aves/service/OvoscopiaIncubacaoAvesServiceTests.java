package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.dto.ItemOvoscopiaIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.RegistrarOvoscopiaIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.entity.AchadoOvoscopiaAves;
import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.ItemOvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.OvoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.OvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoscopiaIncubacaoAvesRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvoscopiaIncubacaoAvesServiceTests {
    @Mock private IncubacaoAvesRepository incubacaoRepository;
    @Mock private OvoIncubacaoAvesRepository ovoRepository;
    @Mock private OvoscopiaIncubacaoAvesRepository ovoscopiaRepository;
    @Mock private CodigoCriacaoService codigoService;
    @Mock private TarefaService tarefaService;
    private OvoscopiaIncubacaoAvesService service;
    private IncubacaoAves incubacao;

    @BeforeEach
    void preparar() {
        service = new OvoscopiaIncubacaoAvesService(incubacaoRepository, ovoRepository, ovoscopiaRepository,
                codigoService, tarefaService, new AvesProperties(),
                Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC));
        incubacao = incubacao(3);
    }

    @Test
    void geraOvosNumeradosConformeQuantidadeInicial() {
        when(ovoRepository.countByIncubacaoId(1L)).thenReturn(0L);
        when(ovoRepository.findByIncubacaoIdOrderByNumeroAsc(1L)).thenReturn(List.of());

        service.garantirOvos(incubacao);

        ArgumentCaptor<List<OvoIncubacaoAves>> captor = ArgumentCaptor.forClass(List.class);
        verify(ovoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(OvoIncubacaoAves::getNumero)
                .containsExactly(1, 2, 3);
    }

    @Test
    void duasOvoscopiasSucessivasPreservamHistoricoEAtualizamSituacaoAtual() {
        List<OvoIncubacaoAves> ovos = ovos(incubacao, 3);
        OvoscopiaIncubacaoAves dia7 = ovoscopia(10L, incubacao, LocalDate.of(2026, 9, 7),
                item(100L, ovos.get(0), AchadoOvoscopiaAves.REAVALIAR, "Sombra fraca"),
                item(101L, ovos.get(1), AchadoOvoscopiaAves.RACHADURA, "Trinca fina"));
        OvoscopiaIncubacaoAves dia14 = ovoscopia(11L, incubacao, LocalDate.of(2026, 9, 14),
                item(102L, ovos.get(0), AchadoOvoscopiaAves.DESENVOLVIMENTO_VISIVEL, "Evoluiu"));
        when(ovoRepository.findByIncubacaoIdOrderByNumeroAsc(1L)).thenReturn(ovos);
        when(ovoscopiaRepository.findByIncubacaoIdOrderByDataOvoscopiaDescIdDesc(1L))
                .thenReturn(List.of(dia14, dia7));

        var resumos = service.ovos(1L);
        var historico = service.ovoscopias(1L);

        assertThat(historico).hasSize(2);
        assertThat(resumos.get(0).situacaoAtual()).isEqualTo(AchadoOvoscopiaAves.DESENVOLVIMENTO_VISIVEL);
        assertThat(resumos.get(0).ultimaObservacao()).isEqualTo("Evoluiu");
        assertThat(resumos.get(1).situacaoAtual()).isEqualTo(AchadoOvoscopiaAves.RACHADURA);
    }

    @Test
    void registrarEmLoteComReavaliarCriaTarefaSemDuplicarPelaMesmaChave() {
        List<OvoIncubacaoAves> ovos = ovos(incubacao, 3);
        when(ovoscopiaRepository.findByChaveIdempotencia("ovo-1")).thenReturn(Optional.empty());
        when(incubacaoRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));
        when(ovoRepository.countByIncubacaoId(1L)).thenReturn(3L);
        when(ovoRepository.findByIncubacaoIdOrderByNumeroAsc(1L)).thenReturn(ovos);
        when(ovoscopiaRepository.save(any())).thenAnswer(inv -> {
            OvoscopiaIncubacaoAves ovoscopia = inv.getArgument(0);
            ReflectionTestUtils.setField(ovoscopia, "id", 10L);
            return ovoscopia;
        });
        RegistrarOvoscopiaIncubacaoAvesRequest request = request(
                AchadoOvoscopiaAves.REAVALIAR,
                AchadoOvoscopiaAves.DESENVOLVIMENTO_VISIVEL,
                AchadoOvoscopiaAves.PERDA_RETIRADA);

        var resumo = service.registrar(1L, request, new UsuarioAtor(2L, "operador", false));

        assertThat(resumo.totalAvaliado()).isEqualTo(3);
        assertThat(resumo.reavaliar()).isEqualTo(1);
        assertThat(resumo.retirados()).isEqualTo(1);
        ArgumentCaptor<TarefaAutomaticaRequest> tarefa = ArgumentCaptor.forClass(TarefaAutomaticaRequest.class);
        verify(tarefaService).sincronizarAutomatica(tarefa.capture(), any());
        assertThat(tarefa.getValue().chaveAutomacao())
                .isEqualTo("CRIACAO:AVES:INCUBACAO:1:OVOSCOPIA_REAVALIACAO");
    }

    @Test
    void fichaDestacaPendenciasAnteriores() {
        List<OvoIncubacaoAves> ovos = ovos(incubacao, 2);
        OvoscopiaIncubacaoAves dia7 = ovoscopia(10L, incubacao, LocalDate.of(2026, 9, 7),
                item(100L, ovos.get(0), AchadoOvoscopiaAves.REAVALIAR, "Olhar novamente"));
        when(incubacaoRepository.findById(1L)).thenReturn(Optional.of(incubacao));
        when(ovoRepository.findByIncubacaoIdOrderByNumeroAsc(1L)).thenReturn(ovos);
        when(ovoscopiaRepository.findByIncubacaoIdOrderByDataOvoscopiaDescIdDesc(1L))
                .thenReturn(List.of(dia7));

        var ficha = service.ficha(1L);

        assertThat(ficha.ovos()).hasSize(2);
        assertThat(ficha.ovos().get(0).pendenteReavaliacao()).isTrue();
        assertThat(ficha.proximaVerificacao()).isEqualTo(LocalDate.of(2026, 9, 17));
    }

    private RegistrarOvoscopiaIncubacaoAvesRequest request(AchadoOvoscopiaAves... achados) {
        RegistrarOvoscopiaIncubacaoAvesRequest request = new RegistrarOvoscopiaIncubacaoAvesRequest();
        request.setDataOvoscopia(LocalDate.of(2026, 9, 10));
        request.setProximaVerificacao(LocalDate.of(2026, 9, 17));
        request.setChaveIdempotencia("ovo-1");
        request.setResponsavel("Operador");
        for (int i = 0; i < achados.length; i++) {
            ItemOvoscopiaIncubacaoAvesRequest item = new ItemOvoscopiaIncubacaoAvesRequest();
            item.setNumero(i + 1);
            item.setAchado(achados[i]);
            item.setObservacao("Obs " + (i + 1));
            request.getItens().add(item);
        }
        return request;
    }

    private IncubacaoAves incubacao(int quantidade) {
        IncubacaoAves item = new IncubacaoAves();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setCodigo("INC-2026-0001");
        item.setMetodo(MetodoIncubacaoAves.CHOCADEIRA);
        item.setEspecie(EspecieAves.GALINHA);
        item.setDataInicio(LocalDate.of(2026, 9, 1));
        item.setDataPrevistaEclosao(LocalDate.of(2026, 9, 22));
        item.setQuantidadeOvos(quantidade);
        item.setStatus(StatusIncubacaoAves.EM_INCUBACAO);
        return item;
    }

    private List<OvoIncubacaoAves> ovos(IncubacaoAves incubacao, int total) {
        List<OvoIncubacaoAves> ovos = new ArrayList<>();
        for (int numero = 1; numero <= total; numero++) {
            OvoIncubacaoAves ovo = new OvoIncubacaoAves();
            ReflectionTestUtils.setField(ovo, "id", (long) numero);
            ovo.setIncubacao(incubacao);
            ovo.setNumero(numero);
            ovos.add(ovo);
        }
        return ovos;
    }

    private OvoscopiaIncubacaoAves ovoscopia(Long id, IncubacaoAves incubacao, LocalDate data,
            ItemOvoscopiaIncubacaoAves... itens) {
        OvoscopiaIncubacaoAves ovoscopia = new OvoscopiaIncubacaoAves();
        ReflectionTestUtils.setField(ovoscopia, "id", id);
        ovoscopia.setIncubacao(incubacao);
        ovoscopia.setDataOvoscopia(data);
        ovoscopia.setDiaIncubacao((int) (data.toEpochDay() - incubacao.getDataInicio().toEpochDay() + 1));
        ovoscopia.setProximaVerificacao(data.plusDays(7));
        for (ItemOvoscopiaIncubacaoAves item : itens) {
            item.setOvoscopia(ovoscopia);
            ovoscopia.getItens().add(item);
        }
        return ovoscopia;
    }

    private ItemOvoscopiaIncubacaoAves item(Long id, OvoIncubacaoAves ovo,
            AchadoOvoscopiaAves achado, String observacao) {
        ItemOvoscopiaIncubacaoAves item = new ItemOvoscopiaIncubacaoAves();
        ReflectionTestUtils.setField(item, "id", id);
        item.setOvo(ovo);
        item.setAchado(achado);
        item.setObservacao(observacao);
        return item;
    }
}

