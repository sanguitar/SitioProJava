package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;

// Read model shared by the physical inventory; requests remain specific to each resource.
public record CadastroFisicoResumo(Long id, Long propriedadeId, Long areaId, String areaNome,
        String codigo, String nome, String tipo, BigDecimal areaHa, BigDecimal capacidade,
        String unidadeCapacidade, BigDecimal capacidadeLitros, String observacao,
        boolean ativo, String status, long versao, BigDecimal areaGisM2, int quantidadeVertices) {
    public CadastroFisicoResumo(Long id, Long propriedadeId, Long areaId, String areaNome,
            String codigo, String nome, String tipo, BigDecimal areaHa, BigDecimal capacidade,
            String unidadeCapacidade, BigDecimal capacidadeLitros, String observacao,
            boolean ativo, String status, long versao) {
        this(id, propriedadeId, areaId, areaNome, codigo, nome, tipo, areaHa, capacidade,
                unidadeCapacidade, capacidadeLitros, observacao, ativo, status, versao, null, 0);
    }
}
