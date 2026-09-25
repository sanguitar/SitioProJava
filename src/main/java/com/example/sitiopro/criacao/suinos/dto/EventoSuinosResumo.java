package com.example.sitiopro.criacao.suinos.dto;
import com.example.sitiopro.criacao.suinos.entity.TipoEventoSuinos;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record EventoSuinosResumo(Long id, TipoEventoSuinos tipo, String tipoRotulo, Integer quantidade,
        BigDecimal valorDecimal, LocalDateTime dataEvento, String usuario, String observacao,
        String instalacaoOrigem, String instalacaoDestino, String itemEstoque, String localEstoque, Long movimentoEstoqueId) {}
