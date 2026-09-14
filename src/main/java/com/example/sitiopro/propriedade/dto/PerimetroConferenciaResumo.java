package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;

public record PerimetroConferenciaResumo(String sistemaGeodesico, Integer crsEpsg,
        BigDecimal areaDocumentalHa, BigDecimal perimetroDocumentalM,
        BigDecimal areaCalculadaM2, BigDecimal perimetroCalculadoM,
        Boolean geometriaValida, String poligonoWkt) {
    public static PerimetroConferenciaResumo vazio() {
        return new PerimetroConferenciaResumo(null, null, null, null, null, null, null, null);
    }

    public boolean possuiValoresDocumentais() {
        return areaDocumentalHa != null || perimetroDocumentalM != null;
    }

    public boolean possuiCalculos() {
        return areaCalculadaM2 != null || perimetroCalculadoM != null;
    }
}
