package com.example.sitiopro.integracao.embrapa.agrofit;

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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgrofitClientTests {

    private HttpServer server;

    @AfterEach
    void encerrarServidor() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void consultaRecorteDeCulturasComBearerToken() throws IOException {
        iniciarServidor(exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer token-teste");
            assertThat(exchange.getRequestURI().getQuery()).contains("page=1");
            responder(exchange, 200, "[{\"nome\":\"Café\"},{\"nome\":\"Milho\"}]");
        });

        assertThat(cliente("token-teste").buscarCulturas(1))
                .extracting(AgrofitCulturaPayload::nome)
                .containsExactly("Café", "Milho");
    }

    @Test
    void credencialAusenteFalhaAntesDaRede() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            responder(exchange, 200, "[]");
        });

        assertThatThrownBy(() -> cliente("").buscarCulturas(1))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("AGROFIT_NAO_CONFIGURADO");
        assertThat(chamadas).hasValue(0);
    }

    @Test
    void erro500RecebeDuasTentativas() throws IOException {
        AtomicInteger chamadas = new AtomicInteger();
        iniciarServidor(exchange -> {
            chamadas.incrementAndGet();
            responder(exchange, 500, "{}");
        });

        assertThatThrownBy(() -> cliente("token-teste").buscarCulturas(1))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_HTTP_5XX");
        assertThat(chamadas).hasValue(2);
    }

    private AgrofitClient cliente(String token) {
        AgrofitProperties properties = new AgrofitProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setToken(token);
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(Duration.ofSeconds(1));
        IntegracaoCoreProperties coreProperties = new IntegracaoCoreProperties();
        coreProperties.setAgrofitLimitPerMinute(10);
        return new AgrofitClient(
                new IntegracaoHttpConfig().agrofitRestClient(properties),
                properties,
                new IntegrationResilienceExecutor(coreProperties));
    }

    private void iniciarServidor(Manipulador manipulador) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/culturas", exchange -> manipulador.tratar(exchange));
        server.start();
    }

    private void responder(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface
    private interface Manipulador {
        void tratar(HttpExchange exchange) throws IOException;
    }
}
