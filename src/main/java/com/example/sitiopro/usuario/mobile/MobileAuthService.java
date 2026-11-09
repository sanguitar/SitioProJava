package com.example.sitiopro.usuario.mobile;

import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.mobile.dto.MobileLoginRequest;
import com.example.sitiopro.usuario.mobile.dto.MobileTokenResponse;
import com.example.sitiopro.usuario.mobile.dto.MobileUserResponse;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class MobileAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final RefreshTokenMobileRepository refreshTokens;
    private final MobileTokenService tokenService;
    private final MobileTokenProperties properties;
    private final Clock clock;

    public MobileAuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
            RefreshTokenMobileRepository refreshTokens, MobileTokenService tokenService,
            MobileTokenProperties properties, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public MobileTokenResponse login(MobileLoginRequest request) {
        Authentication autenticado;
        try {
            autenticado = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    normalizarLogin(request.getLogin()), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw credenciaisInvalidas();
        }
        if (!(autenticado.getPrincipal() instanceof UsuarioPrincipal principal) || !principal.isEnabled()) {
            throw credenciaisInvalidas();
        }
        Usuario usuario = usuarios.findById(principal.getId()).filter(Usuario::isAtivo)
                .orElseThrow(this::credenciaisInvalidas);
        return novaSessao(usuario);
    }

    @Transactional
    public MobileTokenResponse refresh(String tokenRecebido) {
        LocalDateTime agora = agoraUtc();
        RefreshTokenMobile atual = refreshTokens.buscarPorHashParaAtualizacao(hash(tokenRecebido))
                .orElseThrow(this::refreshInvalido);
        if (!atual.estaAtivo(agora)) {
            throw refreshInvalido();
        }
        TokenGerado novo = gerarRefresh(atual.getUsuario(), atual.getSessaoId(), agora);
        atual.revogar(agora, "ROTACIONADO", novo.entidade());
        refreshTokens.flush();
        return resposta(atual.getUsuario(), atual.getSessaoId(), novo.valor());
    }

    @Transactional
    public void logout(Long usuarioId, String sessaoId, String tokenRecebido) {
        RefreshTokenMobile token = refreshTokens.buscarPorHashParaAtualizacao(hash(tokenRecebido))
                .orElseThrow(this::refreshInvalido);
        if (!token.getUsuario().getId().equals(usuarioId) || !token.getSessaoId().equals(sessaoId)) {
            throw refreshInvalido();
        }
        refreshTokens.revogarSessao(sessaoId, usuarioId, agoraUtc(), "LOGOUT");
    }

    private MobileTokenResponse novaSessao(Usuario usuario) {
        String sessaoId = UUID.randomUUID().toString();
        TokenGerado refresh = gerarRefresh(usuario, sessaoId, agoraUtc());
        return resposta(usuario, sessaoId, refresh.valor());
    }

    private TokenGerado gerarRefresh(Usuario usuario, String sessaoId, LocalDateTime agora) {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshTokenMobile entidade = new RefreshTokenMobile(sessaoId, hash(valor), usuario, agora,
                agora.plus(properties.getRefreshTokenTtl()));
        return new TokenGerado(valor, refreshTokens.save(entidade));
    }

    private MobileTokenResponse resposta(Usuario usuario, String sessaoId, String refreshToken) {
        String accessToken = tokenService.emitirAccessToken(usuario, sessaoId);
        return new MobileTokenResponse(accessToken, refreshToken, "Bearer",
                tokenService.accessTokenExpiresInSeconds(), tokenService.refreshTokenExpiresInSeconds(),
                new MobileUserResponse(usuario.getId(), usuario.getNome(), usuario.getLogin(),
                        usuario.getPerfil().name()));
    }

    public static String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new MobileAuthException("REFRESH_TOKEN_INVALIDO", "Refresh token inválido.", HttpStatus.UNAUTHORIZED);
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível no runtime.", ex);
        }
    }

    private String normalizarLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime agoraUtc() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private MobileAuthException credenciaisInvalidas() {
        return new MobileAuthException("CREDENCIAIS_INVALIDAS", "Usuário ou senha inválidos.", HttpStatus.UNAUTHORIZED);
    }

    private MobileAuthException refreshInvalido() {
        return new MobileAuthException("REFRESH_TOKEN_INVALIDO", "Refresh token inválido, expirado ou revogado.",
                HttpStatus.UNAUTHORIZED);
    }

    private record TokenGerado(String valor, RefreshTokenMobile entidade) {
    }
}
