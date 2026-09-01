package com.example.sitiopro.compras.dto;

import com.example.sitiopro.compras.entity.TipoEmbalagem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemCompraRequest {

    @NotNull(message = "Item de estoque é obrigatório")
    private Long itemEstoqueId;

    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @DecimalMin(value = "0.0000", message = "Custo unitário não pode ser negativo")
    private BigDecimal custoUnitario;

    @DecimalMin(value = "0.0001", message = "Quantidade de volumes deve ser maior que zero")
    private BigDecimal quantidadeVolumes;

    private TipoEmbalagem tipoEmbalagem;

    @DecimalMin(value = "0.0001", message = "Conteúdo por volume deve ser maior que zero")
    private BigDecimal conteudoPorVolume;

    @DecimalMin(value = "0.0000", message = "Preço por volume não pode ser negativo")
    private BigDecimal precoPorVolume;

    @Size(max = 20, message = "Unidade-base deve ter no máximo 20 caracteres")
    private String unidadeBase;

    @NotNull(message = "Local de destino é obrigatório")
    private Long localDestinoId;

    @Size(max = 80, message = "Código do lote deve ter no máximo 80 caracteres")
    private String loteCodigo;

    private LocalDate validade;

    public Long getItemEstoqueId() {
        return itemEstoqueId;
    }

    public void setItemEstoqueId(Long itemEstoqueId) {
        this.itemEstoqueId = itemEstoqueId;
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

    public Long getLocalDestinoId() {
        return localDestinoId;
    }

    public void setLocalDestinoId(Long localDestinoId) {
        this.localDestinoId = localDestinoId;
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
