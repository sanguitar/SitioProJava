package com.example.sitiopro.integracao.core;

import java.util.Arrays;

public enum FonteIntegracao {
    OPEN_METEO("open-meteo", "Open-Meteo", "Clima"),
    EMBRAPA_AGROFIT("embrapa-agrofit", "Embrapa Agrofit", "Agronomia");

    private final String slug;
    private final String nome;
    private final String grupo;

    FonteIntegracao(String slug, String nome, String grupo) {
        this.slug = slug;
        this.nome = nome;
        this.grupo = grupo;
    }

    public String getSlug() {
        return slug;
    }

    public String getNome() {
        return nome;
    }

    public String getGrupo() {
        return grupo;
    }

    public static FonteIntegracao porSlug(String slug) {
        return Arrays.stream(values())
                .filter(fonte -> fonte.slug.equals(slug))
                .findFirst()
                .orElseThrow(() -> new IntegracaoOperacaoException(
                        "INTEGRACAO_NAO_ENCONTRADA", "Integração não encontrada."));
    }
}
