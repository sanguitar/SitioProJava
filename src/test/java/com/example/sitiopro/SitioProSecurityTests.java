package com.example.sitiopro;

import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.repository.CategoriaRepository;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import com.example.sitiopro.producao.service.ProducaoService;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@AutoConfigureMockMvc
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
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void configurarMocks() {
        DashboardResumo resumo = new DashboardResumo(new PageImpl<>(List.of()), List.of(), "[]", "[]", 0, 0, 0);
        when(dashboardService.montarResumo(nullable(Long.class), anyInt())).thenReturn(resumo);
        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(categoriaService.nova()).thenReturn(new Categoria());
        when(producaoService.novo()).thenReturn(new Producao());
        when(veiculoService.listarTodos()).thenReturn(List.of());
        when(usuarioService.listarTodos()).thenReturn(List.of());

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
    void loginInvalidoRetornaMensagemGenerica() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "naoexiste")
                        .param("password", "qualquer")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
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
                .andExpect(status().isForbidden());
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
}
