package com.example.sitiopro.integracao.embrapa.agrofit;

import com.example.sitiopro.integracao.core.ExternalHttpExceptionMapper;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegrationResilienceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class AgrofitClient {

    private final RestClient restClient;
    private final AgrofitProperties properties;
    private final IntegrationResilienceExecutor resilienceExecutor;

    public AgrofitClient(@Qualifier("agrofitRestClient") RestClient restClient,
            AgrofitProperties properties, IntegrationResilienceExecutor resilienceExecutor) {
        this.restClient = restClient;
        this.properties = properties;
        this.resilienceExecutor = resilienceExecutor;
    }

    public List<AgrofitCulturaPayload> buscarCulturas(int pagina) {
        if (!properties.configurada()) {
            throw new IntegracaoHttpException(
                    "AGROFIT_NAO_CONFIGURADO", "Credencial oficial do Agrofit não configurada.", null,
                    IntegracaoHttpException.Tipo.PERMANENTE);
        }
        return resilienceExecutor.executar(
                FonteIntegracao.EMBRAPA_AGROFIT,
                () -> executarRequest(Math.max(1, pagina)));
    }

    private List<AgrofitCulturaPayload> executarRequest(int pagina) {
        try {
            AgrofitCulturaPayload[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/culturas").queryParam("page", pagina).build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                    .retrieve()
                    .body(AgrofitCulturaPayload[].class);
            if (response == null) {
                throw new IntegracaoHttpException(
                        "AGROFIT_PAYLOAD_VAZIO", "O Agrofit retornou uma resposta vazia.", null,
                        IntegracaoHttpException.Tipo.PERMANENTE);
            }
            List<AgrofitCulturaPayload> culturas = Arrays.stream(response)
                    .filter(item -> item != null && item.nome() != null && !item.nome().isBlank())
                    .toList();
            if (response.length > 0 && culturas.isEmpty()) {
                throw new IntegracaoHttpException(
                        "AGROFIT_PAYLOAD_INVALIDO", "A resposta do Agrofit não contém culturas válidas.", null,
                        IntegracaoHttpException.Tipo.PERMANENTE);
            }
            return culturas;
        } catch (RuntimeException ex) {
            throw ExternalHttpExceptionMapper.mapear(ex);
        }
    }
}
