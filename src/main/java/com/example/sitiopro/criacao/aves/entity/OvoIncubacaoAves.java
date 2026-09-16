package com.example.sitiopro.criacao.aves.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "aves_incubacao_ovos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_aves_inc_ovos_incubacao_numero", columnNames = {"incubacao_id", "numero"})
})
public class OvoIncubacaoAves extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incubacao_id", nullable = false)
    private IncubacaoAves incubacao;

    @Column(name = "numero", nullable = false)
    private int numero;

    @Version
    @Column(name = "versao", nullable = false)
    private long versao;

    public Long getId() { return id; }
    public IncubacaoAves getIncubacao() { return incubacao; }
    public void setIncubacao(IncubacaoAves incubacao) { this.incubacao = incubacao; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public long getVersao() { return versao; }
}

