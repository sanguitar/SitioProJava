package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record TalhoesGeoJsonResumo(String type, List<Feature> features) {
    public static TalhoesGeoJsonResumo de(List<TalhaoMapaResumo> talhoes) {
        return new TalhoesGeoJsonResumo("FeatureCollection", talhoes.stream().map(TalhoesGeoJsonResumo::feature).toList());
    }

    private static Feature feature(TalhaoMapaResumo talhao) {
        return new Feature("Feature", geometry(talhao.vertices()), Map.of(
                "id", talhao.id(),
                "codigo", talhao.codigo() == null ? "" : talhao.codigo(),
                "nome", talhao.nome(),
                "epsg", 4674,
                "crs", "EPSG:4674",
                "datum", "SIRGAS 2000",
                "areaCadastralHa", talhao.areaCadastralHa() == null ? "" : talhao.areaCadastralHa(),
                "areaGisM2", talhao.areaGisM2() == null ? "" : talhao.areaGisM2(),
                "uso", "operacional"));
    }

    private static Geometry geometry(List<TalhaoMapaResumo.Ponto> vertices) {
        if (vertices.size() < 3) {
            return null;
        }
        List<List<BigDecimal>> anel = new ArrayList<>(vertices.stream().map(TalhoesGeoJsonResumo::coordenada).toList());
        anel.add(coordenada(vertices.getFirst()));
        return new Geometry("Polygon", List.of(anel));
    }

    private static List<BigDecimal> coordenada(TalhaoMapaResumo.Ponto vertice) {
        if (vertice.altitudeGeodesicaM() == null) {
            return List.of(vertice.longitude(), vertice.latitude());
        }
        return List.of(vertice.longitude(), vertice.latitude(), vertice.altitudeGeodesicaM());
    }

    public record Feature(String type, Geometry geometry, Map<String, Object> properties) {}
    public record Geometry(String type, Object coordinates) {}
}
