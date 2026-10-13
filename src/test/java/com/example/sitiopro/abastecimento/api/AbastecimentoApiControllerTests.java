package com.example.sitiopro.abastecimento.api;

import com.example.sitiopro.abastecimento.dto.*;
import com.example.sitiopro.abastecimento.service.*;
import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import com.example.sitiopro.shared.api.ApiExceptionHandler;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AbastecimentoApiControllerTests {
    private final AbastecimentoService service = mock(AbastecimentoService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AbastecimentoApiController(service))
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void registraComDtoEContratoDecimal() throws Exception {
        when(service.registrar(any(), any())).thenReturn(new AbastecimentoResumo(7L, 1L, "TRATOR",
                LocalDate.of(2026, 10, 8), new BigDecimal("12.345"), new BigDecimal("6.7891"),
                new BigDecimal("83.81"), null, new BigDecimal("25.50"), TipoMedidorVeiculo.HORIMETRO,
                "Posto", 0, null, "operador"));
        var principal = new UsernamePasswordAuthenticationToken("operador", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        mvc.perform(post("/api/v1/abastecimentos").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"veiculoId":1,"data":"2026-10-08","litros":12.345,"precoPorLitro":6.7891,
                         "horimetroNoAto":25.50,"local":"Posto","chaveIdempotencia":"ab-api-1","valorTotal":1}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/abastecimentos/7"))
                .andExpect(jsonPath("$.valorTotal").value(83.81));
        verify(service).registrar(argThat(r -> r.getLitros().compareTo(new BigDecimal("12.345")) == 0),
                argThat(a -> a.login().equals("operador")));
    }

    @Test
    void validacaoUsaErroPadronizado() throws Exception {
        mvc.perform(post("/api/v1/abastecimentos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.path").value("/api/v1/abastecimentos"));
    }
}
