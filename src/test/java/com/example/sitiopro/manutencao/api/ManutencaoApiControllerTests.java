package com.example.sitiopro.manutencao.api;

import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.StatusAtivoPatrimonial;
import com.example.sitiopro.manutencao.entity.TipoAtivoPatrimonial;
import com.example.sitiopro.manutencao.entity.TipoLeituraMedidor;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.manutencao.service.ManutencaoPreventivaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManutencaoApiControllerTests {

    private final ManutencaoService service = mock(ManutencaoService.class);
    private final ManutencaoPreventivaService preventiva = mock(ManutencaoPreventivaService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ManutencaoApiController(service, preventiva)).build();
    }

    @Test
    void dashboardRetornaReadModelSemExporEntidades() throws Exception {
        when(service.dashboard()).thenReturn(
                new ManutencaoDashboardResumo(8, 2, 1, 3, new BigDecimal("450.75"), List.of()));

        mvc.perform(get("/api/v1/manutencao"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ativos").value(8))
                .andExpect(jsonPath("$.manutencoesVencidas").value(1))
                .andExpect(jsonPath("$.custosUltimos30Dias").value(450.75));
    }

    @Test
    void cadastroValidoRetornaCreatedELocation() throws Exception {
        var criado = new AtivoPatrimonialResumo(12L, "PAT-2026-0001", "Motobomba",
                TipoAtivoPatrimonial.BOMBA, null, null, null, null, null, null,
                null, null, StatusAtivoPatrimonial.ATIVO, null, 0, null, null);
        when(service.criarAtivo(argThat(request -> request.getNome().equals("Motobomba")
                && request.getTipo() == TipoAtivoPatrimonial.BOMBA
                && request.getChaveIdempotencia().equals("pat-api-1")))).thenReturn(criado);

        mvc.perform(post("/api/v1/manutencao/ativos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Motobomba",
                                  "tipo": "BOMBA",
                                  "status": "ATIVO",
                                  "chaveIdempotencia": "pat-api-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/manutencao/ativos/12"))
                .andExpect(jsonPath("$.codigo").value("PAT-2026-0001"));
    }

    @Test
    void leituraDeMedidorUsaDtoERetornaCreated() throws Exception {
        var criada = new LeituraMedidorResumo(30L, 12L, LocalDateTime.of(2026,10,6,8,0),
                new BigDecimal("125.50"), null, TipoLeituraMedidor.OPERACIONAL, null, 0, null, "operador");
        when(preventiva.registrarLeitura(argThat((LeituraMedidorRequest r) -> r.getAtivoId().equals(12L)
                && r.getHorimetro().compareTo(new BigDecimal("125.50")) == 0), any())).thenReturn(criada);

        mvc.perform(post("/api/v1/manutencao/leituras")
                .principal(new TestingAuthenticationToken("operador", "", "ROLE_OPERADOR"))
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"ativoId":12,"dataLeitura":"2026-10-06T08:00:00","horimetro":125.50,
                 "chaveIdempotencia":"leitura-api-1"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/manutencao/leituras/30"))
                .andExpect(jsonPath("$.tipoLeitura").value("OPERACIONAL"));
    }
}
