package com.example.sitiopro.integracao.embrapa.agrofit.dto;

import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;

public record AgrofitCulturaResumo(String nome) {

    public static AgrofitCulturaResumo de(AgrofitCultura cultura) {
        return new AgrofitCulturaResumo(cultura.getNome());
    }
}
