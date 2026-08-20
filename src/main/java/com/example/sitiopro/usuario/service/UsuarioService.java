package com.example.sitiopro.usuario.service;

import com.example.sitiopro.usuario.dto.AlterarSenhaRequest;
import com.example.sitiopro.usuario.dto.CriarUsuarioRequest;
import com.example.sitiopro.usuario.dto.EditarUsuarioRequest;
import com.example.sitiopro.usuario.dto.ResetSenhaUsuarioRequest;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaPolicy senhaPolicy;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, SenhaPolicy senhaPolicy) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaPolicy = senhaPolicy;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioOperacaoException("Usuário não encontrado."));
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(normalizarLogin(login))
                .orElseThrow(() -> new UsuarioOperacaoException("Usuário não encontrado."));
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    @Transactional
    public Usuario criar(CriarUsuarioRequest request) {
        String login = normalizarLogin(request.getLogin());
        if (usuarioRepository.existsByLogin(login)) {
            throw new UsuarioOperacaoException("Já existe um usuário com este login.");
        }
        senhaPolicy.validar(request.getSenhaInicial());

        Usuario usuario = new Usuario();
        usuario.setNome(normalizarTexto(request.getNome()));
        usuario.setLogin(login);
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenhaInicial()));
        usuario.setPerfil(request.getPerfil());
        usuario.setAtivo(request.isAtivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario criarAdminInicial(String nome, String login, String senha) {
        if (usuarioRepository.count() > 0) {
            throw new UsuarioOperacaoException("Bootstrap inicial só é permitido com tabela de usuários vazia.");
        }
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setNome(nome);
        request.setLogin(login);
        request.setSenhaInicial(senha);
        request.setPerfil(PerfilUsuario.ADMIN);
        request.setAtivo(true);
        return criar(request);
    }

    @Transactional
    public Usuario editar(Long id, EditarUsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        validarNaoRemoveUltimoAdmin(usuario, request.getPerfil(), request.isAtivo());

        usuario.setNome(normalizarTexto(request.getNome()));
        usuario.setPerfil(request.getPerfil());
        usuario.setAtivo(request.isAtivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void resetarSenha(Long id, ResetSenhaUsuarioRequest request) {
        senhaPolicy.validarConfirmacao(request.getNovaSenha(), request.getConfirmarSenha());
        Usuario usuario = buscarPorId(id);
        usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenhaPropria(UsuarioPrincipal principal, AlterarSenhaRequest request) {
        senhaPolicy.validarConfirmacao(request.getNovaSenha(), request.getConfirmarSenha());
        Usuario usuario = buscarPorId(principal.getId());
        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new SenhaInvalidaException("Senha atual inválida.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        validarNaoRemoveUltimoAdmin(usuario, usuario.getPerfil(), false);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarUltimoLogin(Long usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setUltimoLoginEm(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    private void validarNaoRemoveUltimoAdmin(Usuario usuario, PerfilUsuario novoPerfil, boolean novoAtivo) {
        boolean eraAdminAtivo = usuario.getPerfil() == PerfilUsuario.ADMIN && usuario.isAtivo();
        boolean continuaAdminAtivo = novoPerfil == PerfilUsuario.ADMIN && novoAtivo;
        if (eraAdminAtivo && !continuaAdminAtivo
                && usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN) <= 1) {
            throw new UsuarioOperacaoException("Não é permitido deixar o sistema sem ADMIN ativo.");
        }
    }

    private String normalizarLogin(String login) {
        if (!StringUtils.hasText(login)) {
            throw new UsuarioOperacaoException("Login é obrigatório.");
        }
        return login.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String texto) {
        if (!StringUtils.hasText(texto)) {
            throw new UsuarioOperacaoException("Nome é obrigatório.");
        }
        return texto.trim();
    }
}
