package com.example.sitiopro.administracao.configuracao.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "configuracoes_operacionais")
public class ConfiguracaoOperacional extends AuditableEntity {

    public static final int ID_UNICO = 1;

    @Id
    private Integer id = ID_UNICO;

    @Column(name = "nome_propriedade", nullable = false, length = 120)
    private String nomePropriedade;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "dias_padrao_incubacao", nullable = false)
    private int diasPadraoIncubacao;

    @Column(name = "antecedencia_alerta_eclosao_dias", nullable = false)
    private int antecedenciaAlertaEclosaoDias;

    @Column(name = "revisao_localizacao", nullable = false)
    private long revisaoLocalizacao;

    @Version
    @Column(nullable = false)
    private long versao;

    protected ConfiguracaoOperacional() {
    }

    public ConfiguracaoOperacional(String nomePropriedade, String timezone, BigDecimal latitude,
            BigDecimal longitude, int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias) {
        atualizar(nomePropriedade, timezone, latitude, longitude,
                diasPadraoIncubacao, antecedenciaAlertaEclosaoDias);
    }

    public void atualizar(String nomePropriedade, String timezone, BigDecimal latitude,
            BigDecimal longitude, int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias) {
        if (this.timezone != null && (!this.timezone.equals(timezone)
                || !Objects.equals(normalizar(this.latitude), normalizar(latitude))
                || !Objects.equals(normalizar(this.longitude), normalizar(longitude)))) {
            revisaoLocalizacao++;
        }
        this.nomePropriedade = nomePropriedade;
        this.timezone = timezone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.diasPadraoIncubacao = diasPadraoIncubacao;
        this.antecedenciaAlertaEclosaoDias = antecedenciaAlertaEclosaoDias;
    }

    public Integer getId() { return id; }
    public String getNomePropriedade() { return nomePropriedade; }
    public String getTimezone() { return timezone; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public int getDiasPadraoIncubacao() { return diasPadraoIncubacao; }
    public int getAntecedenciaAlertaEclosaoDias() { return antecedenciaAlertaEclosaoDias; }
    public long getVersao() { return versao; }
    public long getRevisaoLocalizacao() { return revisaoLocalizacao; }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? null : valor.stripTrailingZeros();
    }
}
