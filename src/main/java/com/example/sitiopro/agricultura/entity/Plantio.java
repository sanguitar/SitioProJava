package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "agricultura_plantios")
public class Plantio extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 80)
    private String metodo;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantidade;

    @Column(nullable = false, length = 30)
    private String unidade;

    @Column(length = 120)
    private String espacamento;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private OrigemPlantio origem;

    @Column(length = 180)
    private String descricaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movimento_estoque_id")
    private MovimentoEstoque movimentoEstoque;

    @Column(length = 1000)
    private String observacao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getEspacamento() { return espacamento; }
    public void setEspacamento(String espacamento) { this.espacamento = espacamento; }
    public OrigemPlantio getOrigem() { return origem; }
    public void setOrigem(OrigemPlantio origem) { this.origem = origem; }
    public String getDescricaoOrigem() { return descricaoOrigem; }
    public void setDescricaoOrigem(String descricaoOrigem) { this.descricaoOrigem = descricaoOrigem; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; }
    public void setMovimentoEstoque(MovimentoEstoque movimentoEstoque) { this.movimentoEstoque = movimentoEstoque; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
