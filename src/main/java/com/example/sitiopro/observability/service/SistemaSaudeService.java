package com.example.sitiopro.observability.service;

import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.shared.observability.ObservabilityProperties;
import com.example.sitiopro.shared.observability.RequestCorrelation;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class SistemaSaudeService {

    private final HealthEndpoint healthEndpoint;
    private final Environment environment;
    private final ObservabilityProperties observabilityProperties;
    private final HttpClient httpClient;

    public SistemaSaudeService(HealthEndpoint healthEndpoint,
            Environment environment,
            ObservabilityProperties observabilityProperties) {
        this.healthEndpoint = healthEndpoint;
        this.environment = environment;
        this.observabilityProperties = observabilityProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500))
                .build();
    }

    public SistemaSaudeResumo resumo() {
        return new SistemaSaudeResumo(
                statusAplicacao(),
                statusComponente("db"),
                Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()),
                environment.getProperty("info.app.version", "0.0.1-SNAPSHOT"),
                observabilityProperties.getEnvironment(),
                statusObservabilidade(),
                RequestCorrelation.currentRequestId());
    }

    private String statusAplicacao() {
        return healthEndpoint.health().getStatus().getCode();
    }

    private String statusComponente(String nome) {
        try {
            HealthComponent component = healthEndpoint.healthForPath(nome);
            return component == null ? "UNKNOWN" : component.getStatus().getCode();
        } catch (RuntimeException ex) {
            return "UNKNOWN";
        }
    }

    private String statusObservabilidade() {
        if (!observabilityProperties.isEnabled()) {
            return "DESABILITADA";
        }
        if (!StringUtils.hasText(observabilityProperties.getApmServerUrl())) {
            return "CONFIGURACAO_INCOMPLETA";
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(observabilityProperties.getApmServerUrl()))
                    .timeout(Duration.ofMillis(700))
                    .GET()
                    .build();
            int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            return status >= 200 && status < 500 ? "DISPONIVEL" : "INDISPONIVEL";
        } catch (Exception ex) {
            return "INDISPONIVEL";
        }
    }
}
