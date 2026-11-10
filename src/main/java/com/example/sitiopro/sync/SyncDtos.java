package com.example.sitiopro.sync;

import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SyncDtos {
    private SyncDtos() {
    }

    public enum EntityType { TAREFA, AGUA_MEDICAO }
    public enum Operation { UPSERT, DELETE }
    public enum ResultStatus { APPLIED }

    public record SyncRequest(
            @NotNull UUID deviceId,
            @PositiveOrZero Long cursor,
            @NotNull @Size(max = 100) List<@Valid SyncCommand> changes,
            @Min(1) @Max(500) Integer limit) {
        public long cursorValue() { return cursor == null ? 0 : cursor; }
        public int limitValue() { return limit == null ? 500 : limit; }
    }

    public record SyncCommand(
            @NotNull UUID operationId,
            @NotNull EntityType entityType,
            @NotNull UUID entityId,
            @PositiveOrZero Long baseVersion,
            @NotNull Operation operation,
            @NotNull JsonNode payload) {
    }

    public record CommandResult(UUID operationId, EntityType entityType, UUID entityId,
            ResultStatus status, long version, JsonNode payload) {
    }

    public record Conflict(UUID operationId, EntityType entityType, UUID entityId,
            Long sentVersion, long currentVersion, String resolution, JsonNode serverSnapshot) {
    }

    public record EntityChange(long changeId, EntityType entityType, UUID entityId,
            Operation operation, long version, Instant occurredAt, JsonNode payload) {
    }

    public record SyncResponse(List<CommandResult> results, List<Conflict> conflicts,
            List<EntityChange> changes, long nextCursor, boolean hasMore, Instant serverTime) {
    }

    public record BootstrapResponse(List<EntityChange> entities, long cursor, Instant serverTime) {
    }

    public record TarefaPayload(
            @NotBlank @Size(max = 160) String titulo,
            @Size(max = 2000) String descricao,
            @NotNull PrioridadeTarefa prioridade,
            Instant dataVencimento,
            Long responsavelId,
            TipoRecorrencia recorrencia,
            @Min(1) @Max(365) Integer intervaloDias,
            StatusTarefa status) {
    }

    public record AguaMedicaoPayload(
            @NotNull Long recursoOrigemId,
            @NotNull Instant dataOperacao,
            @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 3) BigDecimal quantidadeLitros,
            @Size(max = 1000) String observacao) {
    }

    record StoredOutcome(CommandResult result, Conflict conflict) {
        static StoredOutcome result(CommandResult value) { return new StoredOutcome(value, null); }
        static StoredOutcome conflict(Conflict value) { return new StoredOutcome(null, value); }
    }
}
