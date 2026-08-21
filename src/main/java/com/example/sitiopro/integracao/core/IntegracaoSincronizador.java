package com.example.sitiopro.integracao.core;

import java.time.Duration;
import java.time.ZoneId;

public interface IntegracaoSincronizador {

    FonteIntegracao fonte();

    boolean habilitada();

    boolean configurada();

    String cron();

    ZoneId zonaAgendamento();

    Duration limiteDesatualizacao();

    boolean usaCredencial();

    ResultadoSincronizacao sincronizar();
}
