package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record SafraResumo(Long id, Long propriedadeId, String nome, Integer anoInicio, Integer anoFim, LocalDate dataInicio, LocalDate dataFim, StatusSafra status, String observacao, long versao) {}
