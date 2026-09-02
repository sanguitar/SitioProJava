package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegrationResilienceExecutor;
import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.padrao;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoClientTests {

    @Test
    void semCoordenadasNaoFazRequisicaoExterna() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        var semLocalizacao = new com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura(
                "Teste", "UTC", null, null, 21, 2, 0, null, null);
        assertThatThrownBy(() -> fixture.client().buscarPrevisao(semLocalizacao))
                .isInstanceOf(IntegracaoHttpException.class).extracting("code").isEqualTo("OPEN_METEO_NAO_CONFIGURADO");
        fixture.server().verify();
    }

    @Test
    void lePayloadValidoSemExporDetalhesHttpAoDominio() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        fixture.server().expect(requestTo(containsString("/v1/forecast")))
                .andExpect(queryParam("latitude", "-3"))
                .andExpect(queryParam("longitude", "-60"))
                .andExpect(queryParam("timezone", "UTC"))
                .andRespond(withSuccess(payloadValido(), MediaType.APPLICATION_JSON));

        OpenMeteoResponse response = fixture.client().buscarPrevisao(padrao());

        assertThat(response.hourly().time()).hasSize(1);
        assertThat(response.hourly().temperature2m().getFirst()).isEqualByComparingTo("28.4");
        fixture.server().verify();
    }

    @Test
    void payloadInvalidoFalhaSemRetry() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        fixture.server().expect(ExpectedCount.once(), requestTo(containsString("/v1/forecast")))
                .andRespond(withSuccess("{\"timezone\":\"UTC\",\"hourly\":{\"time\":[]}}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.client().buscarPrevisao(padrao()))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("OPEN_METEO_PAYLOAD_INVALIDO");
        fixture.server().verify();
    }

    @Test
    void timeoutRecebeRetryLimitado() {
        AtomicInteger chamadas = new AtomicInteger();
        Fixture fixture = cliente(Duration.ofMillis(30));
        fixture.server().expect(ExpectedCount.manyTimes(), requestTo(containsString("/v1/forecast")))
                .andRespond(request -> {
                    chamadas.incrementAndGet();
                    throw new SocketTimeoutException("timeout simulado");
                });

        assertThatThrownBy(() -> fixture.client().buscarPrevisao(padrao()))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_TIMEOUT");
        assertThat(chamadas.get()).isBetween(1, 3);
        fixture.server().verify();
    }

    @Test
    void rateLimitRespeitaRetryAfterSemLoopAutomatico() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        fixture.server().expect(ExpectedCount.once(), requestTo(containsString("/v1/forecast")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, "120")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        assertThatThrownBy(() -> fixture.client().buscarPrevisao(padrao()))
                .isInstanceOfSatisfying(IntegracaoHttpException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("API_RATE_LIMIT");
                    assertThat(ex.getRetryAfterSeconds()).isEqualTo(120);
                });
        fixture.server().verify();
    }

    @Test
    void erro400FalhaSemRetry() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        fixture.server().expect(ExpectedCount.once(), requestTo(containsString("/v1/forecast")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        assertThatThrownBy(() -> fixture.client().buscarPrevisao(padrao()))
                .isInstanceOfSatisfying(IntegracaoHttpException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("API_REQUISICAO_INVALIDA");
                    assertThat(ex.getHttpStatus()).isEqualTo(400);
                });
        fixture.server().verify();
    }

    @Test
    void erro500RecebeTresTentativas() {
        Fixture fixture = cliente(Duration.ofSeconds(1));
        fixture.server().expect(ExpectedCount.times(3), requestTo(containsString("/v1/forecast")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        assertThatThrownBy(() -> fixture.client().buscarPrevisao(padrao()))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_HTTP_5XX");
        fixture.server().verify();
    }

    private Fixture cliente(Duration readTimeout) {
        OpenMeteoProperties properties = new OpenMeteoProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://open-meteo.test");
        properties.setReadTimeout(readTimeout);
        properties.setConnectTimeout(Duration.ofSeconds(1));
        IntegracaoCoreProperties coreProperties = new IntegracaoCoreProperties();
        coreProperties.setOpenMeteoLimitPerMinute(20);
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new OpenMeteoClient(builder.build(), properties,
                new IntegrationResilienceExecutor(coreProperties)), server);
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

    private record Fixture(OpenMeteoClient client, MockRestServiceServer server) {
    }
}
