package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AcompanhamentoIncubacaoAvesResumo(
        Long id, LocalDateTime dataHora, TipoAcompanhamentoIncubacaoAves tipo,
        Integer quantidadeAvaliada, Integer ovosFerteis, Integer ovosSemDesenvolvimento,
        Integer perdas, BigDecimal temperatura, BigDecimal umidade, String observacao,
        LocalDateTime criadoEm, String criadoPor) {
}

