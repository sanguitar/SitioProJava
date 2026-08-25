package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.*;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManejoAvesServiceTests {
    @Mock private LoteAvesService loteService;
    @Mock private MortalidadeAvesRepository mortalidadeRepository;
    @Mock private AlimentacaoAvesRepository alimentacaoRepository;
    @Mock private PesagemAvesRepository pesagemRepository;
    @Mock private RegistroPosturaAvesRepository posturaRepository;
    @Mock private TransferenciaLoteAvesRepository transferenciaRepository;
    @Mock private InstalacaoCriacaoService instalacaoService;
    @Mock private EstoqueCatalogoService catalogoService;
    @Mock private EstoqueMovimentoService estoqueService;
    @Mock private AvesAlertasService alertasService;
    private ManejoAvesService service;
    private LoteAves lote;
    private InstalacaoCriacao galinheiro;
    private final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach
    void preparar() {
        service = new ManejoAvesService(loteService, mortalidadeRepository, alimentacaoRepository,
                pesagemRepository, posturaRepository, transferenciaRepository, instalacaoService,
                catalogoService, estoqueService, alertasService,
                Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC));
        galinheiro = instalacao(10L, "Galinheiro", true);
        lote = lote(1L, 100, FinalidadeLoteAves.POSTURA, galinheiro);
        lenient().when(loteService.buscarParaAtualizacao(1L)).thenReturn(lote);
        lenient().when(loteService.detalhar(1L)).thenReturn(mock(LoteAvesDetalhe.class));
    }

    @Test
    void mortalidadeReduzQuantidadeGeraEventoEAlerta() {
        RegistrarMortalidadeAvesRequest request = mortalidade("m-1", 6);
        when(mortalidadeRepository.findByChaveIdempotencia("m-1")).thenReturn(Optional.empty());

        service.registrarMortalidade(1L, request, operador);

        assertThat(lote.getQuantidadeAtual()).isEqualTo(94);
        verify(loteService).registrarEvento(eq(lote), eq(TipoEventoLoteAves.MORTALIDADE), eq(6),
                any(), eq("operador"), any(), isNull(), eq(galinheiro), isNull());
        verify(mortalidadeRepository).save(any(MortalidadeAves.class));
        verify(alertasService).avaliar();
    }

    @Test
    void mortalidadeMaiorQueLoteERecusada() {
        RegistrarMortalidadeAvesRequest request = mortalidade("m-2", 101);
        when(mortalidadeRepository.findByChaveIdempotencia("m-2")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarMortalidade(1L, request, operador))
                .isInstanceOf(AvesOperacaoException.class).extracting("code").isEqualTo("MORTALIDADE_SUPERIOR_AO_LOTE");
        assertThat(lote.getQuantidadeAtual()).isEqualTo(100);
        verify(mortalidadeRepository, never()).save(any());
    }

    @Test
    void mortalidadeRepetidaNaoDuplicaEvento() {
        MortalidadeAves existente = new MortalidadeAves(); existente.setLote(lote); existente.setChaveIdempotencia("m-3");
        when(mortalidadeRepository.findByChaveIdempotencia("m-3")).thenReturn(Optional.of(existente));
        service.registrarMortalidade(1L, mortalidade("m-3", 4), operador);
        verify(loteService, never()).buscarParaAtualizacao(anyLong());
        verify(mortalidadeRepository, never()).save(any());
    }

    @Test
    void mortalidadeConcorrenteERevalidadaDepoisDoLock() {
        MortalidadeAves existente = new MortalidadeAves(); existente.setLote(lote);
        when(mortalidadeRepository.findByChaveIdempotencia("m-4"))
                .thenReturn(Optional.empty(), Optional.of(existente));

        service.registrarMortalidade(1L, mortalidade("m-4", 4), operador);

        verify(loteService).buscarParaAtualizacao(1L);
        verify(loteService, never()).registrarEvento(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(lote.getQuantidadeAtual()).isEqualTo(100);
    }

    @Test
    void alimentacaoBaixaEstoqueEVinculaMovimento() {
        ItemEstoque racao = item(20L); LocalEstoque deposito = local(30L); MovimentoEstoque movimento = movimento(40L);
        RegistrarAlimentacaoAvesRequest request = new RegistrarAlimentacaoAvesRequest(); request.setChaveIdempotencia("a-1");
        request.setItemEstoqueId(20L); request.setLocalEstoqueId(30L); request.setQuantidade(new BigDecimal("2.5"));
        when(alimentacaoRepository.findByChaveIdempotencia("a-1")).thenReturn(Optional.empty());
        when(catalogoService.buscarItem(20L)).thenReturn(racao); when(catalogoService.buscarLocalAtivo(30L)).thenReturn(deposito);
        when(estoqueService.custoMedio(20L)).thenReturn(new BigDecimal("3.20"));
        when(alimentacaoRepository.saveAndFlush(any())).thenAnswer(inv -> { AlimentacaoAves a = inv.getArgument(0); ReflectionTestUtils.setField(a, "id", 50L); return a; });
        when(estoqueService.registrarConsumoCriacao(any(), eq(50L), eq(1L))).thenReturn(movimento);
        when(loteService.registrarEvento(any(), eq(TipoEventoLoteAves.ALIMENTACAO), isNull(), any(), any(), any(), eq("ESTOQUE_MOVIMENTO:40"), isNull(), isNull())).thenReturn(new EventoLoteAves());

        service.registrarAlimentacao(1L, request, operador);

        verify(estoqueService).registrarConsumoCriacao(argThat(r -> r.getQuantidade().compareTo(new BigDecimal("2.5000")) == 0 && r.getLocalOrigemId().equals(30L)), eq(50L), eq(1L));
        verify(alimentacaoRepository).saveAndFlush(argThat(a -> a.getCustoUnitarioReferencia().compareTo(new BigDecimal("3.20")) == 0));
    }

    @Test
    void falhaNoEstoqueInterrompeAlimentacaoAntesDoEvento() {
        RegistrarAlimentacaoAvesRequest request = new RegistrarAlimentacaoAvesRequest(); request.setChaveIdempotencia("a-2");
        request.setItemEstoqueId(20L); request.setLocalEstoqueId(30L); request.setQuantidade(BigDecimal.ONE);
        when(alimentacaoRepository.findByChaveIdempotencia("a-2")).thenReturn(Optional.empty());
        when(catalogoService.buscarItem(20L)).thenReturn(item(20L)); when(catalogoService.buscarLocalAtivo(30L)).thenReturn(local(30L));
        when(alimentacaoRepository.saveAndFlush(any())).thenAnswer(inv -> { AlimentacaoAves a = inv.getArgument(0); ReflectionTestUtils.setField(a, "id", 51L); return a; });
        when(estoqueService.registrarConsumoCriacao(any(), eq(51L), eq(1L)))
                .thenThrow(new EstoqueOperacaoException("ESTOQUE_INSUFICIENTE", "Saldo insuficiente."));
        assertThatThrownBy(() -> service.registrarAlimentacao(1L, request, operador)).isInstanceOf(EstoqueOperacaoException.class);
        verify(loteService, never()).registrarEvento(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void pesagemValidaFaixaComBigDecimal() {
        RegistrarPesagemAvesRequest request = new RegistrarPesagemAvesRequest(); request.setChaveIdempotencia("p-1");
        request.setPesoMedio(new BigDecimal("1.20")); request.setPesoMinimo(new BigDecimal("1.30"));
        when(pesagemRepository.findByChaveIdempotencia("p-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarPesagem(1L, request, operador))
                .isInstanceOf(AvesOperacaoException.class).extracting("code").isEqualTo("PESO_MINIMO_INVALIDO");
    }

    @Test
    void posturaExigeFinalidadeAdequada() {
        lote.setFinalidade(FinalidadeLoteAves.CORTE);
        RegistrarPosturaAvesRequest request = new RegistrarPosturaAvesRequest(); request.setChaveIdempotencia("o-1");
        request.setDataColeta(LocalDate.of(2026, 8, 24)); request.setOvosInteiros(10);
        when(posturaRepository.findByChaveIdempotencia("o-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarPostura(1L, request, operador))
                .isInstanceOf(AvesOperacaoException.class).extracting("code").isEqualTo("LOTE_NAO_PERMITE_POSTURA");
    }

    @Test
    void transferenciaValidaCapacidadeEAtualizaInstalacao() {
        InstalacaoCriacao destino = instalacao(11L, "Piquete", true);
        TransferirLoteAvesRequest request = new TransferirLoteAvesRequest(); request.setChaveIdempotencia("t-1"); request.setInstalacaoDestinoId(11L);
        when(transferenciaRepository.findByChaveIdempotencia("t-1")).thenReturn(Optional.empty());
        when(instalacaoService.reservarCapacidade(11L, 100, 1L)).thenReturn(destino);
        service.transferir(1L, request, operador);
        assertThat(lote.getInstalacaoAtual()).isSameAs(destino);
        verify(instalacaoService).reservarCapacidade(11L, 100, 1L);
        verify(transferenciaRepository).save(any());
    }

    @Test
    void operacoesCriticasSaoTransacionais() throws Exception {
        assertThat(ManejoAvesService.class.getMethod("registrarAlimentacao", Long.class, RegistrarAlimentacaoAvesRequest.class, UsuarioAtor.class).getAnnotation(Transactional.class)).isNotNull();
        assertThat(ManejoAvesService.class.getMethod("registrarMortalidade", Long.class, RegistrarMortalidadeAvesRequest.class, UsuarioAtor.class).getAnnotation(Transactional.class)).isNotNull();
    }

    private RegistrarMortalidadeAvesRequest mortalidade(String chave, int qtd) { RegistrarMortalidadeAvesRequest r = new RegistrarMortalidadeAvesRequest(); r.setChaveIdempotencia(chave); r.setQuantidade(qtd); return r; }
    private LoteAves lote(Long id, int qtd, FinalidadeLoteAves finalidade, InstalacaoCriacao i) { LoteAves l = new LoteAves(); ReflectionTestUtils.setField(l, "id", id); l.setCodigo("L-1"); l.setQuantidadeInicial(qtd); l.setQuantidadeAtual(qtd); l.setStatus(StatusLoteAves.ATIVO); l.setFinalidade(finalidade); l.setInstalacaoAtual(i); return l; }
    private InstalacaoCriacao instalacao(Long id, String nome, boolean ativa) { InstalacaoCriacao i = new InstalacaoCriacao(); ReflectionTestUtils.setField(i, "id", id); i.setNome(nome); i.setAtivo(ativa); return i; }
    private ItemEstoque item(Long id) { UnidadeMedida u = new UnidadeMedida(); u.setSigla("KG"); ItemEstoque i = new ItemEstoque(); ReflectionTestUtils.setField(i, "id", id); i.setNome("Ração"); i.setUnidadeMedida(u); i.setAtivo(true); return i; }
    private LocalEstoque local(Long id) { LocalEstoque l = new LocalEstoque(); ReflectionTestUtils.setField(l, "id", id); l.setNome("Depósito"); l.setAtivo(true); return l; }
    private MovimentoEstoque movimento(Long id) { MovimentoEstoque m = new MovimentoEstoque(); ReflectionTestUtils.setField(m, "id", id); return m; }
}
