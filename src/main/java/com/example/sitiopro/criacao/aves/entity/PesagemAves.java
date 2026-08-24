package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aves_pesagens")
public class PesagemAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(name = "quantidade_amostrada")
    private Integer quantidadeAmostrada;
    @Column(name = "peso_medio", nullable = false, precision = 19, scale = 4)
    private BigDecimal pesoMedio;
    @Column(name = "peso_minimo", precision = 19, scale = 4)
    private BigDecimal pesoMinimo;
    @Column(name = "peso_maximo", precision = 19, scale = 4)
    private BigDecimal pesoMaximo;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evento_id", nullable = false, unique = true)
    private EventoLoteAves evento;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }
    public Integer getQuantidadeAmostrada() { return quantidadeAmostrada; }
    public void setQuantidadeAmostrada(Integer quantidadeAmostrada) { this.quantidadeAmostrada = quantidadeAmostrada; }
    public BigDecimal getPesoMedio() { return pesoMedio; }
    public void setPesoMedio(BigDecimal pesoMedio) { this.pesoMedio = pesoMedio; }
    public BigDecimal getPesoMinimo() { return pesoMinimo; }
    public void setPesoMinimo(BigDecimal pesoMinimo) { this.pesoMinimo = pesoMinimo; }
    public BigDecimal getPesoMaximo() { return pesoMaximo; }
    public void setPesoMaximo(BigDecimal pesoMaximo) { this.pesoMaximo = pesoMaximo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public EventoLoteAves getEvento() { return evento; }
    public void setEvento(EventoLoteAves evento) { this.evento = evento; }
}
