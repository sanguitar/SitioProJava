package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record ColheitaResumo(Long id, Long cultivoId, String culturaNome, LocalDate data, BigDecimal quantidade,
        String unidade, String classificacao, BigDecimal perdas, boolean finalizaCultivo, DestinoColheita destino,
        Long movimentoEstoqueId, String observacao, String responsavel) {
    public ColheitaResumo(Long id, Long cultivoId, String culturaNome, LocalDate data, BigDecimal quantidade,
            String unidade, String classificacao, BigDecimal perdas, boolean finalizaCultivo,
            String observacao, String responsavel) {
        this(id, cultivoId, culturaNome, data, quantidade, unidade, classificacao, perdas, finalizaCultivo,
                DestinoColheita.SEM_ESTOQUE, null, observacao, responsavel);
    }
}
