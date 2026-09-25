package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
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
class SuinosReproducaoServiceTests {
    @Mock AnimalReprodutivoSuinosRepository animais;
    @Mock CicloReprodutivoSuinosRepository ciclos;
    @Mock LoteSuinosRepository lotes;
    @Mock CodigoCriacaoService codigos;
    @Mock SuinosService suinos;
    @Mock SuinosReproducaoOperacionalService operacional;
    @Mock TarefaService tarefas;
    @Mock AlertaService alertas;
    SuinosReproducaoService service;
    LoteSuinos loteMatriz;
    AnimalReprodutivoSuinos matriz;
    final UsuarioAtor admin = new UsuarioAtor(1L, "admin", true);
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach
    void preparar() {
        service = new SuinosReproducaoService(animais, ciclos, lotes, codigos, suinos, operacional,
                tarefas, alertas, Clock.fixed(Instant.parse("2026-09-24T12:00:00Z"), ZoneOffset.UTC));
        loteMatriz = lote(10L, "SU-2026-0010", CategoriaSuino.MATRIZ, 3);
        matriz = animal(20L, "SR-2026-0001", TipoAnimalReprodutivo.MATRIZ, loteMatriz);
        lenient().when(animais.findById(20L)).thenReturn(Optional.of(matriz));
        lenient().when(tarefas.listarRelacionadas(any(), anyString())).thenReturn(List.of());
        lenient().when(alertas.listarRelacionados(any(), anyString())).thenReturn(List.of());
    }

    @Test
    void adminIdentificaMatrizComCodigoAutomatico() {
        CriarAnimalReprodutivoRequest request = animalRequest("animal-1");
        when(lotes.findById(10L)).thenReturn(Optional.of(loteMatriz));
        when(codigos.proximoAnimalSuinos()).thenReturn("SR-2026-0001");
        when(animais.save(any())).thenAnswer(inv -> { var a = inv.<AnimalReprodutivoSuinos>getArgument(0); ReflectionTestUtils.setField(a, "id", 20L); return a; });
        var criado = service.cadastrarAnimal(request, admin);
        assertThat(criado.codigo()).isEqualTo("SR-2026-0001");
        verify(codigos).bloquearIdempotencia("SUINOS_ANIMAL", "animal-1");
    }

    @Test
    void operadorNaoCadastraAnimal() {
        assertThatThrownBy(() -> service.cadastrarAnimal(animalRequest("x"), operador))
                .isInstanceOf(SuinosOperacaoException.class).hasMessageContaining("administradores");
    }

    @Test
    void coberturaCalculaChecagemEPartoEGeraTarefas() {
        RegistrarCoberturaSuinosRequest request = cobertura("cobertura-1");
        when(codigos.proximaReproducaoSuinos()).thenReturn("GES-2026-0001");
        when(ciclos.save(any())).thenAnswer(inv -> { var c = inv.<CicloReprodutivoSuinos>getArgument(0); ReflectionTestUtils.setField(c, "id", 30L); return c; });
        var detalhe = service.registrarCobertura(request, operador);
        assertThat(detalhe.dataPrevistaChecagem()).isEqualTo(LocalDate.of(2026, 9, 1).plusDays(28));
        assertThat(detalhe.dataPrevistaParto()).isEqualTo(LocalDate.of(2026, 9, 1).plusDays(114));
        assertThat(detalhe.status()).isEqualTo(StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM);
        verify(operacional).garantirTarefas(any(), eq(operador));
    }

    @Test
    void retryDaCoberturaRetornaMesmoCiclo() {
        CicloReprodutivoSuinos ciclo = ciclo(30L, StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM);
        when(ciclos.findByChaveIdempotencia("cobertura-1")).thenReturn(Optional.of(ciclo));
        assertThat(service.registrarCobertura(cobertura("cobertura-1"), operador).id()).isEqualTo(30L);
        verify(ciclos, never()).save(any());
    }

    @Test
    void checagemConfirmaGestacao() {
        CicloReprodutivoSuinos ciclo = ciclo(30L, StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM);
        when(ciclos.buscarParaAtualizacao(30L)).thenReturn(Optional.of(ciclo));
        ChecagemGestacaoSuinosRequest request = new ChecagemGestacaoSuinosRequest();
        request.setDataChecagem(LocalDate.of(2026, 9, 24)); request.setGestacaoConfirmada(true);
        var detalhe = service.registrarChecagem(30L, request, operador);
        assertThat(detalhe.status()).isEqualTo(StatusCicloReprodutivoSuinos.GESTANTE);
        verify(operacional).garantirTarefas(ciclo, operador);
    }

    @Test
    void partoCriaUmUnicoLoteDeLeitoes() {
        CicloReprodutivoSuinos ciclo = ciclo(30L, StatusCicloReprodutivoSuinos.GESTANTE);
        LoteSuinos leitoes = lote(40L, "SU-2026-0040", CategoriaSuino.LEITAO, 9);
        when(ciclos.buscarParaAtualizacao(30L)).thenReturn(Optional.of(ciclo));
        when(suinos.criarLoteDoParto(eq(50L), eq(9), any(), any(), anyString(), any(), anyString(), eq(operador)))
                .thenReturn(loteDetalhe(leitoes));
        when(lotes.getReferenceById(40L)).thenReturn(leitoes);
        RegistrarPartoSuinosRequest request = parto("parto-1");
        var detalhe = service.registrarParto(30L, request, operador);
        assertThat(detalhe.loteLeitoesId()).isEqualTo(40L);
        assertThat(detalhe.dataPrevistaDesmame()).isEqualTo(request.getDataParto().plusDays(28));
        verify(suinos, times(1)).criarLoteDoParto(anyLong(), anyInt(), any(), any(), anyString(), any(), anyString(), any());
    }

    @Test
    void retryDoPartoNaoDuplicaLote() {
        CicloReprodutivoSuinos ciclo = ciclo(30L, StatusCicloReprodutivoSuinos.PARTO_REALIZADO);
        ciclo.setChaveParto("parto-1"); ciclo.setLoteLeitoes(lote(40L, "SU-2026-0040", CategoriaSuino.LEITAO, 9));
        when(ciclos.findByChaveParto("parto-1")).thenReturn(Optional.of(ciclo));
        assertThat(service.registrarParto(30L, parto("parto-1"), operador).loteLeitoesId()).isEqualTo(40L);
        verifyNoInteractions(suinos);
    }

    @Test
    void desmameRegistraPesoTransferenciaERetryNaoDuplica() {
        CicloReprodutivoSuinos ciclo = ciclo(30L, StatusCicloReprodutivoSuinos.PARTO_REALIZADO);
        ciclo.setDataParto(LocalDate.of(2026, 8, 20)); ciclo.setLoteLeitoes(lote(40L, "SU-2026-0040", CategoriaSuino.LEITAO, 9));
        when(ciclos.buscarParaAtualizacao(30L)).thenReturn(Optional.of(ciclo));
        RegistrarDesmameSuinosRequest request = new RegistrarDesmameSuinosRequest(); request.setDataDesmame(LocalDate.of(2026, 9, 20));
        request.setPesoMedio(new BigDecimal("7.25")); request.setInstalacaoDestinoId(51L); request.setChaveIdempotencia("desmame-1");
        service.registrarDesmame(30L, request, operador);
        assertThat(ciclo.getStatus()).isEqualTo(StatusCicloReprodutivoSuinos.DESMAMADO);
        verify(suinos).registrarPesagem(eq(40L), any(), eq(operador)); verify(suinos).transferir(eq(40L), any(), eq(operador));
        when(ciclos.findByChaveDesmame("desmame-1")).thenReturn(Optional.of(ciclo));
        service.registrarDesmame(30L, request, operador);
        verify(suinos, times(1)).registrarPesagem(anyLong(), any(), any());
    }

    private CriarAnimalReprodutivoRequest animalRequest(String chave) { var r = new CriarAnimalReprodutivoRequest(); r.setLoteId(10L); r.setTipo(TipoAnimalReprodutivo.MATRIZ); r.setChaveIdempotencia(chave); return r; }
    private RegistrarCoberturaSuinosRequest cobertura(String chave) { var r = new RegistrarCoberturaSuinosRequest(); r.setMatrizId(20L); r.setMetodo(MetodoReproducaoSuinos.INSEMINACAO); r.setDataCobertura(LocalDate.of(2026, 9, 1)); r.setChaveIdempotencia(chave); return r; }
    private RegistrarPartoSuinosRequest parto(String chave) { var r = new RegistrarPartoSuinosRequest(); r.setDataParto(LocalDate.of(2026, 9, 24)); r.setNascidosVivos(9); r.setNatimortos(1); r.setPerdas(0); r.setInstalacaoLeitoesId(50L); r.setChaveIdempotencia(chave); return r; }
    private CicloReprodutivoSuinos ciclo(Long id, StatusCicloReprodutivoSuinos status) { var c = new CicloReprodutivoSuinos(); ReflectionTestUtils.setField(c, "id", id); c.setCodigo("GES-2026-0001"); c.setMatriz(matriz); c.setMetodo(MetodoReproducaoSuinos.INSEMINACAO); c.setDataCobertura(LocalDate.of(2026, 6, 1)); c.setDataPrevistaChecagem(LocalDate.of(2026, 6, 29)); c.setDataPrevistaParto(LocalDate.of(2026, 9, 23)); c.setStatus(status); return c; }
    private AnimalReprodutivoSuinos animal(Long id, String codigo, TipoAnimalReprodutivo tipo, LoteSuinos lote) { var a = new AnimalReprodutivoSuinos(); ReflectionTestUtils.setField(a, "id", id); a.setCodigo(codigo); a.setTipo(tipo); a.setLote(lote); a.setStatus(StatusAnimalReprodutivo.ATIVO); return a; }
    private LoteSuinos lote(Long id, String codigo, CategoriaSuino categoria, int quantidade) { var l = new LoteSuinos(); ReflectionTestUtils.setField(l, "id", id); l.setCodigo(codigo); l.setCategoria(categoria); l.setQuantidadeInicial(quantidade); l.setQuantidadeAtual(quantidade); l.setStatus(StatusLoteSuinos.ATIVO); var i = new InstalacaoCriacao(); ReflectionTestUtils.setField(i, "id", 50L); i.setNome("Maternidade"); l.setInstalacaoAtual(i); return l; }
    private LoteSuinosDetalhe loteDetalhe(LoteSuinos l) { return new LoteSuinosDetalhe(l.getId(), l.getCodigo(), l.getCategoria(), LocalDate.now(), LocalDate.now(), "Parto", l.getQuantidadeInicial(), l.getQuantidadeAtual(), null, 50L, "Maternidade", l.getStatus(), null, BigDecimal.ZERO, 0, List.of(), List.of(), List.of(), 0, null, null, null, null); }
}
