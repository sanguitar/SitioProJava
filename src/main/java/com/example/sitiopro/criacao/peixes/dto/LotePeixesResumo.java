package com.example.sitiopro.criacao.peixes.dto;
import com.example.sitiopro.criacao.peixes.entity.StatusLotePeixes;
import java.math.BigDecimal;
import java.time.LocalDate;
public record LotePeixesResumo(Long id,String codigo,String especie,int quantidadeAtual,BigDecimal pesoMedio,
        BigDecimal biomassaEstimada,Long instalacaoId,String instalacaoNome,StatusLotePeixes status,LocalDate dataEntrada) {}
