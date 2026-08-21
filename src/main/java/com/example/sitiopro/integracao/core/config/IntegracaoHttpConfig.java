package com.example.sitiopro.integracao.core.config;

import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.embrapa.agrofit.AgrofitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({OpenMeteoProperties.class, AgrofitProperties.class})
public class IntegracaoHttpConfig {

    @Bean("openMeteoRestClient")
    public RestClient openMeteoRestClient(OpenMeteoProperties properties) {
        return criarCliente(properties.getBaseUrl(), properties.getConnectTimeout(), properties.getReadTimeout(),
                "SitioPro/0.0.1 (integration=open-meteo)");
    }

    @Bean("agrofitRestClient")
    public RestClient agrofitRestClient(AgrofitProperties properties) {
        return criarCliente(properties.getBaseUrl(), properties.getConnectTimeout(), properties.getReadTimeout(),
                "SitioPro/0.0.1 (integration=embrapa-agrofit)");
    }

    private RestClient criarCliente(String baseUrl, Duration connectTimeout, Duration readTimeout, String userAgent) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }
}
