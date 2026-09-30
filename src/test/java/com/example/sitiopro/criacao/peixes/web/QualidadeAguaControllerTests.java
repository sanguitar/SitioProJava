package com.example.sitiopro.criacao.peixes.web;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.service.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.*;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QualidadeAguaControllerTests {
    QualidadeAguaService qualidade=mock(QualidadeAguaService.class);PeixesService peixes=mock(PeixesService.class);MockMvc mvc;
    @BeforeEach void setup(){mvc=MockMvcBuilders.standaloneSetup(new QualidadeAguaController(qualidade,peixes,Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"),ZoneOffset.UTC))).build();when(peixes.detalhar(1L)).thenReturn(mock(LotePeixesDetalhe.class));when(qualidade.listar(1L)).thenReturn(List.of());when(qualidade.configuracao()).thenReturn(new ConfiguracaoQualidadeAguaDto());}
    @Test void renderizaHistoricoComFormulario() throws Exception {mvc.perform(get("/sitio/criacoes/peixes/lotes/1/qualidade-agua")).andExpect(status().isOk()).andExpect(view().name("criacoes/peixes/qualidade-agua/historico")).andExpect(model().attributeExists("medicoes","medicaoForm","limites"));}
    @Test void postRegistraMedicaoEProtegeBinder() throws Exception {var auth=new UsernamePasswordAuthenticationToken("operador","n/a",List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERADOR")));mvc.perform(post("/sitio/criacoes/peixes/lotes/1/qualidade-agua").principal(auth).param("medidoEm","2026-09-30T08:00").param("temperatura","28").param("ph","7.2").param("oxigenioDissolvido","6").param("responsavel","Operador").param("chaveIdempotencia","agua-mvc").param("lote.id","999")).andExpect(status().is3xxRedirection());verify(qualidade).registrar(eq(1L),any(),any());}
}
