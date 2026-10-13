package com.example.sitiopro.frota.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.frota.dto.VeiculoRequest;
import com.example.sitiopro.frota.model.*;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.AtivoPatrimonialRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTests {
    @Mock VeiculoRepository veiculos;
    @Mock AtivoPatrimonialRepository ativos;
    @Mock CodigoCriacaoService codigos;
    @Mock FipeService fipe;
    VeiculoService service;

    @BeforeEach void setUp() { service = new VeiculoService(veiculos, ativos, codigos, fipe); }

    @Test
    void criaVeiculoComPrecisaoIdempotenciaETipoMedidor() {
        VeiculoRequest request = request("fro-1");
        when(veiculos.saveAndFlush(any())).thenAnswer(invocation -> {
            Veiculo v = invocation.getArgument(0); ReflectionTestUtils.setField(v, "id", 1L); return v;
        });

        var criado = service.criar(request);

        assertThat(criado.nome()).isEqualTo("TRATOR PRINCIPAL");
        assertThat(criado.horimetroAtual()).isEqualByComparingTo("12.35");
        assertThat(criado.tipoMedidor()).isEqualTo(TipoMedidorVeiculo.HORIMETRO);
        verify(codigos).bloquearIdempotencia("FROTA_VEICULO", "fro-1");
    }

    @Test
    void retryDivergenteNaoCriaOutroVeiculo() {
        Veiculo existente = new Veiculo(); existente.setNome("OUTRO"); existente.setChaveIdempotencia("fro-1");
        when(veiculos.findByChaveIdempotencia("fro-1")).thenReturn(Optional.of(existente));
        assertThatThrownBy(() -> service.criar(request("fro-1")))
                .isInstanceOf(FrotaOperacaoException.class).hasMessageContaining("outro veículo");
        verify(veiculos, never()).saveAndFlush(any());
    }

    @Test
    void impedeVincularMesmoAtivoADoisVeiculos() {
        VeiculoRequest request = request("fro-2"); request.setAtivoPatrimonialId(10L);
        AtivoPatrimonial ativo = new AtivoPatrimonial(); ReflectionTestUtils.setField(ativo, "id", 10L);
        ativo.setTipo(TipoAtivoPatrimonial.MAQUINA); ativo.setStatus(StatusAtivoPatrimonial.ATIVO);
        when(ativos.findById(10L)).thenReturn(Optional.of(ativo));
        when(veiculos.existsByAtivoPatrimonialId(10L)).thenReturn(true);
        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(FrotaOperacaoException.class).hasMessageContaining("já está vinculado");
    }

    private VeiculoRequest request(String chave) {
        VeiculoRequest r = new VeiculoRequest(); r.setNome("Trator principal"); r.setTipo("3");
        r.setTipoMedidor(TipoMedidorVeiculo.HORIMETRO); r.setHorimetroAtual(new BigDecimal("12.345"));
        r.setChaveIdempotencia(chave); return r;
    }
}
