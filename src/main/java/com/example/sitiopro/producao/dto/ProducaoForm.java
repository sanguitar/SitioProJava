package com.example.sitiopro.producao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ProducaoForm {

    private Long id;

    @NotNull(message = "Selecione uma categoria.")
    private Long categoriaId;

    @NotBlank(message = "Informe o nome ou a descrição do item.")
    @Size(max = 255, message = "O nome ou a descrição deve ter no máximo 255 caracteres.")
    private String item;

    @NotNull(message = "Informe a quantidade atual.")
    @PositiveOrZero(message = "A quantidade não pode ser negativa.")
    private Integer quantidade;

    @NotBlank(message = "Informe a unidade de medida.")
    @Size(max = 255, message = "A unidade deve ter no máximo 255 caracteres.")
    private String unidade;

    @NotBlank(message = "Informe o status do ativo.")
    @Size(max = 255, message = "O status deve ter no máximo 255 caracteres.")
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
