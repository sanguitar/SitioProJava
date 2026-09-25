package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.*;
import java.time.LocalDate;

public record CicloReprodutivoSuinosResumo(Long id, String codigo, Long matrizId, String matrizCodigo,
        String matrizIdentificacao, MetodoReproducaoSuinos metodo, LocalDate dataCobertura,
        LocalDate dataPrevistaChecagem, LocalDate dataPrevistaParto, LocalDate dataParto,
        StatusCicloReprodutivoSuinos status, Integer nascidosVivos, Long loteLeitoesId,
        String loteLeitoesCodigo, LocalDate dataPrevistaDesmame) {}
