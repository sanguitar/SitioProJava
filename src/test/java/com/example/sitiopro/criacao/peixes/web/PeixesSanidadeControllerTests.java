package com.example.sitiopro.criacao.peixes.web;

import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.service.*;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.*;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PeixesSanidadeControllerTests {
    PeixesSanidadeService sanidade = mock(PeixesSanidadeService.class);
    PeixesService peixes = mock(PeixesService.class);
    EstoqueCatalogoService estoque = mock(EstoqueCatalogoService.class);
    MockMvc mvc;

    @BeforeEach void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new PeixesSanidadeController(sanidade, peixes, estoque,
                Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC))).build();
        when(sanidade.listar(any())).thenReturn(List.of());
        when(sanidade.resumoOperacional()).thenReturn(SanidadePeixesResumo.vazio());
        when(peixes.listar(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 100, 0, 0));
        when(estoque.listarItensAtivos()).thenReturn(List.of());
        when(estoque.listarLocaisAtivos()).thenReturn(List.of());
    }

    @Test void listaHistoricoPorLote() throws Exception {
        mvc.perform(get("/sitio/criacoes/peixes/sanidade").param("loteId", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("criacoes/peixes/sanidade/lista"))
                .andExpect(model().attribute("loteId", 10L));
        verify(sanidade).listar(10L);
    }

    @Test void formularioUsaDtoExplicitoComChaveGerada() throws Exception {
        mvc.perform(get("/sitio/criacoes/peixes/sanidade/novo").param("loteId", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("criacoes/peixes/sanidade/form"))
                .andExpect(model().attributeExists("registroForm", "tipos", "lotes",
                        "itensEstoque", "locaisEstoque"));
    }
}
