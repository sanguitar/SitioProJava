package com.example.sitiopro.agua.dto;

import com.example.sitiopro.manutencao.dto.AtivoPatrimonialResumo;
import com.example.sitiopro.manutencao.dto.RegistroManutencaoResumo;
import java.util.List;

public record AguaDashboardResumo(long recursosAtivos, long volumesConhecidos, long niveisBaixos,
        long inspecoesVencidas, long ativosHidricos, long manutencoesVencidas,
        List<RecursoHidricoOperacionalResumo> recursos,
        List<OperacaoAguaResumo> operacoesRecentes,
        List<AtivoPatrimonialResumo> bombasEstruturas,
        List<RegistroManutencaoResumo> manutencoesRecentes) {
}
