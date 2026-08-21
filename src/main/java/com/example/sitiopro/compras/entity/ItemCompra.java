package com.example.sitiopro.compras.entity;

import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "itens_compra")
public class ItemCompra extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_estoque_id", nullable = false)
    private ItemEstoque itemEstoque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "local_destino_id", nullable = false)
    private LocalEstoque localDestino;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimento_estoque_id")
    private MovimentoEstoque movimentoEstoque;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal custoUnitario;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "lote_codigo", length = 80)
    private String loteCodigo;

    private LocalDate validade;

    public Long getId() {
        return id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public ItemEstoque getItemEstoque() {
        return itemEstoque;
    }

    public void setItemEstoque(ItemEstoque itemEstoque) {
        this.itemEstoque = itemEstoque;
    }

    public LocalEstoque getLocalDestino() {
        return localDestino;
    }

    public void setLocalDestino(LocalEstoque localDestino) {
        this.localDestino = localDestino;
    }

    public MovimentoEstoque getMovimentoEstoque() {
        return movimentoEstoque;
    }

    public void setMovimentoEstoque(MovimentoEstoque movimentoEstoque) {
        this.movimentoEstoque = movimentoEstoque;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getLoteCodigo() {
        return loteCodigo;
    }

    public void setLoteCodigo(String loteCodigo) {
        this.loteCodigo = loteCodigo;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }
}
