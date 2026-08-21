package com.example.sitiopro.integracao.embrapa.agrofit.entity;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "agrofit_culturas")
public class AgrofitCultura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false, length = 180, unique = true)
    private String nomeNormalizado;

    @Column(name = "obtido_em", nullable = false)
    private LocalDateTime obtidoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FonteIntegracao fonte;

    protected AgrofitCultura() {
    }

    public AgrofitCultura(String nome, String nomeNormalizado, LocalDateTime obtidoEm) {
        this.nome = nome;
        this.nomeNormalizado = nomeNormalizado;
        this.obtidoEm = obtidoEm;
        this.fonte = FonteIntegracao.EMBRAPA_AGROFIT;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public void atualizarNome(String nome, LocalDateTime obtidoEm) {
        this.nome = nome;
        this.obtidoEm = obtidoEm;
    }
}
