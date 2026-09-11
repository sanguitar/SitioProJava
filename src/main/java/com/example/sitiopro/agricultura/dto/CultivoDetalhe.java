package com.example.sitiopro.agricultura.dto;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import java.util.List;
public record CultivoDetalhe(CultivoResumo resumo, List<PlantioResumo> plantios,
        List<AcompanhamentoResumo> acompanhamentos, List<ColheitaResumo> colheitas,
        List<AdubacaoResumo> adubacoes, List<IrrigacaoResumo> irrigacoes,
        List<TratamentoResumo> tratamentos, List<OcorrenciaResumo> ocorrencias,
        List<TarefaResumo> tarefas, ClimaResumo clima) {
    public CultivoDetalhe(CultivoResumo resumo, List<PlantioResumo> plantios,
            List<AcompanhamentoResumo> acompanhamentos, List<ColheitaResumo> colheitas,
            List<TarefaResumo> tarefas, ClimaResumo clima) {
        this(resumo, plantios, acompanhamentos, colheitas, List.of(), List.of(), List.of(), List.of(), tarefas, clima);
    }
}
