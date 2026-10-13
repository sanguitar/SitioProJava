package com.example.sitiopro.agua.dto;

import com.example.sitiopro.propriedade.entity.TipoRecursoHidrico;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecursoHidricoOperacionalResumo(Long id, String nome, TipoRecursoHidrico tipo, boolean ativo,
        BigDecimal capacidadeLitros, BigDecimal volumeAtualLitros, BigDecimal percentualOcupacao,
        BigDecimal nivelMinimoAlertaLitros, boolean nivelBaixo, boolean volumeConhecido,
        int intervaloInspecaoDias, LocalDateTime ultimaInspecao, LocalDateTime proximaInspecao,
        boolean inspecaoVencida, String observacao, long versao) {
}
