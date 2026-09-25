package com.example.sitiopro.criacao.suinos.dto;
import com.example.sitiopro.criacao.suinos.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record LoteSuinosResumo(Long id, String codigo, CategoriaSuino categoria, int quantidadeAtual,
        BigDecimal pesoMedio, Long instalacaoId, String instalacaoNome, StatusLoteSuinos status, LocalDate dataEntrada) {}
