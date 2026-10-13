package com.example.sitiopro.frota.dto;

import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VeiculoRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String nome;
    @Size(max = 20)
    private String placa;
    @NotBlank(message = "Tipo é obrigatório")
    @Size(max = 30)
    private String tipo;
    @Size(max = 255)
    private String marcaFipe;
    @Size(max = 255)
    private String modeloFipe;
    private Integer anoModelo;
    @DecimalMin(value = "0.00", message = "A quilometragem não pode ser negativa")
    private BigDecimal kmAtual;
    @NotNull(message = "Tipo de medidor é obrigatório")
    private TipoMedidorVeiculo tipoMedidor = TipoMedidorVeiculo.QUILOMETRAGEM;
    @DecimalMin(value = "0.00", message = "O horímetro não pode ser negativo")
    private BigDecimal horimetroAtual;
    @DecimalMin(value = "0.00", message = "O valor FIPE não pode ser negativo")
    private BigDecimal valorFipe;
    private Integer fipeReferenciaId;
    @Size(max = 4000)
    private String ultimaConsultaFipe;
    private Long ativoPatrimonialId;
    @NotBlank(message = "Chave de idempotência é obrigatória")
    @Size(max = 100)
    private String chaveIdempotencia;
    private Long versao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarcaFipe() { return marcaFipe; }
    public void setMarcaFipe(String marcaFipe) { this.marcaFipe = marcaFipe; }
    public String getModeloFipe() { return modeloFipe; }
    public void setModeloFipe(String modeloFipe) { this.modeloFipe = modeloFipe; }
    public Integer getAnoModelo() { return anoModelo; }
    public void setAnoModelo(Integer anoModelo) { this.anoModelo = anoModelo; }
    public BigDecimal getKmAtual() { return kmAtual; }
    public void setKmAtual(BigDecimal kmAtual) { this.kmAtual = kmAtual; }
    public TipoMedidorVeiculo getTipoMedidor() { return tipoMedidor; }
    public void setTipoMedidor(TipoMedidorVeiculo tipoMedidor) { this.tipoMedidor = tipoMedidor; }
    public BigDecimal getHorimetroAtual() { return horimetroAtual; }
    public void setHorimetroAtual(BigDecimal horimetroAtual) { this.horimetroAtual = horimetroAtual; }
    public BigDecimal getValorFipe() { return valorFipe; }
    public void setValorFipe(BigDecimal valorFipe) { this.valorFipe = valorFipe; }
    public Integer getFipeReferenciaId() { return fipeReferenciaId; }
    public void setFipeReferenciaId(Integer fipeReferenciaId) { this.fipeReferenciaId = fipeReferenciaId; }
    public String getUltimaConsultaFipe() { return ultimaConsultaFipe; }
    public void setUltimaConsultaFipe(String ultimaConsultaFipe) { this.ultimaConsultaFipe = ultimaConsultaFipe; }
    public Long getAtivoPatrimonialId() { return ativoPatrimonialId; }
    public void setAtivoPatrimonialId(Long ativoPatrimonialId) { this.ativoPatrimonialId = ativoPatrimonialId; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
