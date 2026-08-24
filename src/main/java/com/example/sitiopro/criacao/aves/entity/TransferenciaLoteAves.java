package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aves_transferencias")
public class TransferenciaLoteAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "instalacao_origem_id", nullable = false)
    private InstalacaoCriacao instalacaoOrigem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "instalacao_destino_id", nullable = false)
    private InstalacaoCriacao instalacaoDestino;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(nullable = false, length = 100)
    private String usuario;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evento_id", nullable = false, unique = true)
    private EventoLoteAves evento;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public InstalacaoCriacao getInstalacaoOrigem() { return instalacaoOrigem; }
    public void setInstalacaoOrigem(InstalacaoCriacao valor) { this.instalacaoOrigem = valor; }
    public InstalacaoCriacao getInstalacaoDestino() { return instalacaoDestino; }
    public void setInstalacaoDestino(InstalacaoCriacao valor) { this.instalacaoDestino = valor; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public EventoLoteAves getEvento() { return evento; }
    public void setEvento(EventoLoteAves evento) { this.evento = evento; }
}
