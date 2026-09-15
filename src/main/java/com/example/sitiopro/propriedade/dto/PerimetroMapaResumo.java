package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.StatusCrs;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record PerimetroMapaResumo(String formato, String aviso, StatusCrs statusCrs, boolean crsConfirmado,
        String crs, String datum, List<Ponto> vertices, List<Ponto> poligonoFechado,
        List<TalhaoMapaResumo> talhoes) {
    public PerimetroMapaResumo {
        vertices = List.copyOf(vertices);
        poligonoFechado = List.copyOf(poligonoFechado);
        talhoes = List.copyOf(talhoes);
    }

    static PerimetroMapaResumo de(StatusCrs statusCrs, String crs, String datum,
            List<PerimetroResumo.Vertice> vertices, List<TalhaoMapaResumo> talhoes) {
        List<Ponto> pontos = vertices.stream()
                .map(v -> new Ponto(v.ordem(), v.latitude(), v.longitude(), v.altitudeGeodesicaM(), rotulo(v)))
                .toList();
        List<Ponto> poligono = new ArrayList<>();
        if (pontos.size() >= 3) {
            poligono.addAll(pontos);
            poligono.add(pontos.getFirst());
        }
        return new PerimetroMapaResumo("SITIOPRO_PERIMETRO_OPERACIONAL",
                "Mapa operacional das coordenadas cadastradas. Nao representa area juridica, distancia oficial ou CRS presumido.",
                statusCrs, statusCrs == StatusCrs.CONFIRMADO, crs, datum, pontos, poligono, talhoes);
    }

    private static String rotulo(PerimetroResumo.Vertice vertice) {
        if (vertice.marco() == null || vertice.marco().isBlank()) {
            return String.valueOf(vertice.ordem());
        }
        return vertice.ordem() + " - " + vertice.marco();
    }

    public record Ponto(int ordem, BigDecimal latitude, BigDecimal longitude, BigDecimal altitudeGeodesicaM,
            String rotulo) {}
}
