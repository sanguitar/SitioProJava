package com.example.sitiopro.criacao.aves.entity;

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
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aves_incubacao_acompanhamentos")
public class AcompanhamentoIncubacaoAves extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incubacao_id", nullable = false)
    private IncubacaoAves incubacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoAcompanhamentoIncubacaoAves tipo;

    @Column(name = "quantidade_avaliada")
    private Integer quantidadeAvaliada;

    @Column(name = "ovos_ferteis")
    private Integer ovosFerteis;

    @Column(name = "ovos_sem_desenvolvimento")
    private Integer ovosSemDesenvolvimento;

    private Integer perdas;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(precision = 5, scale = 2)
    private BigDecimal umidade;

    @Column(length = 1000)
    private String observacao;

    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @Version
    @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public IncubacaoAves getIncubacao() { return incubacao; }
    public void setIncubacao(IncubacaoAves incubacao) { this.incubacao = incubacao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoAcompanhamentoIncubacaoAves getTipo() { return tipo; }
    public void setTipo(TipoAcompanhamentoIncubacaoAves tipo) { this.tipo = tipo; }
    public Integer getQuantidadeAvaliada() { return quantidadeAvaliada; }
    public void setQuantidadeAvaliada(Integer quantidadeAvaliada) { this.quantidadeAvaliada = quantidadeAvaliada; }
    public Integer getOvosFerteis() { return ovosFerteis; }
    public void setOvosFerteis(Integer ovosFerteis) { this.ovosFerteis = ovosFerteis; }
    public Integer getOvosSemDesenvolvimento() { return ovosSemDesenvolvimento; }
    public void setOvosSemDesenvolvimento(Integer ovosSemDesenvolvimento) { this.ovosSemDesenvolvimento = ovosSemDesenvolvimento; }
    public Integer getPerdas() { return perdas; }
    public void setPerdas(Integer perdas) { this.perdas = perdas; }
    public BigDecimal getTemperatura() { return temperatura; }
    public void setTemperatura(BigDecimal temperatura) { this.temperatura = temperatura; }
    public BigDecimal getUmidade() { return umidade; }
    public void setUmidade(BigDecimal umidade) { this.umidade = umidade; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
