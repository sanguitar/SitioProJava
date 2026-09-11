package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.tarefas.dto.TarefaResumo;
import java.math.BigDecimal;
import java.util.List;

public record AgriculturaResumo(SafraResumo safraAtual, long cultivosAtivos, BigDecimal areaCultivadaHa,
        long ocorrenciasAbertas, long ocorrenciasAltaCritica, long cultivosAfetados,
        List<TarefaResumo> proximosTrabalhos, List<AvisoAgricola> alertas,
        List<ColheitaResumo> colheitasRecentes) {

    public AgriculturaResumo(SafraResumo safraAtual, long cultivosAtivos, BigDecimal areaCultivadaHa,
            List<TarefaResumo> proximosTrabalhos, List<AvisoAgricola> alertas,
            List<ColheitaResumo> colheitasRecentes) {
        this(safraAtual, cultivosAtivos, areaCultivadaHa, 0, 0, 0,
                proximosTrabalhos, alertas, colheitasRecentes);
    }
}
