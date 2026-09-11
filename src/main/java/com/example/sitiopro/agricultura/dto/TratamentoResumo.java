package com.example.sitiopro.agricultura.dto;
import com.example.sitiopro.agricultura.entity.OrigemInsumo;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TratamentoResumo(Long id, Long cultivoId, String culturaNome, LocalDate data, String finalidade,
        String produtoAplicado, BigDecimal quantidade, String unidade, BigDecimal areaTratadaHa, String metodo,
        OrigemInsumo origem, String descricaoOrigem, Long movimentoEstoqueId, String observacao, String responsavel) {}
