package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record CulturaResumo(Long id, String nomeComum, String nomeCientifico, Integer cicloDiasEstimado, boolean ativo, Long agrofitCulturaId, String agrofitNome, String observacao, long versao) {}
