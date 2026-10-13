package com.example.sitiopro.frota.controller;

import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.dto.VeiculoResumo;
import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import com.example.sitiopro.frota.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VeiculoControllerTests {
    private final VeiculoService veiculos = mock(VeiculoService.class);
    private final AbastecimentoService abastecimentos = mock(AbastecimentoService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new VeiculoController(veiculos, abastecimentos)).build();

    @Test
    void formularioUsaDtoERestringeMassAssignment() throws Exception {
        when(veiculos.criar(any())).thenReturn(new VeiculoResumo(1L, "TRATOR", null, "3", null, null,
                null, TipoMedidorVeiculo.HORIMETRO, null, BigDecimal.ZERO, "DISPONIVEL", null,
                null, "fa-truck", null, null, null, 0));
        mvc.perform(post("/sitio/frota/salvar")
                        .param("nome", "Trator").param("tipo", "3").param("tipoMedidor", "HORIMETRO")
                        .param("horimetroAtual", "10.25").param("chaveIdempotencia", "mvc-frota-1")
                        .param("situacao", "BAIXADO").param("id", "999").param("versao", "999"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/sitio/frota/1"));
        verify(veiculos).criar(argThat(r -> r.getNome().equals("Trator")
                && r.getHorimetroAtual().compareTo(new BigDecimal("10.25")) == 0));
    }
}
