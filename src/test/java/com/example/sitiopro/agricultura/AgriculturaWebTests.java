package com.example.sitiopro.agricultura;

import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.entity.*;
import com.example.sitiopro.agricultura.service.*;
import com.example.sitiopro.agricultura.web.AgriculturaController;
import com.example.sitiopro.agricultura.api.AgriculturaApiController;
import com.example.sitiopro.planejamento.controller.AgriculturaPlanejamentoController;
import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.StatusCrs;
import com.example.sitiopro.usuario.security.SecurityConfig;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers={AgriculturaController.class,AgriculturaApiController.class,AgriculturaPlanejamentoController.class})
@Import(SecurityConfig.class)
class AgriculturaWebTests {
    @Autowired MockMvc mvc;
    @MockBean AgriculturaService service;
    @MockBean AgriculturaFichaService fichas;
    @MockBean EstoqueCatalogoService estoque;
    @MockBean TarefaService tarefas;
    @MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean org.springframework.data.jpa.mapping.JpaMetamodelMappingContext metamodel;
    LocalDate data=LocalDate.of(2026,8,1);
    SafraResumo safra=new SafraResumo(2L,1L,"Safra MVC",2026,2026,data,null,StatusSafra.EM_ANDAMENTO,"Nota",0);
    CulturaResumo cultura=new CulturaResumo(4L,"Milho",null,100,true,null,null,null,0);
    CultivoResumo cultivo=new CultivoResumo(5L,2L,"Safra MVC",3L,"TL-0003","Talhao MVC",4L,"Milho",BigDecimal.ONE,
            data,null,data.plusDays(100),null,StatusCultivo.PLANEJADO,"Nota",0);
    OcorrenciaResumo ocorrencia=new OcorrenciaResumo(12L,5L,"Milho",data.atTime(10,0),
            TipoOcorrenciaCultivo.PRAGA,SeveridadeOcorrencia.ALTA,"Lagartas observadas",BigDecimal.ONE,
            null,null,false,null,"op");
    @BeforeEach void preparar() {
        when(service.painel()).thenReturn(new AgriculturaResumo(safra,1,BigDecimal.ONE,List.of(),List.of(),List.of()));
        when(service.mapaOperacional()).thenReturn(mapaOperacional());
        when(service.listarSafras(anyInt())).thenReturn(new PaginaResponse<>(List.of(safra),0,20,1,1));
        when(service.listarCulturas(anyInt())).thenReturn(new PaginaResponse<>(List.of(cultura),0,20,1,1));
        when(service.listarCultivos(anyInt())).thenReturn(new PaginaResponse<>(List.of(cultivo),0,20,1,1));
        when(service.listarColheitas(anyInt())).thenReturn(new PaginaResponse<>(List.of(),0,20,0,0));
        when(service.listarAdubacoes(anyInt())).thenReturn(new PaginaResponse<>(List.of(),0,20,0,0));
        when(service.listarIrrigacoes(anyInt())).thenReturn(new PaginaResponse<>(List.of(),0,20,0,0));
        when(service.listarTratamentos(anyInt())).thenReturn(new PaginaResponse<>(List.of(),0,20,0,0));
        when(service.listarOcorrencias(anyInt())).thenReturn(new PaginaResponse<>(List.of(),0,20,0,0));
        when(service.detalharOcorrencia(12L)).thenReturn(new OcorrenciaDetalhe(ocorrencia,List.of(),List.of()));
        var encerramento=new EncerramentoOcorrenciaRequest(); encerramento.setDataHora(data.atTime(12,0));
        encerramento.setVersao(ocorrencia.versao());
        when(service.novoEncerramentoOcorrencia(ocorrencia.versao())).thenReturn(encerramento);
        when(service.formularioOcorrencia(12L)).thenReturn(new OcorrenciaAtualizacaoRequest());
        when(service.detalharSafra(2L)).thenReturn(safra); when(service.detalharCultura(4L)).thenReturn(cultura);
        when(service.detalharCultivo(5L)).thenReturn(cultivo);
        when(service.formularioSafra(2L)).thenReturn(new SafraRequest());
        when(service.formularioCultura(4L)).thenReturn(new CulturaRequest());
        when(service.formularioCultivo(5L)).thenReturn(new CultivoRequest());
        when(service.salvarSafra(any(),any())).thenReturn(safra);
        when(service.salvarCultura(any(),any())).thenReturn(cultura);
        when(service.salvarCultivo(any(),any())).thenReturn(cultivo);
        when(fichas.detalhar(5L)).thenReturn(new CultivoDetalhe(cultivo,List.of(),List.of(),List.of(),List.of(),ClimaResumo.naoSincronizado()));
    }

    MapaOperacionalAgriculturaResumo mapaOperacional() {
        var talhao = TalhaoMapaResumo.de(3L, "TL-0003", "Talhao MVC", BigDecimal.ONE,
                new BigDecimal("10000.0000"), true, List.of());
        var cultivoMapa = new CultivoMapaResumo(5L, "Milho", "Safra MVC", BigDecimal.ONE,
                data, data.plusDays(100), StatusCultivo.EM_DESENVOLVIMENTO, 1, SeveridadeOcorrencia.ALTA);
        return new MapaOperacionalAgriculturaResumo("SITIOPRO_AGRICULTURA_MAPA_OPERACIONAL", "local",
                StatusCrs.CONFIRMADO, true, "EPSG:4674", "SIRGAS 2000", List.of(), List.of(),
                List.of(TalhaoOperacionalMapaResumo.de(talhao, cultivoMapa)));
    }

    @ParameterizedTest @ValueSource(strings={"ADMIN","OPERADOR"})
    void ambosConsultamPainelEMvcApi(String role) throws Exception {
        mvc.perform(get("/sitio/agricultura").with(user("leitor").roles(role))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Safra MVC")))
                .andExpect(content().string(containsString("Mapa operacional")))
                .andExpect(content().string(containsString("/api/v1/agricultura/mapa")))
                .andExpect(content().string(containsString("proj4@")))
                .andExpect(content().string(containsString("data-ol-map")));
        mvc.perform(get("/api/v1/agricultura/resumo").with(user("leitor").roles(role))).andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivosAtivos").value(1)).andExpect(jsonPath("$.areaCultivadaHa").value(1));
        mvc.perform(get("/api/v1/agricultura/mapa").with(user("leitor").roles(role))).andExpect(status().isOk())
                .andExpect(jsonPath("$.formato").value("SITIOPRO_AGRICULTURA_MAPA_OPERACIONAL"))
                .andExpect(jsonPath("$.crs").value("EPSG:4674"))
                .andExpect(jsonPath("$.talhoes[0].cultivoAtivo.id").value(5))
                .andExpect(jsonPath("$.talhoes[0].cultivoAtivo.ocorrenciasAbertas").value(1))
                .andExpect(jsonPath("$.talhoes[0].cultivoAtivo.severidadeMaisAlta").value("ALTA"))
                .andExpect(jsonPath("$.talhoes[0].cultivo").doesNotExist());
    }
    @ParameterizedTest @ValueSource(strings={"safras","culturas","cultivos","colheitas","adubacao","irrigacao","tratamentos","ocorrencias"})
    void listasRenderizamParaOperador(String secao) throws Exception {
        mvc.perform(get("/sitio/agricultura/"+secao).with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
    }
    @ParameterizedTest @ValueSource(strings={"safras","culturas","cultivos"})
    void catalogosAdminComCsrfESemCamposInternos(String secao) throws Exception {
        mvc.perform(get("/sitio/agricultura/"+secao+"/novo").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("name=\"propriedadeId\""))))
                .andExpect(content().string(not(containsString("name=\"criadoPor\""))));
        mvc.perform(get("/sitio/agricultura/"+secao+"/novo").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/agricultura/"+secao).with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/agricultura/"+secao).with(user("op").roles("OPERADOR")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agricultura/"+secao).with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @ParameterizedTest @CsvSource({"safras,2","culturas,4","cultivos,5"})
    void fichasEEdicoesRenderizam(String secao,String id) throws Exception {
        mvc.perform(get("/sitio/agricultura/"+secao+"/"+id).with(user("op").roles("OPERADOR"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/agricultura/"+secao+"/"+id+"/editar").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/sitio/agricultura/"+secao+"/"+id+"/editar").with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
    }
    @ParameterizedTest @ValueSource(strings={"plantios","acompanhamentos","colheitas","adubacoes","irrigacoes","tratamentos","ocorrencias","tarefas"})
    void formulariosDeCampoDisponiveisParaOperadorECsrfObrigatorio(String operacao) throws Exception {
        mvc.perform(get("/sitio/agricultura/cultivos/5/"+operacao+"/novo").with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/sitio/agricultura/cultivos/5/"+operacao).with(user("op").roles("OPERADOR"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agricultura/cultivos/5/"+operacao).with(user("op").roles("OPERADOR"))
                .contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void fichaRenderizaClimaDegradadoESemSecrets() throws Exception {
        mvc.perform(get("/sitio/agricultura/cultivos/5").with(user("op").roles("OPERADOR"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Clima indispon")))
                .andExpect(content().string(not(containsString("jdbc:sqlserver"))))
                .andExpect(content().string(not(containsString("name=\"password\""))))
                .andExpect(content().string(not(containsString("/cultivos/5/status"))));
    }
    @Test void fichaRenderizaHistoricosComEscaping() throws Exception {
        var planted=new CultivoResumo(5L,2L,"Safra",3L,"TL-0003","Campo",4L,"Milho",BigDecimal.ONE,data,20L,data.plusDays(100),null,StatusCultivo.IMPLANTADO,null,1);
        var p=new PlantioResumo(6L,data,"Manual",BigDecimal.TEN,"kg",null,OrigemPlantio.EXTERNA,"Fornecedor",null,null,"op");
        var a=new AcompanhamentoResumo(7L,data.atTime(10,0),TipoAcompanhamentoCultivo.GERAL,"<script>alert(1)</script>",null,"op");
        var h=new ColheitaResumo(8L,5L,"Milho",data.plusDays(20),BigDecimal.TEN,"kg","A",BigDecimal.ONE,false,null,"op");
        when(fichas.detalhar(5L)).thenReturn(new CultivoDetalhe(planted,List.of(p),List.of(a),List.of(h),List.of(),ClimaResumo.naoSincronizado()));
        mvc.perform(get("/sitio/agricultura/cultivos/5").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }
    @Test void culturaPostSalvaPorDto() throws Exception {
        mvc.perform(post("/sitio/agricultura/culturas").with(user("admin").roles("ADMIN")).with(csrf())
                .param("nomeComum","Milho").param("id","999").param("criadoPor","FORCADO"))
                .andExpect(redirectedUrl("/sitio/agricultura/culturas/4"));
        verify(service).salvarCultura(isNull(),argThat(r -> r.getNomeComum().equals("Milho")));
    }
    @Test void cultivoPostNaoPermiteForcarEstadoOuPropriedade() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos").with(user("admin").roles("ADMIN")).with(csrf())
                .param("safraId","2").param("talhaoId","3").param("culturaId","4").param("areaCultivadaHa","1")
                .param("dataPlantio","2026-08-01").param("status","COLHIDO").param("propriedadeId","999")
                .param("dataColheitaReal","2026-08-02").param("revisaoOperacoes","999"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).salvarCultivo(isNull(),argThat(r -> r.getTalhaoId().equals(3L)));
    }
    @Test void cultivoInvalidoMantemFormularioEValores() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos").with(user("admin").roles("ADMIN")).with(csrf())
                .param("safraId","2").param("culturaId","4").param("areaCultivadaHa","0").param("dataPlantio","2026-08-01"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form","talhaoId","areaCultivadaHa"));
        verify(service,never()).salvarCultivo(any(),any());
    }
    @Test void plantioExternoOperadorComCsrfFunciona() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/plantios").with(user("op").roles("OPERADOR")).with(csrf())
                .param("data","2026-08-01").param("quantidade","10").param("unidade","kg").param("descricaoOrigem","Externa").param("versao","0"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).registrarPlantio(eq(5L),any());
    }
    @Test void acompanhamentoOperadorComCsrfFunciona() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/acompanhamentos").with(user("op").roles("OPERADOR")).with(csrf())
                .param("dataHora","2026-08-02T10:30").param("tipo","GERAL").param("descricao","Tudo bem").param("versao","0").param("criadoPor","FORCADO"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).registrarAcompanhamento(eq(5L),any());
    }
    @Test void colheitaOperadorComCsrfFunciona() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/colheitas").with(user("op").roles("OPERADOR")).with(csrf())
                .param("data","2026-08-20").param("quantidade","10").param("unidade","kg").param("versao","0"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).registrarColheita(eq(5L),any());
    }
    @Test void operacoesDeCampoUsamDtosPermitidosECsrf() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/adubacoes").with(user("op").roles("OPERADOR")).with(csrf())
                .param("data","2026-08-02").param("produto","Composto").param("quantidade","2.5").param("unidade","kg")
                .param("origem","EXTERNA").param("descricaoOrigem","Fornecedor").param("chaveIdempotencia","adubacao-mvc-001")
                .param("versao","0").param("cultivo.id","999").param("movimentoEstoque.id","999"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        mvc.perform(post("/sitio/agricultura/cultivos/5/irrigacoes").with(user("op").roles("OPERADOR")).with(csrf())
                .param("dataHora","2026-08-02T10:30").param("duracaoMinutos","30")
                .param("chaveIdempotencia","irrigacao-mvc-001").param("versao","0"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        mvc.perform(post("/sitio/agricultura/cultivos/5/tratamentos").with(user("op").roles("OPERADOR")).with(csrf())
                .param("data","2026-08-02").param("finalidade","Controle observado").param("produtoAplicado","Produto")
                .param("quantidade","1").param("unidade","L").param("origem","EXTERNA").param("descricaoOrigem","Fornecedor")
                .param("chaveIdempotencia","tratamento-mvc-001").param("versao","0"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        mvc.perform(post("/sitio/agricultura/cultivos/5/ocorrencias").with(user("op").roles("OPERADOR")).with(csrf())
                .param("dataHora","2026-08-02T10:30").param("tipo","DANO_CLIMATICO").param("severidade","MEDIA")
                .param("titulo","Dano por vento").param("descricao","Dano observado")
                .param("chaveIdempotencia","ocorrencia-mvc-001").param("versao","0"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).registrarAdubacao(eq(5L),argThat(r -> r.getProduto().equals("Composto")));
        verify(service).registrarIrrigacao(eq(5L),any()); verify(service).registrarTratamento(eq(5L),any());
        verify(service).registrarOcorrencia(eq(5L),any());
    }
    @Test void apiOperacoesDeCampoAceitamDtoExplicito() throws Exception {
        mvc.perform(post("/api/v1/agricultura/cultivos/5/irrigacoes").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{\"dataHora\":\"2026-08-02T10:30:00\",\"duracaoMinutos\":30,\"chaveIdempotencia\":\"irrigacao-api-001\",\"versao\":0,\"cultivoId\":999}"))
                .andExpect(status().isCreated());
        verify(service).registrarIrrigacao(eq(5L),argThat(r -> r.getDuracaoMinutos()==30));
    }
    @Test void ocorrenciaDetalheEEdicaoFuncionamComAgrofitLocalDegradado() throws Exception {
        mvc.perform(get("/sitio/agricultura/ocorrencias/12").with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Lagartas observadas")))
                .andExpect(content().string(containsString("Sem refer")))
                .andExpect(content().string(not(containsString("api_key"))))
                .andExpect(content().string(not(containsString("name=\"password\""))));
        mvc.perform(get("/sitio/agricultura/ocorrencias/12/editar").with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("name=\"status\""))))
                .andExpect(content().string(not(containsString("name=\"cultivoId\""))));
    }
    @Test void atualizacaoEncerramentoETarefaProtegemCsrfEMassAssignment() throws Exception {
        mvc.perform(post("/sitio/agricultura/ocorrencias/12").with(user("op").roles("OPERADOR")).with(csrf())
                .param("tipo","DOENCA").param("severidade","CRITICA").param("titulo","Lesoes")
                .param("descricao","Lesoes em expansao").param("acompanhamento","Nova vistoria")
                .param("chaveIdempotencia","ocorrencia-update-mvc").param("versao","0")
                .param("status","ENCERRADA").param("resolucao","FORCADA").param("cultivo.id","999"))
                .andExpect(redirectedUrl("/sitio/agricultura/ocorrencias/12"));
        verify(service).atualizarOcorrencia(eq(12L),argThat(r -> r.getTitulo().equals("Lesoes")));
        mvc.perform(post("/sitio/agricultura/ocorrencias/12/encerrar").with(user("op").roles("OPERADOR")).with(csrf())
                .param("dataHora","2026-08-03T10:30").param("resolucao","Foco estabilizado")
                .param("chaveIdempotencia","ocorrencia-close-mvc").param("versao","0")
                .param("severidade","BAIXA"))
                .andExpect(redirectedUrl("/sitio/agricultura/ocorrencias/12"));
        verify(service).encerrarOcorrencia(eq(12L),argThat(r -> r.getResolucao().equals("Foco estabilizado")));
        mvc.perform(post("/sitio/agricultura/ocorrencias/12/tarefa-inspecao")
                .with(user("op").roles("OPERADOR")).with(csrf()))
                .andExpect(redirectedUrl("/sitio/agricultura/ocorrencias/12"));
        verify(service).criarTarefaInspecao(eq(12L),argThat(a -> a.login().equals("op")));
        mvc.perform(post("/sitio/agricultura/ocorrencias/12/encerrar").with(user("op").roles("OPERADOR")))
                .andExpect(status().isForbidden());
    }
    @Test void apiOcorrenciaUsaDtosParaConsultaAtualizacaoEEncerramento() throws Exception {
        when(service.atualizarOcorrencia(eq(12L),any())).thenReturn(ocorrencia);
        when(service.encerrarOcorrencia(eq(12L),any())).thenReturn(ocorrencia);
        mvc.perform(get("/api/v1/agricultura/ocorrencias/12").with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resumo.id").value(12))
                .andExpect(jsonPath("$.resumo.cultivo").doesNotExist());
        mvc.perform(put("/api/v1/agricultura/ocorrencias/12").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{\"tipo\":\"DOENCA\",\"severidade\":\"ALTA\",\"titulo\":\"Lesoes\",\"descricao\":\"Campo\",\"acompanhamento\":\"Vistoria\",\"chaveIdempotencia\":\"ocorrencia-update-api\",\"versao\":0,\"status\":\"ENCERRADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(12));
        mvc.perform(post("/api/v1/agricultura/ocorrencias/12/encerrar").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{\"dataHora\":\"2026-08-03T10:30:00\",\"resolucao\":\"Estabilizada\",\"chaveIdempotencia\":\"ocorrencia-close-api\",\"versao\":0,\"severidade\":\"BAIXA\"}"))
                .andExpect(status().isOk());
    }
    @Test void tarefaOperadorMantemContextoCalculado() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/tarefas").with(user("op").roles("OPERADOR")).with(csrf())
                .param("titulo","Capina").param("moduloOrigem","CRIACOES").param("referenciaOrigem","INC:999"))
                .andExpect(redirectedUrl("/sitio/agricultura/cultivos/5"));
        verify(service).criarTarefa(eq(5L),any(),argThat(a -> !a.admin() && a.login().equals("op")));
    }
    @Test void statusExigeAdminEProtegeCsrf() throws Exception {
        mvc.perform(post("/sitio/agricultura/cultivos/5/status").with(user("op").roles("OPERADOR")).with(csrf())
                .param("status","CANCELADO").param("versao","0")).andExpect(status().isForbidden());
        mvc.perform(post("/sitio/agricultura/cultivos/5/status").with(user("admin").roles("ADMIN"))
                .param("status","CANCELADO").param("versao","0")).andExpect(status().isForbidden());
    }
    @ParameterizedTest @ValueSource(strings={"safras","culturas","cultivos"})
    void apiConsultaDtoEPagina(String secao) throws Exception {
        mvc.perform(get("/api/v1/agricultura/"+secao).with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conteudo[0].id").isNumber())
                .andExpect(jsonPath("$.conteudo[0].hibernateLazyInitializer").doesNotExist());
    }
    @Test void apiDetalheNaoExpoeEntidades() throws Exception {
        mvc.perform(get("/api/v1/agricultura/cultivos/5").with(user("op").roles("OPERADOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resumo.talhaoId").value(3))
                .andExpect(jsonPath("$.resumo.talhao").doesNotExist()).andExpect(jsonPath("$.clima.disponivel").value(false));
    }
    @Test void apiCultivoExigeTalhaoEAceitaDtoValido() throws Exception {
        mvc.perform(post("/api/v1/agricultura/cultivos").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"safraId\":2,\"culturaId\":4,\"dataPlantio\":\"2026-08-01\",\"areaCultivadaHa\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/agricultura/cultivos").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType("application/json").content("{\"safraId\":2,\"talhaoId\":3,\"culturaId\":4,\"dataPlantio\":\"2026-08-01\",\"areaCultivadaHa\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(5));
    }
    @Test void apiOperacaoConflitanteRetorna409SemStacktrace() throws Exception {
        when(service.registrarColheita(eq(5L),any())).thenThrow(new AgriculturaOperacaoException("Recarregue",org.springframework.http.HttpStatus.CONFLICT));
        mvc.perform(post("/api/v1/agricultura/cultivos/5/colheitas").with(user("op").roles("OPERADOR")).with(csrf())
                .contentType("application/json").content("{\"data\":\"2026-08-20\",\"quantidade\":10,\"unidade\":\"kg\",\"versao\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("AGRICULTURA_OPERACAO_INVALIDA"));
    }
    @ParameterizedTest @CsvSource({
        "areas,/sitio/propriedade/talhoes","areas/novo,/sitio/propriedade/talhoes",
        "areas/detalhe,/sitio/propriedade/talhoes","areas/historico,/sitio/propriedade/talhoes",
        "plantios,/sitio/agricultura/cultivos","plantios/novo,/sitio/agricultura/cultivos",
        "plantios/detalhe,/sitio/agricultura/cultivos","plantios/historico,/sitio/agricultura/cultivos",
        "culturas/detalhe,/sitio/agricultura/culturas","culturas/historico,/sitio/agricultura/culturas",
        "colheitas/novo,/sitio/agricultura/colheitas","colheitas/detalhe,/sitio/agricultura/colheitas",
        "colheitas/historico,/sitio/agricultura/colheitas"})
    void atalhosLegadosPreservados(String origem,String destino) throws Exception {
        mvc.perform(get("/sitio/agricultura/"+origem).with(user("admin").roles("ADMIN"))).andExpect(redirectedUrl(destino));
    }
    @Test void autenticacaoObrigatoria() throws Exception {
        mvc.perform(get("/sitio/agricultura")).andExpect(status().is3xxRedirection());
    }
}
