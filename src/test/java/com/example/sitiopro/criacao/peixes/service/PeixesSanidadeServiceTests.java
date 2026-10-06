package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
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
class PeixesSanidadeServiceTests {
    @Mock RegistroSanitarioPeixesRepository registros;
    @Mock LotePeixesRepository lotes;
    @Mock EstoqueCatalogoService catalogo;
    @Mock EstoqueMovimentoService estoque;
    @Mock CodigoCriacaoService codigos;
    @Mock TarefaService tarefas;
    @Mock PeixesSanidadeAlertasService alertas;
    PeixesSanidadeService service;
    LotePeixes lote;
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach
    void preparar() {
        lote = new LotePeixes();
        ReflectionTestUtils.setField(lote, "id", 10L);
        lote.setCodigo("PX-2026-0001");
        lote.setStatus(StatusLotePeixes.ATIVO);
        service = new PeixesSanidadeService(registros, lotes, catalogo, estoque, codigos,
                tarefas, alertas, Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC));
        lenient().when(lotes.findById(10L)).thenReturn(Optional.of(lote));
        lenient().when(registros.saveAndFlush(any())).thenAnswer(inv -> {
            RegistroSanitarioPeixes registro = inv.getArgument(0);
            ReflectionTestUtils.setField(registro, "id", 30L);
            return registro;
        });
    }

    @Test
    void registraHistoricoEAgendaProximaAcao() {
        RegistroSanitarioPeixesRequest request = request("san-1");
        request.setProximaAcao("Reavaliar peixes");
        request.setProximaAcaoData(LocalDateTime.of(2026, 10, 2, 8, 0));

        RegistroSanitarioPeixesResumo criado = service.registrar(request, operador);

        assertThat(criado.tipo()).isEqualTo(TipoRegistroSanitarioPeixes.EXAME);
        assertThat(criado.loteCodigo()).isEqualTo("PX-2026-0001");
        verify(tarefas).sincronizarAutomatica(argThat(t ->
                t.chaveAutomacao().equals(PeixesSanidadeService.chaveTarefa(30L))
                        && t.dataVencimento().equals(LocalDateTime.of(2026, 10, 2, 8, 0))), eq(operador));
        verify(alertas).avaliar();
    }

    @Test
    void consumoUsaSomenteServicoOficialDoEstoque() {
        RegistroSanitarioPeixesRequest request = request("san-consumo");
        request.setItemEstoqueId(40L);
        request.setLocalEstoqueId(50L);
        request.setQuantidadeConsumida(new BigDecimal("1.2500"));
        ItemEstoque item = new ItemEstoque(); ReflectionTestUtils.setField(item, "id", 40L); item.setNome("Produto");
        LocalEstoque local = new LocalEstoque(); ReflectionTestUtils.setField(local, "id", 50L); local.setNome("Depósito");
        MovimentoEstoque movimento = new MovimentoEstoque(); ReflectionTestUtils.setField(movimento, "id", 60L);
        when(catalogo.buscarItem(40L)).thenReturn(item);
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local);
        when(estoque.registrarConsumoSanidadePeixes(any(), eq(30L), eq(10L))).thenReturn(movimento);

        RegistroSanitarioPeixesResumo criado = service.registrar(request, operador);

        assertThat(criado.quantidadeConsumida()).isEqualByComparingTo("1.2500");
        assertThat(criado.movimentoEstoqueId()).isEqualTo(60L);
        verify(estoque).registrarConsumoSanidadePeixes(argThat(m ->
                m.getItemId().equals(40L) && m.getLocalOrigemId().equals(50L)), eq(30L), eq(10L));
    }

    @Test
    void retryRetornaRegistroSemDuplicarEstoqueTarefaOuAlerta() {
        RegistroSanitarioPeixes existente = registro(30L);
        when(registros.findByChaveIdempotencia("san-retry")).thenReturn(Optional.of(existente));

        service.registrar(request("san-retry"), operador);

        verify(registros, never()).saveAndFlush(any());
        verifyNoInteractions(estoque, tarefas, alertas);
    }

    @Test
    void falhaDoEstoqueInterrompeFluxoTransacional() {
        RegistroSanitarioPeixesRequest request = request("san-rollback");
        request.setItemEstoqueId(40L); request.setLocalEstoqueId(50L);
        request.setQuantidadeConsumida(BigDecimal.ONE);
        ItemEstoque item = new ItemEstoque(); ReflectionTestUtils.setField(item, "id", 40L);
        LocalEstoque local = new LocalEstoque(); ReflectionTestUtils.setField(local, "id", 50L);
        when(catalogo.buscarItem(40L)).thenReturn(item);
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local);
        when(estoque.registrarConsumoSanidadePeixes(any(), eq(30L), eq(10L)))
                .thenThrow(new EstoqueOperacaoException("ESTOQUE_INSUFICIENTE", "Saldo insuficiente."));

        assertThatThrownBy(() -> service.registrar(request, operador)).hasMessage("Saldo insuficiente.");
        verifyNoInteractions(tarefas, alertas);
    }

    @Test
    void concluirProximaAcaoFinalizaTarefaEReavaliaAlertas() {
        RegistroSanitarioPeixes registro = registro(30L);
        registro.setProximaAcao("Inspecionar");
        registro.setProximaAcaoData(LocalDateTime.of(2026, 9, 29, 8, 0));
        when(registros.buscarParaAtualizacao(30L)).thenReturn(Optional.of(registro));

        RegistroSanitarioPeixesResumo concluido = service.concluirProximaAcao(30L, operador);

        assertThat(concluido.proximaAcaoConcluida()).isTrue();
        verify(tarefas).concluirAutomatica(PeixesSanidadeService.chaveTarefa(30L), operador);
        verify(alertas).avaliar();
    }

    @Test
    void validaDataCustoProximaAcaoEConsumoIncompleto() {
        RegistroSanitarioPeixesRequest request = request("san-invalido");
        request.setCusto(new BigDecimal("-1"));
        assertThatThrownBy(() -> service.registrar(request, operador)).hasMessageContaining("custo");
        request.setCusto(null); request.setProximaAcao("Reavaliar");
        assertThatThrownBy(() -> service.registrar(request, operador)).hasMessageContaining("em conjunto");
        request.setProximaAcao(null); request.setQuantidadeConsumida(BigDecimal.ONE);
        assertThatThrownBy(() -> service.registrar(request, operador)).hasMessageContaining("item, local");
    }

    private RegistroSanitarioPeixesRequest request(String chave) {
        RegistroSanitarioPeixesRequest request = new RegistroSanitarioPeixesRequest();
        request.setLoteId(10L); request.setTipo(TipoRegistroSanitarioPeixes.EXAME);
        request.setDataProcedimento(LocalDateTime.of(2026, 9, 30, 8, 0));
        request.setProcedimentoProduto("Avaliação visual"); request.setResponsavel("Operador");
        request.setChaveIdempotencia(chave);
        return request;
    }

    private RegistroSanitarioPeixes registro(Long id) {
        RegistroSanitarioPeixes registro = new RegistroSanitarioPeixes();
        ReflectionTestUtils.setField(registro, "id", id);
        registro.setLote(lote); registro.setTipo(TipoRegistroSanitarioPeixes.EXAME);
        registro.setDataProcedimento(LocalDateTime.of(2026, 9, 30, 8, 0));
        registro.setProcedimentoProduto("Avaliação visual"); registro.setResponsavel("Operador");
        return registro;
    }
}
