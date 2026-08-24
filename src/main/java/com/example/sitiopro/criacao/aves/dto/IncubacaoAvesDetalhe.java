package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncubacaoAvesDetalhe(Long id, String codigo, Long instalacaoId, String instalacaoNome,
        LocalDate dataInicio, int quantidadeOvos, String origemOvos, Long loteReprodutorId,
        String loteReprodutorCodigo, LocalDate dataPrevistaEclosao, StatusIncubacaoAves status,
        String observacao, Integer pintinhosEclodidos, Integer ovosPerdidos, LocalDate dataEclosao,
        BigDecimal taxaEclosao, BigDecimal taxaPerdas, Long loteResultanteId, String loteResultanteCodigo,
        long versao, LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor) {
}
