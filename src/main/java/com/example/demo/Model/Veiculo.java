package com.example.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: Kawasaki ZX-10R
    private String placa; // Ou identificação interna para tratores
    private String tipo; // Carro, Moto, Trator, Caminhão
    private String marcaFipe; // Ex: Kawasaki
    private String modeloFipe; // Ex: NINJA ZX-10R 1000cc
    private Integer anoModelo; // 2012

    private Double kmAtual;
    private String situacao; // "Em Uso", "Manutenção", "Parado"
    private Double valorFipe; // Valor atualizado via API
    private String ultimaConsultaFipe; // Mês/Ano da consulta

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarcaFipe() {
        return marcaFipe;
    }

    public void setMarcaFipe(String marcaFipe) {
        this.marcaFipe = marcaFipe;
    }

    public String getModeloFipe() {
        return modeloFipe;
    }

    public void setModeloFipe(String modeloFipe) {
        this.modeloFipe = modeloFipe;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public Double getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Double kmAtual) {
        this.kmAtual = kmAtual;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public Double getValorFipe() {
        return valorFipe;
    }

    public void setValorFipe(Double valorFipe) {
        this.valorFipe = valorFipe;
    }

    public String getUltimaConsultaFipe() {
        return ultimaConsultaFipe;
    }

    public void setUltimaConsultaFipe(String ultimaConsultaFipe) {
        this.ultimaConsultaFipe = ultimaConsultaFipe;
    }
}