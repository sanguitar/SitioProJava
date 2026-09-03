package com.example.sitiopro.propriedade;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.propriedade.service.*;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropriedadeServiceTests {
    @Mock PropriedadeRepository propriedades;
    @Mock AreaPropriedadeRepository areas;
    @Mock TalhaoRepository talhoes;
    @Mock PiqueteRepository piquetes;
    @Mock EstruturaPropriedadeRepository estruturas;
    @Mock RecursoHidricoRepository recursos;
    @Mock InstalacaoCriacaoRepository instalacoes;
    @Mock EntityManager em;
    static ValidatorFactory factory;
    PropriedadeService service;
    Propriedade principal;

    @BeforeAll static void validator() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void fechar() { factory.close(); }
    @BeforeEach void preparar() {
        principal = new Propriedade();
        principal.setNome("Sítio de teste"); principal.setPrincipal(true);
        ReflectionTestUtils.setField(principal, "id", 42L);
        lenient().when(propriedades.findByPrincipalTrue()).thenReturn(Optional.of(principal));
        service = new PropriedadeService(propriedades, areas, talhoes, piquetes, estruturas,
                recursos, instalacoes, factory.getValidator(), em);
    }

    @Test void consultaPrincipalSemAssumirIdUm() {
        assertThat(service.resumo().id()).isEqualTo(42L);
        verify(propriedades, never()).findById(any());
    }
    @Test void propriedadeNormalizaUfEPreservaPrecisao() {
        var r = service.formulario(); r.setUf(" ro "); r.setAreaTotalHa(new BigDecimal("12.3456"));
        r.setLatitudeCentral(new BigDecimal("-8.123456")); r.setLongitudeCentral(new BigDecimal("-63.123456"));
        assertThat(service.atualizar(r).areaTotalHa()).isEqualByComparingTo("12.3456");
        assertThat(principal.getUf()).isEqualTo("RO");
        assertThat(principal.getRevisaoLocalizacao()).isEqualTo(1);
    }
    @Test void rejeitaUfInexistente() {
        var r = service.formulario(); r.setUf("ZZ");
        assertThatThrownBy(() -> service.atualizar(r)).isInstanceOf(PropriedadeOperacaoException.class);
    }
    @Test void coordenadasDevemSerEmPar() {
        var r = service.formulario(); r.setLatitudeCentral(BigDecimal.ONE);
        assertThatThrownBy(() -> service.atualizar(r)).hasMessageContaining("conjunto");
    }
    @ParameterizedTest @ValueSource(strings = {"-1", "0", "1.12345", "10000000000"})
    void rejeitaAreaInvalida(String area) {
        var r = service.formulario(); r.setAreaTotalHa(new BigDecimal(area));
        assertThatThrownBy(() -> service.atualizar(r)).isInstanceOf(PropriedadeOperacaoException.class);
    }
    @Test void edicaoObsoletaDaPropriedadeNaoSobrescreve() {
        var r = service.formulario(); r.setVersao(99L);
        assertThatThrownBy(() -> service.atualizar(r)).hasMessageContaining("Recarregue");
        verify(propriedades, never()).saveAndFlush(any());
    }
    @Test void administracaoAtualizaSomenteDadosFisicos() {
        principal.setMunicipio("Município original");
        service.atualizarDadosFisicos("Nome administrado", BigDecimal.ONE, BigDecimal.TEN, 0L);
        assertThat(principal.getNome()).isEqualTo("Nome administrado");
        assertThat(principal.getMunicipio()).isEqualTo("Município original");
        assertThat(principal.getRevisaoLocalizacao()).isEqualTo(1);
    }
    @Test void mesmaCoordenadaComOutraEscalaNaoInvalidaClima() {
        principal.inicializarCoordenadas(BigDecimal.ONE, BigDecimal.TEN);
        service.atualizarDadosFisicos("Nome", new BigDecimal("1.000000"), new BigDecimal("10.000000"), 0L);
        assertThat(principal.getRevisaoLocalizacao()).isZero();
    }
    @Test void areaDeOutraPropriedadeEhRecusada() {
        var r = talhao(); r.setAreaId(77L);
        assertThatThrownBy(() -> service.salvarTalhao(null, r)).hasMessageContaining("não pertence");
    }
    @Test void capacidadeExigeUnidade() {
        var r = estruturaPropriedade(); r.setCapacidade(BigDecimal.TEN);
        assertThatThrownBy(() -> service.salvarEstruturaPropriedade(null, r)).hasMessageContaining("conjunto");
    }
    @Test void estruturaVinculadaNaoPodeSerDesativada() {
        EstruturaPropriedade e = new EstruturaPropriedade(); e.setPropriedade(principal);
        when(estruturas.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        when(instalacoes.existsByEstruturaIdAndAtivoTrue(7L)).thenReturn(true);
        var r = estruturaPropriedade(); r.setVersao(0L); r.setAtivo(false);
        assertThatThrownBy(() -> service.salvarEstruturaPropriedade(7L,r)).hasMessageContaining("Desvincule");
    }
    @Test void vinculoNaoAceitaEstruturaInativa() {
        EstruturaPropriedade e = new EstruturaPropriedade(); e.setAtivo(false);
        when(estruturas.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        assertThatThrownBy(() -> service.estruturaParaVinculo(7L)).hasMessageContaining("inativa");
    }
    @Test void propriedadeInativaImpedeNovosCadastros() {
        principal.setAtivo(false);
        assertThatThrownBy(() -> service.salvarAreaPropriedade(null,areaPropriedade())).hasMessageContaining("Reative");
    }

    @Test void criaAreaPropriedadeComPropriedadeDoServidor() {
        var r = areaPropriedade();
        var resultado = service.salvarAreaPropriedade(null, r);
        assertThat(resultado.propriedadeId()).isEqualTo(42L);
        assertThat(resultado.ativo()).isTrue();
        verify(areas).saveAndFlush(argThat(e -> e.getNome().equals("Nome físico") && e.getPropriedade() == principal));
    }
    @Test void areaPropriedadeRejeitaNomeDuplicado() {
        when(areas.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(42L,"Nome físico",-1L)).thenReturn(true);
        assertThatThrownBy(() -> service.salvarAreaPropriedade(null,areaPropriedade())).hasMessageContaining("Já existe");
    }
    @Test void areaPropriedadeExigeVersaoAtualNaEdicao() {
        var e = new AreaPropriedade();
        when(areas.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        var r = areaPropriedade(); r.setVersao(1L);
        assertThatThrownBy(() -> service.salvarAreaPropriedade(7L,r)).hasMessageContaining("Recarregue");
        verify(areas, never()).saveAndFlush(any());
    }
    @Test void areaPropriedadeNaoConsultaIdDeOutraPropriedade() {
        assertThatThrownBy(() -> service.detalharAreaPropriedade(999L)).hasMessageContaining("nesta propriedade");
    }
    private AreaPropriedadeRequest areaPropriedade() {
        var r = new AreaPropriedadeRequest(); r.setNome("Nome físico");
        r.setTipo(TipoAreaPropriedade.AGRICOLA);
        return r;
    }

    @Test void criaTalhaoComPropriedadeDoServidor() {
        var r = talhao();
        var resultado = service.salvarTalhao(null, r);
        assertThat(resultado.propriedadeId()).isEqualTo(42L);
        assertThat(resultado.ativo()).isTrue();
        verify(talhoes).saveAndFlush(argThat(e -> e.getNome().equals("Nome físico") && e.getPropriedade() == principal));
    }
    @Test void talhaoRejeitaNomeDuplicado() {
        when(talhoes.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(42L,"Nome físico",-1L)).thenReturn(true);
        assertThatThrownBy(() -> service.salvarTalhao(null,talhao())).hasMessageContaining("Já existe");
    }
    @Test void talhaoExigeVersaoAtualNaEdicao() {
        var e = new Talhao();
        when(talhoes.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        var r = talhao(); r.setVersao(1L);
        assertThatThrownBy(() -> service.salvarTalhao(7L,r)).hasMessageContaining("Recarregue");
        verify(talhoes, never()).saveAndFlush(any());
    }
    @Test void talhaoNaoConsultaIdDeOutraPropriedade() {
        assertThatThrownBy(() -> service.detalharTalhao(999L)).hasMessageContaining("nesta propriedade");
    }
    private TalhaoRequest talhao() {
        var r = new TalhaoRequest(); r.setNome("Nome físico");
        r.setAreaHa(new BigDecimal("1.5"));
        return r;
    }

    @Test void criaPiqueteComPropriedadeDoServidor() {
        var r = piquete();
        var resultado = service.salvarPiquete(null, r);
        assertThat(resultado.propriedadeId()).isEqualTo(42L);
        assertThat(resultado.ativo()).isTrue();
        verify(piquetes).saveAndFlush(argThat(e -> e.getNome().equals("Nome físico") && e.getPropriedade() == principal));
    }
    @Test void piqueteRejeitaNomeDuplicado() {
        when(piquetes.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(42L,"Nome físico",-1L)).thenReturn(true);
        assertThatThrownBy(() -> service.salvarPiquete(null,piquete())).hasMessageContaining("Já existe");
    }
    @Test void piqueteExigeVersaoAtualNaEdicao() {
        var e = new Piquete();
        when(piquetes.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        var r = piquete(); r.setVersao(1L);
        assertThatThrownBy(() -> service.salvarPiquete(7L,r)).hasMessageContaining("Recarregue");
        verify(piquetes, never()).saveAndFlush(any());
    }
    @Test void piqueteNaoConsultaIdDeOutraPropriedade() {
        assertThatThrownBy(() -> service.detalharPiquete(999L)).hasMessageContaining("nesta propriedade");
    }
    private PiqueteRequest piquete() {
        var r = new PiqueteRequest(); r.setNome("Nome físico");

        return r;
    }

    @Test void criaEstruturaPropriedadeComPropriedadeDoServidor() {
        var r = estruturaPropriedade();
        var resultado = service.salvarEstruturaPropriedade(null, r);
        assertThat(resultado.propriedadeId()).isEqualTo(42L);
        assertThat(resultado.ativo()).isTrue();
        verify(estruturas).saveAndFlush(argThat(e -> e.getNome().equals("Nome físico") && e.getPropriedade() == principal));
    }
    @Test void estruturaPropriedadeRejeitaNomeDuplicado() {
        when(estruturas.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(42L,"Nome físico",-1L)).thenReturn(true);
        assertThatThrownBy(() -> service.salvarEstruturaPropriedade(null,estruturaPropriedade())).hasMessageContaining("Já existe");
    }
    @Test void estruturaPropriedadeExigeVersaoAtualNaEdicao() {
        var e = new EstruturaPropriedade();
        when(estruturas.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        var r = estruturaPropriedade(); r.setVersao(1L);
        assertThatThrownBy(() -> service.salvarEstruturaPropriedade(7L,r)).hasMessageContaining("Recarregue");
        verify(estruturas, never()).saveAndFlush(any());
    }
    @Test void estruturaPropriedadeNaoConsultaIdDeOutraPropriedade() {
        assertThatThrownBy(() -> service.detalharEstruturaPropriedade(999L)).hasMessageContaining("nesta propriedade");
    }
    private EstruturaPropriedadeRequest estruturaPropriedade() {
        var r = new EstruturaPropriedadeRequest(); r.setNome("Nome físico");
        r.setTipo(TipoEstruturaPropriedade.GALPAO);
        return r;
    }

    @Test void criaRecursoHidricoComPropriedadeDoServidor() {
        var r = recursoHidrico();
        var resultado = service.salvarRecursoHidrico(null, r);
        assertThat(resultado.propriedadeId()).isEqualTo(42L);
        assertThat(resultado.ativo()).isTrue();
        verify(recursos).saveAndFlush(argThat(e -> e.getNome().equals("Nome físico") && e.getPropriedade() == principal));
    }
    @Test void recursoHidricoRejeitaNomeDuplicado() {
        when(recursos.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(42L,"Nome físico",-1L)).thenReturn(true);
        assertThatThrownBy(() -> service.salvarRecursoHidrico(null,recursoHidrico())).hasMessageContaining("Já existe");
    }
    @Test void recursoHidricoExigeVersaoAtualNaEdicao() {
        var e = new RecursoHidrico();
        when(recursos.findByIdAndPropriedadeId(7L,42L)).thenReturn(Optional.of(e));
        var r = recursoHidrico(); r.setVersao(1L);
        assertThatThrownBy(() -> service.salvarRecursoHidrico(7L,r)).hasMessageContaining("Recarregue");
        verify(recursos, never()).saveAndFlush(any());
    }
    @Test void recursoHidricoNaoConsultaIdDeOutraPropriedade() {
        assertThatThrownBy(() -> service.detalharRecursoHidrico(999L)).hasMessageContaining("nesta propriedade");
    }
    private RecursoHidricoRequest recursoHidrico() {
        var r = new RecursoHidricoRequest(); r.setNome("Nome físico");
        r.setTipo(TipoRecursoHidrico.POCO);
        return r;
    }
}
