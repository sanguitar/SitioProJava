package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aves_mortalidades")
public class MortalidadeAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @Column(nullable = false)
    private int quantidade;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(length = 200)
    private String causa;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evento_id", nullable = false, unique = true)
    private EventoLoteAves evento;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public String getCausa() { return causa; }
    public void setCausa(String causa) { this.causa = causa; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public EventoLoteAves getEvento() { return evento; }
    public void setEvento(EventoLoteAves evento) { this.evento = evento; }
}
