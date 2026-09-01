package com.example.sitiopro.integracao.embrapa.agrofit;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AgrofitClientTests {

    @Test
    void consultaRecorteDeCulturasComBearerToken() {
        Fixture fixture = cliente("token-teste");
        fixture.server().expect(requestTo(containsString("/culturas?page=1")))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-teste"))
                .andRespond(withSuccess("[{\"nome\":\"Café\"},{\"nome\":\"Milho\"}]",
                        MediaType.APPLICATION_JSON));

        assertThat(fixture.client().buscarCulturas(1))
                .extracting(AgrofitCulturaPayload::nome)
                .containsExactly("Café", "Milho");
        fixture.server().verify();
    }

    @Test
    void credencialAusenteFalhaAntesDaRede() {
        Fixture fixture = cliente("");

        assertThatThrownBy(() -> fixture.client().buscarCulturas(1))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("AGROFIT_NAO_CONFIGURADO");
        fixture.server().verify();
    }

    @Test
    void erro500RecebeDuasTentativas() {
        Fixture fixture = cliente("token-teste");
        fixture.server().expect(ExpectedCount.times(2), requestTo(containsString("/culturas?page=1")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        assertThatThrownBy(() -> fixture.client().buscarCulturas(1))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_HTTP_5XX");
        fixture.server().verify();
    }

    private Fixture cliente(String token) {
        AgrofitProperties properties = new AgrofitProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://agrofit.test");
        properties.setToken(token);
        IntegracaoCoreProperties coreProperties = new IntegracaoCoreProperties();
        coreProperties.setAgrofitLimitPerMinute(10);
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new AgrofitClient(builder.build(), properties,
                new IntegrationResilienceExecutor(coreProperties)), server);
    }

    private record Fixture(AgrofitClient client, MockRestServiceServer server) {
    }
}
