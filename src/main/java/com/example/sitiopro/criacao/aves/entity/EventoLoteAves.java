package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "aves_eventos")
public class EventoLoteAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private TipoEventoLoteAves tipo;
    private Integer quantidade;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(nullable = false, length = 80)
    private String origem;
    @Column(nullable = false, length = 100)
    private String usuario;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "referencia_externa", length = 160)
    private String referenciaExterna;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instalacao_origem_id")
    private InstalacaoCriacao instalacaoOrigem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instalacao_destino_id")
    private InstalacaoCriacao instalacaoDestino;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public TipoEventoLoteAves getTipo() { return tipo; }
    public void setTipo(TipoEventoLoteAves tipo) { this.tipo = tipo; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public void setReferenciaExterna(String referenciaExterna) { this.referenciaExterna = referenciaExterna; }
    public InstalacaoCriacao getInstalacaoOrigem() { return instalacaoOrigem; }
    public void setInstalacaoOrigem(InstalacaoCriacao instalacaoOrigem) { this.instalacaoOrigem = instalacaoOrigem; }
    public InstalacaoCriacao getInstalacaoDestino() { return instalacaoDestino; }
    public void setInstalacaoDestino(InstalacaoCriacao instalacaoDestino) { this.instalacaoDestino = instalacaoDestino; }
}
