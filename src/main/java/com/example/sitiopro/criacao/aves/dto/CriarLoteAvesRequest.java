package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarLoteAvesRequest {
    @Size(max = 120) private String nome;
    @NotNull(message = "Espécie é obrigatória") private EspecieAves especie = EspecieAves.GALINHA;
    @NotNull(message = "Finalidade é obrigatória") private FinalidadeLoteAves finalidade;
    @Size(max = 120) private String linhagem;
    @NotBlank(message = "Origem é obrigatória") @Size(max = 200) private String origem;
    @NotNull(message = "Data de entrada é obrigatória") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataEntrada;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataNascimento;
    @NotNull(message = "Quantidade inicial é obrigatória") @Min(value = 1, message = "Quantidade inicial deve ser maior que zero")
    private Integer quantidadeInicial;
    @NotNull(message = "Classificação de sexo é obrigatória") private SexoLoteAves sexo = SexoLoteAves.NAO_DEFINIDO;
    @NotNull(message = "Instalação é obrigatória") private Long instalacaoId;
    @Size(max = 1000) private String observacoes;
    @DecimalMin(value = "0.0000", message = "Custo inicial não pode ser negativo") private BigDecimal custoInicial;
    @NotBlank(message = "Chave de idempotência é obrigatória") @Size(max = 100) private String chaveIdempotencia;

    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public EspecieAves getEspecie() { return especie; } public void setEspecie(EspecieAves v) { especie = v; }
    public FinalidadeLoteAves getFinalidade() { return finalidade; } public void setFinalidade(FinalidadeLoteAves v) { finalidade = v; }
    public String getLinhagem() { return linhagem; } public void setLinhagem(String v) { linhagem = v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem = v; }
    public LocalDate getDataEntrada() { return dataEntrada; } public void setDataEntrada(LocalDate v) { dataEntrada = v; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate v) { dataNascimento = v; }
    public Integer getQuantidadeInicial() { return quantidadeInicial; } public void setQuantidadeInicial(Integer v) { quantidadeInicial = v; }
    public SexoLoteAves getSexo() { return sexo; } public void setSexo(SexoLoteAves v) { sexo = v; }
    public Long getInstalacaoId() { return instalacaoId; } public void setInstalacaoId(Long v) { instalacaoId = v; }
    public String getObservacoes() { return observacoes; } public void setObservacoes(String v) { observacoes = v; }
    public BigDecimal getCustoInicial() { return custoInicial; } public void setCustoInicial(BigDecimal v) { custoInicial = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
