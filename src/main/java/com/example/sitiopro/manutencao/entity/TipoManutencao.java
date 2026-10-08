package com.example.sitiopro.manutencao.entity;

public enum TipoManutencao {
    PREVENTIVA("Preventiva"), CORRETIVA("Corretiva"), INSPECAO("Inspeção"),
    LUBRIFICACAO("Lubrificação"), TROCA_PECA("Troca de peça"), OUTRO("Outro");

    private final String rotulo;
    TipoManutencao(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
