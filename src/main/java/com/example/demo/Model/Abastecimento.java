package com.example.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "abastecimentos")
public class Abastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Evita gargalo de performance (Lazy Loading)
    @JoinColumn(name = "veiculo_id", nullable = false)
    @NotNull(message = "Veículo é obrigatório")
    private Veiculo veiculo;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @Positive(message = "Quantidade de litros deve ser positiva")
    private Double litros;

    @Positive(message = "Preço deve ser positivo")
    private Double precoPorLitro;

    private Double valorTotal;

    @PositiveOrZero(message = "KM não pode ser negativa")
    private Double kmNoAto;

    @NotBlank(message = "Local é obrigatório")
    private String local; // Posto ou Tanque Sítio

    public Abastecimento() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Double getLitros() {
        return litros;
    }

    public void setLitros(Double litros) {
        this.litros = litros;
    }

    public Double getPrecoPorLitro() {
        return precoPorLitro;
    }

    public void setPrecoPorLitro(Double precoPorLitro) {
        this.precoPorLitro = precoPorLitro;
    }

    public Double getKmNoAto() {
        return kmNoAto;
    }

    public void setKmNoAto(Double kmNoAto) {
        this.kmNoAto = kmNoAto;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    // Business Logic: Garante que o total esteja sempre certo antes de persistir
    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (this.litros != null && this.precoPorLitro != null) {
            this.valorTotal = this.litros * this.precoPorLitro;
        }
    }

    public Double getValorTotal() {
        return valorTotal;
    }
}