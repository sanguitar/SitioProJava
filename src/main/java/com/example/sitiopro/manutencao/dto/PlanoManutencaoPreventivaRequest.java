package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.TipoPeriodicidadeManutencao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlanoManutencaoPreventivaRequest {
    @NotNull private Long ativoId;
    @NotBlank @Size(max = 160) private String nome;
    @Size(max = 500) private String descricao;
    @NotNull private TipoPeriodicidadeManutencao tipoPeriodicidade;
    @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) private BigDecimal intervalo;
    private LocalDateTime dataReferencia;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    private Long versao;

    public Long getAtivoId() { return ativoId; } public void setAtivoId(Long v) { ativoId = v; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public TipoPeriodicidadeManutencao getTipoPeriodicidade() { return tipoPeriodicidade; } public void setTipoPeriodicidade(TipoPeriodicidadeManutencao v) { tipoPeriodicidade = v; }
    public BigDecimal getIntervalo() { return intervalo; } public void setIntervalo(BigDecimal v) { intervalo = v; }
    public LocalDateTime getDataReferencia() { return dataReferencia; } public void setDataReferencia(LocalDateTime v) { dataReferencia = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public Long getVersao() { return versao; } public void setVersao(Long v) { versao = v; }
}
