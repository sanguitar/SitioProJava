package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.StatusCrs;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PerimetroResumo(Long id, long versao, StatusCrs statusCrs, String crs, String datum,
        String observacao, List<Vertice> vertices, LocalDateTime alteradoEm, String alteradoPor,
        PerimetroConferenciaResumo conferencia, List<TalhaoMapaResumo> talhoes) {
    public PerimetroResumo {
        vertices = List.copyOf(vertices);
        conferencia = conferencia == null ? PerimetroConferenciaResumo.vazio() : conferencia;
        talhoes = talhoes == null ? List.of() : List.copyOf(talhoes);
    }
    public PerimetroResumo(Long id, long versao, StatusCrs statusCrs, String crs, String datum,
            String observacao, List<Vertice> vertices, LocalDateTime alteradoEm, String alteradoPor,
            PerimetroConferenciaResumo conferencia) {
        this(id, versao, statusCrs, crs, datum, observacao, vertices, alteradoEm, alteradoPor, conferencia, List.of());
    }
    public PerimetroResumo(Long id, long versao, StatusCrs statusCrs, String crs, String datum,
            String observacao, List<Vertice> vertices, LocalDateTime alteradoEm, String alteradoPor) {
        this(id, versao, statusCrs, crs, datum, observacao, vertices, alteradoEm, alteradoPor,
                PerimetroConferenciaResumo.vazio(), List.of());
    }
    public int getQuantidadeVertices() { return vertices.size(); }
    public PerimetroMapaResumo getMapa() { return PerimetroMapaResumo.de(statusCrs, crs, datum, vertices, talhoes); }
    public PerimetroGeoJsonResumo getGeoJson() {
        return PerimetroGeoJsonResumo.de(statusCrs, crs, datum, conferencia, vertices);
    }
    public String getStatusGeorreferenciamento() {
        if (vertices.isEmpty()) return "SEM_VERTICES";
        if (vertices.size() < 3) return "EM_CADASTRO";
        return statusCrs == StatusCrs.NAO_CONFIRMADO ? "CRS_NAO_CONFIRMADO" : "CADASTRADO_SEM_VALIDACAO_OFICIAL";
    }
    public static PerimetroResumo vazio() {
        return new PerimetroResumo(null, -1, StatusCrs.NAO_CONFIRMADO, null, null, null, List.of(), null, null);
    }
    public record Vertice(int ordem, BigDecimal latitude, BigDecimal longitude, BigDecimal altitudeGeodesicaM,
            String marco, String observacao) {
        public Vertice(int ordem, BigDecimal latitude, BigDecimal longitude, String marco, String observacao) {
            this(ordem, latitude, longitude, null, marco, observacao);
        }
    }
}
