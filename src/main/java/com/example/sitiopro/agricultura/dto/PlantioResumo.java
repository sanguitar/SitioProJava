package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record PlantioResumo(Long id, LocalDate data, String metodo, BigDecimal quantidade, String unidade, String espacamento, OrigemPlantio origem, String descricaoOrigem, Long movimentoEstoqueId, String observacao, String responsavel) {}
