package com.example.sitiopro.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_change_log")
public class SyncChangeLog {
    @Id
    @Column(name = "change_id")
    private Long changeId;
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private SyncDtos.EntityType entityType;
    @Column(name = "entity_public_id", nullable = false)
    private UUID entityPublicId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SyncDtos.Operation operation;
    @Column(name = "entity_version", nullable = false)
    private long entityVersion;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected SyncChangeLog() {
    }

    public Long getChangeId() { return changeId; }
    public SyncDtos.EntityType getEntityType() { return entityType; }
    public UUID getEntityPublicId() { return entityPublicId; }
    public SyncDtos.Operation getOperation() { return operation; }
    public long getEntityVersion() { return entityVersion; }
    public Instant getOccurredAt() { return occurredAt; }
}
