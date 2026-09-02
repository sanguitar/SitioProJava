package com.example.sitiopro.administracao.configuracao.service;

public class ConfiguracaoOperacionalInvalidaException extends RuntimeException {

    private final String campo;

    public ConfiguracaoOperacionalInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
