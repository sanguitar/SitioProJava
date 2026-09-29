package com.example.sitiopro.criacao.peixes.dto;
import com.example.sitiopro.criacao.peixes.entity.TipoEventoPeixes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record EventoPeixesResumo(Long id,TipoEventoPeixes tipo,String tipoRotulo,Integer quantidade,
        BigDecimal valorDecimal,LocalDateTime dataEvento,String usuario,String observacao,
        String instalacaoOrigem,String instalacaoDestino,String itemEstoque,String localEstoque,Long movimentoEstoqueId) {}
