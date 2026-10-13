package com.example.sitiopro.frota.controller;

import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.dto.VeiculoRequest;
import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import com.example.sitiopro.frota.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/sitio/frota")
public class VeiculoController {
    private final VeiculoService veiculos;
    private final AbastecimentoService abastecimentos;

    public VeiculoController(VeiculoService veiculos, AbastecimentoService abastecimentos) {
        this.veiculos = veiculos; this.abastecimentos = abastecimentos;
    }

    @InitBinder("veiculoForm")
    void restringirCampos(WebDataBinder binder) {
        binder.setAllowedFields("nome", "placa", "tipo", "marcaFipe", "modeloFipe", "anoModelo",
                "kmAtual", "tipoMedidor", "horimetroAtual", "fipeReferenciaId",
                "ativoPatrimonialId", "chaveIdempotencia", "versao");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("veiculos", abastecimentos.resumoFrota());
        return "frota/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        VeiculoRequest request = new VeiculoRequest();
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararFormulario(model, request, null);
        return "frota/cadastro";
    }

    @PostMapping("/salvar")
    public String criar(@Valid @ModelAttribute("veiculoForm") VeiculoRequest request, BindingResult result,
            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepararFormulario(model, request, null); return "frota/cadastro"; }
        try {
            var criado = veiculos.criar(request);
            redirect.addFlashAttribute("mensagem", "Veículo cadastrado com sucesso.");
            return "redirect:/sitio/frota/" + criado.id();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("veiculoForm", ex.getMessage()));
            prepararFormulario(model, request, null); return "frota/cadastro";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("resumo", abastecimentos.resumoOperacional(id));
        model.addAttribute("historico", abastecimentos.listar(id, 0, 20));
        return "frota/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        prepararFormulario(model, veiculos.formulario(id), id);
        return "frota/cadastro";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("veiculoForm") VeiculoRequest request, BindingResult result,
            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepararFormulario(model, request, id); return "frota/cadastro"; }
        try {
            veiculos.atualizar(id, request);
            redirect.addFlashAttribute("mensagem", "Veículo atualizado com sucesso.");
            return "redirect:/sitio/frota/" + id;
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("veiculoForm", ex.getMessage()));
            prepararFormulario(model, request, id); return "frota/cadastro";
        }
    }

    private void prepararFormulario(Model model, VeiculoRequest request, Long id) {
        model.addAttribute("veiculoForm", request); model.addAttribute("veiculoId", id);
        model.addAttribute("tiposMedidor", TipoMedidorVeiculo.values());
        model.addAttribute("ativos", veiculos.listarAtivosVinculaveis(id));
    }
}
