package com.example.sitiopro.usuario.mobile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("sitiopro.security.mobile")
public class MobileTokenProperties {

    private String issuer = "sitiopro";
    private String audience = "sitiopro-mobile";
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private String signingSecretBase64;
    private boolean requireConfiguredSigningSecret;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public Duration getAccessTokenTtl() { return accessTokenTtl; }
    public void setAccessTokenTtl(Duration accessTokenTtl) { this.accessTokenTtl = accessTokenTtl; }
    public Duration getRefreshTokenTtl() { return refreshTokenTtl; }
    public void setRefreshTokenTtl(Duration refreshTokenTtl) { this.refreshTokenTtl = refreshTokenTtl; }
    public String getSigningSecretBase64() { return signingSecretBase64; }
    public void setSigningSecretBase64(String signingSecretBase64) { this.signingSecretBase64 = signingSecretBase64; }
    public boolean isRequireConfiguredSigningSecret() { return requireConfiguredSigningSecret; }
    public void setRequireConfiguredSigningSecret(boolean value) { this.requireConfiguredSigningSecret = value; }
}
