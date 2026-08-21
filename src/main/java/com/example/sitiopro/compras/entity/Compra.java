package com.example.sitiopro.compras.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompra status = StatusCompra.RASCUNHO;

    @Column(name = "numero_documento", length = 80)
    private String numeroDocumento;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal frete = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @Column(name = "confirmado_por", length = 100)
    private String confirmadoPor;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCompra> itens = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(LocalDate dataCompra) {
        this.dataCompra = dataCompra;
    }

    public StatusCompra getStatus() {
        return status;
    }

    public void setStatus(StatusCompra status) {
        this.status = status;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getConfirmadoEm() {
        return confirmadoEm;
    }

    public void setConfirmadoEm(LocalDateTime confirmadoEm) {
        this.confirmadoEm = confirmadoEm;
    }

    public String getConfirmadoPor() {
        return confirmadoPor;
    }

    public void setConfirmadoPor(String confirmadoPor) {
        this.confirmadoPor = confirmadoPor;
    }

    public Long getVersao() {
        return versao;
    }

    public List<ItemCompra> getItens() {
        return itens;
    }

    public void adicionarItem(ItemCompra item) {
        itens.add(item);
        item.setCompra(this);
    }

    public void removerItem(ItemCompra item) {
        itens.remove(item);
        item.setCompra(null);
    }
}
