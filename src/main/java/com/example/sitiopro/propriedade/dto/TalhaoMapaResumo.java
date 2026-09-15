package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record TalhaoMapaResumo(Long id, String codigo, String nome, String cor, BigDecimal areaCadastralHa,
        BigDecimal areaGisM2, boolean geometriaValida, List<Ponto> vertices, List<Ponto> poligonoFechado) {
    public TalhaoMapaResumo {
        vertices = List.copyOf(vertices);
        poligonoFechado = List.copyOf(poligonoFechado);
    }

    public static TalhaoMapaResumo de(Long id, String codigo, String nome, BigDecimal areaCadastralHa,
            BigDecimal areaGisM2, boolean geometriaValida, List<Vertice> vertices) {
        List<Ponto> pontos = vertices.stream()
                .map(v -> new Ponto(v.ordem(), v.latitude(), v.longitude(), v.altitudeGeodesicaM(), rotulo(v)))
                .toList();
        List<Ponto> poligono = new ArrayList<>();
        if (pontos.size() >= 3) {
            poligono.addAll(pontos);
            poligono.add(pontos.getFirst());
        }
        return new TalhaoMapaResumo(id, codigo, nome, cor(id), areaCadastralHa, areaGisM2, geometriaValida,
                pontos, poligono);
    }

    private static String rotulo(Vertice vertice) {
        if (vertice.marco() == null || vertice.marco().isBlank()) {
            return String.valueOf(vertice.ordem());
        }
        return vertice.ordem() + " - " + vertice.marco();
    }

    private static String cor(Long id) {
        String[] cores = {"#2f80ed", "#9b51e0", "#f2994a", "#27ae60", "#eb5757", "#00a6a6"};
        return cores[(int) Math.floorMod(id == null ? 0 : id, cores.length)];
    }

    public record Vertice(int ordem, BigDecimal latitude, BigDecimal longitude, BigDecimal altitudeGeodesicaM,
            String marco, String observacao) {}
    public record Ponto(int ordem, BigDecimal latitude, BigDecimal longitude, BigDecimal altitudeGeodesicaM,
            String rotulo) {}
}
