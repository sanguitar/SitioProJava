package com.example.sitiopro.criacao.peixes.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "peixes_qualidade_agua_configuracao")
public class ConfiguracaoQualidadeAgua extends AuditableEntity {
    public static final int ID_UNICO = 1;
    @Id private Integer id = ID_UNICO;
    @Column(name="temperatura_min",nullable=false,precision=8,scale=4) private BigDecimal temperaturaMin;
    @Column(name="temperatura_max",nullable=false,precision=8,scale=4) private BigDecimal temperaturaMax;
    @Column(name="ph_min",nullable=false,precision=8,scale=4) private BigDecimal phMin;
    @Column(name="ph_max",nullable=false,precision=8,scale=4) private BigDecimal phMax;
    @Column(name="oxigenio_min",nullable=false,precision=8,scale=4) private BigDecimal oxigenioMin;
    @Column(name="transparencia_min_cm",nullable=false,precision=10,scale=4) private BigDecimal transparenciaMinCm;
    @Column(name="amonia_max",nullable=false,precision=8,scale=4) private BigDecimal amoniaMax;
    @Column(name="nitrito_max",nullable=false,precision=8,scale=4) private BigDecimal nitritoMax;
    @Column(name="intervalo_medicao_dias",nullable=false) private int intervaloMedicaoDias;
    @Version @Column(nullable=false) private long versao;
    protected ConfiguracaoQualidadeAgua(){}
    public ConfiguracaoQualidadeAgua(BigDecimal tMin,BigDecimal tMax,BigDecimal pMin,BigDecimal pMax,
            BigDecimal oMin,BigDecimal trMin,BigDecimal aMax,BigDecimal nMax,int intervalo){
        atualizar(tMin,tMax,pMin,pMax,oMin,trMin,aMax,nMax,intervalo);
    }
    public void atualizar(BigDecimal tMin,BigDecimal tMax,BigDecimal pMin,BigDecimal pMax,BigDecimal oMin,BigDecimal trMin,BigDecimal aMax,BigDecimal nMax,int intervalo){temperaturaMin=tMin;temperaturaMax=tMax;phMin=pMin;phMax=pMax;oxigenioMin=oMin;transparenciaMinCm=trMin;amoniaMax=aMax;nitritoMax=nMax;intervaloMedicaoDias=intervalo;}
    public Integer getId(){return id;} public BigDecimal getTemperaturaMin(){return temperaturaMin;} public BigDecimal getTemperaturaMax(){return temperaturaMax;} public BigDecimal getPhMin(){return phMin;} public BigDecimal getPhMax(){return phMax;} public BigDecimal getOxigenioMin(){return oxigenioMin;} public BigDecimal getTransparenciaMinCm(){return transparenciaMinCm;} public BigDecimal getAmoniaMax(){return amoniaMax;} public BigDecimal getNitritoMax(){return nitritoMax;} public int getIntervaloMedicaoDias(){return intervaloMedicaoDias;} public long getVersao(){return versao;}
}
