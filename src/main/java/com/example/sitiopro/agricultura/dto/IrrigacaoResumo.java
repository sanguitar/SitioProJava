package com.example.sitiopro.agricultura.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record IrrigacaoResumo(Long id, Long cultivoId, String culturaNome, LocalDateTime dataHora,
        Integer duracaoMinutos, BigDecimal volumeLitros, String metodo, String observacao, String responsavel) {}
