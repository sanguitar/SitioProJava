package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record CultivoResumo(Long id, Long safraId, String safraNome, Long talhaoId, String talhaoCodigo, String talhaoNome, Long culturaId, String culturaNome, BigDecimal areaCultivadaHa, LocalDate dataPlantio, Long diasDesdePlantio, LocalDate previsaoColheita, LocalDate dataColheitaReal, StatusCultivo status, String observacao, long versao) {}
