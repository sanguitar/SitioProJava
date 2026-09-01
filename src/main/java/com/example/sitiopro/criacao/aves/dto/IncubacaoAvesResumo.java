package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncubacaoAvesResumo(
        Long id,
        String codigo,
        MetodoIncubacaoAves metodo,
        EspecieAves especie,
        Long instalacaoId,
        String instalacaoNome,
        LocalDate dataInicio,
        int quantidadeOvos,
        LocalDate dataPrevistaEclosao,
        StatusIncubacaoAves status,
        Integer pintinhosEclodidos,
        Integer ovosPerdidos,
        BigDecimal taxaEclosao,
        BigDecimal taxaPerdas,
        Long loteResultanteId,
        long diasRestantes,
        int progressoPercentual) {
}
