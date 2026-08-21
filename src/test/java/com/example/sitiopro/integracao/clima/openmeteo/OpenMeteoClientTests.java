package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegrationResilienceExecutor;
import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import com.example.sitiopro.integracao.core.config.IntegracaoHttpConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenMeteoClientTests {

    private HttpServer server;
    private ExecutorService executor;

    @AfterEach
    void encerrarServidor() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void lePayloadValidoSemExporDetalhesHttpAoDominio() throws IOException {
        iniciarServidor(exchange -> responder(exchange, 200, payloadValido(), null));

        OpenMeteoResponse response = cliente(Duration.ofSeconds(1)).buscarPrevisao();

        assertThat(response.hourly().time()).hasSize(1);
        assertThat(response.hourly().temperature2m().getFirst()).isEqualByComparingTo("28.4");
    }

    @Test
    void payloadInvalidoFalhaSemRetry() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            responder(exchange, 200, "{\"timezone\":\"UTC\",\"hourly\":{\"time\":[]}}", null);
        });

        assertThatThrownBy(() -> cliente(Duration.ofSeconds(1)).buscarPrevisao())
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("OPEN_METEO_PAYLOAD_INVALIDO");
        assertThat(chamadas).hasValue(1);
    }

    @Test
    void timeoutRecebeRetryLimitado() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            try {
                Thread.sleep(150);
                responder(exchange, 200, payloadValido(), null);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                exchange.close();
            }
        });

        assertThatThrownBy(() -> cliente(Duration.ofMillis(30)).buscarPrevisao())
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isIn("API_TIMEOUT", "API_CONEXAO");
        assertThat(chamadas.get()).isBetween(1, 3);
    }

    @Test
    void rateLimitRespeitaRetryAfterSemLoopAutomatico() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            responder(exchange, 429, "{}", "120");
        });

        assertThatThrownBy(() -> cliente(Duration.ofSeconds(1)).buscarPrevisao())
                .isInstanceOfSatisfying(IntegracaoHttpException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("API_RATE_LIMIT");
                    assertThat(ex.getRetryAfterSeconds()).isEqualTo(120);
                });
        assertThat(chamadas).hasValue(1);
    }

    @Test
    void erro500RecebeTresTentativas() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            responder(exchange, 500, "{}", null);
        });

        assertThatThrownBy(() -> cliente(Duration.ofSeconds(1)).buscarPrevisao())
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_HTTP_5XX");
        assertThat(chamadas).hasValue(3);
    }

    private OpenMeteoClient cliente(Duration readTimeout) {
        OpenMeteoProperties properties = new OpenMeteoProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setLatitude("-3.0");
        properties.setLongitude("-60.0");
        properties.setTimezone("UTC");
        properties.setReadTimeout(readTimeout);
        properties.setConnectTimeout(Duration.ofSeconds(1));
        IntegracaoCoreProperties coreProperties = new IntegracaoCoreProperties();
        coreProperties.setOpenMeteoLimitPerMinute(20);
        return new OpenMeteoClient(
                new IntegracaoHttpConfig().openMeteoRestClient(properties),
                properties,
                new IntegrationResilienceExecutor(coreProperties));
    }

    private void iniciarServidor(Manipulador manipulador) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.createContext("/v1/forecast", exchange -> manipulador.tratar(exchange));
        server.start();
    }

    private void responder(HttpExchange exchange, int status, String body, String retryAfter) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (retryAfter != null) {
            exchange.getResponseHeaders().set("Retry-After", retryAfter);
        }
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String payloadValido() {
        return """
                {
                  "timezone": "UTC",
                  "hourly": {
                    "time": ["2026-08-21T12:00"],
                    "temperature_2m": [28.4],
                    "relative_humidity_2m": [78],
                    "precipitation": [1.2],
                    "precipitation_probability": [60],
                    "wind_speed_10m": [8.0],
                    "wind_gusts_10m": [14.0],
                    "et0_fao_evapotranspiration": [0.12],
                    "soil_moisture_0_to_1cm": [0.31],
                    "weather_code": [61]
                  }
                }
                """;
    }

    @FunctionalInterface
    private interface Manipulador {
        void tratar(HttpExchange exchange) throws IOException;
    }
}
