package com.example.sitiopro.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionHardeningConfigurationTests {

    @Test
    void perfilProducaoUsaSessaoECookiesSeguros() throws IOException {
        PropertySourcesPropertyResolver properties = productionProperties();

        assertThat(properties.getProperty("server.forward-headers-strategy")).isEqualTo("native");
        assertThat(properties.getProperty("server.servlet.session.tracking-modes")).isEqualTo("cookie");
        assertThat(properties.getProperty("server.servlet.session.timeout")).isEqualTo("30m");
        assertThat(properties.getProperty("server.servlet.session.cookie.http-only", Boolean.class)).isTrue();
        assertThat(properties.getProperty("server.servlet.session.cookie.secure", Boolean.class)).isTrue();
        assertThat(properties.getProperty("server.servlet.session.cookie.same-site")).isEqualTo("lax");
    }

    @Test
    void perfilProducaoExigeCriptografiaEValidacaoDoSqlServer() throws IOException {
        String datasourceUrl = productionProperties().getRequiredProperty("spring.datasource.url");

        assertThat(datasourceUrl)
                .contains("encrypt=true")
                .contains("trustServerCertificate=false")
                .doesNotContain("password");
    }

    @Test
    void perfilProducaoMantemActuatorMinimo() throws IOException {
        PropertySourcesPropertyResolver properties = productionProperties();

        assertThat(properties.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,info,metrics");
        assertThat(properties.getProperty("management.endpoint.health.show-details"))
                .isEqualTo("when_authorized");
        assertThat(properties.getProperty("management.endpoint.env.show-values")).isEqualTo("never");
    }

    @Test
    void nginxProducaoRedirecionaParaTlsEEncaminhaHeadersConfiaveis() throws IOException {
        String nginx = Files.readString(Path.of("infra/nginx/nginx.prod.conf"));

        assertThat(nginx)
                .contains("return 308 https://$host$request_uri")
                .contains("ssl_protocols TLSv1.2 TLSv1.3")
                .contains("Strict-Transport-Security")
                .contains("X-Content-Type-Options")
                .contains("Content-Security-Policy")
                .contains("proxy_set_header X-Forwarded-Proto https")
                .contains("proxy_set_header X-Forwarded-For $remote_addr")
                .contains("ssl_certificate /etc/nginx/tls/tls.crt")
                .contains("ssl_certificate_key /etc/nginx/tls/tls.key");
    }

    @Test
    void sqlServerProducaoApresentaCertificadoEForcaTls() throws IOException {
        String compose = Files.readString(Path.of("docker-compose.prod.yml"));
        String sqlServer = Files.readString(Path.of("infra/sqlserver/mssql.prod.conf"));

        assertThat(compose)
                .contains("SQLSERVER_TLS_CERTIFICATE_PATH")
                .contains("SQLSERVER_TLS_PRIVATE_KEY_PATH")
                .contains("mssql.prod.conf:/var/opt/mssql/mssql.conf:ro");
        assertThat(sqlServer)
                .contains("forceencryption = 1")
                .contains("tlsprotocols = 1.2")
                .contains("tlscert = /var/opt/mssql/secrets/sitiopro-sqlserver.crt")
                .contains("tlskey = /var/opt/mssql/secrets/sitiopro-sqlserver.key");
    }

    private PropertySourcesPropertyResolver productionProperties() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(new MapPropertySource("test-database", Map.of(
                "DB_HOST", "sql.example.internal",
                "DB_PORT", "1433",
                "DB_NAME", "sitio_db",
                "DB_USERNAME", "app",
                "DB_PASSWORD", "fictitious")));
        loader.load("application-prod", new ClassPathResource("application-prod.yml")).forEach(sources::addLast);
        loader.load("application", new ClassPathResource("application.yml")).forEach(sources::addLast);
        return new PropertySourcesPropertyResolver(sources);
    }
}
