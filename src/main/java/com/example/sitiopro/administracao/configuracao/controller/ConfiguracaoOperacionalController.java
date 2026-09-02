package com.example.sitiopro.administracao.configuracao.controller;

import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalForm;
import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalInvalidaException;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/admin/configuracoes")
public class ConfiguracaoOperacionalController {

    private final ConfiguracaoOperacionalService service;

    public ConfiguracaoOperacionalController(ConfiguracaoOperacionalService service) {
        this.service = service;
    }

    @InitBinder("configuracaoForm")
    void restringirCampos(WebDataBinder binder) {
        binder.setAllowedFields("nomePropriedade", "timezone", "latitude", "longitude",
                "diasPadraoIncubacao", "antecedenciaAlertaEclosaoDias");
    }

    @GetMapping
    public String editar(Model model) {
        ConfiguracaoOperacionalLeitura configuracao = service.obter();
        model.addAttribute("active", "configuracoes");
        model.addAttribute("configuracao", configuracao);
        model.addAttribute("configuracaoForm", configuracao.paraFormulario());
        return "admin/configuracoes";
    }

    @GetMapping({"/novo", "/detalhe", "/historico"})
    public String atalhosAnteriores() {
        return "redirect:/sitio/admin/configuracoes";
    }

    @PostMapping
    public String atualizar(@Valid @ModelAttribute("configuracaoForm") ConfiguracaoOperacionalForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                service.atualizar(form);
            } catch (ConfiguracaoOperacionalInvalidaException ex) {
                bindingResult.rejectValue(ex.getCampo(), "configuracao.invalida", ex.getMessage());
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("active", "configuracoes");
            model.addAttribute("configuracao", service.obter());
            return "admin/configuracoes";
        }
        redirectAttributes.addFlashAttribute("mensagem", "Configurações operacionais atualizadas.");
        return "redirect:/sitio/admin/configuracoes";
    }
}
