package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.TipoLeituraMedidor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeituraMedidorResumo(Long id, Long ativoId, LocalDateTime dataLeitura,
        BigDecimal horimetro, BigDecimal quilometragem, TipoLeituraMedidor tipoLeitura,
        String justificativaAjuste, long versao, LocalDateTime criadoEm, String criadoPor) {}
