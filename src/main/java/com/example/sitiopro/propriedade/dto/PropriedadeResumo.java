package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;

public record PropriedadeResumo(Long id, String nome, String municipio, String uf,
        BigDecimal areaTotalHa, BigDecimal latitudeCentral, BigDecimal longitudeCentral,
        String observacao, boolean ativo, long versao, long areas, long talhoes,
        long piquetes, long estruturas, long recursosHidricos) {
}
