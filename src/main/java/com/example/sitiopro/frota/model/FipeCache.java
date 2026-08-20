package com.example.sitiopro.frota.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "fipe_cache")
public class FipeCache implements Serializable {

    @Id
    private Integer id;
    private Integer tipo;
    private String marca;
    private String modelo;
    private String anoModelo;
    private Double valor;

    @Column(columnDefinition = "nvarchar(max)")
    private String historicoJson;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(String anoModelo) {
        this.anoModelo = anoModelo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getHistoricoJson() {
        return historicoJson;
    }

    public void setHistoricoJson(String historicoJson) {
        this.historicoJson = historicoJson;
    }
}
