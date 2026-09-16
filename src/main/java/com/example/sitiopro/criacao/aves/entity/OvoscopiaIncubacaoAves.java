package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aves_incubacao_ovoscopias")
public class OvoscopiaIncubacaoAves extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incubacao_id", nullable = false)
    private IncubacaoAves incubacao;

    @Column(name = "data_ovoscopia", nullable = false)
    private LocalDate dataOvoscopia;

    @Column(name = "dia_incubacao", nullable = false)
    private int diaIncubacao;

    @Column(name = "proxima_verificacao")
    private LocalDate proximaVerificacao;

    @Column(name = "responsavel", length = 120)
    private String responsavel;

    @Column(name = "observacao_geral", length = 1000)
    private String observacaoGeral;

    @Column(name = "chave_idempotencia", nullable = false, length = 100)
    private String chaveIdempotencia;

    @OneToMany(mappedBy = "ovoscopia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOvoscopiaIncubacaoAves> itens = new ArrayList<>();

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    public Long getId() { return id; }
    public IncubacaoAves getIncubacao() { return incubacao; }
    public void setIncubacao(IncubacaoAves incubacao) { this.incubacao = incubacao; }
    public LocalDate getDataOvoscopia() { return dataOvoscopia; }
    public void setDataOvoscopia(LocalDate dataOvoscopia) { this.dataOvoscopia = dataOvoscopia; }
    public int getDiaIncubacao() { return diaIncubacao; }
    public void setDiaIncubacao(int diaIncubacao) { this.diaIncubacao = diaIncubacao; }
    public LocalDate getProximaVerificacao() { return proximaVerificacao; }
    public void setProximaVerificacao(LocalDate proximaVerificacao) { this.proximaVerificacao = proximaVerificacao; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getObservacaoGeral() { return observacaoGeral; }
    public void setObservacaoGeral(String observacaoGeral) { this.observacaoGeral = observacaoGeral; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public List<ItemOvoscopiaIncubacaoAves> getItens() { return itens; }
    public long getVersao() { return versao; }
}

