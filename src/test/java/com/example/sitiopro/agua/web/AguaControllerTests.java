package com.example.sitiopro.agua.web;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import com.example.sitiopro.agua.service.AguaService;
import com.example.sitiopro.propriedade.entity.TipoRecursoHidrico;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AguaControllerTests {
    AguaService service = mock(AguaService.class);
    MockMvc mvc;
    RecursoHidricoOperacionalResumo recurso;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AguaController(service,
                Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC))).build();
        recurso = recurso();
        when(service.dashboard()).thenReturn(new AguaDashboardResumo(1, 1, 0, 0, 0, 0,
                List.of(recurso), List.of(), List.of(), List.of()));
        when(service.listarRecursos()).thenReturn(List.of(recurso));
        when(service.detalharRecurso(10L)).thenReturn(recurso);
        when(service.listarOperacoesDoRecurso(10L)).thenReturn(List.of());
        when(service.listarOperacoes(anyInt(), anyInt())).thenReturn(new PaginaResponse<>(List.of(), 0, 30, 0, 0));
    }

    @Test
    void dashboardERecursosRenderizamReadModels() throws Exception {
        mvc.perform(get("/sitio/agua")).andExpect(status().isOk())
                .andExpect(view().name("agua/dashboard")).andExpect(model().attributeExists("resumo"));
        mvc.perform(get("/sitio/agua/recursos/10")).andExpect(status().isOk())
                .andExpect(view().name("agua/recursos/detalhe"))
                .andExpect(model().attributeExists("recurso", "operacoes"));
    }

    @Test
    void formularioUsaDtoExplicitoEChaveGeradaNoBackend() throws Exception {
        mvc.perform(get("/sitio/agua/registros/novo").param("recursoId", "10"))
                .andExpect(status().isOk()).andExpect(view().name("agua/operacoes/form"))
                .andExpect(model().attributeExists("operacaoForm", "tiposOperacao", "recursos"))
                .andExpect(model().attribute("operacaoForm", org.hamcrest.Matchers.hasProperty(
                        "chaveIdempotencia", org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankOrNullString()))));
    }

    @Test
    void postRegistraOperacaoERedirecionaAoRecurso() throws Exception {
        OperacaoAguaResumo criada = new OperacaoAguaResumo(30L, TipoOperacaoAgua.MEDICAO_NIVEL,
                LocalDateTime.of(2026, 10, 8, 8, 0), 10L, "Caixa", null, null,
                new BigDecimal("500"), null, new BigDecimal("500"), null, null,
                null, 0, null, "operador");
        when(service.registrar(any(), any())).thenReturn(criada);

        mvc.perform(post("/sitio/agua/registros")
                        .principal(new TestingAuthenticationToken("operador", "", "ROLE_OPERADOR"))
                        .param("recursoOrigemId", "10").param("tipo", "MEDICAO_NIVEL")
                        .param("dataOperacao", "2026-10-08T08:00").param("quantidadeLitros", "500")
                        .param("chaveIdempotencia", "agua-web-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/agua/recursos/10"));
    }

    private RecursoHidricoOperacionalResumo recurso() {
        return new RecursoHidricoOperacionalResumo(10L, "Caixa", TipoRecursoHidrico.CAIXA_DAGUA, true,
                new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("50"),
                new BigDecimal("200"), false, true, 7, null,
                LocalDateTime.of(2026, 10, 15, 8, 0), false, null, 0);
    }
}
