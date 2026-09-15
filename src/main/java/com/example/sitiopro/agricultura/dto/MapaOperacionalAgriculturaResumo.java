package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.propriedade.dto.PerimetroMapaResumo;
import com.example.sitiopro.propriedade.entity.StatusCrs;
import java.util.List;

public record MapaOperacionalAgriculturaResumo(String formato, String aviso, StatusCrs statusCrs,
        boolean crsConfirmado, String crs, String datum, List<PerimetroMapaResumo.Ponto> vertices,
        List<PerimetroMapaResumo.Ponto> poligonoFechado, List<TalhaoOperacionalMapaResumo> talhoes) {
    public MapaOperacionalAgriculturaResumo {
        vertices = vertices == null ? List.of() : List.copyOf(vertices);
        poligonoFechado = poligonoFechado == null ? List.of() : List.copyOf(poligonoFechado);
        talhoes = talhoes == null ? List.of() : List.copyOf(talhoes);
    }

    public static MapaOperacionalAgriculturaResumo de(PerimetroMapaResumo mapa,
            List<TalhaoOperacionalMapaResumo> talhoes) {
        return new MapaOperacionalAgriculturaResumo("SITIOPRO_AGRICULTURA_MAPA_OPERACIONAL",
                "Mapa operacional local. A geometria vem dos Talhoes oficiais e os cultivos apenas referenciam esses Talhoes.",
                mapa.statusCrs(), mapa.crsConfirmado(), mapa.crs(), mapa.datum(),
                mapa.vertices(), mapa.poligonoFechado(), talhoes);
    }
}
