package com.example.sitiopro.agua.service;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.entity.*;
import com.example.sitiopro.agua.repository.OperacaoAguaRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AguaServiceTests {
    @Mock RecursoHidricoRepository recursos;
    @Mock PropriedadeRepository propriedades;
    @Mock OperacaoAguaRepository operacoes;
    @Mock CodigoCriacaoService locks;
    @Mock TarefaService tarefas;
    @Mock AguaAlertasService alertas;
    @Mock ManutencaoService manutencao;
    AguaService service;
    Propriedade propriedade;
    RecursoHidrico origem;
    RecursoHidrico destino;
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach
    void preparar() {
        service = new AguaService(recursos, propriedades, operacoes, locks, tarefas, alertas,
                manutencao, Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC));
        propriedade = new Propriedade();
        ReflectionTestUtils.setField(propriedade, "id", 1L);
        origem = recurso(10L, "Caixa principal", "500.000");
        destino = recurso(11L, "Caixa secundária", "100.000");
        lenient().when(propriedades.findByPrincipalTrue()).thenReturn(Optional.of(propriedade));
        lenient().when(recursos.buscarParaAtualizacao(10L, 1L)).thenReturn(Optional.of(origem));
        lenient().when(recursos.buscarParaAtualizacao(11L, 1L)).thenReturn(Optional.of(destino));
        lenient().when(operacoes.findByChaveIdempotencia(anyString())).thenReturn(Optional.empty());
        lenient().when(operacoes.saveAndFlush(any())).thenAnswer(inv -> {
            OperacaoAgua o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "id", 30L);
            return o;
        });
        lenient().when(alertas.limiteEfetivo(any())).thenAnswer(inv -> {
            RecursoHidrico r = inv.getArgument(0);
            return r.getNivelMinimoAlertaLitros();
        });
    }

    @Test
    void medicaoDefineVolumeEAgendaInspecao() {
        origem.setVolumeAtualLitros(null);
        OperacaoAguaResumo resultado = service.registrar(request(TipoOperacaoAgua.MEDICAO_NIVEL, "200.125", "med-1"), operador);

        assertThat(resultado.volumeOrigemAntesLitros()).isNull();
        assertThat(resultado.volumeOrigemDepoisLitros()).isEqualByComparingTo("200.125");
        assertThat(origem.getVolumeAtualLitros()).isEqualByComparingTo("200.125");
        verify(tarefas).sincronizarAutomatica(argThat(t -> t.moduloOrigem() == ModuloOrigem.AGUA
                && t.referenciaOrigem().equals("RECURSO_HIDRICO:10")), eq(operador));
        verify(alertas).avaliar();
    }

    @Test
    void abastecimentoConsumoERetiradaMantemSaldoPreciso() {
        service.registrar(request(TipoOperacaoAgua.ABASTECIMENTO, "50.125", "ab-1"), operador);
        assertThat(origem.getVolumeAtualLitros()).isEqualByComparingTo("550.125");

        service.registrar(request(TipoOperacaoAgua.CONSUMO, "25.100", "co-1"), operador);
        service.registrar(request(TipoOperacaoAgua.RETIRADA, "5.025", "re-1"), operador);
        assertThat(origem.getVolumeAtualLitros()).isEqualByComparingTo("520.000");
    }

    @Test
    void transferenciaAtualizaOsDoisRecursosAtomicamente() {
        OperacaoAguaRequest request = request(TipoOperacaoAgua.TRANSFERENCIA, "200", "tr-1");
        request.setRecursoDestinoId(11L);

        OperacaoAguaResumo resultado = service.registrar(request, operador);

        assertThat(origem.getVolumeAtualLitros()).isEqualByComparingTo("300.000");
        assertThat(destino.getVolumeAtualLitros()).isEqualByComparingTo("300.000");
        assertThat(resultado.volumeDestinoAntesLitros()).isEqualByComparingTo("100.000");
        assertThat(resultado.volumeDestinoDepoisLitros()).isEqualByComparingTo("300.000");
    }

    @Test
    void saldoInsuficienteInterrompeAntesDePersistirEAlertar() {
        assertThatThrownBy(() -> service.registrar(request(TipoOperacaoAgua.CONSUMO, "600", "co-falha"), operador))
                .isInstanceOf(AguaOperacaoException.class).hasMessageContaining("negativo");

        verify(operacoes, never()).saveAndFlush(any());
        verify(alertas, never()).avaliar();
    }

    @Test
    void retryRetornaOperacaoSemDuplicarSaldoTarefaOuAlerta() {
        OperacaoAgua existente = operacaoExistente("retry-1");
        when(operacoes.findByChaveIdempotencia("retry-1")).thenReturn(Optional.of(existente));

        OperacaoAguaResumo resultado = service.registrar(request(TipoOperacaoAgua.CONSUMO, "10", "retry-1"), operador);

        assertThat(resultado.id()).isEqualTo(90L);
        assertThat(origem.getVolumeAtualLitros()).isEqualByComparingTo("500.000");
        verify(operacoes, never()).saveAndFlush(any());
        verifyNoInteractions(tarefas, alertas);
    }

    @Test
    void retryComPayloadDivergenteEhRejeitado() {
        OperacaoAgua existente = operacaoExistente("retry-divergente");
        when(operacoes.findByChaveIdempotencia("retry-divergente")).thenReturn(Optional.of(existente));
        OperacaoAguaRequest request = request(TipoOperacaoAgua.CONSUMO, "11", "retry-divergente");

        assertThatThrownBy(() -> service.registrar(request, operador))
                .isInstanceOf(AguaOperacaoException.class)
                .hasMessageContaining("outra operação");
        verify(operacoes, never()).saveAndFlush(any());
    }

    @Test
    void inspecaoConcluiCicloAnteriorECriaProximoSemDuplicacao() {
        LocalDateTime anterior = origem.getProximaInspecao();
        OperacaoAguaRequest request = request(TipoOperacaoAgua.INSPECAO, null, "insp-1");

        service.registrar(request, operador);

        assertThat(origem.getUltimaInspecao()).isEqualTo(request.getDataOperacao());
        assertThat(origem.getProximaInspecao()).isEqualTo(request.getDataOperacao().plusDays(7));
        verify(tarefas).concluirAutomatica(AguaService.chaveInspecao(10L, anterior), operador);
        verify(tarefas).sincronizarAutomatica(argThat(t -> t.chaveAutomacao()
                .equals(AguaService.chaveInspecao(10L, origem.getProximaInspecao()))), eq(operador));
    }

    @Test
    void configuracaoProtegeVersaoELimiteDaCapacidade() {
        RecursoHidricoOperacaoConfigRequest request = new RecursoHidricoOperacaoConfigRequest();
        request.setVersao(0L); request.setIntervaloInspecaoDias(15);
        request.setNivelMinimoAlertaLitros(new BigDecimal("250"));
        request.setProximaInspecao(LocalDateTime.of(2026, 10, 20, 8, 0));

        service.configurar(10L, request, operador);
        assertThat(origem.getIntervaloInspecaoDias()).isEqualTo(15);
        verify(tarefas).sincronizarAutomatica(any(), eq(operador));

        request.setNivelMinimoAlertaLitros(new BigDecimal("1001"));
        assertThatThrownBy(() -> service.configurar(10L, request, operador)).hasMessageContaining("capacidade");
    }

    private OperacaoAguaRequest request(TipoOperacaoAgua tipo, String quantidade, String chave) {
        OperacaoAguaRequest r = new OperacaoAguaRequest();
        r.setRecursoOrigemId(10L); r.setTipo(tipo);
        r.setDataOperacao(LocalDateTime.of(2026, 10, 8, 8, 0));
        r.setQuantidadeLitros(quantidade == null ? null : new BigDecimal(quantidade));
        r.setChaveIdempotencia(chave);
        return r;
    }

    private RecursoHidrico recurso(Long id, String nome, String volume) {
        RecursoHidrico r = new RecursoHidrico();
        ReflectionTestUtils.setField(r, "id", id);
        r.setPropriedade(propriedade); r.setNome(nome); r.setTipo(TipoRecursoHidrico.RESERVATORIO);
        r.setAtivo(true); r.setCapacidadeLitros(new BigDecimal("1000.000"));
        r.setVolumeAtualLitros(new BigDecimal(volume)); r.setNivelMinimoAlertaLitros(new BigDecimal("200.000"));
        r.setIntervaloInspecaoDias(7); r.setProximaInspecao(LocalDateTime.of(2026, 10, 15, 8, 0));
        return r;
    }

    private OperacaoAgua operacaoExistente(String chave) {
        OperacaoAgua o = new OperacaoAgua(); ReflectionTestUtils.setField(o, "id", 90L);
        o.setRecursoOrigem(origem); o.setTipo(TipoOperacaoAgua.CONSUMO);
        o.setDataOperacao(LocalDateTime.of(2026, 10, 8, 8, 0)); o.setQuantidadeLitros(BigDecimal.TEN);
        o.setVolumeOrigemAntesLitros(new BigDecimal("500")); o.setVolumeOrigemDepoisLitros(new BigDecimal("490"));
        o.setChaveIdempotencia(chave); return o;
    }
}
