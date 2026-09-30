package com.example.sitiopro.criacao.peixes.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record MedicaoQualidadeAguaResumo(Long id,Long loteId,String loteCodigo,Long instalacaoId,String instalacaoNome,LocalDateTime medidoEm,BigDecimal temperatura,BigDecimal ph,BigDecimal oxigenioDissolvido,BigDecimal transparenciaCm,BigDecimal amonia,BigDecimal nitrito,String responsavel,String observacao,List<String> desvios,boolean dentroDosLimites,long versao){}
