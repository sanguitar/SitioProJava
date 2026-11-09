package com.example.sitiopro.usuario.mobile;

import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class MobileJwtStateValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID = new OAuth2Error(
            "invalid_token", "Token revogado, usuário inativo ou sessão expirada.", null);

    private final RefreshTokenMobileRepository refreshTokens;
    private final UsuarioRepository usuarios;
    private final Clock clock;

    public MobileJwtStateValidator(RefreshTokenMobileRepository refreshTokens,
            UsuarioRepository usuarios, Clock clock) {
        this.refreshTokens = refreshTokens;
        this.usuarios = usuarios;
        this.clock = clock;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tipo = token.getClaimAsString(MobileTokenService.CLAIM_TOKEN_TYPE);
        String sessaoId = token.getClaimAsString(MobileTokenService.CLAIM_SESSION_ID);
        Long usuarioId = numero(token.getClaim(MobileTokenService.CLAIM_USER_ID));
        if (!MobileTokenService.TOKEN_TYPE_ACCESS.equals(tipo) || sessaoId == null || usuarioId == null) {
            return OAuth2TokenValidatorResult.failure(INVALID);
        }
        boolean usuarioAtivo = usuarios.findById(usuarioId).map(usuario -> usuario.isAtivo()).orElse(false);
        LocalDateTime agora = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        boolean sessaoAtiva = refreshTokens
                .existsBySessaoIdAndUsuarioIdAndRevogadoEmIsNullAndExpiraEmAfter(sessaoId, usuarioId, agora);
        return usuarioAtivo && sessaoAtiva
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(INVALID);
    }

    private Long numero(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
