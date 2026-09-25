package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.MetodoReproducaoSuinos;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class RegistrarCoberturaSuinosRequest {
    @NotNull private Long matrizId;
    private Long reprodutorId;
    @NotNull private MetodoReproducaoSuinos metodo;
    @NotNull private LocalDate dataCobertura;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public Long getMatrizId() { return matrizId; } public void setMatrizId(Long v) { matrizId = v; }
    public Long getReprodutorId() { return reprodutorId; } public void setReprodutorId(Long v) { reprodutorId = v; }
    public MetodoReproducaoSuinos getMetodo() { return metodo; } public void setMetodo(MetodoReproducaoSuinos v) { metodo = v; }
    public LocalDate getDataCobertura() { return dataCobertura; } public void setDataCobertura(LocalDate v) { dataCobertura = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
