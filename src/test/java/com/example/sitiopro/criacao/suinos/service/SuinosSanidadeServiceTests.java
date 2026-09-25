package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
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
class SuinosSanidadeServiceTests {
    @Mock RegistroSanitarioSuinosRepository registros;
    @Mock LoteSuinosRepository lotes;
    @Mock AnimalReprodutivoSuinosRepository animais;
    @Mock EstoqueCatalogoService catalogo;
    @Mock EstoqueMovimentoService estoque;
    @Mock CodigoCriacaoService codigos;
    @Mock TarefaService tarefas;
    SuinosSanidadeService service;
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);
    LoteSuinos lote;

    @BeforeEach
    void preparar() {
        service = new SuinosSanidadeService(registros, lotes, animais, catalogo, estoque, codigos,
                tarefas, Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"), ZoneOffset.UTC));
        lote = new LoteSuinos(); ReflectionTestUtils.setField(lote, "id", 10L); lote.setCodigo("SU-2026-0010");
        lenient().when(lotes.findById(10L)).thenReturn(Optional.of(lote));
        lenient().when(registros.save(any())).thenAnswer(inv -> {
            RegistroSanitarioSuinos registro = inv.getArgument(0);
            ReflectionTestUtils.setField(registro, "id", 30L);
            return registro;
        });
    }

    @Test
    void registraHistoricoDoLoteEGeraTarefaDaProximaAcao() {
        RegistroSanitarioSuinosRequest request = request("san-1");
        request.setProximaAcao("Reavaliar condição");
        request.setProximaAcaoData(LocalDate.of(2026, 10, 2));

        RegistroSanitarioSuinosResumo criado = service.registrar(request, operador);

        assertThat(criado.loteCodigo()).isEqualTo("SU-2026-0010");
        assertThat(criado.tipo()).isEqualTo(TipoRegistroSanitarioSuinos.EXAME);
        ArgumentCaptor<TarefaAutomaticaRequest> tarefa = ArgumentCaptor.forClass(TarefaAutomaticaRequest.class);
        verify(tarefas).sincronizarAutomatica(tarefa.capture(), eq(operador));
        assertThat(tarefa.getValue().chaveAutomacao()).isEqualTo(
                "CRIACAO:SUINOS:SANIDADE:30:PROXIMA_ACAO");
    }

    @Test
    void registraHistoricoDeAnimalReprodutivo() {
        AnimalReprodutivoSuinos animal = new AnimalReprodutivoSuinos();
        ReflectionTestUtils.setField(animal, "id", 20L); animal.setCodigo("SR-2026-0001");
        animal.setIdentificacao("Matriz 1");
        when(animais.findById(20L)).thenReturn(Optional.of(animal));
        RegistroSanitarioSuinosRequest request = request("san-animal");
        request.setLoteId(null); request.setAnimalReprodutivoId(20L);

        RegistroSanitarioSuinosResumo criado = service.registrar(request, operador);

        assertThat(criado.animalCodigo()).isEqualTo("SR-2026-0001");
        assertThat(criado.loteId()).isNull();
    }

    @Test
    void consumoUsaServicoOficialDeEstoque() {
        ItemEstoque item = new ItemEstoque(); ReflectionTestUtils.setField(item, "id", 40L); item.setNome("Vacina");
        LocalEstoque local = new LocalEstoque(); ReflectionTestUtils.setField(local, "id", 50L); local.setNome("Farmácia");
        MovimentoEstoque movimento = new MovimentoEstoque(); ReflectionTestUtils.setField(movimento, "id", 60L);
        when(catalogo.buscarItem(40L)).thenReturn(item);
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local);
        when(estoque.registrarConsumoSanidadeSuinos(any(), eq(30L), eq("lote SU-2026-0010")))
                .thenReturn(movimento);
        RegistroSanitarioSuinosRequest request = request("san-consumo");
        request.setItemEstoqueId(40L); request.setLocalEstoqueId(50L);
        request.setQuantidadeConsumida(new BigDecimal("1.2500"));

        RegistroSanitarioSuinosResumo criado = service.registrar(request, operador);

        assertThat(criado.movimentoEstoqueId()).isEqualTo(60L);
        ArgumentCaptor<MovimentoEstoqueRequest> consumo = ArgumentCaptor.forClass(MovimentoEstoqueRequest.class);
        verify(estoque).registrarConsumoSanidadeSuinos(consumo.capture(), eq(30L), anyString());
        assertThat(consumo.getValue().getQuantidade()).isEqualByComparingTo("1.2500");
    }

    @Test
    void retryRetornaRegistroExistenteSemDuplicarMovimentoOuTarefa() {
        RegistroSanitarioSuinos existente = registro(30L);
        when(registros.findByChaveIdempotencia("san-1")).thenReturn(Optional.of(existente));

        assertThat(service.registrar(request("san-1"), operador).id()).isEqualTo(30L);

        verify(registros, never()).save(any());
        verifyNoInteractions(estoque, tarefas);
    }

    @Test
    void falhaDoEstoquePropagaParaRollbackTransacional() {
        ItemEstoque item = new ItemEstoque(); ReflectionTestUtils.setField(item, "id", 40L);
        LocalEstoque local = new LocalEstoque(); ReflectionTestUtils.setField(local, "id", 50L);
        when(catalogo.buscarItem(40L)).thenReturn(item);
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local);
        when(estoque.registrarConsumoSanidadeSuinos(any(), anyLong(), anyString()))
                .thenThrow(new EstoqueOperacaoException("SALDO_INSUFICIENTE", "Saldo insuficiente."));
        RegistroSanitarioSuinosRequest request = request("san-rollback");
        request.setItemEstoqueId(40L); request.setLocalEstoqueId(50L);
        request.setQuantidadeConsumida(BigDecimal.ONE);

        assertThatThrownBy(() -> service.registrar(request, operador))
                .isInstanceOf(EstoqueOperacaoException.class).hasMessageContaining("Saldo insuficiente");
        verify(tarefas, never()).sincronizarAutomatica(any(), any());
    }

    @Test
    void conclusaoDaProximaAcaoETarefaSaoIdempotentes() {
        RegistroSanitarioSuinos registro = registro(30L);
        registro.setProximaAcao("Reavaliar"); registro.setProximaAcaoData(LocalDate.of(2026, 10, 2));
        when(registros.buscarParaAtualizacao(30L)).thenReturn(Optional.of(registro));

        service.concluirProximaAcao(30L, operador);
        service.concluirProximaAcao(30L, operador);

        assertThat(registro.isProximaAcaoConcluida()).isTrue();
        verify(tarefas, times(1)).concluirAutomatica(
                "CRIACAO:SUINOS:SANIDADE:30:PROXIMA_ACAO", operador);
    }

    @Test
    void exigeExatamenteUmAlvo() {
        RegistroSanitarioSuinosRequest request = request("san-invalido");
        request.setAnimalReprodutivoId(20L);
        assertThatThrownBy(() -> service.registrar(request, operador))
                .isInstanceOf(SuinosOperacaoException.class).hasMessageContaining("exatamente um lote ou animal");
    }

    private RegistroSanitarioSuinosRequest request(String chave) {
        RegistroSanitarioSuinosRequest request = new RegistroSanitarioSuinosRequest();
        request.setLoteId(10L); request.setTipo(TipoRegistroSanitarioSuinos.EXAME);
        request.setDataProcedimento(LocalDate.of(2026, 9, 25));
        request.setProcedimentoProduto("Avaliação clínica"); request.setResponsavel("Operador");
        request.setChaveIdempotencia(chave); return request;
    }

    private RegistroSanitarioSuinos registro(Long id) {
        RegistroSanitarioSuinos registro = new RegistroSanitarioSuinos(); ReflectionTestUtils.setField(registro, "id", id);
        registro.setLote(lote); registro.setTipo(TipoRegistroSanitarioSuinos.EXAME);
        registro.setDataProcedimento(LocalDate.of(2026, 9, 25)); registro.setProcedimentoProduto("Avaliação clínica");
        registro.setResponsavel("Operador"); registro.setChaveIdempotencia("san-1"); return registro;
    }
}
