package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agricultura_ocorrencia_historicos")
public class HistoricoOcorrenciaCultivo extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    private OcorrenciaCultivo ocorrencia;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoHistoricoOcorrencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SeveridadeOcorrencia severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusOcorrenciaCultivo status;

    @Column(nullable = false, length = 1500)
    private String descricao;

    @Column(name = "chave_idempotencia", nullable = false, length = 80)
    private String chaveIdempotencia;

    public Long getId() { return id; }
    public OcorrenciaCultivo getOcorrencia() { return ocorrencia; }
    public void setOcorrencia(OcorrenciaCultivo ocorrencia) { this.ocorrencia = ocorrencia; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoHistoricoOcorrencia getTipo() { return tipo; }
    public void setTipo(TipoHistoricoOcorrencia tipo) { this.tipo = tipo; }
    public SeveridadeOcorrencia getSeveridade() { return severidade; }
    public void setSeveridade(SeveridadeOcorrencia severidade) { this.severidade = severidade; }
    public StatusOcorrenciaCultivo getStatus() { return status; }
    public void setStatus(StatusOcorrenciaCultivo status) { this.status = status; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
}
