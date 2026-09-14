package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.StatusCrs;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PerimetroResumo(Long id, long versao, StatusCrs statusCrs, String crs, String datum,
        String observacao, List<Vertice> vertices, LocalDateTime alteradoEm, String alteradoPor) {
    public PerimetroResumo { vertices = List.copyOf(vertices); }
    public int getQuantidadeVertices() { return vertices.size(); }
    public String getStatusGeorreferenciamento() {
        if (vertices.isEmpty()) return "SEM_VERTICES";
        if (vertices.size() < 3) return "EM_CADASTRO";
        return statusCrs == StatusCrs.NAO_CONFIRMADO ? "CRS_NAO_CONFIRMADO" : "CADASTRADO_SEM_VALIDACAO_OFICIAL";
    }
    public static PerimetroResumo vazio() {
        return new PerimetroResumo(null, -1, StatusCrs.NAO_CONFIRMADO, null, null, null, List.of(), null, null);
    }
    public record Vertice(int ordem, BigDecimal latitude, BigDecimal longitude, String marco, String observacao) {}
}
