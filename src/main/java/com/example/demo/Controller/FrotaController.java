package com.example.demo.Controller;

import com.example.demo.Model.Veiculo;
import com.example.demo.Repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sitio/frota")
public class FrotaController {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @GetMapping
    public String listarFrota(Model model) {
        model.addAttribute("veiculos", veiculoRepository.findAll());
        model.addAttribute("usuario", "Systems Analyst");
        return "frota/lista";
    }

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        return "frota/cadastro";
    }

    @PostMapping("/salvar")
    public String salvarVeiculo(@ModelAttribute Veiculo veiculo) {
        // Aqui no futuro chamaremos a API da Fipe para setar o valor
        veiculo.setSituacao("Em Uso");
        veiculoRepository.save(veiculo);
        return "redirect:/sitio/frota";
    }

    @GetMapping("/excluir/{id}")
    public String excluirVeiculo(@PathVariable Long id) {
        if (id != null) {
            veiculoRepository.deleteById(id);
        }
        return "redirect:/sitio/frota";
    }
}