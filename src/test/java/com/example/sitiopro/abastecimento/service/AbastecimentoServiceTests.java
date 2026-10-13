package com.example.sitiopro.abastecimento.service;

import com.example.sitiopro.abastecimento.dto.AbastecimentoRequest;
import com.example.sitiopro.abastecimento.model.Abastecimento;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.frota.dto.VeiculoResumo;
import com.example.sitiopro.frota.model.*;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.manutencao.entity.AtivoPatrimonial;
import com.example.sitiopro.manutencao.service.ManutencaoPreventivaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbastecimentoServiceTests {
    @Mock AbastecimentoRepository repositorio;
    @Mock VeiculoService veiculos;
    @Mock CodigoCriacaoService codigos;
    @Mock ManutencaoPreventivaService manutencao;
    AbastecimentoService service;
    final UsuarioAtor ator = new UsuarioAtor(1L, "operador", false);

    @BeforeEach
    void setUp() {
        service = new AbastecimentoService(repositorio, veiculos, codigos, manutencao,
                Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void registraComBigDecimalAtualizaHodometroECalculaTotal() throws Exception {
        Veiculo veiculo = veiculo(TipoMedidorVeiculo.QUILOMETRAGEM, "100.00", null);
        AbastecimentoRequest request = request("150.25", null, "10.125", "6.7891", "abc");
        when(veiculos.buscarParaAtualizacao(1L)).thenReturn(veiculo);
        when(repositorio.saveAndFlush(any())).thenAnswer(invocation -> {
            Abastecimento salvo = invocation.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 9L);
            return salvo;
        });

        var resposta = service.registrar(request, ator);

        assertThat(resposta.valorTotal()).isEqualByComparingTo("68.74");
        assertThat(resposta.litros()).isEqualByComparingTo("10.125");
        assertThat(veiculo.getKmAtual()).isEqualByComparingTo("150.25");
        verify(codigos).bloquearIdempotencia("FROTA_ABASTECIMENTO", "abc");
        verifyNoInteractions(manutencao);
        assertThat(VeiculoService.class.getMethod("buscarParaAtualizacao", Long.class)
                .getAnnotation(Transactional.class).propagation()).isEqualTo(Propagation.MANDATORY);
        assertThat(VeiculoRepository.class.getMethod("buscarParaAtualizacao", Long.class)
                .getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void rejeitaRegressaoDeHodometro() {
        when(veiculos.buscarParaAtualizacao(1L)).thenReturn(
                veiculo(TipoMedidorVeiculo.QUILOMETRAGEM, "200.00", null));
        assertThatThrownBy(() -> service.registrar(request("199.99", null, "10", "5", "regressao"), ator))
                .isInstanceOf(AbastecimentoOperacaoException.class).hasMessageContaining("não pode regredir");
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void horimetroVinculadoSincronizaLeituraOficialDaManutencao() {
        Veiculo veiculo = veiculo(TipoMedidorVeiculo.HORIMETRO, null, "20.00");
        AtivoPatrimonial ativo = new AtivoPatrimonial(); ReflectionTestUtils.setField(ativo, "id", 77L);
        veiculo.setAtivoPatrimonial(ativo);
        when(veiculos.buscarParaAtualizacao(1L)).thenReturn(veiculo);
        when(repositorio.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.registrar(request(null, "25.50", "8", "7", "horas"), ator);

        assertThat(veiculo.getHorimetroAtual()).isEqualByComparingTo("25.50");
        verify(manutencao).registrarLeitura(argThat(leitura -> leitura.getAtivoId().equals(77L)
                && leitura.getHorimetro().compareTo(new BigDecimal("25.50")) == 0
                && leitura.getQuilometragem() == null
                && leitura.getChaveIdempotencia().equals("ABASTECIMENTO:horas")), eq(ator));
    }

    @Test
    void retryIgualRetornaRegistroSemDuplicar() {
        AbastecimentoRequest request = request("150", null, "10", "5", "retry");
        Abastecimento existente = abastecimento(request, veiculo(TipoMedidorVeiculo.QUILOMETRAGEM, "150", null));
        when(repositorio.findByChaveIdempotencia("retry")).thenReturn(Optional.of(existente));

        assertThat(service.registrar(request, ator).valorTotal()).isEqualByComparingTo("50.00");
        verify(repositorio, never()).saveAndFlush(any());
        verifyNoInteractions(manutencao);
    }

    @Test
    void retryDivergenteEhRecusado() {
        AbastecimentoRequest request = request("150", null, "10", "5", "retry");
        AbastecimentoRequest outro = request("151", null, "10", "5", "retry");
        when(repositorio.findByChaveIdempotencia("retry"))
                .thenReturn(Optional.of(abastecimento(request, veiculo(TipoMedidorVeiculo.QUILOMETRAGEM, "150", null))));
        assertThatThrownBy(() -> service.registrar(outro, ator))
                .isInstanceOf(AbastecimentoOperacaoException.class).hasMessageContaining("outro abastecimento");
    }

    @Test
    void calculaConsumoMedioECustosBasicos() {
        VeiculoResumo veiculo = new VeiculoResumo(1L, "TRATOR", null, "3", null, null, null,
                TipoMedidorVeiculo.QUILOMETRAGEM, new BigDecimal("220"), null, "DISPONIVEL", null,
                null, "fa-truck", null, null, null, 0);
        when(veiculos.detalhar(1L)).thenReturn(veiculo);
        when(repositorio.findByVeiculoIdOrderByDataAscIdAsc(1L)).thenReturn(List.of(
                historico("100", "20", "100"), historico("160", "10", "60"), historico("220", "20", "140")));

        var resumo = service.resumoOperacional(1L);
        assertThat(resumo.litros()).isEqualByComparingTo("50.000");
        assertThat(resumo.custoTotal()).isEqualByComparingTo("300.00");
        assertThat(resumo.consumoMedio()).isEqualByComparingTo("4.00");
        assertThat(resumo.unidadeConsumo()).isEqualTo("km/l");
    }

    private AbastecimentoRequest request(String km, String horas, String litros, String preco, String chave) {
        AbastecimentoRequest r = new AbastecimentoRequest(); r.setVeiculoId(1L); r.setData(LocalDate.of(2026, 10, 8));
        r.setKmNoAto(decimal(km)); r.setHorimetroNoAto(decimal(horas)); r.setLitros(decimal(litros));
        r.setPrecoPorLitro(decimal(preco)); r.setLocal("Posto"); r.setChaveIdempotencia(chave); return r;
    }
    private Veiculo veiculo(TipoMedidorVeiculo tipo, String km, String horas) {
        Veiculo v = new Veiculo(); ReflectionTestUtils.setField(v, "id", 1L); v.setNome("TRATOR");
        v.setTipoMedidor(tipo); v.setKmAtual(decimal(km)); v.setHorimetroAtual(decimal(horas)); return v;
    }
    private Abastecimento abastecimento(AbastecimentoRequest r, Veiculo v) {
        Abastecimento a = new Abastecimento(); a.setVeiculo(v); a.setData(r.getData()); a.setLitros(r.getLitros());
        a.setPrecoPorLitro(r.getPrecoPorLitro()); a.setKmNoAto(r.getKmNoAto()); a.setHorimetroNoAto(r.getHorimetroNoAto());
        a.setLocal(r.getLocal()); a.setChaveIdempotencia(r.getChaveIdempotencia()); a.calcularTotal(); return a;
    }
    private Abastecimento historico(String km, String litros, String total) {
        Abastecimento a = new Abastecimento(); a.setKmNoAto(decimal(km)); a.setLitros(decimal(litros)); a.setValorTotal(decimal(total)); return a;
    }
    private BigDecimal decimal(String valor) { return valor == null ? null : new BigDecimal(valor); }
}
