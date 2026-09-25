package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.TipoRegistroSanitarioSuinos;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RegistroSanitarioSuinosRequest {
    private Long loteId;
    private Long animalReprodutivoId;
    @NotNull private TipoRegistroSanitarioSuinos tipo;
    @NotNull @PastOrPresent private LocalDate dataProcedimento;
    @NotBlank @Size(max = 200) private String procedimentoProduto;
    @Size(max = 500) private String motivo;
    @NotBlank @Size(max = 120) private String responsavel;
    @Size(max = 1000) private String observacao;
    @Size(max = 250) private String proximaAcao;
    private LocalDate proximaAcaoData;
    @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) private BigDecimal custo;
    private Long itemEstoqueId;
    private Long localEstoqueId;
    @DecimalMin("0.0001") @Digits(integer = 15, fraction = 4) private BigDecimal quantidadeConsumida;
    @Size(max = 100) private String loteEstoqueCodigo;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;

    public Long getLoteId() { return loteId; } public void setLoteId(Long v) { loteId = v; }
    public Long getAnimalReprodutivoId() { return animalReprodutivoId; } public void setAnimalReprodutivoId(Long v) { animalReprodutivoId = v; }
    public TipoRegistroSanitarioSuinos getTipo() { return tipo; } public void setTipo(TipoRegistroSanitarioSuinos v) { tipo = v; }
    public LocalDate getDataProcedimento() { return dataProcedimento; } public void setDataProcedimento(LocalDate v) { dataProcedimento = v; }
    public String getProcedimentoProduto() { return procedimentoProduto; } public void setProcedimentoProduto(String v) { procedimentoProduto = v; }
    public String getMotivo() { return motivo; } public void setMotivo(String v) { motivo = v; }
    public String getResponsavel() { return responsavel; } public void setResponsavel(String v) { responsavel = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getProximaAcao() { return proximaAcao; } public void setProximaAcao(String v) { proximaAcao = v; }
    public LocalDate getProximaAcaoData() { return proximaAcaoData; } public void setProximaAcaoData(LocalDate v) { proximaAcaoData = v; }
    public BigDecimal getCusto() { return custo; } public void setCusto(BigDecimal v) { custo = v; }
    public Long getItemEstoqueId() { return itemEstoqueId; } public void setItemEstoqueId(Long v) { itemEstoqueId = v; }
    public Long getLocalEstoqueId() { return localEstoqueId; } public void setLocalEstoqueId(Long v) { localEstoqueId = v; }
    public BigDecimal getQuantidadeConsumida() { return quantidadeConsumida; } public void setQuantidadeConsumida(BigDecimal v) { quantidadeConsumida = v; }
    public String getLoteEstoqueCodigo() { return loteEstoqueCodigo; } public void setLoteEstoqueCodigo(String v) { loteEstoqueCodigo = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
