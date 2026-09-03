package com.example.sitiopro.propriedade.web;

import com.example.sitiopro.propriedade.service.PropriedadeOperacaoException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = PropriedadeController.class)
public class PropriedadeWebExceptionHandler {
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ModelAndView concorrencia(Exception ex) {
        return operacao(new PropriedadeOperacaoException(null, "Registro alterado. Recarregue os dados.",
                org.springframework.http.HttpStatus.CONFLICT));
    }
    @ExceptionHandler(PropriedadeOperacaoException.class)
    public ModelAndView operacao(PropriedadeOperacaoException ex) {
        ModelAndView view = new ModelAndView("propriedade/erro");
        view.setStatus(ex.getStatus());
        view.addObject("active", "propriedade");
        view.addObject("titulo", "Propriedade");
        view.addObject("erro", ex.getMessage());
        return view;
    }
}
