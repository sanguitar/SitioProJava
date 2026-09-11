package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "agricultura_adubacoes")
public class AdubacaoCultivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;
    @Column(nullable = false) private LocalDate data;
    @Column(nullable = false, length = 180) private String produto;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantidade;
    @Column(nullable = false, length = 30) private String unidade;
    @Column(name = "area_aplicada_ha", precision = 14, scale = 4) private BigDecimal areaAplicadaHa;
    @Column(length = 120) private String metodo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrigemInsumo origem;
    @Column(name = "descricao_origem", length = 180) private String descricaoOrigem;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movimento_estoque_id") private MovimentoEstoque movimentoEstoque;
    @Column(name = "chave_idempotencia", nullable = false, length = 80) private String chaveIdempotencia;
    @Column(length = 1000) private String observacao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public BigDecimal getAreaAplicadaHa() { return areaAplicadaHa; }
    public void setAreaAplicadaHa(BigDecimal areaAplicadaHa) { this.areaAplicadaHa = areaAplicadaHa; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public OrigemInsumo getOrigem() { return origem; }
    public void setOrigem(OrigemInsumo origem) { this.origem = origem; }
    public String getDescricaoOrigem() { return descricaoOrigem; }
    public void setDescricaoOrigem(String descricaoOrigem) { this.descricaoOrigem = descricaoOrigem; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; }
    public void setMovimentoEstoque(MovimentoEstoque movimentoEstoque) { this.movimentoEstoque = movimentoEstoque; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
