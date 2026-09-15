package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.SeveridadeOcorrencia;
import com.example.sitiopro.agricultura.entity.StatusCultivo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CultivoMapaResumo(Long id, String cultura, String safra, BigDecimal areaCultivadaHa,
        LocalDate dataPlantio, LocalDate previsaoColheita, StatusCultivo status,
        long ocorrenciasAbertas, SeveridadeOcorrencia severidadeMaisAlta) {
    public boolean possuiOcorrenciaRelevante() {
        return severidadeMaisAlta == SeveridadeOcorrencia.ALTA || severidadeMaisAlta == SeveridadeOcorrencia.CRITICA;
    }
}
