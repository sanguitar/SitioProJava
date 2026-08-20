package com.example.sitiopro.planejamento;

public enum StatusPlanejamento {
    FUNCIONAL("FUNCIONAL", "status-funcional"),
    EM_DESENVOLVIMENTO("EM DESENVOLVIMENTO", "status-em-desenvolvimento"),
    PLANEJADO("PLANEJADO", "status-planejado"),
    PRECISA_REVISAO("PRECISA REVISÃO", "status-precisa-revisao");

    private final String rotulo;
    private final String classe;

    StatusPlanejamento(String rotulo, String classe) {
        this.rotulo = rotulo;
        this.classe = classe;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getClasse() {
        return classe;
    }
}
