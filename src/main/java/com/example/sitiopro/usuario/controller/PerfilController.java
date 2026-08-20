package com.example.sitiopro.usuario.controller;

import com.example.sitiopro.usuario.dto.AlterarSenhaRequest;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import com.example.sitiopro.usuario.service.SenhaInvalidaException;
import com.example.sitiopro.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String perfil(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("active", "perfil");
        model.addAttribute("usuarioLogado", principal);
        model.addAttribute("senhaForm", new AlterarSenhaRequest());
        return "usuario/perfil";
    }

    @PostMapping("/senha")
    public String alterarSenha(@AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @ModelAttribute("senhaForm") AlterarSenhaRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("active", "perfil");
            model.addAttribute("usuarioLogado", principal);
            return "usuario/perfil";
        }
        try {
            usuarioService.alterarSenhaPropria(principal, request);
            attributes.addFlashAttribute("mensagem", "Senha alterada com sucesso.");
            return "redirect:/sitio/perfil";
        } catch (SenhaInvalidaException ex) {
            bindingResult.reject("senha.erro", ex.getMessage());
            model.addAttribute("active", "perfil");
            model.addAttribute("usuarioLogado", principal);
            return "usuario/perfil";
        }
    }
}
