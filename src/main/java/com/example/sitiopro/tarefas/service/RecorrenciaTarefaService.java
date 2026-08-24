package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.OrigemTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TarefaRecorrencia;
import com.example.sitiopro.tarefas.entity.TipoEventoOperacional;
import com.example.sitiopro.tarefas.repository.TarefaRecorrenciaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RecorrenciaTarefaService {

    private static final Logger log = LoggerFactory.getLogger(RecorrenciaTarefaService.class);

    private final TarefaRecorrenciaRepository recorrenciaRepository;
    private final TarefaRepository tarefaRepository;
    private final HistoricoOperacionalService historicoService;
    private final TarefasAlertasProperties properties;
    private final Clock clock;

    public RecorrenciaTarefaService(TarefaRecorrenciaRepository recorrenciaRepository,
            TarefaRepository tarefaRepository,
            HistoricoOperacionalService historicoService,
            TarefasAlertasProperties properties,
            Clock clock) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.tarefaRepository = tarefaRepository;
        this.historicoService = historicoService;
        this.properties = properties;
        this.clock = clock;
    }

    public int gerarOcorrenciasVencidas() {
        LocalDateTime agora = LocalDateTime.now(clock);
        int limite = properties.getRecorrenciasLimitePorExecucao();
        List<Long> ids = recorrenciaRepository.buscarIdsVencidos(agora, PageRequest.of(0, limite));
        int geradas = 0;
        for (Long id : ids) {
            if (geradas >= limite) {
                break;
            }
            TarefaRecorrencia recorrencia = recorrenciaRepository.buscarParaAtualizacao(id).orElse(null);
            if (recorrencia == null || !recorrencia.isAtiva()) {
                continue;
            }
            while (!recorrencia.getProximaOcorrenciaEm().isAfter(agora) && geradas < limite) {
                LocalDateTime ocorrenciaEm = recorrencia.getProximaOcorrenciaEm();
                if (!tarefaRepository.existsByRecorrenciaOrigemIdAndOcorrenciaProgramadaEm(id, ocorrenciaEm)) {
                    gerar(recorrencia, ocorrenciaEm);
                    geradas++;
                }
                recorrencia.setProximaOcorrenciaEm(proxima(recorrencia, ocorrenciaEm));
            }
        }
        return geradas;
    }

    private void gerar(TarefaRecorrencia recorrencia, LocalDateTime ocorrenciaEm) {
        Tarefa modelo = recorrencia.getTarefaModelo();
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(modelo.getTitulo());
        tarefa.setDescricao(modelo.getDescricao());
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(modelo.getPrioridade());
        tarefa.setDataVencimento(ocorrenciaEm);
        tarefa.setResponsavel(modelo.getResponsavel());
        tarefa.setCriadoPorUsuario(modelo.getCriadoPorUsuario());
        tarefa.setOrigem(OrigemTarefa.AUTOMATICA);
        tarefa.setModuloOrigem(ModuloOrigem.TAREFAS);
        tarefa.setReferenciaOrigem("RECORRENCIA:" + recorrencia.getId());
        tarefa.setRecorrenciaOrigem(recorrencia);
        tarefa.setOcorrenciaProgramadaEm(ocorrenciaEm);
        tarefa.setAtivo(true);
        tarefa = tarefaRepository.save(tarefa);
        historicoService.registrarTarefa(tarefa, TipoEventoOperacional.RECORRENCIA_GERADA,
                null, "sistema", "Ocorrência gerada pela recorrência " + recorrencia.getId() + ".");
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "tarefa.recurrence.generated",
                "module", "tarefas",
                "tarefa.id", tarefa.getId(),
                "tarefa.recorrencia.id", recorrencia.getId()))) {
            log.info("Ocorrência recorrente gerada.");
        }
    }

    private LocalDateTime proxima(TarefaRecorrencia recorrencia, LocalDateTime base) {
        return switch (recorrencia.getTipo()) {
            case DIARIA -> base.plusDays(1);
            case SEMANAL -> base.plusWeeks(1);
            case MENSAL -> base.plusMonths(1);
            case INTERVALO_DIAS -> base.plusDays(recorrencia.getIntervaloDias());
            case NENHUMA -> throw new IllegalStateException("Definição persistida não pode ter recorrência NENHUMA.");
        };
    }
}
