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

@Entity
@Table(name = "aves_incubacao_ovoscopia_itens")
public class ItemOvoscopiaIncubacaoAves extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ovoscopia_id", nullable = false)
    private OvoscopiaIncubacaoAves ovoscopia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ovo_id", nullable = false)
    private OvoIncubacaoAves ovo;

    @Enumerated(EnumType.STRING)
    @Column(name = "achado", nullable = false, length = 40)
    private AchadoOvoscopiaAves achado;

    @Column(name = "observacao", length = 1000)
    private String observacao;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    public Long getId() { return id; }
    public OvoscopiaIncubacaoAves getOvoscopia() { return ovoscopia; }
    public void setOvoscopia(OvoscopiaIncubacaoAves ovoscopia) { this.ovoscopia = ovoscopia; }
    public OvoIncubacaoAves getOvo() { return ovo; }
    public void setOvo(OvoIncubacaoAves ovo) { this.ovo = ovo; }
    public AchadoOvoscopiaAves getAchado() { return achado; }
    public void setAchado(AchadoOvoscopiaAves achado) { this.achado = achado; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getVersao() { return versao; }
}

