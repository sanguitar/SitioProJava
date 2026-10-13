package com.example.sitiopro.frota.api;

import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.dto.*;
import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import com.example.sitiopro.frota.service.*;
import com.example.sitiopro.shared.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FrotaApiControllerTests {
    private final VeiculoService veiculos = mock(VeiculoService.class);
    private final AbastecimentoService abastecimentos = mock(AbastecimentoService.class);
    private final FipeService fipe = mock(FipeService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FrotaApiController(veiculos, abastecimentos, fipe))
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void listaReadModelSemExporEntidadeOuCacheFipe() throws Exception {
        var veiculo = new VeiculoResumo(1L, "TRATOR", null, "3", null, null, null,
                TipoMedidorVeiculo.HORIMETRO, null, new BigDecimal("10.50"), "DISPONIVEL", null,
                null, "fa-truck", 2L, "PAT-2026-0001", "Trator", 0);
        when(abastecimentos.resumoFrota()).thenReturn(List.of(new VeiculoOperacionalResumo(
                veiculo, 2, new BigDecimal("30.000"), new BigDecimal("180.00"),
                new BigDecimal("1.25"), "h/l")));

        mvc.perform(get("/api/v1/frota"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].veiculo.nome").value("TRATOR"))
                .andExpect(jsonPath("$[0].consumoMedio").value(1.25))
                .andExpect(jsonPath("$[0].veiculo.chaveIdempotencia").doesNotExist())
                .andExpect(jsonPath("$[0].veiculo.ativoPatrimonial.chaveIdempotencia").doesNotExist());
    }

    @Test
    void criaPorDtoExplicito() throws Exception {
        when(veiculos.criar(any())).thenReturn(new VeiculoResumo(9L, "TRATOR", null, "3", null, null,
                null, TipoMedidorVeiculo.HORIMETRO, null, BigDecimal.ZERO, "DISPONIVEL", null,
                null, "fa-truck", null, null, null, 0));
        mvc.perform(post("/api/v1/frota").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Trator","tipo":"3","tipoMedidor":"HORIMETRO","horimetroAtual":0,
                 "chaveIdempotencia":"api-frota-1","situacao":"BAIXADO"}
                """))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/v1/frota/9"));
        verify(veiculos).criar(argThat(r -> r.getNome().equals("Trator") && r.getTipoMedidor() == TipoMedidorVeiculo.HORIMETRO));
    }

    @Test
    void detalheFipeUsaContratoPublico() throws Exception {
        when(fipe.buscarDetalhes(10)).thenReturn(new FipeDetalheResponse(10, "2025", new BigDecimal("123456.78"),
                new com.fasterxml.jackson.databind.ObjectMapper().createArrayNode()));
        mvc.perform(get("/api/v1/frota/fipe/detalhes/10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(123456.78))
                .andExpect(jsonPath("$.historicoJson").doesNotExist());
    }
}
