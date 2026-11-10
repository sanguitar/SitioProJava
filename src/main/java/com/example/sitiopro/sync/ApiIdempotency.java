package com.example.sitiopro.sync;

import com.example.sitiopro.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_idempotency")
public class ApiIdempotency {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "operation_id", nullable = false)
    private UUID operationId;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, length = 80)
    private String operacao;
    @Column(nullable = false, length = 160)
    private String rota;
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;
    @Column(name = "http_status", nullable = false)
    private int httpStatus;
    @Column(name = "response_json", nullable = false, columnDefinition = "nvarchar(max)")
    private String responseJson;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    protected ApiIdempotency() {
    }

    public ApiIdempotency(UUID operationId, UUID deviceId, Usuario usuario, String operacao,
            String rota, String requestHash, int httpStatus, String responseJson,
            Instant criadoEm, Instant expiraEm) {
        this.operationId = operationId;
        this.deviceId = deviceId;
        this.usuario = usuario;
        this.operacao = operacao;
        this.rota = rota;
        this.requestHash = requestHash;
        this.httpStatus = httpStatus;
        this.responseJson = responseJson;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }

    public String getRequestHash() { return requestHash; }
    public String getResponseJson() { return responseJson; }
}
