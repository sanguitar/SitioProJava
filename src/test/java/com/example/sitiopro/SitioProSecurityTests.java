package com.example.sitiopro;

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
import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoEstadoRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoExecucaoRepository;
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
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import com.example.sitiopro.producao.service.ProducaoService;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class SitioProSecurityTests {

    private static final String SENHA_VALIDA = "SenhaMuitoForte123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DashboardService dashboardService;

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
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void configurarMocks() {
        DashboardResumo resumo = new DashboardResumo(
                new PageImpl<>(List.of()), List.of(), "[]", "[]", 0, 0, 0,
                ClimaResumo.naoSincronizado());
        when(dashboardService.montarResumo(nullable(Long.class), anyInt())).thenReturn(resumo);
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novo()).thenReturn(new Producao());
        when(veiculoService.listarTodos()).thenReturn(List.of());
        when(usuarioService.listarTodos()).thenReturn(List.of());
        when(estoqueMovimentoService.montarResumo()).thenReturn(new EstoqueDashboardResumo(
                0, 0, 0, BigDecimal.ZERO, List.of(), List.of(), List.of()));
        when(estoqueMovimentoService.registrarMovimento(any(), eq(false))).thenReturn(new MovimentoEstoqueResponse(
                1L, 1L, "Ração postura", TipoMovimentoEstoque.ENTRADA, "Entrada",
                BigDecimal.TEN, "KG", null, "Depósito", null, null, null,
                null, LocalDateTime.now(), "operador", null, null, null));
        when(sistemaSaudeService.resumo()).thenReturn(new SistemaSaudeResumo(
                "UP", "UP", Duration.ofMinutes(5), "0.0.1-SNAPSHOT", "test",
                "DESABILITADA", "test-request"));
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

        Usuario admin = usuario(1L, "Administrador", "admin", PerfilUsuario.ADMIN, true);
        Usuario operador = usuario(2L, "Operador", "operador", PerfilUsuario.OPERADOR, true);
        Usuario inativo = usuario(3L, "Inativo", "inativo", PerfilUsuario.OPERADOR, false);

        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByLogin("operador")).thenReturn(Optional.of(operador));
        when(usuarioRepository.findByLogin("inativo")).thenReturn(Optional.of(inativo));
        when(usuarioRepository.findByLogin("naoexiste")).thenReturn(Optional.empty());
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
    void operadorNaoAcessaGestaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/sitio/admin/usuarios")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden());
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
                                  "quantidade": 2,
                                  "custoUnitario": 3.5,
                                  "localDestinoId": 1
                                }
                                """))
                .andExpect(status().isOk());

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
                        .param("quantidade", "3")
                        .param("custoUnitario", "5.00")
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
                .andExpect(jsonPath("$.paths['/api/v1/estoque/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/compras']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/fornecedores']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clima/resumo']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/clima/previsao']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/integracoes']").exists());
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
}
