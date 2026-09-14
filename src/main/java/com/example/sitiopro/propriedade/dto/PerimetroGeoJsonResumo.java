package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.StatusCrs;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PerimetroGeoJsonResumo(String type, Geometry geometry, Map<String, Object> properties) {
    public static PerimetroGeoJsonResumo de(StatusCrs statusCrs, String crs, String datum,
            PerimetroConferenciaResumo conferencia, List<PerimetroResumo.Vertice> vertices) {
        Geometry geometry = geometry(vertices);
        return new PerimetroGeoJsonResumo("Feature", geometry, Map.of(
                "statusCrs", statusCrs.name(),
                "crs", crs == null ? "" : crs,
                "datum", datum == null ? "" : datum,
                "crsEpsg", conferencia.crsEpsg() == null ? "" : conferencia.crsEpsg(),
                "uso", "operacional",
                "aviso", "GeoJSON operacional; nao representa certificacao fundiaria."));
    }

    private static Geometry geometry(List<PerimetroResumo.Vertice> vertices) {
        if (vertices.isEmpty()) {
            return null;
        }
        if (vertices.size() == 1) {
            return new Geometry("Point", coordenada(vertices.getFirst()));
        }
        if (vertices.size() == 2) {
            return new Geometry("LineString", vertices.stream().map(PerimetroGeoJsonResumo::coordenada).toList());
        }
        List<List<BigDecimal>> anel = new ArrayList<>(vertices.stream().map(PerimetroGeoJsonResumo::coordenada).toList());
        anel.add(coordenada(vertices.getFirst()));
        return new Geometry("Polygon", List.of(anel));
    }

    private static List<BigDecimal> coordenada(PerimetroResumo.Vertice vertice) {
        if (vertice.altitudeGeodesicaM() == null) {
            return List.of(vertice.longitude(), vertice.latitude());
        }
        return List.of(vertice.longitude(), vertice.latitude(), vertice.altitudeGeodesicaM());
    }

    public record Geometry(String type, Object coordinates) {}
}
