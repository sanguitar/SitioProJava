package com.example.sitiopro.administracao.configuracao.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import com.example.sitiopro.propriedade.entity.Propriedade;

@Entity
@Table(name = "configuracoes_operacionais")
public class ConfiguracaoOperacional extends AuditableEntity {

    public static final int ID_UNICO = 1;

    @Id
    private Integer id = ID_UNICO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propriedade_id", nullable = false)
    private Propriedade propriedade;

    @Column(nullable = false, length = 80)
    private String timezone;

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

    public ConfiguracaoOperacional(Propriedade propriedade, String timezone,
            int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias) {
        this.propriedade = propriedade;
        atualizar(timezone, diasPadraoIncubacao, antecedenciaAlertaEclosaoDias);
    }

    public void atualizar(String timezone, int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias) {
        if (this.timezone != null && !this.timezone.equals(timezone)) {
            revisaoLocalizacao++;
        }
        this.timezone = timezone;
        this.diasPadraoIncubacao = diasPadraoIncubacao;
        this.antecedenciaAlertaEclosaoDias = antecedenciaAlertaEclosaoDias;
    }

    public Integer getId() { return id; }
    public Propriedade getPropriedade() { return propriedade; }
    public String getNomePropriedade() { return propriedade.getNome(); }
    public String getTimezone() { return timezone; }
    public BigDecimal getLatitude() { return propriedade.getLatitudeCentral(); }
    public BigDecimal getLongitude() { return propriedade.getLongitudeCentral(); }
    public int getDiasPadraoIncubacao() { return diasPadraoIncubacao; }
    public int getAntecedenciaAlertaEclosaoDias() { return antecedenciaAlertaEclosaoDias; }
    public long getVersao() { return versao; }
    public long getRevisaoLocalizacao() { return revisaoLocalizacao + propriedade.getRevisaoLocalizacao(); }
}
