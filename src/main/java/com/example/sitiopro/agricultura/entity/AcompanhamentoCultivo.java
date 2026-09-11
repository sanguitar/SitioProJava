package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agricultura_acompanhamentos")
public class AcompanhamentoCultivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private TipoAcompanhamentoCultivo tipo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(length = 1000)
    private String observacao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoAcompanhamentoCultivo getTipo() { return tipo; }
    public void setTipo(TipoAcompanhamentoCultivo tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
