package com.example.sitiopro.integracao.embrapa.agrofit;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sitiopro.integracoes.embrapa-agrofit.enabled", havingValue = "true")
public class AgrofitScheduler {

    private static final Logger log = LoggerFactory.getLogger(AgrofitScheduler.class);

    private final IntegracaoOrquestrador orquestrador;

    public AgrofitScheduler(IntegracaoOrquestrador orquestrador) {
        this.orquestrador = orquestrador;
    }

    @Scheduled(
            cron = "${sitiopro.integracoes.embrapa-agrofit.cron:0 29 3 * * MON}",
            zone = "${SITIOPRO_INTEGRATIONS_ZONE:UTC}")
    public void sincronizar() {
        try {
            orquestrador.sincronizar(FonteIntegracao.EMBRAPA_AGROFIT);
        } catch (IntegracaoOperacaoException ex) {
            log.warn("Agendamento Agrofit não concluiu: {}", ex.getCode());
        }
    }
}
