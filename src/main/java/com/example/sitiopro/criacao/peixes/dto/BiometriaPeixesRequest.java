package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class BiometriaPeixesRequest extends OperacaoPeixesBase {
    @NotNull @DecimalMin("0.0001") private BigDecimal pesoMedio;
    public BigDecimal getPesoMedio(){return pesoMedio;} public void setPesoMedio(BigDecimal v){pesoMedio=v;}
}
