package com.example.sitiopro.producao.controller;

import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.service.ProducaoService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class ProducaoController {

    private final ProducaoService producaoService;
    private final EstoqueCatalogoService estoqueCatalogoService;

    public ProducaoController(ProducaoService producaoService, EstoqueCatalogoService estoqueCatalogoService) {
        this.producaoService = producaoService;
        this.estoqueCatalogoService = estoqueCatalogoService;
    }

    @InitBinder("producaoForm")
    void restringirCamposProducao(WebDataBinder binder) {
        binder.setAllowedFields("id", "categoriaId", "item", "quantidade", "unidade", "status");
    }

    @GetMapping("/cadastro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producaoForm", producaoService.novoFormulario());
        return prepararFormulario(model);
    }

    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("producaoForm", producaoService.formularioEdicao(id));
        return prepararFormulario(model);
    }

    @PostMapping("/salvar")
    public String salvarItem(@Valid @ModelAttribute("producaoForm") ProducaoForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return prepararFormulario(model);
        }
        try {
            producaoService.salvar(form);
        } catch (EstoqueOperacaoException ex) {
            bindingResult.rejectValue("categoriaId", ex.getCode(), ex.getMessage());
            return prepararFormulario(model);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("cadastroRural.invalido", ex.getMessage());
            return prepararFormulario(model);
        }
        return "redirect:/sitio/painel";
    }

    @PostMapping("/excluir/{id}")
    public String excluirItem(@PathVariable("id") Long id) {
        producaoService.excluir(id);
        return "redirect:/sitio/painel";
    }

    private String prepararFormulario(Model model) {
        model.addAttribute("categorias", estoqueCatalogoService.listarCategoriasAtivas());
        return "producao/cadastro";
    }
}
