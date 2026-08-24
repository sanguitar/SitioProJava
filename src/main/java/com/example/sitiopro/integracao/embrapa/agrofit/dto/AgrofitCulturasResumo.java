package com.example.sitiopro.integracao.embrapa.agrofit.dto;

import java.util.List;

public record AgrofitCulturasResumo(List<AgrofitCulturaResumo> culturas) {

    public AgrofitCulturasResumo {
        culturas = List.copyOf(culturas);
    }
}
