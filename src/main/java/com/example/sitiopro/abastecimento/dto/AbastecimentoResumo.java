package com.example.sitiopro.abastecimento.dto;

import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AbastecimentoResumo(
        Long id, Long veiculoId, String veiculoNome, LocalDate data,
        BigDecimal litros, BigDecimal precoPorLitro, BigDecimal valorTotal,
        BigDecimal kmNoAto, BigDecimal horimetroNoAto, TipoMedidorVeiculo tipoMedidor,
        String local, long versao, LocalDateTime criadoEm, String criadoPor) {

    public BigDecimal leitura() {
        return tipoMedidor == TipoMedidorVeiculo.HORIMETRO ? horimetroNoAto : kmNoAto;
    }
}
