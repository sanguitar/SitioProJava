package com.example.sitiopro.integracao.clima.entity;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "previsoes_climaticas", uniqueConstraints = @UniqueConstraint(
        name = "uk_previsoes_climaticas_fonte_contexto_data",
        columnNames = {"fonte", "contexto", "data_hora_previsao"}))
public class PrevisaoClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FonteIntegracao fonte;

    @Column(nullable = false, length = 80)
    private String contexto;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(name = "data_hora_previsao", nullable = false)
    private LocalDateTime dataHoraPrevisao;

    @Column(precision = 7, scale = 2)
    private BigDecimal temperatura;

    @Column(name = "umidade_relativa")
    private Integer umidadeRelativa;

    @Column(precision = 9, scale = 3)
    private BigDecimal precipitacao;

    @Column(name = "probabilidade_precipitacao")
    private Integer probabilidadePrecipitacao;

    @Column(name = "velocidade_vento", precision = 9, scale = 2)
    private BigDecimal velocidadeVento;

    @Column(name = "rajada_vento", precision = 9, scale = 2)
    private BigDecimal rajadaVento;

    @Column(precision = 9, scale = 4)
    private BigDecimal et0;

    @Column(name = "umidade_solo", precision = 8, scale = 5)
    private BigDecimal umidadeSolo;

    @Column(name = "codigo_tempo")
    private Integer codigoTempo;

    @Column(name = "obtido_em", nullable = false)
    private LocalDateTime obtidoEm;

    protected PrevisaoClimatica() {
    }

    public PrevisaoClimatica(FonteIntegracao fonte, String contexto, String timezone,
            LocalDateTime dataHoraPrevisao) {
        this.fonte = fonte;
        this.contexto = contexto;
        this.timezone = timezone;
        this.dataHoraPrevisao = dataHoraPrevisao;
    }

    public boolean atualizar(BigDecimal temperatura, Integer umidadeRelativa, BigDecimal precipitacao,
            Integer probabilidadePrecipitacao, BigDecimal velocidadeVento, BigDecimal rajadaVento,
            BigDecimal et0, BigDecimal umidadeSolo, Integer codigoTempo, LocalDateTime obtidoEm,
            String timezone) {
        boolean alterada = !decimalIgual(this.temperatura, temperatura)
                || !Objects.equals(this.umidadeRelativa, umidadeRelativa)
                || !decimalIgual(this.precipitacao, precipitacao)
                || !Objects.equals(this.probabilidadePrecipitacao, probabilidadePrecipitacao)
                || !decimalIgual(this.velocidadeVento, velocidadeVento)
                || !decimalIgual(this.rajadaVento, rajadaVento)
                || !decimalIgual(this.et0, et0)
                || !decimalIgual(this.umidadeSolo, umidadeSolo)
                || !Objects.equals(this.codigoTempo, codigoTempo)
                || !Objects.equals(this.timezone, timezone);
        if (alterada) {
            this.temperatura = temperatura;
            this.umidadeRelativa = umidadeRelativa;
            this.precipitacao = precipitacao;
            this.probabilidadePrecipitacao = probabilidadePrecipitacao;
            this.velocidadeVento = velocidadeVento;
            this.rajadaVento = rajadaVento;
            this.et0 = et0;
            this.umidadeSolo = umidadeSolo;
            this.codigoTempo = codigoTempo;
            this.timezone = timezone;
        }
        this.obtidoEm = obtidoEm;
        return alterada;
    }

    public Long getId() {
        return id;
    }

    public FonteIntegracao getFonte() {
        return fonte;
    }

    public String getContexto() {
        return contexto;
    }

    public String getTimezone() {
        return timezone;
    }

    public LocalDateTime getDataHoraPrevisao() {
        return dataHoraPrevisao;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public Integer getUmidadeRelativa() {
        return umidadeRelativa;
    }

    public BigDecimal getPrecipitacao() {
        return precipitacao;
    }

    public Integer getProbabilidadePrecipitacao() {
        return probabilidadePrecipitacao;
    }

    public BigDecimal getVelocidadeVento() {
        return velocidadeVento;
    }

    public BigDecimal getRajadaVento() {
        return rajadaVento;
    }

    public BigDecimal getEt0() {
        return et0;
    }

    public BigDecimal getUmidadeSolo() {
        return umidadeSolo;
    }

    public Integer getCodigoTempo() {
        return codigoTempo;
    }

    public LocalDateTime getObtidoEm() {
        return obtidoEm;
    }

    private boolean decimalIgual(BigDecimal atual, BigDecimal novo) {
        if (atual == null || novo == null) {
            return atual == novo;
        }
        return atual.compareTo(novo) == 0;
    }
}
