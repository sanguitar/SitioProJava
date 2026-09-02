package com.example.sitiopro.usuario.service;

import com.example.sitiopro.usuario.dto.AlterarSenhaRequest;
import com.example.sitiopro.usuario.dto.CriarUsuarioRequest;
import com.example.sitiopro.usuario.dto.EditarUsuarioRequest;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import com.example.sitiopro.usuario.security.UsuarioSessaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {

    private static final String SENHA_VALIDA = "SenhaMuitoForte123";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioSessaoService usuarioSessaoService;

    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, new SenhaPolicy(),
                usuarioSessaoService);
    }

    @Test
    void criarNormalizaLoginECriptografaSenha() {
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setNome("  Operador Campo  ");
        request.setLogin("  Operador  ");
        request.setSenhaInicial(SENHA_VALIDA);
        request.setPerfil(PerfilUsuario.OPERADOR);
        request.setAtivo(true);

        when(usuarioRepository.existsByLogin("operador")).thenReturn(false);
        when(passwordEncoder.encode(SENHA_VALIDA)).thenReturn("{bcrypt}hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = usuarioService.criar(request);

        assertThat(criado.getNome()).isEqualTo("Operador Campo");
        assertThat(criado.getLogin()).isEqualTo("operador");
        assertThat(criado.getSenhaHash()).isEqualTo("{bcrypt}hash");
        assertThat(criado.getSenhaHash()).isNotEqualTo(SENHA_VALIDA);
    }

    @Test
    void criarRecusaLoginDuplicado() {
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setNome("Operador");
        request.setLogin("operador");
        request.setSenhaInicial(SENHA_VALIDA);
        request.setPerfil(PerfilUsuario.OPERADOR);

        when(usuarioRepository.existsByLogin("operador")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(UsuarioOperacaoException.class)
                .hasMessageContaining("Já existe");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void existePorLoginNormalizaLoginAntesDeConsultar() {
        when(usuarioRepository.existsByLogin("sanderson")).thenReturn(true);

        boolean existe = usuarioService.existePorLogin("  Sanderson  ");

        assertThat(existe).isTrue();
    }

    @Test
    void criarAdminInicialCriaAdminAtivoComSenhaCriptografadaQuandoTabelaVazia() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.existsByLogin("sanderson")).thenReturn(false);
        when(passwordEncoder.encode(SENHA_VALIDA)).thenReturn("{bcrypt}hash-admin");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario criado = usuarioService.criarAdminInicial("  Sânderson  ", "  Sanderson  ", SENHA_VALIDA);

        assertThat(criado.getNome()).isEqualTo("Sânderson");
        assertThat(criado.getLogin()).isEqualTo("sanderson");
        assertThat(criado.getPerfil()).isEqualTo(PerfilUsuario.ADMIN);
        assertThat(criado.isAtivo()).isTrue();
        assertThat(criado.getSenhaHash()).isEqualTo("{bcrypt}hash-admin");
        assertThat(criado.getSenhaHash()).isNotEqualTo(SENHA_VALIDA);
    }

    @Test
    void naoPermiteDesativarUltimoAdminAtivo() {
        Usuario admin = usuario(1L, PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.buscarAtivosParaAtualizacao(PerfilUsuario.ADMIN)).thenReturn(List.of(admin));

        assertThatThrownBy(() -> usuarioService.desativar(1L))
                .isInstanceOf(UsuarioOperacaoException.class)
                .hasMessageContaining("ADMIN ativo");

        verify(usuarioSessaoService, never()).revogarAposCommit(1L);
    }

    @Test
    void naoPermiteRebaixarUltimoAdminAtivo() {
        Usuario admin = usuario(1L, PerfilUsuario.ADMIN, true);
        EditarUsuarioRequest request = new EditarUsuarioRequest();
        request.setNome("Administrador");
        request.setPerfil(PerfilUsuario.OPERADOR);
        request.setAtivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.buscarAtivosParaAtualizacao(PerfilUsuario.ADMIN)).thenReturn(List.of(admin));

        assertThatThrownBy(() -> usuarioService.editar(1L, request))
                .isInstanceOf(UsuarioOperacaoException.class)
                .hasMessageContaining("ADMIN ativo");
    }

    @Test
    void desativarUsuarioRevogaSomenteDepoisDaPersistencia() {
        Usuario operador = usuario(2L, PerfilUsuario.OPERADOR, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(operador));

        usuarioService.desativar(2L);

        assertThat(operador.isAtivo()).isFalse();
        verify(usuarioRepository).save(operador);
        verify(usuarioSessaoService).revogarAposCommit(2L);
    }

    @Test
    void editarUsuarioParaInativoTambemRevogaSessoes() {
        Usuario operador = usuario(2L, PerfilUsuario.OPERADOR, true);
        EditarUsuarioRequest request = new EditarUsuarioRequest();
        request.setNome("Operador");
        request.setPerfil(PerfilUsuario.OPERADOR);
        request.setAtivo(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(operador));
        when(usuarioRepository.save(operador)).thenReturn(operador);

        usuarioService.editar(2L, request);

        verify(usuarioSessaoService).revogarAposCommit(2L);
    }

    @Test
    void alterarSenhaPropriaExigeSenhaAtual() {
        Usuario usuario = usuario(2L, PerfilUsuario.OPERADOR, true);
        AlterarSenhaRequest request = alterarSenha("errada", SENHA_VALIDA + "Nova");

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash-atual")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.alterarSenhaPropria(new UsuarioPrincipal(usuario), request))
                .isInstanceOf(SenhaInvalidaException.class)
                .hasMessageContaining("Senha atual");
    }

    @Test
    void alterarSenhaPropriaAtualizaHash() {
        Usuario usuario = usuario(2L, PerfilUsuario.OPERADOR, true);
        AlterarSenhaRequest request = alterarSenha("atual", SENHA_VALIDA + "Nova");

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("atual", "hash-atual")).thenReturn(true);
        when(passwordEncoder.encode(SENHA_VALIDA + "Nova")).thenReturn("hash-novo");

        usuarioService.alterarSenhaPropria(new UsuarioPrincipal(usuario), request);

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        verify(usuarioRepository).save(usuario);
    }

    private Usuario usuario(Long id, PerfilUsuario perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setNome("Usuário");
        usuario.setLogin("usuario");
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        usuario.setSenhaHash("hash-atual");
        return usuario;
    }

    private AlterarSenhaRequest alterarSenha(String atual, String nova) {
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual(atual);
        request.setNovaSenha(nova);
        request.setConfirmarSenha(nova);
        return request;
    }
}
