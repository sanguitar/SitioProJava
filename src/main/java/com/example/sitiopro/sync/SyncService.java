package com.example.sitiopro.sync;

import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {
    private final SyncCommandService commands;
    private final SyncChangeLogRepository changeLog;
    private final SyncSnapshotMapper snapshots;
    private final Clock clock;

    public SyncService(SyncCommandService commands, SyncChangeLogRepository changeLog,
            SyncSnapshotMapper snapshots, Clock clock) {
        this.commands = commands;
        this.changeLog = changeLog;
        this.snapshots = snapshots;
        this.clock = clock;
    }

    public SyncDtos.SyncResponse sincronizar(SyncDtos.SyncRequest request, UsuarioAtor ator) {
        List<SyncDtos.CommandResult> resultados = new ArrayList<>();
        List<SyncDtos.Conflict> conflitos = new ArrayList<>();
        for (SyncDtos.SyncCommand command : request.changes()) {
            SyncDtos.StoredOutcome outcome = commands.processar(request.deviceId(), command, ator);
            if (outcome.result() != null) resultados.add(outcome.result());
            if (outcome.conflict() != null) conflitos.add(outcome.conflict());
        }
        PullResult pull = pull(request.cursorValue(), request.limitValue());
        return new SyncDtos.SyncResponse(List.copyOf(resultados), List.copyOf(conflitos), pull.changes(),
                pull.nextCursor(), pull.hasMore(), clock.instant());
    }

    @Transactional(readOnly = true)
    public SyncDtos.BootstrapResponse bootstrap() {
        long cursor = changeLog.cursorAtual();
        Instant agora = clock.instant();
        return new SyncDtos.BootstrapResponse(snapshots.bootstrap(agora), cursor, agora);
    }

    @Transactional(readOnly = true)
    public PullResult pull(long cursor, int limit) {
        int tamanho = Math.max(1, Math.min(limit, 500));
        List<SyncChangeLog> encontrados = changeLog.buscarDepois(Math.max(0, cursor), PageRequest.of(0, tamanho + 1));
        boolean hasMore = encontrados.size() > tamanho;
        List<SyncChangeLog> pagina = hasMore ? encontrados.subList(0, tamanho) : encontrados;
        List<SyncDtos.EntityChange> changes = pagina.stream().map(this::mapear).toList();
        long nextCursor = pagina.isEmpty() ? Math.max(0, cursor) : pagina.getLast().getChangeId();
        return new PullResult(changes, nextCursor, hasMore);
    }

    private SyncDtos.EntityChange mapear(SyncChangeLog change) {
        if (change.getOperation() == SyncDtos.Operation.DELETE) {
            return new SyncDtos.EntityChange(change.getChangeId(), change.getEntityType(),
                    change.getEntityPublicId(), change.getOperation(), change.getEntityVersion(),
                    change.getOccurredAt(), null);
        }
        SyncSnapshot snapshot = snapshots.snapshot(change.getEntityType(), change.getEntityPublicId());
        return new SyncDtos.EntityChange(change.getChangeId(), change.getEntityType(),
                change.getEntityPublicId(), change.getOperation(), snapshot.version(),
                change.getOccurredAt(), snapshot.payload());
    }

    public record PullResult(List<SyncDtos.EntityChange> changes, long nextCursor, boolean hasMore) {
    }
}
