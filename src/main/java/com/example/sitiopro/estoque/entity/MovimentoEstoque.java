package com.example.sitiopro.estoque.entity;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estoque_movimentos")
public class MovimentoEstoque extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEstoque item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentoEstoque tipo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_origem_id")
    private LocalEstoque localOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_destino_id")
    private LocalEstoque localDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private LoteEstoque lote;

    @Column(name = "custo_unitario", precision = 19, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "custo_total", precision = 19, scale = 4)
    private BigDecimal custoTotal;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_movimento", nullable = false)
    private LocalDateTime dataMovimento;

    @Column(name = "origem_modulo", length = 40)
    private String origemModulo;

    @Column(name = "origem_referencia_id")
    private Long origemReferenciaId;

    @Column(name = "origem_descricao", length = 200)
    private String origemDescricao;

    public Long getId() {
        return id;
    }

    public ItemEstoque getItem() {
        return item;
    }

    public void setItem(ItemEstoque item) {
        this.item = item;
    }

    public TipoMovimentoEstoque getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentoEstoque tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public LocalEstoque getLocalOrigem() {
        return localOrigem;
    }

    public void setLocalOrigem(LocalEstoque localOrigem) {
        this.localOrigem = localOrigem;
    }

    public LocalEstoque getLocalDestino() {
        return localDestino;
    }

    public void setLocalDestino(LocalEstoque localDestino) {
        this.localDestino = localDestino;
    }

    public LoteEstoque getLote() {
        return lote;
    }

    public void setLote(LoteEstoque lote) {
        this.lote = lote;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataMovimento() {
        return dataMovimento;
    }

    public void setDataMovimento(LocalDateTime dataMovimento) {
        this.dataMovimento = dataMovimento;
    }

    public String getOrigemModulo() {
        return origemModulo;
    }

    public void setOrigemModulo(String origemModulo) {
        this.origemModulo = origemModulo;
    }

    public Long getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public void setOrigemReferenciaId(Long origemReferenciaId) {
        this.origemReferenciaId = origemReferenciaId;
    }

    public String getOrigemDescricao() {
        return origemDescricao;
    }

    public void setOrigemDescricao(String origemDescricao) {
        this.origemDescricao = origemDescricao;
    }
}
