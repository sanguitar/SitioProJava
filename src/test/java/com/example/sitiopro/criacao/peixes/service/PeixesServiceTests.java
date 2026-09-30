package com.example.sitiopro.criacao.peixes.service;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.entity.*;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
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
class PeixesServiceTests {
    @Mock LotePeixesRepository lotes; @Mock EventoPeixesRepository eventos; @Mock InstalacaoCriacaoService instalacoes;
    @Mock CodigoCriacaoService codigos; @Mock EstoqueCatalogoService catalogo; @Mock EstoqueMovimentoService estoque;
    @Mock TarefaService tarefas; @Mock AlertaService alertas;
    @Mock QualidadeAguaService qualidadeAgua;
    PeixesService service; LotePeixes lote; InstalacaoCriacao tanque;
    final UsuarioAtor admin=new UsuarioAtor(1L,"admin",true), operador=new UsuarioAtor(2L,"operador",false);
    @BeforeEach void setup(){service=new PeixesService(lotes,eventos,instalacoes,codigos,catalogo,estoque,tarefas,alertas,qualidadeAgua,Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"),ZoneOffset.UTC));tanque=tanque(10L,"Tanque 1");lote=lote(1L,100,tanque);lenient().when(lotes.findById(1L)).thenReturn(Optional.of(lote));lenient().when(lotes.buscarParaAtualizacao(1L)).thenReturn(Optional.of(lote));lenient().when(eventos.findByLoteIdOrderByDataEventoDescIdDesc(1L)).thenReturn(List.of());lenient().when(tarefas.listarRelacionadas(any(),anyString())).thenReturn(List.of());lenient().when(alertas.listarRelacionados(any(),anyString())).thenReturn(List.of());lenient().when(qualidadeAgua.dashboard()).thenReturn(QualidadeAguaDashboardResumo.vazio());lenient().when(eventos.save(any())).thenAnswer(i->i.getArgument(0));}
    @Test void adminCriaLoteComCodigoPxEBiomassa(){var r=criacao("novo");when(instalacoes.reservarCapacidadePeixes(10L,100,null)).thenReturn(tanque);when(codigos.proximoLotePeixes()).thenReturn("PX-2026-0001");when(lotes.save(any())).thenAnswer(i->{LotePeixes l=i.getArgument(0);ReflectionTestUtils.setField(l,"id",1L);return l;});var criado=service.criar(r,admin);assertThat(criado.codigo()).isEqualTo("PX-2026-0001");assertThat(criado.biomassaEstimada()).isEqualByComparingTo("25.0000");verify(codigos).bloquearIdempotencia("LOTE_PEIXES","novo");}
    @Test void operadorNaoCriaLote(){assertThatThrownBy(()->service.criar(criacao("x"),operador)).isInstanceOf(PeixesOperacaoException.class).extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);}
    @Test void entradaIncrementaQuantidadeComIdempotencia(){var r=entrada("e1",10);when(eventos.findByChaveIdempotencia("e1")).thenReturn(Optional.empty());when(instalacoes.reservarCapacidadePeixes(10L,10,1L)).thenReturn(tanque);service.registrarEntrada(1L,r,operador);assertThat(lote.getQuantidadeAtual()).isEqualTo(110);verify(eventos).save(argThat(e->e.getTipo()==TipoEventoPeixes.ENTRADA_PEIXES&&e.getQuantidade()==10));}
    @Test void retryNaoDuplicaEntrada(){EventoPeixes e=new EventoPeixes();e.setLote(lote);when(eventos.findByChaveIdempotencia("e2")).thenReturn(Optional.of(e));service.registrarEntrada(1L,entrada("e2",10),operador);assertThat(lote.getQuantidadeAtual()).isEqualTo(100);verify(lotes,never()).buscarParaAtualizacao(anyLong());}
    @Test void mortalidadeReduzEEncerraQuandoZera(){var r=perda("p1",100,TipoEventoPeixes.MORTALIDADE);when(eventos.findByChaveIdempotencia("p1")).thenReturn(Optional.empty());service.registrarPerda(1L,r,operador);assertThat(lote.getQuantidadeAtual()).isZero();assertThat(lote.getStatus()).isEqualTo(StatusLotePeixes.ENCERRADO);}
    @Test void perdaMaiorQueQuantidadeFalha(){var r=perda("p2",101,TipoEventoPeixes.PERDA);when(eventos.findByChaveIdempotencia("p2")).thenReturn(Optional.empty());assertThatThrownBy(()->service.registrarPerda(1L,r,operador)).hasMessageContaining("exceder");}
    @Test void biometriaAtualizaPesoEBiomassa(){var r=new BiometriaPeixesRequest();base(r,"b1");r.setPesoMedio(new BigDecimal("0.375"));when(eventos.findByChaveIdempotencia("b1")).thenReturn(Optional.empty());var detalhe=service.registrarBiometria(1L,r,operador);assertThat(lote.getPesoMedio()).isEqualByComparingTo("0.3750");assertThat(detalhe.biomassaEstimada()).isEqualByComparingTo("37.5000");}
    @Test void transferenciaPreservaOrigemEValidaTanque(){InstalacaoCriacao destino=tanque(11L,"Tanque 2");var r=new TransferenciaPeixesRequest();base(r,"t1");r.setInstalacaoDestinoId(11L);when(eventos.findByChaveIdempotencia("t1")).thenReturn(Optional.empty());when(instalacoes.reservarCapacidadePeixes(11L,100,1L)).thenReturn(destino);service.transferir(1L,r,operador);assertThat(lote.getInstalacaoAtual()).isSameAs(destino);verify(eventos).save(argThat(e->e.getInstalacaoOrigem()==tanque&&e.getInstalacaoDestino()==destino));}
    @Test void alimentacaoUsaEstoqueOficialEVinculaMovimento(){ItemEstoque item=item(20L);LocalEstoque local=local(30L);MovimentoEstoque movimento=new MovimentoEstoque();ReflectionTestUtils.setField(movimento,"id",40L);var r=alimentacao("a1");when(eventos.findByChaveIdempotencia("a1")).thenReturn(Optional.empty());when(catalogo.buscarItem(20L)).thenReturn(item);when(catalogo.buscarLocalAtivo(30L)).thenReturn(local);when(eventos.save(any())).thenAnswer(i->{EventoPeixes e=i.getArgument(0);ReflectionTestUtils.setField(e,"id",50L);return e;});when(estoque.registrarConsumoCriacaoPeixes(any(),eq(50L),eq(1L))).thenReturn(movimento);service.registrarAlimentacao(1L,r,operador);verify(estoque).registrarConsumoCriacaoPeixes(argThat(m->m.getQuantidade().compareTo(new BigDecimal("5.5000"))==0),eq(50L),eq(1L));verify(eventos).save(argThat(e->e.getMovimentoEstoque()==movimento));}
    @Test void retryAlimentacaoNaoDuplicaMovimento(){EventoPeixes e=new EventoPeixes();e.setLote(lote);when(eventos.findByChaveIdempotencia("a2")).thenReturn(Optional.of(e));service.registrarAlimentacao(1L,alimentacao("a2"),operador);verifyNoInteractions(estoque,catalogo);}
    @Test void falhaEstoquePropagaParaRollbackTransacional() throws Exception {ItemEstoque item=item(20L);LocalEstoque local=local(30L);when(eventos.findByChaveIdempotencia("a3")).thenReturn(Optional.empty());when(catalogo.buscarItem(20L)).thenReturn(item);when(catalogo.buscarLocalAtivo(30L)).thenReturn(local);when(eventos.save(any())).thenAnswer(i->{EventoPeixes e=i.getArgument(0);ReflectionTestUtils.setField(e,"id",51L);return e;});when(estoque.registrarConsumoCriacaoPeixes(any(),eq(51L),eq(1L))).thenThrow(new EstoqueOperacaoException("SALDO_INSUFICIENTE","Saldo insuficiente"));assertThatThrownBy(()->service.registrarAlimentacao(1L,alimentacao("a3"),operador)).isInstanceOf(EstoqueOperacaoException.class);assertThat(PeixesService.class.getMethod("registrarAlimentacao",Long.class,AlimentacaoPeixesRequest.class,UsuarioAtor.class).getAnnotation(Transactional.class)).isNotNull();}
    private CriarLotePeixesRequest criacao(String chave){var r=new CriarLotePeixesRequest();r.setEspecie("Tambaqui");r.setQuantidadeInicial(100);r.setDataEntrada(LocalDate.of(2026,9,25));r.setOrigem("Fornecedor local");r.setPesoMedio(new BigDecimal("0.25"));r.setInstalacaoId(10L);r.setChaveIdempotencia(chave);return r;}
    private EntradaPeixesRequest entrada(String chave,int q){var r=new EntradaPeixesRequest();base(r,chave);r.setQuantidade(q);return r;} private PerdaPeixesRequest perda(String chave,int q,TipoEventoPeixes tipo){var r=new PerdaPeixesRequest();base(r,chave);r.setQuantidade(q);r.setTipo(tipo);return r;}
    private AlimentacaoPeixesRequest alimentacao(String chave){var r=new AlimentacaoPeixesRequest();base(r,chave);r.setItemEstoqueId(20L);r.setLocalEstoqueId(30L);r.setQuantidade(new BigDecimal("5.5"));return r;} private void base(OperacaoPeixesBase r,String chave){r.setChaveIdempotencia(chave);r.setDataEvento(LocalDateTime.of(2026,9,25,8,0));}
    private LotePeixes lote(Long id,int qtd,InstalacaoCriacao i){LotePeixes l=new LotePeixes();ReflectionTestUtils.setField(l,"id",id);l.setCodigo("PX-2026-0001");l.setEspecie("Tambaqui");l.setQuantidadeInicial(qtd);l.setQuantidadeAtual(qtd);l.setDataEntrada(LocalDate.of(2026,9,1));l.setOrigem("Teste");l.setPesoMedio(new BigDecimal("0.2500"));l.setInstalacaoAtual(i);l.setStatus(StatusLotePeixes.ATIVO);return l;}
    private InstalacaoCriacao tanque(Long id,String nome){InstalacaoCriacao i=new InstalacaoCriacao();ReflectionTestUtils.setField(i,"id",id);i.setNome(nome);i.setTipo(TipoInstalacaoCriacao.TANQUE_PISCICULTURA);i.setAtivo(true);return i;} private ItemEstoque item(Long id){ItemEstoque i=new ItemEstoque();ReflectionTestUtils.setField(i,"id",id);i.setNome("Ração peixe");i.setAtivo(true);return i;} private LocalEstoque local(Long id){LocalEstoque l=new LocalEstoque();ReflectionTestUtils.setField(l,"id",id);l.setNome("Depósito");l.setAtivo(true);return l;}
}
