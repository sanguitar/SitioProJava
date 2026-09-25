package com.example.sitiopro.criacao.suinos.dto;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.tarefas.dto.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
public record LoteSuinosDetalhe(Long id, String codigo, CategoriaSuino categoria, LocalDate dataEntrada,
        LocalDate dataNascimento, String origem, int quantidadeInicial, int quantidadeAtual, BigDecimal pesoMedio,
        Long instalacaoId, String instalacaoNome, StatusLoteSuinos status, String observacao,
        BigDecimal consumoAcumulado, int mortalidadeAcumulada, List<EventoSuinosResumo> historico,
        List<AlertaResumo> alertas, List<TarefaResumo> tarefas, long versao,
        LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor) {}
