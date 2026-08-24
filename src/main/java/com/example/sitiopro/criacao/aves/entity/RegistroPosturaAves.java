package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "aves_posturas")
public class RegistroPosturaAves extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lote_id", nullable = false)
    private LoteAves lote;
    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;
    @Column(name = "ovos_inteiros", nullable = false)
    private int ovosInteiros;
    @Column(name = "ovos_quebrados", nullable = false)
    private int ovosQuebrados;
    @Column(name = "ovos_descartados", nullable = false)
    private int ovosDescartados;
    @Column(length = 1000)
    private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evento_id", nullable = false, unique = true)
    private EventoLoteAves evento;

    public Long getId() { return id; }
    public LoteAves getLote() { return lote; }
    public void setLote(LoteAves lote) { this.lote = lote; }
    public LocalDate getDataColeta() { return dataColeta; }
    public void setDataColeta(LocalDate dataColeta) { this.dataColeta = dataColeta; }
    public int getOvosInteiros() { return ovosInteiros; }
    public void setOvosInteiros(int ovosInteiros) { this.ovosInteiros = ovosInteiros; }
    public int getOvosQuebrados() { return ovosQuebrados; }
    public void setOvosQuebrados(int ovosQuebrados) { this.ovosQuebrados = ovosQuebrados; }
    public int getOvosDescartados() { return ovosDescartados; }
    public void setOvosDescartados(int ovosDescartados) { this.ovosDescartados = ovosDescartados; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public EventoLoteAves getEvento() { return evento; }
    public void setEvento(EventoLoteAves evento) { this.evento = evento; }
}
