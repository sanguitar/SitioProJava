package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuinosServiceTests {
    @Mock LoteSuinosRepository lotes; @Mock EventoSuinosRepository eventos; @Mock InstalacaoCriacaoService instalacoes;
    @Mock CodigoCriacaoService codigos; @Mock EstoqueCatalogoService catalogo; @Mock EstoqueMovimentoService estoque;
    @Mock TarefaService tarefas; @Mock AlertaService alertas;
    SuinosService service; LoteSuinos lote; InstalacaoCriacao instalacao;
    final UsuarioAtor admin=new UsuarioAtor(1L,"admin",true); final UsuarioAtor operador=new UsuarioAtor(2L,"operador",false);

    @BeforeEach void setup(){service=new SuinosService(lotes,eventos,instalacoes,codigos,catalogo,estoque,tarefas,alertas,Clock.fixed(Instant.parse("2026-09-24T12:00:00Z"),ZoneOffset.UTC));instalacao=instalacao(10L,"Pocilga");lote=lote(1L,20,instalacao);lenient().when(lotes.findById(1L)).thenReturn(Optional.of(lote));lenient().when(lotes.buscarParaAtualizacao(1L)).thenReturn(Optional.of(lote));lenient().when(eventos.findByLoteIdOrderByDataEventoDescIdDesc(1L)).thenReturn(List.of());lenient().when(tarefas.listarRelacionadas(any(),anyString())).thenReturn(List.of());lenient().when(alertas.listarRelacionados(any(),anyString())).thenReturn(List.of());lenient().when(eventos.save(any())).thenAnswer(i->i.getArgument(0));}

    @Test void adminCriaLoteComCodigoAutomatico(){CriarLoteSuinosRequest r=criacao("novo");when(instalacoes.reservarCapacidadeSuinos(10L,12,null)).thenReturn(instalacao);when(codigos.proximoLoteSuinos()).thenReturn("SU-2026-0001");when(lotes.save(any())).thenAnswer(i->{LoteSuinos l=i.getArgument(0);ReflectionTestUtils.setField(l,"id",1L);return l;});service.criar(r,admin);verify(lotes).save(argThat(l->l.getCodigo().equals("SU-2026-0001")&&l.getCategoria()==CategoriaSuino.CRESCIMENTO&&l.getQuantidadeAtual()==12));verify(codigos).bloquearIdempotencia("LOTE_SUINOS","novo");}
    @Test void operadorNaoCriaLote(){assertThatThrownBy(()->service.criar(criacao("x"),operador)).isInstanceOf(SuinosOperacaoException.class).extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);}
    @Test void entradaIncrementaQuantidadeERegistraHistorico(){EntradaSuinosRequest r=entrada("e1",4);when(eventos.findByChaveIdempotencia("e1")).thenReturn(Optional.empty());service.registrarEntrada(1L,r,operador);assertThat(lote.getQuantidadeAtual()).isEqualTo(24);verify(eventos).save(argThat(e->e.getTipo()==TipoEventoSuinos.ENTRADA_ANIMAIS&&e.getQuantidade()==4));}
    @Test void retryNaoDuplicaEntrada(){EventoSuinos e=new EventoSuinos();e.setLote(lote);when(eventos.findByChaveIdempotencia("e2")).thenReturn(Optional.of(e));service.registrarEntrada(1L,entrada("e2",4),operador);assertThat(lote.getQuantidadeAtual()).isEqualTo(20);verify(lotes,never()).buscarParaAtualizacao(anyLong());}
    @Test void mortalidadeReduzQuantidadeEZeroEncerraLote(){PerdaSuinosRequest r=perda("p1",20,TipoEventoSuinos.MORTALIDADE);when(eventos.findByChaveIdempotencia("p1")).thenReturn(Optional.empty());service.registrarPerda(1L,r,operador);assertThat(lote.getQuantidadeAtual()).isZero();assertThat(lote.getStatus()).isEqualTo(StatusLoteSuinos.ENCERRADO);}
    @Test void perdaMaiorQueLoteERejeitada(){PerdaSuinosRequest r=perda("p2",21,TipoEventoSuinos.PERDA);when(eventos.findByChaveIdempotencia("p2")).thenReturn(Optional.empty());assertThatThrownBy(()->service.registrarPerda(1L,r,operador)).isInstanceOf(SuinosOperacaoException.class).hasMessageContaining("exceder");}
    @Test void pesagemAtualizaPesoComBigDecimal(){PesagemSuinosRequest r=new PesagemSuinosRequest();base(r,"w1");r.setPesoMedio(new BigDecimal("42.375"));when(eventos.findByChaveIdempotencia("w1")).thenReturn(Optional.empty());service.registrarPesagem(1L,r,operador);assertThat(lote.getPesoMedio()).isEqualByComparingTo("42.3750");}
    @Test void transferenciaPreservaOrigemEAtualizaDestino(){InstalacaoCriacao destino=instalacao(11L,"Baia 2");TransferenciaSuinosRequest r=new TransferenciaSuinosRequest();base(r,"t1");r.setInstalacaoDestinoId(11L);when(eventos.findByChaveIdempotencia("t1")).thenReturn(Optional.empty());when(instalacoes.reservarCapacidadeSuinos(11L,20,1L)).thenReturn(destino);service.transferir(1L,r,operador);assertThat(lote.getInstalacaoAtual()).isSameAs(destino);verify(eventos).save(argThat(e->e.getInstalacaoOrigem()==instalacao&&e.getInstalacaoDestino()==destino));}
    @Test void alimentacaoConsomePeloServicoOficialEVinculaMovimento(){ItemEstoque item=item(20L);LocalEstoque local=local(30L);MovimentoEstoque movimento=new MovimentoEstoque();ReflectionTestUtils.setField(movimento,"id",40L);AlimentacaoSuinosRequest r=alimentacao("a1");when(eventos.findByChaveIdempotencia("a1")).thenReturn(Optional.empty());when(catalogo.buscarItem(20L)).thenReturn(item);when(catalogo.buscarLocalAtivo(30L)).thenReturn(local);when(eventos.save(any())).thenAnswer(i->{EventoSuinos e=i.getArgument(0);ReflectionTestUtils.setField(e,"id",50L);return e;});when(estoque.registrarConsumoCriacaoSuinos(any(),eq(50L),eq(1L))).thenReturn(movimento);service.registrarAlimentacao(1L,r,operador);verify(estoque).registrarConsumoCriacaoSuinos(argThat(m->m.getQuantidade().compareTo(new BigDecimal("5.5000"))==0&&m.getLocalOrigemId().equals(30L)),eq(50L),eq(1L));verify(eventos).save(argThat(e->e.getMovimentoEstoque()==movimento));}
    @Test void retryDeAlimentacaoNaoMovimentaEstoque(){EventoSuinos e=new EventoSuinos();e.setLote(lote);when(eventos.findByChaveIdempotencia("a2")).thenReturn(Optional.of(e));service.registrarAlimentacao(1L,alimentacao("a2"),operador);verifyNoInteractions(estoque,catalogo);}
    @Test void falhaNoEstoquePropagaETransacaoPodeFazerRollback(){ItemEstoque item=item(20L);LocalEstoque local=local(30L);when(eventos.findByChaveIdempotencia("a3")).thenReturn(Optional.empty());when(catalogo.buscarItem(20L)).thenReturn(item);when(catalogo.buscarLocalAtivo(30L)).thenReturn(local);when(eventos.save(any())).thenAnswer(i->{EventoSuinos e=i.getArgument(0);ReflectionTestUtils.setField(e,"id",51L);return e;});when(estoque.registrarConsumoCriacaoSuinos(any(),eq(51L),eq(1L))).thenThrow(new EstoqueOperacaoException("SALDO_INSUFICIENTE","Saldo insuficiente"));assertThatThrownBy(()->service.registrarAlimentacao(1L,alimentacao("a3"),operador)).isInstanceOf(EstoqueOperacaoException.class);}
    @Test void operacoesCriticasSaoTransacionais() throws Exception {assertThat(SuinosService.class.getMethod("registrarAlimentacao",Long.class,AlimentacaoSuinosRequest.class,UsuarioAtor.class).getAnnotation(Transactional.class)).isNotNull();assertThat(SuinosService.class.getMethod("registrarPerda",Long.class,PerdaSuinosRequest.class,UsuarioAtor.class).getAnnotation(Transactional.class)).isNotNull();}

    private CriarLoteSuinosRequest criacao(String chave){CriarLoteSuinosRequest r=new CriarLoteSuinosRequest();r.setCategoria(CategoriaSuino.CRESCIMENTO);r.setDataEntrada(LocalDate.of(2026,9,24));r.setOrigem("Produtor local");r.setQuantidadeInicial(12);r.setInstalacaoId(10L);r.setChaveIdempotencia(chave);return r;}
    private EntradaSuinosRequest entrada(String chave,int q){EntradaSuinosRequest r=new EntradaSuinosRequest();base(r,chave);r.setQuantidade(q);return r;}
    private PerdaSuinosRequest perda(String chave,int q,TipoEventoSuinos tipo){PerdaSuinosRequest r=new PerdaSuinosRequest();base(r,chave);r.setQuantidade(q);r.setTipo(tipo);return r;}
    private AlimentacaoSuinosRequest alimentacao(String chave){AlimentacaoSuinosRequest r=new AlimentacaoSuinosRequest();base(r,chave);r.setItemEstoqueId(20L);r.setLocalEstoqueId(30L);r.setQuantidade(new BigDecimal("5.5"));return r;}
    private void base(OperacaoSuinosBase r,String chave){r.setChaveIdempotencia(chave);r.setDataEvento(LocalDateTime.of(2026,9,24,8,0));}
    private LoteSuinos lote(Long id,int qtd,InstalacaoCriacao i){LoteSuinos l=new LoteSuinos();ReflectionTestUtils.setField(l,"id",id);l.setCodigo("SU-2026-0001");l.setCategoria(CategoriaSuino.CRESCIMENTO);l.setDataEntrada(LocalDate.of(2026,9,1));l.setOrigem("Teste");l.setQuantidadeInicial(qtd);l.setQuantidadeAtual(qtd);l.setInstalacaoAtual(i);l.setStatus(StatusLoteSuinos.ATIVO);return l;}
    private InstalacaoCriacao instalacao(Long id,String nome){InstalacaoCriacao i=new InstalacaoCriacao();ReflectionTestUtils.setField(i,"id",id);i.setNome(nome);i.setAtivo(true);return i;}
    private ItemEstoque item(Long id){ItemEstoque i=new ItemEstoque();ReflectionTestUtils.setField(i,"id",id);i.setNome("Ração");i.setAtivo(true);return i;}
    private LocalEstoque local(Long id){LocalEstoque l=new LocalEstoque();ReflectionTestUtils.setField(l,"id",id);l.setNome("Depósito");l.setAtivo(true);return l;}
}
