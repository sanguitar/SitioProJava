package com.example.sitiopro.propriedade.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "propriedades")
public class Propriedade extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 120)
    private String municipio;
    @Column(length = 2)
    private String uf;
    @Column(name = "area_total_ha", precision = 14, scale = 4)
    private BigDecimal areaTotalHa;
    @Column(name = "latitude_central", precision = 9, scale = 6)
    private BigDecimal latitudeCentral;
    @Column(name = "longitude_central", precision = 9, scale = 6)
    private BigDecimal longitudeCentral;
    @Column(length = 1000)
    private String observacao;
    @Column(nullable = false)
    private boolean ativo = true;
    @Column(nullable = false)
    private boolean principal;
    @Column(nullable = false)
    private long revisaoLocalizacao;
    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public BigDecimal getAreaTotalHa() { return areaTotalHa; }
    public void setAreaTotalHa(BigDecimal areaTotalHa) { this.areaTotalHa = areaTotalHa; }
    public BigDecimal getLatitudeCentral() { return latitudeCentral; }
    public BigDecimal getLongitudeCentral() { return longitudeCentral; }
    public void atualizarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        if (!Objects.equals(normalizar(latitudeCentral), normalizar(latitude))
                || !Objects.equals(normalizar(longitudeCentral), normalizar(longitude))) {
            revisaoLocalizacao++;
        }
        latitudeCentral = latitude;
        longitudeCentral = longitude;
    }
    public void inicializarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        latitudeCentral = latitude;
        longitudeCentral = longitude;
    }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    public long getVersao() { return versao; }
    public long getRevisaoLocalizacao() { return revisaoLocalizacao; }
    private BigDecimal normalizar(BigDecimal valor) { return valor == null ? null : valor.stripTrailingZeros(); }
}
