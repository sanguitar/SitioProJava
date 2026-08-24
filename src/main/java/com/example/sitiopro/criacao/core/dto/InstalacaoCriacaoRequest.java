package com.example.sitiopro.criacao.core.dto;

import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InstalacaoCriacaoRequest {
    @NotBlank(message = "Nome é obrigatório") @Size(max = 120)
    private String nome;
    @NotNull(message = "Tipo é obrigatório")
    private TipoInstalacaoCriacao tipo;
    @Size(max = 500)
    private String descricao;
    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    private Integer capacidade;
    private boolean ativo = true;

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
}
