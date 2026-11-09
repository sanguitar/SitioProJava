package com.example.sitiopro.usuario.mobile;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;

@Configuration
@EnableConfigurationProperties(MobileTokenProperties.class)
public class MobileTokenConfig {

    private static final Logger log = LoggerFactory.getLogger(MobileTokenConfig.class);

    @Bean
    SecretKey mobileJwtSecretKey(MobileTokenProperties properties) {
        if (StringUtils.hasText(properties.getSigningSecretBase64())) {
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(properties.getSigningSecretBase64().trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("SITIOPRO_MOBILE_JWT_SECRET_BASE64 não é Base64 válido.", ex);
            }
            if (bytes.length < 32) {
                throw new IllegalStateException("SITIOPRO_MOBILE_JWT_SECRET_BASE64 deve conter ao menos 32 bytes.");
            }
            return new SecretKeySpec(bytes, "HmacSHA256");
        }
        if (properties.isRequireConfiguredSigningSecret()) {
            throw new IllegalStateException("Defina SITIOPRO_MOBILE_JWT_SECRET_BASE64 no ambiente de produção.");
        }
        log.warn("Chave JWT mobile efêmera gerada. Access tokens serão invalidados ao reiniciar; configure o segredo em ambientes compartilhados.");
        try {
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            generator.init(256);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("HmacSHA256 indisponível no runtime.", ex);
        }
    }

    @Bean
    JwtEncoder mobileJwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
    }

    @Bean
    JwtDecoder mobileJwtDecoder(SecretKey secretKey, MobileTokenProperties properties,
            MobileJwtStateValidator stateValidator) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(properties.getIssuer());
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<Collection<String>>("aud",
                values -> values != null && values.contains(properties.getAudience()));
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(issuer, audience, stateValidator);
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
