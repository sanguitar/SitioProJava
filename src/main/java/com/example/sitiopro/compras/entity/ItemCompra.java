package com.example.sitiopro.compras.entity;

import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
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

    @Column(name = "quantidade_volumes", precision = 19, scale = 4)
    private BigDecimal quantidadeVolumes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_embalagem", length = 20)
    private TipoEmbalagem tipoEmbalagem;

    @Column(name = "conteudo_por_volume", precision = 19, scale = 4)
    private BigDecimal conteudoPorVolume;

    @Column(name = "preco_por_volume", precision = 19, scale = 4)
    private BigDecimal precoPorVolume;

    @Column(name = "unidade_base", length = 20)
    private String unidadeBase;

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

    public BigDecimal getQuantidadeVolumes() {
        return quantidadeVolumes;
    }

    public void setQuantidadeVolumes(BigDecimal quantidadeVolumes) {
        this.quantidadeVolumes = quantidadeVolumes;
    }

    public TipoEmbalagem getTipoEmbalagem() {
        return tipoEmbalagem;
    }

    public void setTipoEmbalagem(TipoEmbalagem tipoEmbalagem) {
        this.tipoEmbalagem = tipoEmbalagem;
    }

    public BigDecimal getConteudoPorVolume() {
        return conteudoPorVolume;
    }

    public void setConteudoPorVolume(BigDecimal conteudoPorVolume) {
        this.conteudoPorVolume = conteudoPorVolume;
    }

    public BigDecimal getPrecoPorVolume() {
        return precoPorVolume;
    }

    public void setPrecoPorVolume(BigDecimal precoPorVolume) {
        this.precoPorVolume = precoPorVolume;
    }

    public String getUnidadeBase() {
        return unidadeBase;
    }

    public void setUnidadeBase(String unidadeBase) {
        this.unidadeBase = unidadeBase;
    }

    public boolean possuiApresentacaoComercial() {
        return quantidadeVolumes != null
                && tipoEmbalagem != null
                && conteudoPorVolume != null
                && precoPorVolume != null
                && unidadeBase != null;
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
