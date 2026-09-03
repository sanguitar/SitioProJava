package com.example.sitiopro.propriedade.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class CadastroFisico extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 1000)
    private String observacao;
    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public Propriedade getPropriedade() { return propriedade; }
    public void setPropriedade(Propriedade propriedade) { this.propriedade = propriedade; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getVersao() { return versao; }
}
