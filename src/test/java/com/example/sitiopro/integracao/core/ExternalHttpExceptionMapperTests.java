package com.example.sitiopro.integracao.core;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalHttpExceptionMapperTests {

    @Test
    void mapeiaTimeoutDoClienteHttpSemExporDetalhesInternos() {
        ResourceAccessException falha = new ResourceAccessException(
                "I/O externo",
                new HttpTimeoutException("request timed out"));

        IntegracaoHttpException resultado = ExternalHttpExceptionMapper.mapear(falha);

        assertThat(resultado.getCode()).isEqualTo("API_TIMEOUT");
        assertThat(resultado.retryable()).isTrue();
        assertThat(resultado.getMessage()).doesNotContain("request timed out");
    }
}
