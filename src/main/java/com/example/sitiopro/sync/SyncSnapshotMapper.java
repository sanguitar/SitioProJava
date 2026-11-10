package com.example.sitiopro.sync;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.agua.dto.OperacaoAguaResumo;
import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import com.example.sitiopro.agua.repository.OperacaoAguaRepository;
import com.example.sitiopro.agua.service.AguaService;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SyncSnapshotMapper {
    private final TarefaRepository tarefas;
    private final OperacaoAguaRepository operacoesAgua;
    private final TarefaService tarefaService;
    private final AguaService aguaService;
    private final ConfiguracaoOperacionalService configuracao;
    private final ObjectMapper objectMapper;

    public SyncSnapshotMapper(TarefaRepository tarefas, OperacaoAguaRepository operacoesAgua,
            TarefaService tarefaService, AguaService aguaService,
            ConfiguracaoOperacionalService configuracao, ObjectMapper objectMapper) {
        this.tarefas = tarefas;
        this.operacoesAgua = operacoesAgua;
        this.tarefaService = tarefaService;
        this.aguaService = aguaService;
        this.configuracao = configuracao;
        this.objectMapper = objectMapper;
    }

    public SyncSnapshot snapshot(SyncDtos.EntityType type, UUID publicId) {
        return switch (type) {
            case TAREFA -> tarefa(publicId);
            case AGUA_MEDICAO -> agua(publicId);
        };
    }

    public List<SyncDtos.EntityChange> bootstrap(Instant instante) {
        List<SyncDtos.EntityChange> entidades = new ArrayList<>();
        tarefas.findAllByOrderByIdAsc().forEach(tarefa -> {
            SyncSnapshot snapshot = tarefa(tarefa.getPublicId());
            entidades.add(new SyncDtos.EntityChange(0, SyncDtos.EntityType.TAREFA, tarefa.getPublicId(),
                    SyncDtos.Operation.UPSERT, snapshot.version(), instante, snapshot.payload()));
        });
        operacoesAgua.findByTipoOrderByIdAsc(TipoOperacaoAgua.MEDICAO_NIVEL).forEach(operacao -> {
            SyncSnapshot snapshot = agua(operacao.getPublicId());
            entidades.add(new SyncDtos.EntityChange(0, SyncDtos.EntityType.AGUA_MEDICAO, operacao.getPublicId(),
                    SyncDtos.Operation.UPSERT, snapshot.version(), instante, snapshot.payload()));
        });
        return List.copyOf(entidades);
    }

    private SyncSnapshot tarefa(UUID publicId) {
        TarefaDetalhe detalhe = tarefaService.detalharPorPublicId(publicId);
        ZoneId zone = configuracao.obter().zoneId();
        TarefaView view = new TarefaView(detalhe.titulo(), detalhe.descricao(), detalhe.status(),
                detalhe.prioridade(), instante(detalhe.dataCriacao(), zone), instante(detalhe.dataInicio(), zone),
                instante(detalhe.dataVencimento(), zone), instante(detalhe.dataConclusao(), zone),
                detalhe.responsavelId(), detalhe.responsavelNome(), detalhe.origem(), detalhe.moduloOrigem(),
                detalhe.referenciaOrigem(), detalhe.recorrencia(), detalhe.intervaloDias(),
                instante(detalhe.proximaOcorrenciaEm(), zone), detalhe.recorrenciaAtiva(), detalhe.ativo());
        return new SyncSnapshot(detalhe.versao(), objectMapper.valueToTree(view));
    }

    private SyncSnapshot agua(UUID publicId) {
        OperacaoAguaResumo detalhe = aguaService.detalharOperacaoPorPublicId(publicId);
        ZoneId zone = configuracao.obter().zoneId();
        AguaView view = new AguaView(detalhe.tipo(), instante(detalhe.dataOperacao(), zone),
                detalhe.recursoOrigemId(), detalhe.recursoOrigemNome(), detalhe.quantidadeLitros(),
                detalhe.volumeOrigemAntesLitros(), detalhe.volumeOrigemDepoisLitros(), detalhe.observacao(),
                instante(detalhe.criadoEm(), zone), detalhe.criadoPor());
        return new SyncSnapshot(detalhe.versao(), objectMapper.valueToTree(view));
    }

    private Instant instante(LocalDateTime valor, ZoneId zone) {
        return valor == null ? null : valor.atZone(zone).toInstant();
    }

    private record TarefaView(Object titulo, Object descricao, Object status, Object prioridade,
            Instant criadoEm, Instant iniciadoEm, Instant vencimentoEm, Instant concluidoEm,
            Long responsavelId, String responsavelNome, Object origem, Object moduloOrigem,
            String referenciaOrigem, Object recorrencia, Integer intervaloDias,
            Instant proximaOcorrenciaEm, boolean recorrenciaAtiva, boolean ativo) {
    }

    private record AguaView(Object tipo, Instant dataOperacao, Long recursoOrigemId,
            String recursoOrigemNome, Object quantidadeLitros, Object volumeAntesLitros,
            Object volumeDepoisLitros, String observacao, Instant criadoEm, String criadoPor) {
    }
}
