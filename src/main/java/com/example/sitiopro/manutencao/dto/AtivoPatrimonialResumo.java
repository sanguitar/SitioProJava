package com.example.sitiopro.manutencao.dto;

import com.example.sitiopro.manutencao.entity.*;
import java.math.BigDecimal;
import java.time.*;

public record AtivoPatrimonialResumo(Long id, String codigo, String nome, TipoAtivoPatrimonial tipo,
        String marca, String modelo, String numeroSerie, LocalDate dataAquisicao,
        BigDecimal valorAquisicao, String localizacao, Long estruturaId, String estruturaNome,
        StatusAtivoPatrimonial status, String observacao, long versao,
        LocalDateTime criadoEm, String criadoPor) {}
