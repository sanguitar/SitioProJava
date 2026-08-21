package com.example.sitiopro.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ItemEstoqueRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 140, message = "Nome deve ter no máximo 140 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;

    @NotNull(message = "Unidade de medida é obrigatória")
    private Long unidadeMedidaId;

    @DecimalMin(value = "0.0000", message = "Estoque mínimo não pode ser negativo")
    private BigDecimal estoqueMinimo;

    private boolean ativo = true;

    private boolean controlaLote;

    private boolean controlaValidade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Long getUnidadeMedidaId() {
        return unidadeMedidaId;
    }

    public void setUnidadeMedidaId(Long unidadeMedidaId) {
        this.unidadeMedidaId = unidadeMedidaId;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isControlaLote() {
        return controlaLote;
    }

    public void setControlaLote(boolean controlaLote) {
        this.controlaLote = controlaLote;
    }

    public boolean isControlaValidade() {
        return controlaValidade;
    }

    public void setControlaValidade(boolean controlaValidade) {
        this.controlaValidade = controlaValidade;
    }
}
