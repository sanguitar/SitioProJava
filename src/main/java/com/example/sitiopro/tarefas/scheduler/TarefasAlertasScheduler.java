package com.example.sitiopro.tarefas.scheduler;

import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.service.AutomacaoTarefasService;
import com.example.sitiopro.tarefas.service.SqlServerApplicationLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "sitiopro.tarefas", name = "scheduler-enabled",
        havingValue = "true", matchIfMissing = true)
public class TarefasAlertasScheduler {

    private static final Logger log = LoggerFactory.getLogger(TarefasAlertasScheduler.class);
    private static final String LOCK_ALERTAS = "sitiopro:tarefas:avaliar-alertas";
    private static final String LOCK_RECORRENCIAS = "sitiopro:tarefas:gerar-recorrencias";

    private final AutomacaoTarefasService automacaoService;
    private final SqlServerApplicationLock applicationLock;

    public TarefasAlertasScheduler(AutomacaoTarefasService automacaoService,
            SqlServerApplicationLock applicationLock) {
        this.automacaoService = automacaoService;
        this.applicationLock = applicationLock;
    }

    @Scheduled(
            fixedDelayString = "${sitiopro.tarefas.alertas-intervalo:PT5M}",
            initialDelayString = "${sitiopro.tarefas.scheduler-atraso-inicial:PT30S}")
    public void avaliarAlertas() {
        executar("alerta.scheduler.completed",
                () -> applicationLock.executar(LOCK_ALERTAS, automacaoService::avaliarAlertas).orElse(false));
    }

    @Scheduled(
            fixedDelayString = "${sitiopro.tarefas.recorrencias-intervalo:PT1M}",
            initialDelayString = "${sitiopro.tarefas.scheduler-atraso-inicial:PT30S}")
    public void gerarRecorrencias() {
        executar("tarefa.recurrence.scheduler.completed",
                () -> applicationLock.executar(LOCK_RECORRENCIAS, automacaoService::gerarRecorrencias).orElse(0));
    }

    private void executar(String evento, Supplier<?> operacao) {
        long inicio = System.nanoTime();
        try {
            Object resultado = operacao.get();
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", evento,
                    "module", "tarefas",
                    "event.result", String.valueOf(resultado),
                    "event.duration", System.nanoTime() - inicio))) {
                log.info("Ciclo automático do módulo de tarefas concluído.");
            }
        } catch (RuntimeException ex) {
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "tarefas.scheduler.failed",
                    "module", "tarefas",
                    "error.type", ex.getClass().getName(),
                    "event.duration", System.nanoTime() - inicio))) {
                log.error("Falha no ciclo automático do módulo de tarefas.");
            }
        }
    }
}
