package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record AcompanhamentoResumo(Long id, LocalDateTime dataHora, TipoAcompanhamentoCultivo tipo, String descricao, String observacao, String responsavel) {}
