package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class MedicaoQualidadeAguaRequest {
    @NotNull private LocalDateTime medidoEm;
    @NotNull @DecimalMin("-5") @DecimalMax("50") private BigDecimal temperatura;
    @NotNull @DecimalMin("0") @DecimalMax("14") private BigDecimal ph;
    @NotNull @DecimalMin("0") private BigDecimal oxigenioDissolvido;
    @DecimalMin("0") private BigDecimal transparenciaCm;
    @DecimalMin("0") private BigDecimal amonia;
    @DecimalMin("0") private BigDecimal nitrito;
    @NotBlank @Size(max=120) private String responsavel;
    @Size(max=1000) private String observacao;
    @NotBlank @Size(max=100) private String chaveIdempotencia;
    public LocalDateTime getMedidoEm(){return medidoEm;} public void setMedidoEm(LocalDateTime v){medidoEm=v;} public BigDecimal getTemperatura(){return temperatura;} public void setTemperatura(BigDecimal v){temperatura=v;} public BigDecimal getPh(){return ph;} public void setPh(BigDecimal v){ph=v;} public BigDecimal getOxigenioDissolvido(){return oxigenioDissolvido;} public void setOxigenioDissolvido(BigDecimal v){oxigenioDissolvido=v;} public BigDecimal getTransparenciaCm(){return transparenciaCm;} public void setTransparenciaCm(BigDecimal v){transparenciaCm=v;} public BigDecimal getAmonia(){return amonia;} public void setAmonia(BigDecimal v){amonia=v;} public BigDecimal getNitrito(){return nitrito;} public void setNitrito(BigDecimal v){nitrito=v;} public String getResponsavel(){return responsavel;} public void setResponsavel(String v){responsavel=v;} public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;} public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
}
