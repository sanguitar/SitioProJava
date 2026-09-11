package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.OrigemInsumo;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TratamentoRequest {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate data;
    @NotBlank @Size(max = 180) private String finalidade;
    @NotBlank @Size(max = 180) private String produtoAplicado;
    @NotNull @DecimalMin("0.0001") @Digits(integer = 14, fraction = 4) private BigDecimal quantidade;
    @NotBlank @Size(max = 30) private String unidade;
    @DecimalMin("0.0001") @Digits(integer = 10, fraction = 4) private BigDecimal areaTratadaHa;
    @Size(max = 120) private String metodo;
    @NotNull private OrigemInsumo origem = OrigemInsumo.EXTERNA;
    @Size(max = 180) private String descricaoOrigem;
    private Long itemEstoqueId;
    private Long localEstoqueId;
    @Size(max = 80) private String loteCodigo;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(min = 8, max = 80) @Pattern(regexp = "[A-Za-z0-9._:-]+") private String chaveIdempotencia;
    @NotNull @PositiveOrZero private Long versao;

    public LocalDate getData() { return data; } public void setData(LocalDate v) { data = v; }
    public String getFinalidade() { return finalidade; } public void setFinalidade(String v) { finalidade = v; }
    public String getProdutoAplicado() { return produtoAplicado; } public void setProdutoAplicado(String v) { produtoAplicado = v; }
    public BigDecimal getQuantidade() { return quantidade; } public void setQuantidade(BigDecimal v) { quantidade = v; }
    public String getUnidade() { return unidade; } public void setUnidade(String v) { unidade = v; }
    public BigDecimal getAreaTratadaHa() { return areaTratadaHa; } public void setAreaTratadaHa(BigDecimal v) { areaTratadaHa = v; }
    public String getMetodo() { return metodo; } public void setMetodo(String v) { metodo = v; }
    public OrigemInsumo getOrigem() { return origem; } public void setOrigem(OrigemInsumo v) { origem = v; }
    public String getDescricaoOrigem() { return descricaoOrigem; } public void setDescricaoOrigem(String v) { descricaoOrigem = v; }
    public Long getItemEstoqueId() { return itemEstoqueId; } public void setItemEstoqueId(Long v) { itemEstoqueId = v; }
    public Long getLocalEstoqueId() { return localEstoqueId; } public void setLocalEstoqueId(Long v) { localEstoqueId = v; }
    public String getLoteCodigo() { return loteCodigo; } public void setLoteCodigo(String v) { loteCodigo = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public Long getVersao() { return versao; } public void setVersao(Long v) { versao = v; }
}
