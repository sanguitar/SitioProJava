package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.TipoManutencao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistroManutencaoRequest {
    @NotNull private Long ativoId;
    private Long planoPreventivoId;
    @NotNull private TipoManutencao tipo;
    @NotNull @PastOrPresent private LocalDateTime dataManutencao;
    @NotBlank @Size(max = 500) private String descricao;
    @NotBlank @Size(max = 120) private String responsavel;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal horimetro;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal quilometragem;
    @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal custo = BigDecimal.ZERO;
    private LocalDateTime proximaManutencao;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    @Valid private List<ConsumoManutencaoRequest> consumos = new ArrayList<>();

    public Long getAtivoId() { return ativoId; } public void setAtivoId(Long v) { ativoId = v; }
    public Long getPlanoPreventivoId() { return planoPreventivoId; } public void setPlanoPreventivoId(Long v) { planoPreventivoId = v; }
    public TipoManutencao getTipo() { return tipo; } public void setTipo(TipoManutencao v) { tipo = v; }
    public LocalDateTime getDataManutencao() { return dataManutencao; } public void setDataManutencao(LocalDateTime v) { dataManutencao = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public String getResponsavel() { return responsavel; } public void setResponsavel(String v) { responsavel = v; }
    public BigDecimal getHorimetro() { return horimetro; } public void setHorimetro(BigDecimal v) { horimetro = v; }
    public BigDecimal getQuilometragem() { return quilometragem; } public void setQuilometragem(BigDecimal v) { quilometragem = v; }
    public BigDecimal getCusto() { return custo; } public void setCusto(BigDecimal v) { custo = v; }
    public LocalDateTime getProximaManutencao() { return proximaManutencao; } public void setProximaManutencao(LocalDateTime v) { proximaManutencao = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public List<ConsumoManutencaoRequest> getConsumos() { return consumos; } public void setConsumos(List<ConsumoManutencaoRequest> v) { consumos = v == null ? new ArrayList<>() : v; }
}
