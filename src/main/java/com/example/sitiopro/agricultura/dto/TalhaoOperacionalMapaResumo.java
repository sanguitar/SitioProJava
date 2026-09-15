package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.propriedade.dto.TalhaoMapaResumo;
import java.math.BigDecimal;
import java.util.List;

public record TalhaoOperacionalMapaResumo(Long id, String codigo, String nome, String cor,
        BigDecimal areaCadastralHa, BigDecimal areaGisM2, boolean geometriaValida,
        List<TalhaoMapaResumo.Ponto> vertices, List<TalhaoMapaResumo.Ponto> poligonoFechado,
        CultivoMapaResumo cultivoAtivo) {
    public TalhaoOperacionalMapaResumo {
        vertices = vertices == null ? List.of() : List.copyOf(vertices);
        poligonoFechado = poligonoFechado == null ? List.of() : List.copyOf(poligonoFechado);
    }

    public boolean possuiCultivoAtivo() {
        return cultivoAtivo != null;
    }

    public boolean possuiOcorrenciaRelevante() {
        return cultivoAtivo != null && cultivoAtivo.possuiOcorrenciaRelevante();
    }

    public static TalhaoOperacionalMapaResumo de(TalhaoMapaResumo talhao, CultivoMapaResumo cultivo) {
        return new TalhaoOperacionalMapaResumo(talhao.id(), talhao.codigo(), talhao.nome(), talhao.cor(),
                talhao.areaCadastralHa(), talhao.areaGisM2(), talhao.geometriaValida(),
                talhao.vertices(), talhao.poligonoFechado(), cultivo);
    }
}
