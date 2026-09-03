package com.example.sitiopro.criacao.core.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import com.example.sitiopro.propriedade.entity.EstruturaPropriedade;

@Entity
@Table(name = "criacao_instalacoes")
public class InstalacaoCriacao extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoInstalacaoCriacao tipo;

    @Column(length = 500)
    private String descricao;

    private Integer capacidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estrutura_id")
    private EstruturaPropriedade estrutura;

    public EstruturaPropriedade getEstrutura() { return estrutura; }
    public void setEstrutura(EstruturaPropriedade estrutura) { this.estrutura = estrutura; }

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoInstalacaoCriacao getTipo() { return tipo; }
    public void setTipo(TipoInstalacaoCriacao tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public long getVersao() { return versao; }
}
