package com.example.sitiopro.abastecimento.controller;

import com.example.sitiopro.abastecimento.dto.AbastecimentoResumo;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import com.example.sitiopro.frota.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AbastecimentoControllerTests {
    private final AbastecimentoService abastecimentos = mock(AbastecimentoService.class);
    private final VeiculoService veiculos = mock(VeiculoService.class);
    private final AbastecimentoController controller = new AbastecimentoController(abastecimentos, veiculos,
            Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC));
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

    @Test
    void formularioNaoAceitaValorTotalOuEntidadeVeiculo() throws Exception {
        when(abastecimentos.registrar(any(), any())).thenReturn(new AbastecimentoResumo(8L, 1L, "TRATOR",
                LocalDate.of(2026, 10, 8), BigDecimal.TEN, new BigDecimal("6.0000"), new BigDecimal("60.00"),
                new BigDecimal("100"), null, TipoMedidorVeiculo.QUILOMETRAGEM, "Posto", 0, null, "operador"));
        var principal = new UsernamePasswordAuthenticationToken("operador", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        mvc.perform(post("/sitio/abastecimento/salvar").principal(principal)
                        .param("veiculoId", "1").param("data", "2026-10-08").param("litros", "10")
                        .param("precoPorLitro", "6.0000").param("kmNoAto", "100").param("local", "Posto")
                        .param("chaveIdempotencia", "mvc-ab-1").param("valorTotal", "0.01").param("veiculo.id", "999"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/sitio/abastecimentos/8"));
        verify(abastecimentos).registrar(argThat(r -> r.getVeiculoId().equals(1L)
                && r.getLitros().compareTo(BigDecimal.TEN) == 0), any());
    }
}
