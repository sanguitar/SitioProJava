package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.TipoRegistroSanitarioSuinos;
import java.math.BigDecimal;
import java.time.*;

public record RegistroSanitarioSuinosResumo(Long id, TipoRegistroSanitarioSuinos tipo,
        LocalDate dataProcedimento, String procedimentoProduto, String motivo, String responsavel,
        String observacao, Long loteId, String loteCodigo, Long animalReprodutivoId,
        String animalCodigo, String animalIdentificacao, String proximaAcao, LocalDate proximaAcaoData,
        boolean proximaAcaoConcluida, LocalDateTime proximaAcaoConcluidaEm, BigDecimal custo,
        Long itemEstoqueId, String itemEstoqueNome, Long localEstoqueId, String localEstoqueNome,
        BigDecimal quantidadeConsumida, String loteEstoqueCodigo, Long movimentoEstoqueId,
        long versao, LocalDateTime criadoEm, String criadoPor) {}
