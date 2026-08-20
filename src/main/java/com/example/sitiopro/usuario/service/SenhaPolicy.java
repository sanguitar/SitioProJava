package com.example.sitiopro.usuario.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SenhaPolicy {

    public static final int TAMANHO_MINIMO = 12;
    public static final int TAMANHO_MAXIMO = 128;

    public void validar(String senha) {
        if (!StringUtils.hasText(senha)) {
            throw new SenhaInvalidaException("Informe uma senha.");
        }
        if (senha.length() < TAMANHO_MINIMO) {
            throw new SenhaInvalidaException("A senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres.");
        }
        if (senha.length() > TAMANHO_MAXIMO) {
            throw new SenhaInvalidaException("A senha deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
        }
    }

    public void validarConfirmacao(String senha, String confirmacao) {
        validar(senha);
        if (!senha.equals(confirmacao)) {
            throw new SenhaInvalidaException("A confirmação da senha não confere.");
        }
    }
}
