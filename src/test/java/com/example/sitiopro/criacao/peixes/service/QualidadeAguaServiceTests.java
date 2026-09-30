package com.example.sitiopro.criacao.peixes.service;
import com.example.sitiopro.criacao.core.entity.*;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.entity.*;
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
class QualidadeAguaServiceTests {
    @Mock MedicaoQualidadeAguaRepository medicoes; @Mock ConfiguracaoQualidadeAguaRepository configuracoes;
    @Mock LotePeixesRepository lotes; @Mock CodigoCriacaoService codigos; @Mock TarefaService tarefas; @Mock AlertaService alertas;
    QualidadeAguaService service; LotePeixes lote; ConfiguracaoQualidadeAgua config; final UsuarioAtor operador=new UsuarioAtor(2L,"operador",false);
    @BeforeEach void setup(){config=new ConfiguracaoQualidadeAgua(d("24"),d("32"),d("6.5"),d("8.5"),d("5"),d("30"),d("0.5"),d("0.2"),7);InstalacaoCriacao tanque=new InstalacaoCriacao();ReflectionTestUtils.setField(tanque,"id",10L);tanque.setNome("Tanque 1");tanque.setTipo(TipoInstalacaoCriacao.TANQUE_PISCICULTURA);lote=new LotePeixes();ReflectionTestUtils.setField(lote,"id",1L);lote.setCodigo("PX-2026-0001");lote.setStatus(StatusLotePeixes.ATIVO);lote.setInstalacaoAtual(tanque);service=new QualidadeAguaService(medicoes,configuracoes,lotes,codigos,tarefas,alertas,Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"),ZoneOffset.UTC));lenient().when(configuracoes.findById(1)).thenReturn(Optional.of(config));lenient().when(lotes.findById(1L)).thenReturn(Optional.of(lote));lenient().when(lotes.findByStatusOrderByCodigoAsc(StatusLotePeixes.ATIVO)).thenReturn(List.of(lote));lenient().when(medicoes.saveAndFlush(any())).thenAnswer(i->{MedicaoQualidadeAgua m=i.getArgument(0);ReflectionTestUtils.setField(m,"id",20L);return m;});}
    @Test void registraMedicaoCompletaAgendaProximaEPermaneceNormal(){var r=req("m1",d("28"),d("7.2"),d("6"));var salvo=service.registrar(1L,r,operador);assertThat(salvo.dentroDosLimites()).isTrue();assertThat(salvo.instalacaoNome()).isEqualTo("Tanque 1");verify(tarefas).sincronizarAutomatica(argThat(t->t.dataVencimento().equals(LocalDateTime.of(2026,10,7,8,0))),eq(operador));verify(alertas).sincronizar(ModuloOrigem.CRIACOES,TipoAlerta.CRIACAO_PEIXES_QUALIDADE_AGUA,List.of());}
    @Test void valoresForaDosLimitesGeramAlertaIdempotente(){var r=req("m2",d("35"),d("5.9"),d("3"));r.setAmonia(d("0.8"));var atual=medicao(20L,d("35"),d("5.9"),d("3"));atual.setAmonia(d("0.8"));when(medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(1L)).thenReturn(Optional.empty(),Optional.of(atual));service.registrar(1L,r,operador);verify(alertas).sincronizar(eq(ModuloOrigem.CRIACOES),eq(TipoAlerta.CRIACAO_PEIXES_QUALIDADE_AGUA),argThat(c->c.size()==1&&c.get(0).chaveDeduplicacao().equals("CRIACAO:PEIXES:LOTE:1:QUALIDADE_AGUA")));}
    @Test void medicaoNormalPosteriorResolveAlertaEConcluiTarefaAnterior(){MedicaoQualidadeAgua anterior=medicao(19L,d("35"),d("5"),d("3"));when(medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(1L)).thenReturn(Optional.of(anterior),Optional.of(medicao(20L,d("28"),d("7"),d("6"))));service.registrar(1L,req("m3",d("28"),d("7"),d("6")),operador);verify(tarefas).concluirAutomatica("CRIACAO:PEIXES:LOTE:1:QUALIDADE_AGUA:19",operador);verify(alertas).sincronizar(ModuloOrigem.CRIACOES,TipoAlerta.CRIACAO_PEIXES_QUALIDADE_AGUA,List.of());}
    @Test void retryNaoDuplicaMedicaoNemTarefa(){MedicaoQualidadeAgua existente=medicao(20L,d("28"),d("7"),d("6"));when(medicoes.findByChaveIdempotencia("m4")).thenReturn(Optional.of(existente));service.registrar(1L,req("m4",d("28"),d("7"),d("6")),operador);verify(medicoes,never()).saveAndFlush(any());verifyNoInteractions(tarefas,alertas);}
    @Test void opcionaisPodemFicarAusentesEHistoricoPreservaOrdemDoRepositorio(){MedicaoQualidadeAgua m=medicao(20L,d("28"),d("7"),d("6"));when(medicoes.findByLoteIdOrderByMedidoEmDescIdDesc(1L)).thenReturn(List.of(m));assertThat(service.listar(1L)).singleElement().satisfies(x->{assertThat(x.amonia()).isNull();assertThat(x.transparenciaCm()).isNull();});}
    @Test void somenteAdminAlteraLimitesEValidaIntervalos(){var dto=service.configuracao();assertThatThrownBy(()->service.atualizarConfiguracao(dto,operador)).isInstanceOf(PeixesOperacaoException.class);dto.setTemperaturaMin(d("34"));assertThatThrownBy(()->service.atualizarConfiguracao(dto,new UsuarioAtor(1L,"admin",true))).hasMessageContaining("mínima");}
    private MedicaoQualidadeAguaRequest req(String chave,BigDecimal t,BigDecimal ph,BigDecimal o){var r=new MedicaoQualidadeAguaRequest();r.setChaveIdempotencia(chave);r.setMedidoEm(LocalDateTime.of(2026,9,30,8,0));r.setTemperatura(t);r.setPh(ph);r.setOxigenioDissolvido(o);r.setResponsavel("Operador");return r;}
    private MedicaoQualidadeAgua medicao(Long id,BigDecimal t,BigDecimal ph,BigDecimal o){var m=new MedicaoQualidadeAgua();ReflectionTestUtils.setField(m,"id",id);m.setLote(lote);m.setInstalacao(lote.getInstalacaoAtual());m.setMedidoEm(LocalDateTime.of(2026,9,30,8,0));m.setTemperatura(t);m.setPh(ph);m.setOxigenioDissolvido(o);m.setResponsavel("Operador");return m;}private BigDecimal d(String v){return new BigDecimal(v);}
}
