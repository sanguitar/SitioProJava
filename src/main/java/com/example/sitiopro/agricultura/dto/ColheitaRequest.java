package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ColheitaRequest {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;
    @NotNull @DecimalMin("0.0001") @Digits(integer = 14, fraction = 4)
    private BigDecimal quantidade;
    @NotBlank @Size(max = 30)
    private String unidade;
    @Size(max = 120)
    private String classificacao;
    @DecimalMin("0") @Digits(integer = 14, fraction = 4)
    private BigDecimal perdas;
    private boolean finalizaCultivo = true;
    @NotNull
    private DestinoColheita destino = DestinoColheita.SEM_ESTOQUE;
    private Long itemEstoqueId;
    private Long localEstoqueId;
    @Size(max = 80)
    private String loteCodigo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validade;
    @Size(max = 80) @Pattern(regexp = "[A-Za-z0-9._:-]+")
    private String chaveIdempotencia;
    @Size(max = 1000)
    private String observacao;
    @NotNull @PositiveOrZero
    private Long versao;

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }
    public BigDecimal getPerdas() { return perdas; }
    public void setPerdas(BigDecimal perdas) { this.perdas = perdas; }
    public boolean isFinalizaCultivo() { return finalizaCultivo; }
    public void setFinalizaCultivo(boolean finalizaCultivo) { this.finalizaCultivo = finalizaCultivo; }
    public DestinoColheita getDestino() { return destino; }
    public void setDestino(DestinoColheita destino) { this.destino = destino; }
    public Long getItemEstoqueId() { return itemEstoqueId; }
    public void setItemEstoqueId(Long itemEstoqueId) { this.itemEstoqueId = itemEstoqueId; }
    public Long getLocalEstoqueId() { return localEstoqueId; }
    public void setLocalEstoqueId(Long localEstoqueId) { this.localEstoqueId = localEstoqueId; }
    public String getLoteCodigo() { return loteCodigo; }
    public void setLoteCodigo(String loteCodigo) { this.loteCodigo = loteCodigo; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
