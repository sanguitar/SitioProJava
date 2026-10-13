package com.example.sitiopro.agua.api;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import com.example.sitiopro.agua.service.AguaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AguaApiControllerTests {
    AguaService service = mock(AguaService.class);
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AguaApiController(service)).build();
    }

    @Test
    void operacaoUsaDtoERetornaCreatedComLocation() throws Exception {
        OperacaoAguaResumo criada = new OperacaoAguaResumo(30L, TipoOperacaoAgua.TRANSFERENCIA,
                LocalDateTime.of(2026, 10, 8, 8, 0), 10L, "Caixa 1", 11L, "Caixa 2",
                new BigDecimal("100"), new BigDecimal("500"), new BigDecimal("400"),
                new BigDecimal("100"), new BigDecimal("200"), null, 0, null, "operador");
        when(service.registrar(argThat(r -> r.getTipo() == TipoOperacaoAgua.TRANSFERENCIA
                && r.getRecursoDestinoId().equals(11L)), any())).thenReturn(criada);

        mvc.perform(post("/api/v1/agua/operacoes")
                        .principal(new TestingAuthenticationToken("operador", "", "ROLE_OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"recursoOrigemId":10,"recursoDestinoId":11,"tipo":"TRANSFERENCIA",
                                 "dataOperacao":"2026-10-08T08:00:00","quantidadeLitros":100,
                                 "chaveIdempotencia":"agua-api-1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/agua/operacoes/30"))
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.recursoDestinoId").value(11));
    }

    @Test
    void requestInvalidoNaoChegaAoServico() throws Exception {
        mvc.perform(post("/api/v1/agua/operacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CONSUMO\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
