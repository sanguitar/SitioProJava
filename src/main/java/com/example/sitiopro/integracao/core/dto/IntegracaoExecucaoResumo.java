package com.example.sitiopro.integracao.core.dto;

import com.example.sitiopro.integracao.core.StatusExecucaoIntegracao;
import com.example.sitiopro.integracao.core.entity.IntegracaoExecucao;

import java.time.Duration;
import java.time.LocalDateTime;

public record IntegracaoExecucaoResumo(
        Long id,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm,
        StatusExecucaoIntegracao status,
        int registrosLidos,
        int registrosInseridos,
        int registrosAtualizados,
        int registrosIgnorados,
        String erroCodigo,
        String erroResumo,
        String traceId,
        Long duracaoMillis) {

    public static IntegracaoExecucaoResumo de(IntegracaoExecucao execucao) {
        Long duracao = execucao.getFinalizadoEm() == null
                ? null
                : Duration.between(execucao.getIniciadoEm(), execucao.getFinalizadoEm()).toMillis();
        return new IntegracaoExecucaoResumo(
                execucao.getId(),
                execucao.getIniciadoEm(),
                execucao.getFinalizadoEm(),
                execucao.getStatus(),
                execucao.getRegistrosLidos(),
                execucao.getRegistrosInseridos(),
                execucao.getRegistrosAtualizados(),
                execucao.getRegistrosIgnorados(),
                execucao.getErroCodigo(),
                execucao.getErroResumo(),
                execucao.getTraceId(),
                duracao);
    }
}
