package com.example.sitiopro.criacao.peixes.dto;
import java.util.List;
public record QualidadeAguaDashboardResumo(long lotesMonitorados,long lotesForaDosLimites,long lotesSemMedicao,List<MedicaoQualidadeAguaResumo> medicoesAtuais){
    public static QualidadeAguaDashboardResumo vazio(){return new QualidadeAguaDashboardResumo(0,0,0,List.of());}
}
