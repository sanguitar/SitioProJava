package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;

import java.time.LocalDate;
import java.util.List;

public record FichaOvoscopiaIncubacaoAves(
        String propriedade,
        String codigoIncubacao,
        MetodoIncubacaoAves metodo,
        LocalDate dataInicio,
        long diaIncubacao,
        LocalDate dataOvoscopia,
        LocalDate proximaVerificacao,
        List<OvoIncubacaoAvesResumo> ovos) {
}

