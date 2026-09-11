package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class PlantioRequest {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;
    @Size(max = 80)
    private String metodo;
    @NotNull @DecimalMin("0.0001") @Digits(integer = 14, fraction = 4)
    private BigDecimal quantidade;
    @NotBlank @Size(max = 30)
    private String unidade;
    @Size(max = 120)
    private String espacamento;
    @NotNull
    private OrigemPlantio origem = OrigemPlantio.EXTERNA;
    @Size(max = 180)
    private String descricaoOrigem;
    @Positive
    private Long itemEstoqueId;
    @Positive
    private Long localEstoqueId;
    @Size(max = 80)
    private String loteCodigo;
    @Size(max = 1000)
    private String observacao;
    @NotNull @PositiveOrZero
    private Long versao;

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
    public Long getItemEstoqueId() { return itemEstoqueId; }
    public void setItemEstoqueId(Long itemEstoqueId) { this.itemEstoqueId = itemEstoqueId; }
    public Long getLocalEstoqueId() { return localEstoqueId; }
    public void setLocalEstoqueId(Long localEstoqueId) { this.localEstoqueId = localEstoqueId; }
    public String getLoteCodigo() { return loteCodigo; }
    public void setLoteCodigo(String loteCodigo) { this.loteCodigo = loteCodigo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
