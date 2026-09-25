package com.example.sitiopro.criacao.suinos.entity;

public enum StatusCicloReprodutivoSuinos {
    AGUARDANDO_CHECAGEM("Aguardando checagem", false),
    GESTANTE("Gestante", false),
    NAO_CONFIRMADA("Gestação não confirmada", true),
    PARTO_REALIZADO("Parto realizado", false),
    DESMAMADO("Desmamado", true),
    ENCERRADO("Encerrado", true);
    private final String rotulo;
    private final boolean finalizado;
    StatusCicloReprodutivoSuinos(String rotulo, boolean finalizado) { this.rotulo = rotulo; this.finalizado = finalizado; }
    public String getRotulo() { return rotulo; }
    public boolean isFinalizado() { return finalizado; }
}
