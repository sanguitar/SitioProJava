package com.example.sitiopro.criacao.peixes.dto;
import com.example.sitiopro.criacao.peixes.entity.StatusLotePeixes;
import com.example.sitiopro.tarefas.dto.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
public record LotePeixesDetalhe(Long id,String codigo,String especie,int quantidadeInicial,int quantidadeAtual,
        LocalDate dataEntrada,String origem,BigDecimal pesoMedio,BigDecimal biomassaEstimada,
        Long instalacaoId,String instalacaoNome,StatusLotePeixes status,String observacao,
        BigDecimal consumoAcumulado,long mortalidadeAcumulada,List<EventoPeixesResumo> historico,
        List<AlertaResumo> alertas,List<TarefaResumo> tarefas,long versao,
        LocalDateTime criadoEm,String criadoPor,LocalDateTime alteradoEm,String alteradoPor) {}
