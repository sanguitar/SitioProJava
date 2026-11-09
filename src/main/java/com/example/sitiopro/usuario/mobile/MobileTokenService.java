package com.example.sitiopro.usuario.mobile;

import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MobileTokenService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_SESSION_ID = "sid";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";

    private final JwtEncoder encoder;
    private final MobileTokenProperties properties;
    private final Clock clock;

    public MobileTokenService(JwtEncoder encoder, MobileTokenProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public String emitirAccessToken(Usuario usuario, String sessaoId) {
        return emitir(usuario.getId(), usuario.getNome(), usuario.getLogin(), usuario.getPerfil().name(), sessaoId);
    }

    public String emitirAccessToken(UsuarioPrincipal usuario, String sessaoId) {
        return emitir(usuario.getId(), usuario.getNome(), usuario.getUsername(), usuario.getPerfil().name(), sessaoId);
    }

    private String emitir(Long usuarioId, String nome, String login, String perfil, String sessaoId) {
        Instant agora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(login)
                .audience(List.of(properties.getAudience()))
                .issuedAt(agora)
                .expiresAt(agora.plus(properties.getAccessTokenTtl()))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_USER_ID, usuarioId)
                .claim(CLAIM_SESSION_ID, sessaoId)
                .claim(CLAIM_ROLES, List.of(perfil))
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .claim("name", nome)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenExpiresInSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }

    public long refreshTokenExpiresInSeconds() {
        return properties.getRefreshTokenTtl().toSeconds();
    }
}
