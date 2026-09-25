package com.example.sitiopro.criacao.suinos.service;
import org.springframework.http.HttpStatus;
public class SuinosOperacaoException extends RuntimeException {
    private final String codigo; private final HttpStatus status;
    public SuinosOperacaoException(String codigo,String mensagem){this(codigo,mensagem,HttpStatus.UNPROCESSABLE_ENTITY);}
    public SuinosOperacaoException(String codigo,String mensagem,HttpStatus status){super(mensagem);this.codigo=codigo;this.status=status;}
    public String getCodigo(){return codigo;} public HttpStatus getStatus(){return status;}
}
