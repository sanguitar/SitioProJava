package com.example.sitiopro.agua.dto;

import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacaoAguaResumo(Long id, TipoOperacaoAgua tipo, LocalDateTime dataOperacao,
        Long recursoOrigemId, String recursoOrigemNome, Long recursoDestinoId, String recursoDestinoNome,
        BigDecimal quantidadeLitros, BigDecimal volumeOrigemAntesLitros,
        BigDecimal volumeOrigemDepoisLitros, BigDecimal volumeDestinoAntesLitros,
        BigDecimal volumeDestinoDepoisLitros, String observacao, long versao,
        LocalDateTime criadoEm, String criadoPor) {
}
