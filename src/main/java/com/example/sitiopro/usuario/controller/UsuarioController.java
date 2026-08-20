package com.example.sitiopro.usuario.controller;

import com.example.sitiopro.usuario.dto.CriarUsuarioRequest;
import com.example.sitiopro.usuario.dto.EditarUsuarioRequest;
import com.example.sitiopro.usuario.dto.ResetSenhaUsuarioRequest;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.service.SenhaInvalidaException;
import com.example.sitiopro.usuario.service.UsuarioOperacaoException;
import com.example.sitiopro.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("active", "usuarios");
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuario/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormularioCriacao(model, new CriarUsuarioRequest());
        return "usuario/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("usuarioForm") CriarUsuarioRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormularioCriacao(model, request);
            return "usuario/form";
        }
        try {
            usuarioService.criar(request);
            attributes.addFlashAttribute("mensagem", "Usuário criado com sucesso.");
            return "redirect:/sitio/admin/usuarios";
        } catch (UsuarioOperacaoException | SenhaInvalidaException ex) {
            bindingResult.reject("usuario.erro", ex.getMessage());
            prepararFormularioCriacao(model, request);
            return "usuario/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        EditarUsuarioRequest request = new EditarUsuarioRequest();
        request.setNome(usuario.getNome());
        request.setPerfil(usuario.getPerfil());
        request.setAtivo(usuario.isAtivo());
        prepararFormularioEdicao(model, usuario, request);
        return "usuario/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("usuarioForm") EditarUsuarioRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            prepararFormularioEdicao(model, usuario, request);
            return "usuario/form";
        }
        try {
            usuarioService.editar(id, request);
            attributes.addFlashAttribute("mensagem", "Usuário atualizado com sucesso.");
            return "redirect:/sitio/admin/usuarios";
        } catch (UsuarioOperacaoException ex) {
            bindingResult.reject("usuario.erro", ex.getMessage());
            prepararFormularioEdicao(model, usuario, request);
            return "usuario/form";
        }
    }

    @GetMapping("/{id}/senha")
    public String senha(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        prepararFormularioSenha(model, usuario, new ResetSenhaUsuarioRequest());
        return "usuario/senha";
    }

    @PostMapping("/{id}/senha")
    public String resetarSenha(@PathVariable Long id,
            @Valid @ModelAttribute("senhaForm") ResetSenhaUsuarioRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            prepararFormularioSenha(model, usuario, request);
            return "usuario/senha";
        }
        try {
            usuarioService.resetarSenha(id, request);
            attributes.addFlashAttribute("mensagem", "Senha atualizada com sucesso.");
            return "redirect:/sitio/admin/usuarios";
        } catch (SenhaInvalidaException ex) {
            bindingResult.reject("senha.erro", ex.getMessage());
            prepararFormularioSenha(model, usuario, request);
            return "usuario/senha";
        }
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            usuarioService.desativar(id);
            attributes.addFlashAttribute("mensagem", "Usuário desativado.");
        } catch (UsuarioOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/admin/usuarios";
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes attributes) {
        usuarioService.ativar(id);
        attributes.addFlashAttribute("mensagem", "Usuário ativado.");
        return "redirect:/sitio/admin/usuarios";
    }

    private void prepararFormularioCriacao(Model model, CriarUsuarioRequest request) {
        model.addAttribute("active", "usuarios");
        model.addAttribute("modo", "novo");
        model.addAttribute("usuarioForm", request);
        model.addAttribute("perfis", PerfilUsuario.values());
    }

    private void prepararFormularioEdicao(Model model, Usuario usuario, EditarUsuarioRequest request) {
        model.addAttribute("active", "usuarios");
        model.addAttribute("modo", "editar");
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioForm", request);
        model.addAttribute("perfis", PerfilUsuario.values());
    }

    private void prepararFormularioSenha(Model model, Usuario usuario, ResetSenhaUsuarioRequest request) {
        model.addAttribute("active", "usuarios");
        model.addAttribute("usuario", usuario);
        model.addAttribute("senhaForm", request);
    }
}
