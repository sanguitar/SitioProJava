package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.*;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.tarefas.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteAvesServiceTests {
    @Mock private LoteAvesRepository loteRepository; @Mock private EventoLoteAvesRepository eventoRepository;
    @Mock private AlimentacaoAvesRepository alimentacaoRepository; @Mock private MortalidadeAvesRepository mortalidadeRepository;
    @Mock private PesagemAvesRepository pesagemRepository; @Mock private RegistroPosturaAvesRepository posturaRepository;
    @Mock private TransferenciaLoteAvesRepository transferenciaRepository; @Mock private InstalacaoCriacaoService instalacaoService;
    @Mock private EstoqueMovimentoService estoqueService; @Mock private AlertaService alertaService; @Mock private TarefaService tarefaService;
    @Mock private CodigoCriacaoService codigoService;
    private LoteAvesService service; private InstalacaoCriacao instalacao;
    private final UsuarioAtor admin = new UsuarioAtor(1L, "admin", true);

    @BeforeEach
    void preparar() {
        service = new LoteAvesService(loteRepository, eventoRepository, alimentacaoRepository,
                mortalidadeRepository, pesagemRepository, posturaRepository, transferenciaRepository,
                instalacaoService, estoqueService, alertaService, tarefaService,
                codigoService,
                Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC));
        instalacao = new InstalacaoCriacao(); ReflectionTestUtils.setField(instalacao, "id", 10L);
        instalacao.setNome("Galinheiro"); instalacao.setAtivo(true);
        lenient().when(alimentacaoRepository.findByLoteIdOrderByDataEventoDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(mortalidadeRepository.findByLoteIdOrderByDataEventoDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(pesagemRepository.findByLoteIdOrderByDataEventoDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(posturaRepository.findByLoteIdOrderByDataColetaDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(transferenciaRepository.findByLoteIdOrderByDataEventoDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(eventoRepository.findByLoteIdOrderByDataEventoDescIdDesc(anyLong())).thenReturn(List.of());
        lenient().when(alertaService.listarRelacionados(any(), any())).thenReturn(List.of());
        lenient().when(tarefaService.listarRelacionadas(any(), any())).thenReturn(List.of());
        lenient().when(codigoService.proximoLoteAves()).thenReturn("AV-2026-0001");
    }

    @Test
    void adminCriaLoteComQuantidadeInicialProtegidaEEvento() {
        CriarLoteAvesRequest request = request(); AtomicReference<LoteAves> salvo = new AtomicReference<>();
        when(loteRepository.findByChaveIdempotencia("lote-op-1")).thenReturn(Optional.empty());
        when(instalacaoService.reservarCapacidade(10L, 50, null)).thenReturn(instalacao);
        when(loteRepository.save(any())).thenAnswer(inv -> { LoteAves l = inv.getArgument(0); ReflectionTestUtils.setField(l, "id", 1L); salvo.set(l); return l; });
        when(loteRepository.findById(1L)).thenAnswer(inv -> Optional.ofNullable(salvo.get()));

        LoteAvesDetalhe detalhe = service.criar(request, admin);

        assertThat(detalhe.quantidadeInicial()).isEqualTo(50);
        assertThat(detalhe.quantidadeAtual()).isEqualTo(50);
        assertThat(detalhe.codigo()).isEqualTo("AV-2026-0001");
        assertThat(detalhe.status()).isEqualTo(StatusLoteAves.ATIVO);
        verify(codigoService).bloquearIdempotencia("LOTE_AVES", "lote-op-1");
        verify(instalacaoService).reservarCapacidade(10L, 50, null);
        verify(eventoRepository).save(argThat(e -> e.getTipo() == TipoEventoLoteAves.ENTRADA_INICIAL && e.getQuantidade() == 50));
    }

    @Test
    void operadorNaoPodeCriarLote() {
        assertThatThrownBy(() -> service.criar(request(), new UsuarioAtor(2L, "operador", false)))
                .isInstanceOf(AvesOperacaoException.class).extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        verifyNoInteractions(loteRepository);
    }

    @Test
    void repeticaoDaCriacaoRetornaMesmoLote() {
        LoteAves lote = loteAtivo();
        when(loteRepository.findByChaveIdempotencia("lote-op-1")).thenReturn(Optional.of(lote));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        LoteAvesDetalhe detalhe = service.criar(request(), admin);
        assertThat(detalhe.id()).isEqualTo(1L);
        verify(codigoService).bloquearIdempotencia("LOTE_AVES", "lote-op-1");
        verify(codigoService, never()).proximoLoteAves();
        verify(loteRepository, never()).save(any());
    }

    @Test
    void quantidadeAtualNaoExisteNoDtoDeEdicao() {
        assertThat(Arrays.stream(AtualizarLoteAvesRequest.class.getDeclaredFields()).map(java.lang.reflect.Field::getName))
                .doesNotContain("codigo", "quantidadeAtual", "custoInicial", "status", "criadoPor", "versao");
        assertThat(Arrays.stream(CriarLoteAvesRequest.class.getDeclaredFields()).map(java.lang.reflect.Field::getName))
                .doesNotContain("codigo", "quantidadeAtual", "status", "criadoPor", "versao");
    }

    @Test
    void encerramentoEIdempotenteENaoDuplicaEvento() {
        LoteAves lote = loteAtivo();
        when(loteRepository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        EncerrarLoteAvesRequest request = new EncerrarLoteAvesRequest(); request.setStatusFinal(StatusLoteAves.ABATIDO);
        service.encerrar(1L, request, admin);
        service.encerrar(1L, request, admin);
        assertThat(lote.getStatus()).isEqualTo(StatusLoteAves.ABATIDO);
        verify(eventoRepository, times(1)).save(argThat(e -> e.getTipo() == TipoEventoLoteAves.ABATE));
    }

    private CriarLoteAvesRequest request() { CriarLoteAvesRequest r = new CriarLoteAvesRequest(); r.setNome("Poedeiras"); r.setEspecie(EspecieAves.GALINHA); r.setFinalidade(FinalidadeLoteAves.POSTURA); r.setOrigem("Fornecedor local"); r.setDataEntrada(LocalDate.of(2026,8,20)); r.setQuantidadeInicial(50); r.setSexo(SexoLoteAves.FEMEAS); r.setInstalacaoId(10L); r.setCustoInicial(new BigDecimal("500")); r.setChaveIdempotencia("lote-op-1"); return r; }
    private LoteAves loteAtivo() { LoteAves l = new LoteAves(); ReflectionTestUtils.setField(l, "id", 1L); l.setCodigo("AV-001"); l.setEspecie(EspecieAves.GALINHA); l.setFinalidade(FinalidadeLoteAves.POSTURA); l.setOrigem("Fornecedor"); l.setDataEntrada(LocalDate.of(2026,8,20)); l.setQuantidadeInicial(50); l.setQuantidadeAtual(50); l.setSexo(SexoLoteAves.FEMEAS); l.setInstalacaoAtual(instalacao); l.setStatus(StatusLoteAves.ATIVO); l.setChaveIdempotencia("lote-op-1"); return l; }
}
