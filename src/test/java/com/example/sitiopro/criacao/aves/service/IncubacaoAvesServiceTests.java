package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.criacao.core.entity.*;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncubacaoAvesServiceTests {
    @Mock private IncubacaoAvesRepository repository;
    @Mock private InstalacaoCriacaoService instalacaoService;
    @Mock private LoteAvesService loteService;
    @Mock private AvesAlertasService alertasService;
    @Mock private CodigoCriacaoService codigoService;
    @Mock private RegistroPosturaAvesRepository posturaRepository;
    @Mock private IncubacaoAcompanhamentoService acompanhamentoService;
    @Mock private IncubacaoOperacionalService operacionalService;
    private IncubacaoAvesService service;
    private InstalacaoCriacao incubadora;
    private final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach
    void preparar() {
        AvesProperties properties = new AvesProperties();
        service = new IncubacaoAvesService(repository, instalacaoService, loteService, alertasService, codigoService,
                posturaRepository, acompanhamentoService, operacionalService, properties,
                Clock.fixed(Instant.parse("2026-08-24T12:00:00Z"), ZoneOffset.UTC));
        incubadora = new InstalacaoCriacao(); ReflectionTestUtils.setField(incubadora, "id", 10L);
        incubadora.setNome("Incubadora principal"); incubadora.setTipo(TipoInstalacaoCriacao.INCUBADORA); incubadora.setAtivo(true);
        lenient().when(codigoService.proximaIncubacaoAves()).thenReturn("INC-2026-0001");
        lenient().when(acompanhamentoService.listar(anyLong())).thenReturn(List.of());
        lenient().when(operacionalService.tarefas(anyLong())).thenReturn(List.of());
        lenient().when(operacionalService.alertas(anyLong())).thenReturn(List.of());
    }

    @Test
    void operadorIniciaIncubacaoComPrevisao() {
        CriarIncubacaoAvesRequest request = criarRequest();
        when(repository.findByChaveIdempotencia("inc-1")).thenReturn(Optional.empty());
        when(instalacaoService.buscarAtiva(10L)).thenReturn(incubadora);
        when(repository.save(any())).thenAnswer(inv -> { IncubacaoAves i = inv.getArgument(0); ReflectionTestUtils.setField(i, "id", 1L); return i; });
        IncubacaoAvesDetalhe detalhe = service.criar(request, operador);
        assertThat(detalhe.status()).isEqualTo(StatusIncubacaoAves.EM_INCUBACAO);
        assertThat(detalhe.quantidadeOvos()).isEqualTo(60);
        assertThat(detalhe.codigo()).isEqualTo("INC-2026-0001");
        assertThat(detalhe.metodo()).isEqualTo(MetodoIncubacaoAves.CHOCADEIRA);
        assertThat(detalhe.especie()).isEqualTo(EspecieAves.GALINHA);
        assertThat(detalhe.dataPrevistaEclosao()).isEqualTo(LocalDate.of(2026, 8, 24));
        verify(codigoService).bloquearIdempotencia("INCUBACAO_AVES", "inc-1");
        verify(operacionalService).garantirTarefas(any(IncubacaoAves.class), eq(operador));
        verify(alertasService).avaliar();
    }

    @Test
    void operadorNaoConsegueForcarPrevisaoDiferenteDoPeriodoConfigurado() {
        CriarIncubacaoAvesRequest request = criarRequest();
        request.setDataPrevistaEclosao(LocalDate.of(2026, 9, 30));
        when(repository.findByChaveIdempotencia("inc-1")).thenReturn(Optional.empty());
        when(instalacaoService.buscarAtiva(10L)).thenReturn(incubadora);
        when(repository.save(any())).thenAnswer(inv -> {
            IncubacaoAves i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", 1L);
            return i;
        });

        IncubacaoAvesDetalhe detalhe = service.criar(request, operador);

        assertThat(detalhe.dataPrevistaEclosao()).isEqualTo(LocalDate.of(2026, 8, 24));
    }

    @Test
    void adminTambemUsaPrevisaoAutomaticaNoCadastroEAjustaDepoisComMotivo() {
        CriarIncubacaoAvesRequest request = criarRequest();
        request.setDataPrevistaEclosao(LocalDate.of(2026, 9, 30));
        when(repository.findByChaveIdempotencia("inc-1")).thenReturn(Optional.empty());
        when(instalacaoService.buscarAtiva(10L)).thenReturn(incubadora);
        when(repository.save(any())).thenAnswer(inv -> {
            IncubacaoAves i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", 1L);
            return i;
        });

        IncubacaoAvesDetalhe detalhe = service.criar(request, new UsuarioAtor(1L, "admin", true));

        assertThat(detalhe.dataPrevistaEclosao()).isEqualTo(LocalDate.of(2026, 8, 24));
    }

    @Test
    void galinhaChocaAceitaGalinheiroSemDadosDeEquipamento() {
        incubadora.setTipo(TipoInstalacaoCriacao.GALINHEIRO);
        CriarIncubacaoAvesRequest request = criarRequest();
        request.setMetodo(MetodoIncubacaoAves.GALINHA_CHOCA);
        when(repository.findByChaveIdempotencia("inc-1")).thenReturn(Optional.empty());
        when(instalacaoService.buscarAtiva(10L)).thenReturn(incubadora);
        when(repository.save(any())).thenAnswer(inv -> {
            IncubacaoAves i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", 1L);
            return i;
        });

        IncubacaoAvesDetalhe detalhe = service.criar(request, operador);

        assertThat(detalhe.metodo()).isEqualTo(MetodoIncubacaoAves.GALINHA_CHOCA);
        assertThat(detalhe.dataPrevistaEclosao()).isEqualTo(LocalDate.of(2026, 8, 24));
    }

    @Test
    void instalacaoQueNaoEIncubadoraERecusada() {
        incubadora.setTipo(TipoInstalacaoCriacao.GALINHEIRO);
        when(repository.findByChaveIdempotencia("inc-1")).thenReturn(Optional.empty());
        when(instalacaoService.buscarAtiva(10L)).thenReturn(incubadora);
        assertThatThrownBy(() -> service.criar(criarRequest(), operador))
                .isInstanceOf(AvesOperacaoException.class).extracting("code").isEqualTo("INSTALACAO_NAO_INCUBADORA");
    }

    @Test
    void finalizacaoCalculaTaxasECriaUmUnicoLote() {
        IncubacaoAves incubacao = incubacaoAberta();
        when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));
        LoteAves pintinhos = new LoteAves(); ReflectionTestUtils.setField(pintinhos, "id", 20L); pintinhos.setCodigo("PIN-001");
        when(loteService.criarDeIncubacao(any(), eq("operador"), eq(1L))).thenReturn(pintinhos);
        FinalizarIncubacaoAvesRequest request = finalizarRequest();

        IncubacaoAvesDetalhe primeira = service.finalizar(1L, request, operador);
        IncubacaoAvesDetalhe repetida = service.finalizar(1L, request, operador);

        assertThat(primeira.taxaEclosao()).isEqualByComparingTo("80.00");
        assertThat(primeira.taxaPerdas()).isEqualByComparingTo("20.00");
        assertThat(repetida.loteResultanteId()).isEqualTo(20L);
        verify(loteService, times(1)).criarDeIncubacao(any(), eq("operador"), eq(1L));
        ArgumentCaptor<CriarLoteAvesRequest> captor = ArgumentCaptor.forClass(CriarLoteAvesRequest.class);
        verify(loteService).criarDeIncubacao(captor.capture(), anyString(), anyLong());
        assertThat(captor.getValue().getQuantidadeInicial()).isEqualTo(48);
        assertThat(captor.getValue().getDataNascimento()).isEqualTo(LocalDate.of(2026, 8, 23));
        assertThat(captor.getValue().getEspecie()).isEqualTo(EspecieAves.GALINHA);
        assertThat(java.util.Arrays.stream(CriarLoteAvesRequest.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)).doesNotContain("codigo");
    }

    @Test
    void eclodidosEPerdasNaoPodemSuperarOvos() {
        when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacaoAberta()));
        FinalizarIncubacaoAvesRequest request = finalizarRequest(); request.setPintinhosEclodidos(50); request.setOvosPerdidos(20);
        assertThatThrownBy(() -> service.finalizar(1L, request, operador))
                .isInstanceOf(AvesOperacaoException.class).extracting("code").isEqualTo("RESULTADO_SUPERA_OVOS");
        verify(loteService, never()).criarDeIncubacao(any(), anyString(), anyLong());
    }

    @Test
    void somenteAdminCancelaIncubacao() {
        assertThatThrownBy(() -> service.cancelar(1L, operador)).isInstanceOf(AvesOperacaoException.class)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        IncubacaoAves incubacao = incubacaoAberta(); when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));
        service.cancelar(1L, new UsuarioAtor(1L, "admin", true));
        assertThat(incubacao.getStatus()).isEqualTo(StatusIncubacaoAves.CANCELADA);
    }

    @Test
    void somenteAdminAjustaPrevisaoComMotivo() {
        IncubacaoAves incubacao = incubacaoAberta();
        when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(incubacao));
        AjustarPrevisaoIncubacaoAvesRequest request = new AjustarPrevisaoIncubacaoAvesRequest();
        request.setDataPrevistaEclosao(LocalDate.of(2026, 8, 25));
        request.setMotivo("Atraso observado no desenvolvimento");

        assertThatThrownBy(() -> service.ajustarPrevisao(1L, request, operador))
                .isInstanceOf(AvesOperacaoException.class)
                .extracting("status").isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);

        service.ajustarPrevisao(1L, request, new UsuarioAtor(1L, "admin", true));

        assertThat(incubacao.getDataPrevistaEclosao()).isEqualTo(LocalDate.of(2026, 8, 25));
        assertThat(incubacao.getMotivoAjustePrevisao()).isEqualTo("Atraso observado no desenvolvimento");
        verify(operacionalService).garantirTarefas(incubacao, new UsuarioAtor(1L, "admin", true));
    }

    @Test
    void codigosNaoPodemSerEnviadosNosDtosDeIncubacao() {
        assertThat(java.util.Arrays.stream(CriarIncubacaoAvesRequest.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)).doesNotContain("codigo");
        assertThat(java.util.Arrays.stream(FinalizarIncubacaoAvesRequest.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)).doesNotContain("codigoLote");
    }

    private CriarIncubacaoAvesRequest criarRequest() { CriarIncubacaoAvesRequest r = new CriarIncubacaoAvesRequest(); r.setMetodo(MetodoIncubacaoAves.CHOCADEIRA); r.setEspecie(EspecieAves.GALINHA); r.setInstalacaoId(10L); r.setDataInicio(LocalDate.of(2026,8,3)); r.setQuantidadeOvos(60); r.setDataPrevistaEclosao(LocalDate.of(2026,8,24)); r.setChaveIdempotencia("inc-1"); return r; }
    private IncubacaoAves incubacaoAberta() { IncubacaoAves i = new IncubacaoAves(); ReflectionTestUtils.setField(i, "id", 1L); i.setCodigo("INC-001"); i.setMetodo(MetodoIncubacaoAves.CHOCADEIRA); i.setEspecie(EspecieAves.GALINHA); i.setInstalacao(incubadora); i.setDataInicio(LocalDate.of(2026,8,3)); i.setQuantidadeOvos(60); i.setDataPrevistaEclosao(LocalDate.of(2026,8,24)); i.setStatus(StatusIncubacaoAves.EM_INCUBACAO); i.setChaveIdempotencia("inc-1"); return i; }
    private FinalizarIncubacaoAvesRequest finalizarRequest() { FinalizarIncubacaoAvesRequest r = new FinalizarIncubacaoAvesRequest(); r.setPintinhosEclodidos(48); r.setOvosPerdidos(12); r.setDataEclosao(LocalDate.of(2026,8,23)); r.setCriarLote(true); r.setInstalacaoDestinoId(11L); r.setChaveIdempotenciaLote("pintinhos-1"); return r; }
}
