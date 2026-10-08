package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.tarefas.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoPreventivaServiceTests {
    @Mock PlanoManutencaoPreventivaRepository planos;
    @Mock LeituraMedidorAtivoRepository leituras;
    @Mock AtivoPatrimonialRepository ativos;
    @Mock CodigoCriacaoService codigos;
    @Mock TarefaService tarefas;
    @Mock ManutencaoAlertasService alertas;
    ManutencaoPreventivaService service;
    AtivoPatrimonial ativo;
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);
    final UsuarioAtor admin = new UsuarioAtor(1L, "admin", true);

    @BeforeEach void preparar() {
        service = new ManutencaoPreventivaService(planos, leituras, ativos, codigos, tarefas, alertas,
                Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC));
        ativo = new AtivoPatrimonial(); ReflectionTestUtils.setField(ativo, "id", 10L);
        ativo.setCodigo("PAT-2026-0001"); ativo.setNome("Trator");
        lenient().when(ativos.buscarParaAtualizacao(10L)).thenReturn(Optional.of(ativo));
        lenient().when(planos.findByAtivoPatrimonialIdOrderByAtivoDescNomeAsc(10L)).thenReturn(List.of());
        lenient().when(planos.saveAndFlush(any())).thenAnswer(inv -> { PlanoManutencaoPreventiva p=inv.getArgument(0); ReflectionTestUtils.setField(p,"id",20L); return p; });
        lenient().when(leituras.saveAndFlush(any())).thenAnswer(inv -> { LeituraMedidorAtivo l=inv.getArgument(0); ReflectionTestUtils.setField(l,"id",30L); return l; });
    }

    @Test void planoPorDiasCalculaProximaManutencaoECriaUmaTarefaPorCiclo() {
        PlanoManutencaoPreventivaRequest request = planoRequest(TipoPeriodicidadeManutencao.DIAS, "30");
        request.setDataReferencia(LocalDateTime.of(2026, 10, 1, 8, 0));

        PlanoManutencaoPreventivaResumo criado = service.criarPlano(request, admin);

        assertThat(criado.proximaData()).isEqualTo(LocalDateTime.of(2026, 10, 31, 8, 0));
        verify(tarefas).sincronizarAutomatica(argThat(t -> t.chaveAutomacao().equals("MANUTENCAO:PLANO:20:CICLO:1")
                && t.dataVencimento().equals(criado.proximaData())), eq(admin));
    }

    @Test void planoPorHorimetroExigeLeituraInicialECalculaLimite() {
        LeituraMedidorAtivo leitura = leitura(100, null, LocalDateTime.of(2026, 10, 5, 8, 0));
        when(leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(leitura));

        PlanoManutencaoPreventivaResumo criado = service.criarPlano(planoRequest(TipoPeriodicidadeManutencao.HORIMETRO, "50"), admin);

        assertThat(criado.proximoValor()).isEqualByComparingTo("150.00");
        assertThat(criado.leituraAtual()).isEqualByComparingTo("100.00");
    }

    @Test void leituraOperacionalNaoPodeRegredir() {
        LeituraMedidorAtivo anterior = leitura(120, null, LocalDateTime.of(2026, 10, 5, 8, 0));
        when(leituras.findTopByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(anterior));
        when(leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(anterior));

        assertThatThrownBy(() -> service.registrarLeitura(leituraRequest("110", null, "leitura-1"), operador))
                .isInstanceOf(ManutencaoOperacaoException.class).hasMessageContaining("não pode regredir");
        verify(leituras, never()).saveAndFlush(any());
    }

    @Test void ajusteAdministrativoPermiteCorrecaoSemApagarHistorico() {
        LeituraMedidorAtivo anterior = leitura(120, null, LocalDateTime.of(2026, 10, 5, 8, 0));
        when(leituras.findTopByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(anterior));
        LeituraMedidorRequest request = leituraRequest("110", null, "ajuste-1");
        request.setJustificativaAjuste("Troca do painel do horímetro");

        LeituraMedidorResumo criada = service.registrarAjusteAdministrativo(request, admin);

        assertThat(criada.tipoLeitura()).isEqualTo(TipoLeituraMedidor.AJUSTE_ADMINISTRATIVO);
        assertThat(criada.justificativaAjuste()).isEqualTo("Troca do painel do horímetro");
        verify(leituras, never()).delete(any());
    }

    @Test void operadorNaoPodeExecutarAjusteAdministrativo() {
        LeituraMedidorRequest request = leituraRequest("10", null, "ajuste-negado");
        request.setJustificativaAjuste("Correção");
        assertThatThrownBy(() -> service.registrarAjusteAdministrativo(request, operador))
                .isInstanceOf(ManutencaoOperacaoException.class)
                .extracting(e -> ((ManutencaoOperacaoException)e).getStatus()).isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test void retryDaLeituraNaoDuplicaRegistroTarefaOuAlerta() {
        LeituraMedidorAtivo existente = leitura(100, null, LocalDateTime.of(2026, 10, 6, 8, 0));
        existente.setAtivoPatrimonial(ativo); existente.setTipoLeitura(TipoLeituraMedidor.OPERACIONAL);
        when(leituras.findByChaveIdempotencia("leitura-retry")).thenReturn(Optional.of(existente));

        service.registrarLeitura(leituraRequest("100", null, "leitura-retry"), operador);

        verify(leituras, never()).saveAndFlush(any()); verifyNoInteractions(tarefas, alertas);
    }

    @Test void concluirPlanoAvancaCicloSemReutilizarTarefaFinalizada() {
        PlanoManutencaoPreventiva plano = plano(TipoPeriodicidadeManutencao.HORIMETRO, "100", "200");
        when(planos.buscarParaAtualizacao(20L)).thenReturn(Optional.of(plano));
        LeituraMedidorAtivo atual = leitura(150, null, LocalDateTime.of(2026, 10, 6, 8, 0));
        when(leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(atual));

        service.concluirCiclo(20L, 10L, LocalDateTime.of(2026, 10, 6, 8, 0), new BigDecimal("150"), null, operador);

        assertThat(plano.getCicloAtual()).isEqualTo(2);
        assertThat(plano.getProximoValor()).isEqualByComparingTo("250.00");
        verify(tarefas).concluirAutomatica("MANUTENCAO:PLANO:20:CICLO:1", operador);
        verify(tarefas).sincronizarAutomatica(argThat(t -> t.chaveAutomacao().equals("MANUTENCAO:PLANO:20:CICLO:2")), eq(operador));
    }

    private PlanoManutencaoPreventivaRequest planoRequest(TipoPeriodicidadeManutencao tipo, String intervalo) {
        PlanoManutencaoPreventivaRequest r = new PlanoManutencaoPreventivaRequest(); r.setAtivoId(10L);
        r.setNome("Revisão programada"); r.setTipoPeriodicidade(tipo); r.setIntervalo(new BigDecimal(intervalo));
        r.setChaveIdempotencia("plano-" + tipo); return r;
    }
    private LeituraMedidorRequest leituraRequest(String horimetro, String km, String chave) {
        LeituraMedidorRequest r = new LeituraMedidorRequest(); r.setAtivoId(10L);
        r.setDataLeitura(LocalDateTime.of(2026, 10, 6, 8, 0));
        r.setHorimetro(horimetro == null ? null : new BigDecimal(horimetro));
        r.setQuilometragem(km == null ? null : new BigDecimal(km)); r.setChaveIdempotencia(chave); return r;
    }
    private LeituraMedidorAtivo leitura(double horimetro, Double km, LocalDateTime data) {
        LeituraMedidorAtivo l = new LeituraMedidorAtivo(); l.setAtivoPatrimonial(ativo); l.setDataLeitura(data);
        l.setHorimetro(BigDecimal.valueOf(horimetro).setScale(2));
        l.setQuilometragem(km == null ? null : BigDecimal.valueOf(km).setScale(2)); l.setTipoLeitura(TipoLeituraMedidor.OPERACIONAL); return l;
    }
    private PlanoManutencaoPreventiva plano(TipoPeriodicidadeManutencao tipo, String intervalo, String limite) {
        PlanoManutencaoPreventiva p = new PlanoManutencaoPreventiva(); ReflectionTestUtils.setField(p,"id",20L);
        p.setAtivoPatrimonial(ativo); p.setNome("Revisão"); p.setTipoPeriodicidade(tipo);
        p.setIntervalo(new BigDecimal(intervalo)); p.setValorReferencia(new BigDecimal("100"));
        p.setProximoValor(new BigDecimal(limite)); p.setAtivo(true); p.setCicloAtual(1); return p;
    }
}
