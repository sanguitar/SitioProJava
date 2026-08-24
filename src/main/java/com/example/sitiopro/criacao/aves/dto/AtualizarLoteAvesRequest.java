package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AtualizarLoteAvesRequest {
    @NotBlank @Size(max = 80) private String codigo;
    @Size(max = 120) private String nome;
    @NotNull private EspecieAves especie;
    @NotNull private FinalidadeLoteAves finalidade;
    @Size(max = 120) private String linhagem;
    @NotBlank @Size(max = 200) private String origem;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataEntrada;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataNascimento;
    @NotNull private SexoLoteAves sexo;
    @Size(max = 1000) private String observacoes;

    public String getCodigo() { return codigo; } public void setCodigo(String v) { codigo = v; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public EspecieAves getEspecie() { return especie; } public void setEspecie(EspecieAves v) { especie = v; }
    public FinalidadeLoteAves getFinalidade() { return finalidade; } public void setFinalidade(FinalidadeLoteAves v) { finalidade = v; }
    public String getLinhagem() { return linhagem; } public void setLinhagem(String v) { linhagem = v; }
    public String getOrigem() { return origem; } public void setOrigem(String v) { origem = v; }
    public LocalDate getDataEntrada() { return dataEntrada; } public void setDataEntrada(LocalDate v) { dataEntrada = v; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate v) { dataNascimento = v; }
    public SexoLoteAves getSexo() { return sexo; } public void setSexo(SexoLoteAves v) { sexo = v; }
    public String getObservacoes() { return observacoes; } public void setObservacoes(String v) { observacoes = v; }
}
