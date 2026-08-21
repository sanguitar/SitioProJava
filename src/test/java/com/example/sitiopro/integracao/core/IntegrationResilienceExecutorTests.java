package com.example.sitiopro.integracao.core;

import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationResilienceExecutorTests {

    @Test
    void abreCircuitBreakerAposFalhasTransitoriasRepetidas() {
        IntegracaoCoreProperties properties = new IntegracaoCoreProperties();
        properties.setOpenMeteoLimitPerMinute(20);
        IntegrationResilienceExecutor executor = new IntegrationResilienceExecutor(properties);
        AtomicInteger chamadasAoProvedor = new AtomicInteger();

        assertThatThrownBy(() -> executor.executar(FonteIntegracao.OPEN_METEO, () -> falhar(chamadasAoProvedor)))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("API_HTTP_5XX");

        assertThatThrownBy(() -> executor.executar(FonteIntegracao.OPEN_METEO, () -> falhar(chamadasAoProvedor)))
                .isInstanceOf(IntegracaoHttpException.class)
                .extracting("code")
                .isEqualTo("CIRCUIT_BREAKER_ABERTO");
        assertThat(chamadasAoProvedor).hasValue(4);
    }

    private String falhar(AtomicInteger chamadasAoProvedor) {
        chamadasAoProvedor.incrementAndGet();
        throw new IntegracaoHttpException(
                "API_HTTP_5XX",
                "O provedor retornou uma falha temporária.",
                500,
                IntegracaoHttpException.Tipo.TRANSIENTE);
    }
}
