package com.example.sitiopro.manutencao.web;

import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.manutencao.service.ManutencaoPreventivaService;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManutencaoControllerTests {
    ManutencaoService service=mock(ManutencaoService.class);ManutencaoPreventivaService preventiva=mock(ManutencaoPreventivaService.class);PropriedadeService propriedade=mock(PropriedadeService.class);EstoqueCatalogoService estoque=mock(EstoqueCatalogoService.class);MockMvc mvc;
    @BeforeEach void preparar(){mvc=MockMvcBuilders.standaloneSetup(new ManutencaoController(service,preventiva,propriedade,estoque,Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"),ZoneOffset.UTC))).build();when(service.dashboard()).thenReturn(new ManutencaoDashboardResumo(0,0,0,0,BigDecimal.ZERO,List.of()));when(service.listarAtivos()).thenReturn(List.of());when(propriedade.listarEstruturaPropriedade(0,100)).thenReturn(new PaginaResponse<>(List.of(),0,100,0,0));when(estoque.listarItensAtivos()).thenReturn(List.of());when(estoque.listarLocaisAtivos()).thenReturn(List.of());}
    @Test void dashboardRenderizaResumo()throws Exception{mvc.perform(get("/sitio/manutencao")).andExpect(status().isOk()).andExpect(view().name("manutencao/dashboard")).andExpect(model().attributeExists("resumo"));}
    @Test void formularioDeManutencaoUsaDtoComTresLinhasDeConsumo()throws Exception{mvc.perform(get("/sitio/manutencao/registros/novo").param("ativoId","10")).andExpect(status().isOk()).andExpect(view().name("manutencao/registros/form")).andExpect(model().attributeExists("manutencaoForm","itensEstoque","locaisEstoque"));}
    @Test void formularioDeLeituraUsaDtoExplicito()throws Exception{AtivoPatrimonialResumo ativo=mock(AtivoPatrimonialResumo.class);when(service.detalharAtivo(10L)).thenReturn(ativo);mvc.perform(get("/sitio/manutencao/ativos/10/leituras/nova")).andExpect(status().isOk()).andExpect(view().name("manutencao/leituras/form")).andExpect(model().attribute("ajuste",false)).andExpect(model().attributeExists("leituraForm"));}
}
