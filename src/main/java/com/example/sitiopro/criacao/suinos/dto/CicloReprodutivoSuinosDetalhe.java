package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.tarefas.dto.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record CicloReprodutivoSuinosDetalhe(Long id, String codigo,
        AnimalReprodutivoSuinosResumo matriz, AnimalReprodutivoSuinosResumo reprodutor,
        MetodoReproducaoSuinos metodo, LocalDate dataCobertura, LocalDate dataPrevistaChecagem,
        LocalDate dataChecagem, LocalDate dataPrevistaParto, LocalDate dataParto,
        Integer nascidosVivos, Integer natimortos, Integer perdasParto,
        Long loteLeitoesId, String loteLeitoesCodigo, LocalDate dataPrevistaDesmame,
        LocalDate dataDesmame, BigDecimal pesoMedioDesmame, StatusCicloReprodutivoSuinos status,
        String observacao, List<TarefaResumo> tarefas, List<AlertaResumo> alertas,
        long versao, LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor) {}
