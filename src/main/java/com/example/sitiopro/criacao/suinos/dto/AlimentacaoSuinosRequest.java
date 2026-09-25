package com.example.sitiopro.criacao.suinos.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class AlimentacaoSuinosRequest extends OperacaoSuinosBase {
    @NotNull private Long itemEstoqueId;
    @NotNull private Long localEstoqueId;
    @NotNull @DecimalMin("0.0001") @Digits(integer=15,fraction=4) private BigDecimal quantidade;
    @Size(max=100) private String loteEstoqueCodigo;
    public Long getItemEstoqueId(){return itemEstoqueId;} public void setItemEstoqueId(Long v){itemEstoqueId=v;}
    public Long getLocalEstoqueId(){return localEstoqueId;} public void setLocalEstoqueId(Long v){localEstoqueId=v;}
    public BigDecimal getQuantidade(){return quantidade;} public void setQuantidade(BigDecimal v){quantidade=v;}
    public String getLoteEstoqueCodigo(){return loteEstoqueCodigo;} public void setLoteEstoqueCodigo(String v){loteEstoqueCodigo=v;}
}
