package com.example.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "veiculos")
public class Veiculo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 20)
    private String placa;

    @NotBlank(message = "Tipo é obrigatório")
    @Column(nullable = false, length = 30)
    private String tipo; // Sugestão Futura: Usar um Enum aqui

    private String marcaFipe;
    private String modeloFipe;
    private Integer anoModelo;

    @PositiveOrZero(message = "KM não pode ser negativa")
    private Double kmAtual;

    @Column(nullable = false, length = 30)
    private String situacao = "DISPONIVEL";

    private Double valorFipe;
    private String ultimaConsultaFipe;

    // Construtor padrão obrigatório pelo JPA
    public Veiculo() {
    }

    // Getters e Setters
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

    // Dentro de Veiculo.java
    private String icone; // Ex: "fa-car", "fa-motorcycle", "fa-tractor"

    // Não esqueça do Getter e Setter!
    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    // Regra Sênior: Equals e HashCode baseados no ID (Garante consistência em Sets)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Veiculo veiculo = (Veiculo) o;
        return id != null && id.equals(veiculo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}