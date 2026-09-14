package com.example.sitiopro.propriedade;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.service.*;
import com.example.sitiopro.propriedade.web.PropriedadeController;
import com.example.sitiopro.propriedade.api.PropriedadeApiController;
import com.example.sitiopro.usuario.security.SecurityConfig;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {PropriedadeController.class, PropriedadeApiController.class})
@Import(SecurityConfig.class)
class PropriedadeWebTests {
    @Autowired MockMvc mvc;
    @MockBean PropriedadeService service;
    @MockBean PerimetroService perimetros;
    @MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;
    CadastroFisicoResumo registro = new CadastroFisicoResumo(7L,42L,null,null,null,"Nome cadastrado","OUTRO",
            null,null,null,null,"Observação",true,"ATIVO",0);
    @BeforeEach void dados() {
        when(perimetros.obter()).thenReturn(PerimetroResumo.vazio());
        var perimetroForm = new PerimetroRequest(); perimetroForm.setVersao(-1L);
        when(perimetros.formulario()).thenReturn(perimetroForm);
        when(perimetros.salvar(any())).thenReturn(PerimetroResumo.vazio());
        when(service.resumo()).thenReturn(new PropriedadeResumo(42L,"Sítio MVC",null,null,null,null,null,null,true,0,0,0,0,0,0));
        var p=new PropriedadeRequest(); p.setNome("Sítio MVC"); p.setVersao(0L);
        when(service.formulario()).thenReturn(p);
        when(service.listarAreaPropriedade(anyInt(),anyInt())).thenReturn(new PaginaResponse<>(List.of(registro),0,20,1,1));
        when(service.detalharAreaPropriedade(7L)).thenReturn(registro);
        when(service.formularioAreaPropriedade(7L)).thenReturn(new AreaPropriedadeRequest());
        when(service.salvarAreaPropriedade(any(),any())).thenReturn(registro);
        when(service.listarTalhao(anyInt(),anyInt())).thenReturn(new PaginaResponse<>(List.of(registro),0,20,1,1));
        when(service.detalharTalhao(7L)).thenReturn(registro);
        when(service.formularioTalhao(7L)).thenReturn(new TalhaoRequest());
        when(service.salvarTalhao(any(),any())).thenReturn(registro);
        when(service.listarPiquete(anyInt(),anyInt())).thenReturn(new PaginaResponse<>(List.of(registro),0,20,1,1));
        when(service.detalharPiquete(7L)).thenReturn(registro);
        when(service.formularioPiquete(7L)).thenReturn(new PiqueteRequest());
        when(service.salvarPiquete(any(),any())).thenReturn(registro);
        when(service.listarEstruturaPropriedade(anyInt(),anyInt())).thenReturn(new PaginaResponse<>(List.of(registro),0,20,1,1));
        when(service.detalharEstruturaPropriedade(7L)).thenReturn(registro);
        when(service.formularioEstruturaPropriedade(7L)).thenReturn(new EstruturaPropriedadeRequest());
        when(service.salvarEstruturaPropriedade(any(),any())).thenReturn(registro);
        when(service.listarRecursoHidrico(anyInt(),anyInt())).thenReturn(new PaginaResponse<>(List.of(registro),0,20,1,1));
        when(service.detalharRecursoHidrico(7L)).thenReturn(registro);
        when(service.formularioRecursoHidrico(7L)).thenReturn(new RecursoHidricoRequest());
        when(service.salvarRecursoHidrico(any(),any())).thenReturn(registro);
    }
    @ParameterizedTest @ValueSource(strings={"ADMIN","OPERADOR"})
    void perimetroConsultaMvcApi(String role) throws Exception {
        mvc.perform(get("/sitio/propriedade/perimetro").with(user("leitor").roles(role)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("NÃO CONFIRMADO")))
                .andExpect(content().string(containsString("data-perimeter-map")))
                .andExpect(content().string(containsString("Exportar para QGIS")))
                .andExpect(content().string(containsString("/js/propriedade.js")));
        mvc.perform(get("/api/v1/propriedade/perimetro").with(user("leitor").roles(role)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statusCrs").value("NAO_CONFIRMADO"))
                .andExpect(jsonPath("$.quantidadeVertices").value(0))
                .andExpect(jsonPath("$.mapa.formato").value("SITIOPRO_PERIMETRO_OPERACIONAL"))
                .andExpect(jsonPath("$.mapa.vertices", hasSize(0)))
                .andExpect(jsonPath("$.mapa.poligonoFechado", hasSize(0)))
                .andExpect(jsonPath("$.mapa.crsConfirmado").value(false))
                .andExpect(jsonPath("$.geoJson.type").value("Feature"))
                .andExpect(jsonPath("$.geoJson.geometry").value(nullValue()));
    }
    @Test void perimetroApiMapaPreservaOrdemEFechaPoligonoComTresVertices() throws Exception {
        when(perimetros.obter()).thenReturn(new PerimetroResumo(9L, 0,
                com.example.sitiopro.propriedade.entity.StatusCrs.NAO_CONFIRMADO, null, null, null,
                List.of(new PerimetroResumo.Vertice(1, new BigDecimal("-23.1"), new BigDecimal("-45.1"), "A", null),
                        new PerimetroResumo.Vertice(2, new BigDecimal("-23.2"), new BigDecimal("-45.2"), "B", null),
                        new PerimetroResumo.Vertice(3, new BigDecimal("-23.3"), new BigDecimal("-45.3"), "C", null)),
                null, null));
        mvc.perform(get("/api/v1/propriedade/perimetro").with(user("leitor").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapa.statusCrs").value("NAO_CONFIRMADO"))
                .andExpect(jsonPath("$.mapa.vertices[*].ordem", contains(1,2,3)))
                .andExpect(jsonPath("$.mapa.vertices[0].rotulo").value("1 - A"))
                .andExpect(jsonPath("$.mapa.poligonoFechado", hasSize(4)))
                .andExpect(jsonPath("$.mapa.poligonoFechado[3].ordem").value(1))
                .andExpect(jsonPath("$.geoJson.geometry.type").value("Polygon"))
                .andExpect(jsonPath("$.geoJson.geometry.coordinates[0]", hasSize(4)))
                .andExpect(jsonPath("$.geoJson.geometry.coordinates[0][0][0]").value(-45.1))
                .andExpect(jsonPath("$.geoJson.properties.statusCrs").value("NAO_CONFIRMADO"));
    }

    @ParameterizedTest @ValueSource(strings={"ADMIN","OPERADOR"})
    void perimetroExportacaoQgisPreservaConteudoOrdemPrecisaoEPermissoes(String role) throws Exception {
        when(perimetros.obter()).thenReturn(new PerimetroResumo(9L, 0,
                com.example.sitiopro.propriedade.entity.StatusCrs.CONFIRMADO, "EPSG:4674", "SIRGAS 2000", null,
                List.of(new PerimetroResumo.Vertice(1, new BigDecimal("-8.346821111"),
                                new BigDecimal("-63.871070000"), new BigDecimal("90.30"), "DZCZ-M-0205", null),
                        new PerimetroResumo.Vertice(2, new BigDecimal("-8.350538889"),
                                new BigDecimal("-63.871139722"), new BigDecimal("89.88"), "DZCZ-M-0168", null),
                        new PerimetroResumo.Vertice(3, new BigDecimal("-8.350611389"),
                                new BigDecimal("-63.871590833"), new BigDecimal("88.97"), "DZCZ-M-0167", null),
                        new PerimetroResumo.Vertice(4, new BigDecimal("-8.346856389"),
                                new BigDecimal("-63.871454722"), new BigDecimal("90.28"), "DZCZ-M-0170", null)),
                null, null));
        mvc.perform(get("/sitio/propriedade/perimetro/exportar-qgis").with(user("leitor").roles(role)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("perimetro-sirgas-2000-epsg-4674.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string("ordem,marco,longitude,latitude,altitude,epsg\r\n"
                        + "1,DZCZ-M-0205,-63.871070000,-8.346821111,90.30,4674\r\n"
                        + "2,DZCZ-M-0168,-63.871139722,-8.350538889,89.88,4674\r\n"
                        + "3,DZCZ-M-0167,-63.871590833,-8.350611389,88.97,4674\r\n"
                        + "4,DZCZ-M-0170,-63.871454722,-8.346856389,90.28,4674\r\n"));
    }

    @Test void perimetroExportacaoQgisExigeAutenticacao() throws Exception {
        mvc.perform(get("/sitio/propriedade/perimetro/exportar-qgis"))
                .andExpect(status().is3xxRedirection());
    }

    @Test void perimetroRestritoAdminECsrf() throws Exception {
        mvc.perform(get("/sitio/propriedade/perimetro/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{\"versao\":-1}")).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{\"versao\":-1}")).andExpect(status().isForbidden());
        verify(perimetros,never()).salvar(any());
    }
    @Test void perimetroFormularioAdicionaERemoveSemPersistir() throws Exception {
        mvc.perform(get("/sitio/propriedade/perimetro/editar").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .param("versao","-1").param("acao","adicionar"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"vertices[0].latitude\"")));
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .param("versao","-1").param("vertices[0].ordem","1").param("remover","0"))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("name=\"vertices[0].latitude\""))));
        verify(perimetros,never()).salvar(any());
    }
    @Test void perimetroDtoEBinderSemMassAssignment() throws Exception {
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"versao\":-1,\"propriedadeId\":999,\"id\":999}"))
                .andExpect(status().isOk());
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .param("versao","-1").param("id","999").param("propriedade.id","999").param("alteradoPor","forjado"))
                .andExpect(redirectedUrl("/sitio/propriedade/perimetro"));
        verify(perimetros,times(2)).salvar(argThat(r -> r.getVersao()==-1 && r.getStatusCrs()==com.example.sitiopro.propriedade.entity.StatusCrs.NAO_CONFIRMADO));
    }
    @Test void perimetroApiValidaCoordenadasEEnum() throws Exception {
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"versao\":-1,\"vertices\":[{\"ordem\":1,\"latitude\":91,\"longitude\":0}]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"versao\":-1,\"statusCrs\":\"WGS84\"}"))
                .andExpect(status().isBadRequest());
        verify(perimetros,never()).salvar(any());
    }
    @Test void perimetroErroDeRegraPreservaFormularioEApiRetornaConflito() throws Exception {
        doThrow(new PropriedadeOperacaoException(null,"Recarregue",org.springframework.http.HttpStatus.CONFLICT))
                .when(perimetros).salvar(any());
        mvc.perform(post("/sitio/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .param("versao","0").param("crs","Referencia informada"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Recarregue")))
                .andExpect(content().string(containsString("Referencia informada")));
        mvc.perform(put("/api/v1/propriedade/perimetro").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"versao\":0}"))
                .andExpect(status().isConflict());
    }
    @ParameterizedTest @ValueSource(strings={"ADMIN","OPERADOR"})
    void ambosConsultamResumoSemSecrets(String role) throws Exception {
        mvc.perform(get("/sitio/propriedade").with(user("leitor").roles(role))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Sítio MVC")))
                .andExpect(content().string(not(containsString("jdbc:sqlserver"))))
                .andExpect(content().string(not(containsString("name=\"password\""))));
        mvc.perform(get("/api/v1/propriedade/resumo").with(user("leitor").roles(role)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(42));
    }
    @Test void formularioDaPropriedadeEhAdminEIncluiCsrfVersao() throws Exception {
        mvc.perform(get("/sitio/propriedade/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"versao\"")))
                .andExpect(content().string(containsString("id=\"ativo\"")))
                .andExpect(content().string(not(containsString("name=\"principal\""))));
        mvc.perform(get("/sitio/propriedade/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
    }
    @Test void propriedadeExigeAdminECsrfNasMutacoes() throws Exception {
        mvc.perform(post("/sitio/propriedade").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }

    @Test void propriedadePermiteUfAindaNaoInformada() throws Exception {
        mvc.perform(post("/sitio/propriedade").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Sítio MVC").param("versao","0").param("uf",""))
                .andExpect(status().is3xxRedirection());
        verify(service).atualizar(argThat(r -> r.getUf() == null));
    }

    @Test void AreaPropriedadeTemMvcApiEFormularioSemCodigoEditavel() throws Exception {
        mvc.perform(get("/sitio/propriedade/areas").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome cadastrado")));
        mvc.perform(get("/sitio/propriedade/areas/7").with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/propriedade/areas/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"codigo\""))))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))));
        mvc.perform(get("/sitio/propriedade/areas/7/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/propriedade/areas").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].nome").value("Nome cadastrado"));
    }
    @Test void AreaPropriedadeProtegeMutacoesETelasDeEdicao() throws Exception {
        mvc.perform(get("/sitio/propriedade/areas/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/sitio/propriedade/areas/7/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/areas").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/areas/7/desativar").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/areas").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/areas").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/areas").with(user("admin").roles("ADMIN")).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/areas/7").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void AreaPropriedadeCriaPorDtoComCsrfERetornaLocation() throws Exception {
        mvc.perform(post("/api/v1/propriedade/areas").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\",\"tipo\":\"AGRICOLA\"}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/propriedade/areas/7"));
        mvc.perform(post("/sitio/propriedade/areas").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Teste").param("tipo","AGRICOLA").param("propriedadeId","999").param("codigo","FORCADO").param("id","999"))
                .andExpect(status().is3xxRedirection());
        verify(service,times(2)).salvarAreaPropriedade(isNull(),any());
    }

    @Test void TalhaoTemMvcApiEFormularioSemCodigoEditavel() throws Exception {
        mvc.perform(get("/sitio/propriedade/talhoes").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome cadastrado")));
        mvc.perform(get("/sitio/propriedade/talhoes/7").with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/propriedade/talhoes/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"codigo\""))))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))));
        mvc.perform(get("/sitio/propriedade/talhoes/7/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/propriedade/talhoes").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].nome").value("Nome cadastrado"));
    }
    @Test void TalhaoProtegeMutacoesETelasDeEdicao() throws Exception {
        mvc.perform(get("/sitio/propriedade/talhoes/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/sitio/propriedade/talhoes/7/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/talhoes").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/talhoes/7/desativar").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/talhoes").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/talhoes").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/talhoes").with(user("admin").roles("ADMIN")).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/talhoes/7").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void TalhaoCriaPorDtoComCsrfERetornaLocation() throws Exception {
        mvc.perform(post("/api/v1/propriedade/talhoes").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\",\"areaHa\":1.25}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/propriedade/talhoes/7"));
        mvc.perform(post("/sitio/propriedade/talhoes").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Teste").param("areaHa","1.25").param("propriedadeId","999").param("codigo","FORCADO").param("id","999"))
                .andExpect(status().is3xxRedirection());
        verify(service,times(2)).salvarTalhao(isNull(),any());
    }

    @Test void PiqueteTemMvcApiEFormularioSemCodigoEditavel() throws Exception {
        mvc.perform(get("/sitio/propriedade/piquetes").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome cadastrado")));
        mvc.perform(get("/sitio/propriedade/piquetes/7").with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/propriedade/piquetes/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"codigo\""))))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))));
        mvc.perform(get("/sitio/propriedade/piquetes/7/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/propriedade/piquetes").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].nome").value("Nome cadastrado"));
    }
    @Test void PiqueteProtegeMutacoesETelasDeEdicao() throws Exception {
        mvc.perform(get("/sitio/propriedade/piquetes/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/sitio/propriedade/piquetes/7/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/piquetes").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/piquetes/7/desativar").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/piquetes").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/piquetes").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/piquetes").with(user("admin").roles("ADMIN")).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/piquetes/7").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void PiqueteCriaPorDtoComCsrfERetornaLocation() throws Exception {
        mvc.perform(post("/api/v1/propriedade/piquetes").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\"}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/propriedade/piquetes/7"));
        mvc.perform(post("/sitio/propriedade/piquetes").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Teste").param("propriedadeId","999").param("codigo","FORCADO").param("id","999"))
                .andExpect(status().is3xxRedirection());
        verify(service,times(2)).salvarPiquete(isNull(),any());
    }

    @Test void EstruturaPropriedadeTemMvcApiEFormularioSemCodigoEditavel() throws Exception {
        mvc.perform(get("/sitio/propriedade/estruturas").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome cadastrado")));
        mvc.perform(get("/sitio/propriedade/estruturas/7").with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/propriedade/estruturas/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"codigo\""))))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))));
        mvc.perform(get("/sitio/propriedade/estruturas/7/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/propriedade/estruturas").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].nome").value("Nome cadastrado"));
    }
    @Test void EstruturaPropriedadeProtegeMutacoesETelasDeEdicao() throws Exception {
        mvc.perform(get("/sitio/propriedade/estruturas/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/sitio/propriedade/estruturas/7/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/estruturas").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/estruturas/7/desativar").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/estruturas").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/estruturas").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/estruturas").with(user("admin").roles("ADMIN")).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/estruturas/7").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void EstruturaPropriedadeCriaPorDtoComCsrfERetornaLocation() throws Exception {
        mvc.perform(post("/api/v1/propriedade/estruturas").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\",\"tipo\":\"GALPAO\"}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/propriedade/estruturas/7"));
        mvc.perform(post("/sitio/propriedade/estruturas").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Teste").param("tipo","GALPAO").param("propriedadeId","999").param("codigo","FORCADO").param("id","999"))
                .andExpect(status().is3xxRedirection());
        verify(service,times(2)).salvarEstruturaPropriedade(isNull(),any());
    }

    @Test void RecursoHidricoTemMvcApiEFormularioSemCodigoEditavel() throws Exception {
        mvc.perform(get("/sitio/propriedade/recursos-hidricos").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome cadastrado")));
        mvc.perform(get("/sitio/propriedade/recursos-hidricos/7").with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/propriedade/recursos-hidricos/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"codigo\""))))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))));
        mvc.perform(get("/sitio/propriedade/recursos-hidricos/7/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/propriedade/recursos-hidricos").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].nome").value("Nome cadastrado"));
    }
    @Test void RecursoHidricoProtegeMutacoesETelasDeEdicao() throws Exception {
        mvc.perform(get("/sitio/propriedade/recursos-hidricos/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(get("/sitio/propriedade/recursos-hidricos/7/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/recursos-hidricos").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/recursos-hidricos/7/desativar").with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/propriedade/recursos-hidricos").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/recursos-hidricos").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/propriedade/recursos-hidricos").with(user("admin").roles("ADMIN")).contentType("application/json").content("{}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/propriedade/recursos-hidricos/7").with(user("op").roles("OPERADOR")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void RecursoHidricoCriaPorDtoComCsrfERetornaLocation() throws Exception {
        mvc.perform(post("/api/v1/propriedade/recursos-hidricos").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\",\"tipo\":\"POCO\"}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/v1/propriedade/recursos-hidricos/7"));
        mvc.perform(post("/sitio/propriedade/recursos-hidricos").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Teste").param("tipo","POCO").param("propriedadeId","999").param("codigo","FORCADO").param("id","999"))
                .andExpect(status().is3xxRedirection());
        verify(service,times(2)).salvarRecursoHidrico(isNull(),any());
    }

    @Test void validacaoDeFormularioPreservaEntradaSemSalvar() throws Exception {
        mvc.perform(post("/sitio/propriedade/talhoes").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nome","Talhão sem área")).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form","areaHa"));
        verify(service,never()).salvarTalhao(any(),any());
    }
    @Test void apiConflitoDeVersaoRetorna409SemDetalhesSql() throws Exception {
        when(service.salvarTalhao(eq(7L),any())).thenThrow(new PropriedadeOperacaoException(null,"Recarregue os dados.",org.springframework.http.HttpStatus.CONFLICT));
        mvc.perform(put("/api/v1/propriedade/talhoes/7").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"nome\":\"Teste\",\"areaHa\":1,\"versao\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PROPRIEDADE_OPERACAO_INVALIDA"));
    }
    @Test void recursoInexistenteRetorna404NaPagina() throws Exception {
        when(service.detalharTalhao(999L)).thenThrow(new PropriedadeOperacaoException(null,"Registro não encontrado.",org.springframework.http.HttpStatus.NOT_FOUND));
        mvc.perform(get("/sitio/propriedade/talhoes/999").with(user("op").roles("OPERADOR"))).andExpect(status().isNotFound());
    }
}
