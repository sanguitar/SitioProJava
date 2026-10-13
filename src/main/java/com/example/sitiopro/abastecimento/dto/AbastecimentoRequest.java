package com.example.sitiopro.abastecimento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AbastecimentoRequest {
    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;
    @NotNull(message = "Data é obrigatória")
    private LocalDate data;
    @NotNull(message = "Quantidade de litros é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade de litros deve ser positiva")
    private BigDecimal litros;
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0001", message = "Preço deve ser positivo")
    private BigDecimal precoPorLitro;
    @DecimalMin(value = "0.00", message = "A quilometragem não pode ser negativa")
    private BigDecimal kmNoAto;
    @DecimalMin(value = "0.00", message = "O horímetro não pode ser negativo")
    private BigDecimal horimetroNoAto;
    @NotBlank(message = "Local é obrigatório")
    @Size(max = 255)
    private String local;
    @NotBlank(message = "Chave de idempotência é obrigatória")
    @Size(max = 100)
    private String chaveIdempotencia;

    public Long getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Long veiculoId) { this.veiculoId = veiculoId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getLitros() { return litros; }
    public void setLitros(BigDecimal litros) { this.litros = litros; }
    public BigDecimal getPrecoPorLitro() { return precoPorLitro; }
    public void setPrecoPorLitro(BigDecimal precoPorLitro) { this.precoPorLitro = precoPorLitro; }
    public BigDecimal getKmNoAto() { return kmNoAto; }
    public void setKmNoAto(BigDecimal kmNoAto) { this.kmNoAto = kmNoAto; }
    public BigDecimal getHorimetroNoAto() { return horimetroNoAto; }
    public void setHorimetroNoAto(BigDecimal horimetroNoAto) { this.horimetroNoAto = horimetroNoAto; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
}
