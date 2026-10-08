package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.TipoPeriodicidadeManutencao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanoManutencaoPreventivaResumo(Long id, Long ativoId, String ativoCodigo, String ativoNome,
        String nome, String descricao, TipoPeriodicidadeManutencao tipoPeriodicidade,
        BigDecimal intervalo, LocalDateTime proximaData, BigDecimal proximoValor,
        BigDecimal leituraAtual, boolean ativo, boolean vencido, int cicloAtual, long versao) {}
