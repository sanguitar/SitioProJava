package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OcorrenciaResumo(Long id, Long cultivoId, String culturaNome, LocalDateTime dataHora,
        TipoOcorrenciaCultivo tipo, String titulo, String descricao, SeveridadeOcorrencia severidade,
        BigDecimal areaAfetadaHa, BigDecimal quantidadePerdida, String unidadePerda, boolean perdaTotal,
        String observacao, StatusOcorrenciaCultivo status, String resolucao, LocalDateTime encerradaEm,
        List<ReferenciaAgrofitResumo> referenciasAgrofit, String responsavel, long versao) {

    public OcorrenciaResumo {
        referenciasAgrofit = referenciasAgrofit == null ? List.of() : List.copyOf(referenciasAgrofit);
    }

    public OcorrenciaResumo(Long id, Long cultivoId, String culturaNome, LocalDateTime dataHora,
            TipoOcorrenciaCultivo tipo, SeveridadeOcorrencia severidade, String descricao,
            BigDecimal areaAfetadaHa, BigDecimal quantidadePerdida, String unidadePerda,
            boolean perdaTotal, String observacao, String responsavel) {
        this(id, cultivoId, culturaNome, dataHora, tipo, descricao, descricao, severidade,
                areaAfetadaHa, quantidadePerdida, unidadePerda, perdaTotal, observacao,
                StatusOcorrenciaCultivo.ABERTA, null, null, List.of(), responsavel, 0);
    }
}
