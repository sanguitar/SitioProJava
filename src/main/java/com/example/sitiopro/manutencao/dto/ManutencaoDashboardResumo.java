package com.example.sitiopro.manutencao.dto;

import java.math.BigDecimal;
import java.util.List;

public record ManutencaoDashboardResumo(long ativos, long ativosEmManutencao,
        long manutencoesVencidas, long proximasManutencoes, long planosAtivos,
        long planosPreventivosVencidos, long planosPreventivosProximos,
        BigDecimal custosUltimos30Dias, List<PlanoManutencaoPreventivaResumo> planosPrioritarios,
        List<RegistroManutencaoResumo> registrosRecentes) {
    public ManutencaoDashboardResumo(long ativos, long ativosEmManutencao,
            long manutencoesVencidas, long proximasManutencoes, BigDecimal custosUltimos30Dias,
            List<RegistroManutencaoResumo> registrosRecentes) {
        this(ativos, ativosEmManutencao, manutencoesVencidas, proximasManutencoes,
                0, 0, 0, custosUltimos30Dias, List.of(), registrosRecentes);
    }
}
