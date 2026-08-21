package com.example.sitiopro.integracao.embrapa.agrofit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgrofitCulturaPayload(String nome) {
}
