package com.example.sitiopro;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.repository.CategoriaRepository;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.compras.service.ComprasOperacaoException;
import com.example.sitiopro.compras.service.FornecedorService;
import com.example.sitiopro.criacao.aves.dto.AvesResumo;
import com.example.sitiopro.criacao.aves.dto.AcompanhamentoIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.service.AvesAlertasService;
import com.example.sitiopro.criacao.aves.service.AvesResumoService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAvesService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAcompanhamentoService;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.aves.service.LoteAvesService;
import com.example.sitiopro.criacao.aves.service.ManejoAvesService;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoResumo;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.dashboard.service.DashboardTendenciasService;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoEstadoRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoExecucaoRepository;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.estoque.dto.EstoqueDashboardResumo;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueResponse;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.LoteEstoqueRepository;
import com.example.sitiopro.estoque.repository.MovimentoEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import com.example.sitiopro.producao.service.ProducaoService;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.tarefas.repository.EventoTarefaAlertaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRecorrenciaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.tarefas.dto.AlertaDetalhe;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaResumoOperacional;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import com.example.sitiopro.tarefas.service.SqlServerApplicationLock;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.security.UsuarioSessaoService;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static com.example.sitiopro.DashboardTestFixture.vazio;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "sitiopro.tarefas.scheduler-enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class SitioProSecurityTests {
    @MockBean private com.example.sitiopro.agricultura.service.AgriculturaService agriculturaService;
    @MockBean private com.example.sitiopro.propriedade.service.PropriedadeService propriedadeService;

    private static final String SENHA_VALIDA = "SenhaMuitoForte123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioSessaoService usuarioSessaoService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private ConfiguracaoOperacionalService configuracaoOperacionalService;

    @MockBean
    private DashboardTendenciasService dashboardTendenciasService;

    @MockBean
    private ProducaoService producaoService;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private VeiculoService veiculoService;

    @MockBean
    private AbastecimentoService abastecimentoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EstoqueCatalogoService estoqueCatalogoService;

    @MockBean
    private EstoqueMovimentoService estoqueMovimentoService;

    @MockBean
    private SistemaSaudeService sistemaSaudeService;

    @MockBean
    private CompraService compraService;

    @MockBean
    private FornecedorService fornecedorService;

    @MockBean
    private IntegracaoPainelService integracaoPainelService;

    @MockBean
    private IntegracaoOrquestrador integracaoOrquestrador;

    @MockBean(name = "openMeteoRestClient")
    private RestClient openMeteoRestClient;

    @MockBean(name = "agrofitRestClient")
    private RestClient agrofitRestClient;

    @MockBean
    private TarefaService tarefaService;

    @MockBean
    private AlertaService alertaService;

    @MockBean
    private ResumoOperacionalService resumoOperacionalService;

    @MockBean
    private AbastecimentoRepository abastecimentoRepository;

    @MockBean
    private CategoriaRepository categoriaRepository;

    @MockBean
    private FipeCacheRepository fipeCacheRepository;

    @MockBean
    private ProducaoRepository producaoRepository;

    @MockBean
    private VeiculoRepository veiculoRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private CategoriaEstoqueRepository estoqueCategoriaRepository;

    @MockBean
    private UnidadeMedidaRepository unidadeMedidaRepository;

    @MockBean
    private LocalEstoqueRepository localEstoqueRepository;

    @MockBean
    private ItemEstoqueRepository itemEstoqueRepository;

    @MockBean
    private LoteEstoqueRepository loteEstoqueRepository;

    @MockBean
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @MockBean
    private IntegracaoEstadoRepository integracaoEstadoRepository;

    @MockBean
    private IntegracaoExecucaoRepository integracaoExecucaoRepository;

    @MockBean
    private PrevisaoClimaticaRepository previsaoClimaticaRepository;

    @MockBean
    private AgrofitCulturaRepository agrofitCulturaRepository;

    @MockBean
    private TarefaRepository tarefaRepository;

    @MockBean
    private TarefaRecorrenciaRepository tarefaRecorrenciaRepository;

    @MockBean
    private AlertaRepository alertaRepository;

    @MockBean
    private EventoTarefaAlertaRepository eventoTarefaAlertaRepository;

    @MockBean
    private SqlServerApplicationLock sqlServerApplicationLock;

    @MockBean
    private CodigoCriacaoService codigoCriacaoService;

    @MockBean private AvesResumoService avesResumoService;
    @MockBean private InstalacaoCriacaoService instalacaoCriacaoService;
    @MockBean private LoteAvesService loteAvesService;
    @MockBean private ManejoAvesService manejoAvesService;
    @MockBean private IncubacaoAvesService incubacaoAvesService;
    @MockBean private IncubacaoAcompanhamentoService incubacaoAcompanhamentoService;
    @MockBean private AvesAlertasService avesAlertasService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void configurarMocks() {
        when(configuracaoOperacionalService.obter()).thenReturn(
                com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.padrao());
        when(dashboardService.montarResumo()).thenReturn(vazio());
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novoFormulario()).thenReturn(new ProducaoForm());
        when(veiculoService.listarTodos()).thenReturn(List.of());
        when(usuarioService.listarTodos()).thenReturn(List.of());
        when(usuarioService.contarAtivos()).thenReturn(2L);
        when(usuarioService.contarAdministradoresAtivos()).thenReturn(1L);
        when(estoqueMovimentoService.montarResumo()).thenReturn(new EstoqueDashboardResumo(
                0, 0, 0, BigDecimal.ZERO, List.of(), List.of(), List.of()));
        when(estoqueMovimentoService.registrarMovimento(any(), eq(false))).thenReturn(new MovimentoEstoqueResponse(
                1L, 1L, "Ração postura", TipoMovimentoEstoque.ENTRADA, "Entrada",
                BigDecimal.TEN, "KG", null, "Depósito", null, null, null,
                null, LocalDateTime.now(), "operador", null, null, null));
        when(estoqueMovimentoService.listarMovimentos(anyInt(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(sistemaSaudeService.resumo()).thenReturn(new SistemaSaudeResumo(
                "UP", "UP", Duration.ofMinutes(5), "0.0.1-SNAPSHOT", "test",
                "DESABILITADA", "test-request"));
        when(integracaoPainelService.resumo()).thenReturn(
                new IntegracaoPainelResumo(0, 0, 0, 0, Map.of()));
        FornecedorResumo fornecedor = new FornecedorResumo(1L, "Agro Vale", null, null, null, true);
        FornecedorRequest fornecedorRequest = new FornecedorRequest();
        fornecedorRequest.setNome("Agro Vale");
        fornecedorRequest.setAtivo(true);
        CompraDetalhe compraRascunho = compraDetalhe(StatusCompra.RASCUNHO);
        CompraDetalhe compraConfirmada = compraDetalhe(StatusCompra.CONFIRMADA);
        when(fornecedorService.formulario(1L)).thenReturn(fornecedorRequest);
        when(fornecedorService.criar(any())).thenReturn(fornecedor);
        when(fornecedorService.atualizar(eq(1L), any())).thenReturn(fornecedor);
        when(compraService.criarCompra(any())).thenReturn(compraRascunho);
        when(compraService.adicionarItem(eq(1L), any())).thenReturn(compraRascunho);
        when(compraService.atualizarItem(eq(1L), eq(501L), any())).thenReturn(compraRascunho);
        when(compraService.confirmarCompra(1L)).thenReturn(compraConfirmada);
        when(tarefaService.listar(any())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(tarefaService.listarResponsaveisAtivos()).thenReturn(List.of());
        when(tarefaService.criar(any(), any())).thenReturn(tarefaDetalhe());
        when(tarefaService.iniciar(eq(1L), any())).thenReturn(tarefaDetalhe());
        when(tarefaService.concluir(eq(1L), any())).thenReturn(tarefaDetalhe());
        when(tarefaService.cancelar(eq(1L), any())).thenReturn(tarefaDetalhe());
        when(alertaService.listar(any())).thenReturn(new PaginaResponse<>(List.of(), 0, 20, 0, 0));
        when(alertaService.reconhecer(eq(1L), any())).thenReturn(alertaDetalhe());
        when(alertaService.resolver(eq(1L), any())).thenReturn(alertaDetalhe());
        when(resumoOperacionalService.resumo()).thenReturn(new TarefaResumoOperacional(0, 0, 0, 0, 0));
        when(avesResumoService.resumo()).thenReturn(new AvesResumo(0, 0, 0, 0, 0, 0, 0, LocalDateTime.now()));
        when(instalacaoCriacaoService.criar(any())).thenReturn(new InstalacaoCriacaoResumo(
                1L, "Galinheiro 1", TipoInstalacaoCriacao.GALINHEIRO, "Galinheiro", null,
                100, 0, true, 0, null, null, null, null));

        Usuario admin = usuario(1L, "Administrador", "admin", PerfilUsuario.ADMIN, true);
        Usuario operador = usuario(2L, "Operador", "operador", PerfilUsuario.OPERADOR, true);
        Usuario inativo = usuario(3L, "Inativo", "inativo", PerfilUsuario.OPERADOR, false);

        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByLogin("operador")).thenReturn(Optional.of(operador));
        when(usuarioRepository.findByLogin("inativo")).thenReturn(Optional.of(inativo));
        when(usuarioRepository.findByLogin("naoexiste")).thenReturn(Optional.empty());
    }

    @Test
    void adminAcessaConfiguracoesSemCamposSecretos() throws Exception {
        String html = mockMvc.perform(get("/sitio/admin/configuracoes").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Dados da propriedade", "name=\"_csrf\"", "name=\"diasPadraoIncubacao\"")
                .doesNotContain("name=\"senha\"", "type=\"password\"", "connectionString", "jdbc:sqlserver", "apiKey", "EMBRAPA_AGROFIT_TOKEN");
    }

    @Test
    void operadorNaoAcessaNemAlteraConfiguracoes() throws Exception {
        mockMvc.perform(get("/sitio/admin/configuracoes").with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/sitio/admin/configuracoes").with(user("operador").roles("OPERADOR")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void configuracoesExigemCsrfMesmoParaAdmin() throws Exception {
        mockMvc.perform(post("/sitio/admin/configuracoes").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAlteraConfiguracoesComCsrf() throws Exception {
        mockMvc.perform(post("/sitio/admin/configuracoes").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("nomePropriedade", "Sítio teste").param("timezone", "America/Porto_Velho")
                        .param("latitude", "-8.123456").param("longitude", "-63.123456")
                        .param("diasPadraoIncubacao", "21").param("antecedenciaAlertaEclosaoDias", "3"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/sitio/admin/configuracoes"));
        verify(configuracaoOperacionalService).atualizar(any());
    }

    @Test
    void rotaProtegidaRedirecionaParaLoginQuandoNaoAutenticada() throws Exception {
        mockMvc.perform(get("/sitio/painel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void assetsDaMarcaSaoPublicosParaTelaDeLogin() throws Exception {
        mockMvc.perform(get("/brand/garca-symbol.svg"))
                .andExpect(status().isOk());
    }

    @Test
    void loginValidoAutenticaEAtualizaUltimoLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", SENHA_VALIDA)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/painel"));

        verify(usuarioService).registrarUltimoLogin(1L);
    }

    @Test
    void loginInvalidoRetornaMensagemGenericaERegistraEventoSeguro(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "naoexiste")
                        .param("password", "qualquer")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        assertThat(output).contains("LOGIN_FAILURE")
                .doesNotContain("qualquer");
    }

    @Test
    void usuarioInativoNaoAutentica() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "inativo")
                        .param("password", SENHA_VALIDA)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logoutUsaPostComCsrf() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void adminAcessaGestaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/sitio/admin/usuarios")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void centralAdministrativaEExclusivaDoAdmin() throws Exception {
        mockMvc.perform(get("/sitio/admin")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Central Administrativa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuários")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Integrações")));

        mockMvc.perform(get("/sitio/admin")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void centralAdministrativaRenderizaMesmoComServicosDegradados() throws Exception {
        when(sistemaSaudeService.resumo()).thenThrow(new IllegalStateException("health indisponível"));
        when(integracaoPainelService.resumo()).thenThrow(new IllegalStateException("integração indisponível"));

        mockMvc.perform(get("/sitio/admin")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Degradada")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("INDISPONIVEL")));
    }

    @Test
    void desativacaoDeUsuarioExigeCsrf() throws Exception {
        mockMvc.perform(post("/sitio/admin/usuarios/2/desativar")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/sitio/admin/usuarios/2/desativar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/admin/usuarios"));

        verify(usuarioService).desativar(2L);
    }

    @Test
    void usuarioDesativadoPerdeSessaoSemAfetarOutroUsuario() throws Exception {
        MvcResult loginOperador = mockMvc.perform(post("/login")
                        .param("username", "operador")
                        .param("password", SENHA_VALIDA)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MvcResult loginAdmin = mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", SENHA_VALIDA)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession sessaoOperador = (MockHttpSession) loginOperador.getRequest().getSession(false);
        MockHttpSession sessaoAdmin = (MockHttpSession) loginAdmin.getRequest().getSession(false);

        assertThat(usuarioSessaoService.revogarAgora(2L)).isEqualTo(1);

        mockMvc.perform(get("/sitio/painel").session(sessaoOperador))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired"));
        mockMvc.perform(get("/sitio/painel").session(sessaoAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void operadorNaoAcessaGestaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/sitio/admin/usuarios")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void formulariosLegadosNaoAceitamIdNemCamposInternosPorMassAssignment() throws Exception {
        mockMvc.perform(post("/sitio/configuracoes/categoria/salvar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("id", "999")
                        .param("nome", "Ferramentas"))
                .andExpect(status().is3xxRedirection());
        verify(categoriaService).salvar(argThat(categoria -> categoria.getId() == null));

        mockMvc.perform(post("/sitio/frota/salvar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("id", "999")
                        .param("nome", "Trator")
                        .param("tipo", "3")
                        .param("situacao", "CAMPO_FORCADO")
                        .param("icone", "CAMPO_FORCADO"))
                .andExpect(status().is3xxRedirection());
        verify(veiculoService).salvar(argThat(veiculo -> veiculo.getId() == null
                && "DISPONIVEL".equals(veiculo.getSituacao()) && veiculo.getIcone() == null));
    }

    @Test
    void adminAcessaSaudeDoSistema() throws Exception {
        mockMvc.perform(get("/sitio/admin/saude")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void operadorNaoAcessaSaudeDoSistema() throws Exception {
        mockMvc.perform(get("/sitio/admin/saude")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonimoNaoAcessaSaudeDoSistema() throws Exception {
        mockMvc.perform(get("/sitio/admin/saude"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void painelIntegracoesSomenteAdmin() throws Exception {
        mockMvc.perform(get("/sitio/admin/integracoes")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/sitio/admin/integracoes")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/sitio/admin/integracoes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void sincronizacaoManualExigeAdminECsrf() throws Exception {
        mockMvc.perform(post("/sitio/admin/integracoes/open-meteo/sincronizar")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/sitio/admin/integracoes/open-meteo/sincronizar")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/sitio/admin/integracoes/open-meteo/sincronizar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/admin/integracoes/open-meteo"));
    }

    @Test
    void operadorAcessaModuloNormal() throws Exception {
        mockMvc.perform(get("/sitio/painel")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void postSemCsrfERecusado() throws Exception {
        mockMvc.perform(post("/sitio/configuracoes/categoria/salvar")
                        .with(user("admin").roles("ADMIN"))
                        .param("nome", "Insumos")
                        .param("icone", "fa-seedling")
                        .param("corHex", "#166534"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postComCsrfEAutorizacaoAdminEProcessado() throws Exception {
        mockMvc.perform(post("/sitio/configuracoes/categoria/salvar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nome", "Insumos")
                        .param("icone", "fa-seedling")
                        .param("corHex", "#166534"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/configuracoes"));

        verify(categoriaService).salvar(any(Categoria.class));
    }

    @Test
    void apiV1FicaNegadaMesmoComUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/qualquer")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void apiEstoqueExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/estoque/resumo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void apiPainelExigeAutenticacaoEAceitaAdminEOperador() throws Exception {
        mockMvc.perform(get("/api/v1/painel/resumo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/api/v1/painel/resumo")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelAtencao").value("NORMAL"));

        mockMvc.perform(get("/api/v1/painel/resumo")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void apiComprasExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/compras"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void apiClimaExigeAutenticacaoEConsultaSomenteDadosLocais() throws Exception {
        mockMvc.perform(get("/api/v1/clima/resumo"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/clima/resumo")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));
    }

    @Test
    void apiAdminIntegracoesSomenteAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/integracoes")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/integracoes")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void apiEstoqueResumoAutenticadaResponde() throws Exception {
        mockMvc.perform(get("/api/v1/estoque/resumo")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void operadorNaoCriaItemPelaApi() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/itens")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"))
                .andExpect(jsonPath("$.path").value("/api/v1/estoque/itens"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void operadorRegistraMovimentoNormalPelaApiComCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/movimentos")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemId": 1,
                                  "tipo": "ENTRADA",
                                  "quantidade": 10,
                                  "localDestinoId": 1
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void operadorConsultaHistoricoPaginadoDoEstoque() throws Exception {
        mockMvc.perform(get("/api/v1/estoque/movimentos")
                        .with(user("operador").roles("OPERADOR"))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20));
    }

    @Test
    void operadorPodeCriarFornecedorCompraAdicionarItemEConfirmarPelaApiComCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/fornecedores")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Agro Vale",
                                  "ativo": true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/compras")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fornecedorId": 1,
                                  "dataCompra": "2026-08-21",
                                  "frete": 0,
                                  "desconto": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        mockMvc.perform(post("/api/v1/compras/1/itens")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemEstoqueId": 1,
                                  "quantidadeVolumes": 2,
                                  "tipoEmbalagem": "SACO",
                                  "conteudoPorVolume": 60,
                                  "precoPorVolume": 135,
                                  "unidadeBase": "KG",
                                  "quantidade": 9999,
                                  "custoUnitario": 9999,
                                  "localDestinoId": 1
                                }
                                """))
                .andExpect(status().isOk());

        verify(compraService).adicionarItem(eq(1L), argThat(request ->
                request.getQuantidadeVolumes().compareTo(new BigDecimal("2")) == 0
                        && request.getConteudoPorVolume().compareTo(new BigDecimal("60")) == 0
                        && request.getPrecoPorVolume().compareTo(new BigDecimal("135")) == 0
                        && "KG".equals(request.getUnidadeBase())
                        && request.getQuantidade().compareTo(new BigDecimal("9999")) == 0));

        mockMvc.perform(post("/api/v1/compras/1/confirmar")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void operadorAtualizaItemDeRascunhoPelaInterfaceMvc() throws Exception {
        mockMvc.perform(post("/sitio/compras/1/itens/501")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .param("itemEstoqueId", "1")
                        .param("quantidadeVolumes", "2")
                        .param("tipoEmbalagem", "SACO")
                        .param("conteudoPorVolume", "60")
                        .param("precoPorVolume", "135.00")
                        .param("localDestinoId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/compras/1"));

        verify(compraService).atualizarItem(eq(1L), eq(501L), any());
    }

    @Test
    void operadorNaoAtualizaFornecedorPelaInterfaceMvc() throws Exception {
        mockMvc.perform(post("/sitio/compras/fornecedores/1")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .param("nome", "Agro Vale")
                        .param("ativo", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAtualizaFornecedorPelaInterfaceMvc() throws Exception {
        mockMvc.perform(post("/sitio/compras/fornecedores/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nome", "Agro Vale")
                        .param("ativo", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/compras/fornecedores/1"));

        verify(fornecedorService).atualizar(eq(1L), any());
    }

    @Test
    void apiComprasRetornaErroPadronizadoParaRegraDeNegocio() throws Exception {
        when(compraService.confirmarCompra(77L)).thenThrow(new ComprasOperacaoException("COMPRA_SEM_ITENS",
                "Inclua ao menos um item antes de confirmar a compra."));

        mockMvc.perform(post("/api/v1/compras/77/confirmar")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMPRA_SEM_ITENS"))
                .andExpect(jsonPath("$.path").value("/api/v1/compras/77/confirmar"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void operadorNaoCancelaCompraPelaInterfaceMvc() throws Exception {
        mockMvc.perform(post("/sitio/compras/1/cancelar")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCancelaCompraPelaInterfaceMvc() throws Exception {
        when(compraService.cancelarRascunho(1L)).thenReturn(compraDetalhe(StatusCompra.CANCELADA));

        mockMvc.perform(post("/sitio/compras/1/cancelar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/compras/1"));
    }

    @Test
    void apiPostSemCsrfERecusado() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/movimentos")
                        .with(user("operador").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"))
                .andExpect(jsonPath("$.path").value("/api/v1/estoque/movimentos"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void itemDeCompraSemCsrfERecusado() throws Exception {
        mockMvc.perform(post("/api/v1/compras/1/itens")
                        .with(user("operador").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemEstoqueId": 1,
                                  "quantidadeVolumes": 1,
                                  "tipoEmbalagem": "PACOTE",
                                  "conteudoPorVolume": 1,
                                  "precoPorVolume": 7.5,
                                  "localDestinoId": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));
    }

    @Test
    void criacoesAvesExigeAutenticacaoEPermiteConsultaAoOperador() throws Exception {
        mockMvc.perform(get("/api/v1/criacoes/aves/resumo"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/criacoes/aves/resumo").with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotesAtivos").value(0));
    }

    @Test
    void operadorNaoCriaInstalacaoMasAdminPodeCriar() throws Exception {
        String json = """
                {"nome":"Galinheiro 1","tipo":"GALINHEIRO","capacidade":100,"ativo":true}
                """;
        mockMvc.perform(post("/api/v1/criacoes/aves/instalacoes")
                        .with(user("operador").roles("OPERADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/criacoes/aves/instalacoes")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void paginasAdministrativasDeAvesSaoRestritasAoAdmin() throws Exception {
        mockMvc.perform(get("/sitio/criacoes/aves/lotes/novo")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/sitio/criacoes/aves/instalacoes/nova")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void operadorRegistraManejoComCsrfMasRequestSemCsrfERecusado() throws Exception {
        String json = """
                {"quantidade":1,"causa":"Acidente","chaveIdempotencia":"op-12345678"}
                """;
        mockMvc.perform(post("/api/v1/criacoes/aves/lotes/1/mortalidades")
                        .with(user("operador").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/criacoes/aves/lotes/1/mortalidades")
                        .with(user("operador").roles("OPERADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    void operadorRegistraAcompanhamentoDeIncubacaoComCsrf() throws Exception {
        AcompanhamentoIncubacaoAvesResumo acompanhamento = new AcompanhamentoIncubacaoAvesResumo(
                10L, LocalDateTime.of(2026, 9, 5, 8, 0),
                TipoAcompanhamentoIncubacaoAves.VERIFICACAO_GERAL,
                null, null, null, null, null, null, "Tudo normal", null, "operador");
        when(incubacaoAcompanhamentoService.registrar(eq(1L), any())).thenReturn(acompanhamento);
        when(incubacaoAcompanhamentoService.detalhar(1L, 10L)).thenReturn(acompanhamento);
        String json = """
                {
                  "tipo":"VERIFICACAO_GERAL",
                  "dataHora":"2026-09-05T08:00:00",
                  "observacao":"Tudo normal",
                  "chaveIdempotencia":"acomp-security-1"
                }
                """;

        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/acompanhamentos")
                        .with(user("operador").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/acompanhamentos")
                        .with(user("operador").roles("OPERADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("VERIFICACAO_GERAL"));

        mockMvc.perform(get("/api/v1/criacoes/aves/incubacoes/1/acompanhamentos/10")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.observacao").value("Tudo normal"));
    }

    @Test
    void ajusteDePrevisaoECancelamentoDeIncubacaoSaoRestritosAoAdmin() throws Exception {
        String ajuste = """
                {"dataPrevistaEclosao":"2026-09-23","motivo":"Desenvolvimento mais lento"}
                """;
        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/ajustar-previsao")
                        .with(user("operador").roles("OPERADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(ajuste))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/cancelar")
                        .with(user("operador").roles("OPERADOR")).with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/ajustar-previsao")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(ajuste))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/criacoes/aves/incubacoes/1/cancelar")
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void apiRetornaErroPadronizadoParaRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/movimentos")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.path").value("/api/v1/estoque/movimentos"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void apiRetornaErroSeguroParaExceptionInesperada(CapturedOutput output) throws Exception {
        when(estoqueMovimentoService.montarResumo())
                .thenThrow(new IllegalStateException("segredo-interno senha=abc"));

        MvcResult result = mockMvc.perform(get("/api/v1/estoque/resumo")
                        .with(user("operador").roles("OPERADOR"))
                        .header("X-Request-ID", "req-api-9999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.requestId").value("req-api-9999"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("req-api-9999")
                .doesNotContain("IllegalStateException")
                .doesNotContain("segredo-interno")
                .doesNotContain("senha=abc");
        assertThat(output).doesNotContain("senha=abc");
    }

    @Test
    void actuatorHealthPublicoComInformacaoMinima() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void actuatorInfoEMetricasSomenteAdmin() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void actuatorSensivelPermaneceBloqueado() throws Exception {
        mockMvc.perform(get("/actuator/env")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/loggers")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestIdValidoEPropagadoNaResposta() throws Exception {
        mockMvc.perform(get("/sitio/painel")
                        .with(user("operador").roles("OPERADOR"))
                        .header("X-Request-ID", "req-front-1234"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", "req-front-1234"));
    }

    @Test
    void requestIdInvalidoERecriadoAntesDePropagar() throws Exception {
        String abusivo = "x".repeat(200);

        MvcResult result = mockMvc.perform(get("/sitio/painel")
                        .with(user("operador").roles("OPERADOR"))
                        .header("X-Request-ID", abusivo))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank()
                .isNotEqualTo(abusivo)
                .matches("[A-Za-z0-9._-]{8,64}");
    }

    @Test
    void swaggerSomenteAdmin() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/v3/api-docs")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/painel/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/estoque/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/compras']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/fornecedores']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clima/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clima/previsao']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/integracoes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/criacoes/aves/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/criacoes/aves/lotes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/criacoes/aves/incubacoes']").exists());
    }

    @Test
    void tarefasEAlertasBloqueiamAcessoAnonimo() throws Exception {
        mockMvc.perform(get("/sitio/tarefas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/api/v1/alertas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void operadorCriaTarefaPelaApiComDtoRestrito() throws Exception {
        mockMvc.perform(post("/api/v1/tarefas")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Verificar reservatório",
                                  "prioridade": "NORMAL",
                                  "recorrencia": "NENHUMA",
                                  "status": "CONCLUIDA",
                                  "origem": "AUTOMATICA",
                                  "criadoPor": 999
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.origem").value("MANUAL"));

        verify(tarefaService).criar(any(), any());
    }

    @Test
    void apiTarefasValidaRequestEExigeCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/tarefas")
                        .with(user("operador").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Atividade\",\"prioridade\":\"NORMAL\",\"recorrencia\":\"NENHUMA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));

        mockMvc.perform(post("/api/v1/tarefas")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"prioridade\":\"NORMAL\",\"recorrencia\":\"NENHUMA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"));
    }

    @Test
    void apiTarefasRecusaFiltroComEnumInvalidoComoErroDeValidacao() throws Exception {
        mockMvc.perform(get("/api/v1/tarefas")
                        .param("status", "STATUS_INEXISTENTE")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_INVALIDA"))
                .andExpect(jsonPath("$.path").value("/api/v1/tarefas"));
    }

    @Test
    void apiTarefasRecusaJsonMalformadoDeFormaPadronizada() throws Exception {
        mockMvc.perform(post("/api/v1/tarefas")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.path").value("/api/v1/tarefas"));
    }

    @Test
    void apiPainelUsaRespostaSeguraParaFalhaInesperada(CapturedOutput output) throws Exception {
        when(dashboardService.montarResumo())
                .thenThrow(new IllegalStateException("jdbc:sqlserver://db:1433;password=nao-expor"));

        mockMvc.perform(get("/api/v1/painel/resumo")
                        .with(user("operador").roles("OPERADOR"))
                        .header("X-Request-ID", "req-painel-9999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.requestId").value("req-painel-9999"));

        assertThat(output).doesNotContain("nao-expor")
                .doesNotContain("jdbc:sqlserver://db:1433");
    }

    @Test
    void operadorReconheceAlertaMasNaoResolveNemCancelaTarefa() throws Exception {
        mockMvc.perform(post("/sitio/alertas/1/reconhecer")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/alertas/1"));
        mockMvc.perform(post("/sitio/alertas/1/resolver")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/sitio/tarefas/1/cancelar")
                        .with(user("operador").roles("OPERADOR"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminResolveAlertaECancelaTarefa() throws Exception {
        mockMvc.perform(post("/sitio/alertas/1/resolver")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/alertas/1"));
        mockMvc.perform(post("/sitio/tarefas/1/cancelar")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sitio/tarefas/1"));
    }

    private Usuario usuario(Long id, String nome, String login, PerfilUsuario perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_VALIDA));
        return usuario;
    }

    private CompraDetalhe compraDetalhe(StatusCompra status) {
        return new CompraDetalhe(
                1L,
                new FornecedorResumo(1L, "Agro Vale", null, null, null, true),
                LocalDate.of(2026, 8, 21),
                "NF-1",
                null,
                status,
                status.getRotulo(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(),
                status == StatusCompra.CONFIRMADA ? LocalDateTime.now() : null,
                status == StatusCompra.CONFIRMADA ? "operador" : null,
                null,
                null,
                null,
                null);
    }

    private TarefaDetalhe tarefaDetalhe() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 24, 12, 0);
        return new TarefaDetalhe(1L, "Verificar reservatório", null, StatusTarefa.PENDENTE,
                PrioridadeTarefa.NORMAL, agora, null, agora.plusDays(1), null,
                null, null, 1L, "Administrador", OrigemTarefa.MANUAL, null, null,
                TipoRecorrencia.NENHUMA, null, null, false, true, 0, false, List.of());
    }

    private AlertaDetalhe alertaDetalhe() {
        LocalDateTime agora = LocalDateTime.of(2026, 8, 24, 12, 0);
        return new AlertaDetalhe(1L, "Ração abaixo do mínimo", "Saldo insuficiente.",
                SeveridadeAlerta.ALTA, StatusAlerta.RECONHECIDO, ModuloOrigem.ESTOQUE,
                TipoAlerta.ESTOQUE_ABAIXO_MINIMO, "ITEM:1", "ESTOQUE:ITEM:1:ABAIXO_MINIMO",
                agora, agora, agora, "Administrador", null, java.util.Map.of(), null, 0, List.of());
    }
}
