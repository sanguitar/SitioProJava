package com.example.sitiopro.producao.controller;

import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.service.ProducaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class ProducaoController {

    private final ProducaoService producaoService;
    private final CategoriaService categoriaService;

    public ProducaoController(ProducaoService producaoService, CategoriaService categoriaService) {
        this.producaoService = producaoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/cadastro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("producao", producaoService.novo());
        return "producao/cadastro";
    }

    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("producao", producaoService.buscarPorId(id));
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "producao/cadastro";
    }

    @PostMapping("/salvar")
    public String salvarItem(@ModelAttribute Producao producao) {
        producaoService.salvar(producao);
        return "redirect:/sitio/painel";
    }

    @GetMapping("/excluir/{id}")
    public String excluirItem(@PathVariable("id") Long id) {
        producaoService.excluir(id);
        return "redirect:/sitio/painel";
    }
}
