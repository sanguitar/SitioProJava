package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sitiopro.integracoes.open-meteo.enabled", havingValue = "true")
public class OpenMeteoScheduler {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoScheduler.class);

    private final IntegracaoOrquestrador orquestrador;

    public OpenMeteoScheduler(IntegracaoOrquestrador orquestrador) {
        this.orquestrador = orquestrador;
    }

    @Scheduled(
            cron = "${sitiopro.integracoes.open-meteo.cron:0 17 */3 * * *}",
            zone = "${SITIOPRO_INTEGRATIONS_ZONE:UTC}")
    public void sincronizar() {
        try {
            orquestrador.sincronizar(FonteIntegracao.OPEN_METEO);
        } catch (IntegracaoOperacaoException ex) {
            log.warn("Agendamento Open-Meteo não concluiu: {}", ex.getCode());
        }
    }
}
