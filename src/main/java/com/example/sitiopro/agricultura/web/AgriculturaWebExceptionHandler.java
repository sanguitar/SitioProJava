package com.example.sitiopro.agricultura.web;
import com.example.sitiopro.agricultura.service.AgriculturaOperacaoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;

@ControllerAdvice(assignableTypes = AgriculturaController.class)
public class AgriculturaWebExceptionHandler {
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ModelAndView concorrencia(Exception ex) {
        return operacao(new AgriculturaOperacaoException("Registro alterado ou vinculo invalido. Recarregue os dados.", HttpStatus.CONFLICT));
    }
    @ExceptionHandler(AgriculturaOperacaoException.class)
    public ModelAndView operacao(AgriculturaOperacaoException ex) {
        ModelAndView view = new ModelAndView("agricultura/erro");
        view.setStatus(ex.getStatus()); view.addObject("erro", ex.getMessage());
        view.addObject("active", "agricultura"); view.addObject("secao", "inicio"); view.addObject("titulo", "Agricultura");
        return view;
    }
}
