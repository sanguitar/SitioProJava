package com.example.sitiopro.integracao.core.dto;

import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;

import java.time.LocalDateTime;

public record IntegracaoFonteResumo(
        String slug,
        String nome,
        String grupo,
        String descricao,
        boolean implementada,
        boolean habilitada,
        boolean configurada,
        boolean emExecucao,
        boolean sincronizacaoManualDisponivel,
        String credencial,
        StatusOperacionalIntegracao status,
        LocalDateTime ultimoSucesso,
        LocalDateTime ultimaTentativa,
        LocalDateTime proximaExecucao,
        IntegracaoExecucaoResumo ultimaExecucao) {
}
