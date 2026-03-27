package com.example.demo.Controller;

import com.example.demo.Model.Veiculo;
import com.example.demo.Service.VeiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sitio/frota") // Mantenha essa rota para o menu funcionar
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("veiculos", veiculoService.listarTodos());
        model.addAttribute("usuario", "Systems Analyst");
        return "frota/lista";
    }

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        // IMPORTANTE: O nome "veiculo" aqui deve ser IGUAL ao th:object="${veiculo}" no
        // HTML
        model.addAttribute("veiculo", new Veiculo());
        return "frota/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Veiculo veiculo) {
        veiculoService.salvar(veiculo);
        return "redirect:/sitio/frota";
    }
}