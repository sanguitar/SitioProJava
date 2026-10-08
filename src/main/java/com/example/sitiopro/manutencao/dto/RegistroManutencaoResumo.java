package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.TipoManutencao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RegistroManutencaoResumo(Long id, Long ativoId, String ativoCodigo, String ativoNome,
        Long planoPreventivoId, String planoPreventivoNome,
        TipoManutencao tipo, LocalDateTime dataManutencao, String descricao, String responsavel,
        BigDecimal horimetro, BigDecimal quilometragem, BigDecimal custo,
        LocalDateTime proximaManutencao, boolean proximaManutencaoConcluida,
        LocalDateTime proximaManutencaoConcluidaEm, String observacao,
        List<ConsumoManutencaoResumo> consumos, long versao,
        LocalDateTime criadoEm, String criadoPor) {}
