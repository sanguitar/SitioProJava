package com.example.sitiopro.categoria.controller;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/configuracoes")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String exibirConfiguracoes(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("novaCategoria", categoriaService.nova());
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        return "categoria/configuracoes";
    }

    @PostMapping("/categoria/salvar")
    public String salvarCategoria(@ModelAttribute Categoria categoria) {
        categoriaService.salvar(categoria);
        return "redirect:/sitio/configuracoes";
    }

    @PostMapping("/categoria/excluir/{id}")
    public String excluirCategoria(@PathVariable("id") Long id) {
        categoriaService.excluir(id);
        return "redirect:/sitio/configuracoes";
    }
}
