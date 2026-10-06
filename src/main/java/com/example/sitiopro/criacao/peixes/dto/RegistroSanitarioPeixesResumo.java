package com.example.sitiopro.criacao.peixes.dto;

import com.example.sitiopro.criacao.peixes.entity.TipoRegistroSanitarioPeixes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistroSanitarioPeixesResumo(Long id, Long loteId, String loteCodigo,
        TipoRegistroSanitarioPeixes tipo, LocalDateTime dataProcedimento,
        String procedimentoProduto, String motivo, String responsavel, String observacao,
        String proximaAcao, LocalDateTime proximaAcaoData, boolean proximaAcaoConcluida,
        LocalDateTime proximaAcaoConcluidaEm, BigDecimal custo, Long itemEstoqueId,
        String itemEstoqueNome, Long localEstoqueId, String localEstoqueNome,
        BigDecimal quantidadeConsumida, String loteEstoqueCodigo, Long movimentoEstoqueId,
        long versao, LocalDateTime criadoEm, String criadoPor) {}
