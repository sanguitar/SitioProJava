package com.example.sitiopro.estoque.controller;

import com.example.sitiopro.estoque.dto.CategoriaEstoqueRequest;
import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.dto.LocalEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/sitio/estoque")
public class EstoqueController {

    private final EstoqueCatalogoService catalogoService;
    private final EstoqueMovimentoService movimentoService;

    public EstoqueController(EstoqueCatalogoService catalogoService, EstoqueMovimentoService movimentoService) {
        this.catalogoService = catalogoService;
        this.movimentoService = movimentoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("active", "estoque");
        model.addAttribute("resumo", movimentoService.montarResumo());
        return "estoque/dashboard";
    }

    @GetMapping("/novo")
    public String aliasNovo() {
        return "redirect:/sitio/estoque/itens/novo";
    }

    @GetMapping("/detalhe")
    public String aliasDetalhe() {
        return "redirect:/sitio/estoque/itens";
    }

    @GetMapping("/historico")
    public String aliasHistorico() {
        return "redirect:/sitio/estoque/movimentacoes";
    }

    @GetMapping("/itens")
    public String itens(Model model) {
        model.addAttribute("active", "estoque");
        model.addAttribute("itens", movimentoService.listarItensComSaldo());
        return "estoque/itens";
    }

    @GetMapping("/itens/novo")
    public String novoItem(Model model) {
        prepararFormularioItem(model, new ItemEstoqueRequest());
        return "estoque/item-form";
    }

    @PostMapping("/itens")
    public String criarItem(@Valid @ModelAttribute("itemForm") ItemEstoqueRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormularioItem(model, request);
            return "estoque/item-form";
        }
        try {
            catalogoService.criarItem(request);
            attributes.addFlashAttribute("mensagem", "Item cadastrado com sucesso.");
            return "redirect:/sitio/estoque/itens";
        } catch (EstoqueOperacaoException ex) {
            bindingResult.reject("estoque.erro", ex.getMessage());
            prepararFormularioItem(model, request);
            return "estoque/item-form";
        }
    }

    @GetMapping("/itens/{id}")
    public String detalheItem(@PathVariable Long id, Model model) {
        model.addAttribute("active", "estoque");
        model.addAttribute("detalhe", movimentoService.detalharItem(id));
        return "estoque/item-detalhe";
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes(Model model) {
        model.addAttribute("active", "estoque");
        model.addAttribute("movimentacoes", movimentoService.listarMovimentos());
        return "estoque/movimentacoes";
    }

    @GetMapping("/movimentacoes/nova")
    public String novaMovimentacao(Model model) {
        MovimentoEstoqueRequest request = new MovimentoEstoqueRequest();
        request.setTipo(TipoMovimentoEstoque.ENTRADA);
        request.setDataMovimento(LocalDateTime.now().withSecond(0).withNano(0));
        prepararFormularioMovimento(model, request);
        return "estoque/movimento-form";
    }

    @PostMapping("/movimentacoes")
    public String registrarMovimentacao(@Valid @ModelAttribute("movimentoForm") MovimentoEstoqueRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormularioMovimento(model, request);
            return "estoque/movimento-form";
        }
        try {
            movimentoService.registrarMovimento(request, usuarioPodeAjustar(authentication));
            attributes.addFlashAttribute("mensagem", "Movimentação registrada com sucesso.");
            return "redirect:/sitio/estoque/movimentacoes";
        } catch (EstoqueOperacaoException ex) {
            bindingResult.reject("estoque.erro", ex.getMessage());
            prepararFormularioMovimento(model, request);
            return "estoque/movimento-form";
        }
    }

    @GetMapping("/locais")
    public String locais(Model model) {
        prepararLocais(model, new LocalEstoqueRequest());
        return "estoque/locais";
    }

    @PostMapping("/locais")
    public String criarLocal(@Valid @ModelAttribute("localForm") LocalEstoqueRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararLocais(model, request);
            return "estoque/locais";
        }
        try {
            catalogoService.criarLocal(request);
            attributes.addFlashAttribute("mensagem", "Local cadastrado com sucesso.");
            return "redirect:/sitio/estoque/locais";
        } catch (EstoqueOperacaoException ex) {
            bindingResult.reject("estoque.erro", ex.getMessage());
            prepararLocais(model, request);
            return "estoque/locais";
        }
    }

    @GetMapping("/categorias")
    public String categorias(Model model) {
        prepararCategorias(model, new CategoriaEstoqueRequest());
        return "estoque/categorias";
    }

    @PostMapping("/categorias")
    public String criarCategoria(@Valid @ModelAttribute("categoriaForm") CategoriaEstoqueRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararCategorias(model, request);
            return "estoque/categorias";
        }
        try {
            catalogoService.criarCategoria(request);
            attributes.addFlashAttribute("mensagem", "Categoria cadastrada com sucesso.");
            return "redirect:/sitio/estoque/categorias";
        } catch (EstoqueOperacaoException ex) {
            bindingResult.reject("estoque.erro", ex.getMessage());
            prepararCategorias(model, request);
            return "estoque/categorias";
        }
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("active", "estoque");
        model.addAttribute("itens", movimentoService.listarItensComSaldo());
        model.addAttribute("vencidos", movimentoService.listarLotesVencidos());
        model.addAttribute("vencendo", movimentoService.listarLotesProximosVencimento(30));
        return "estoque/inventario";
    }

    private void prepararFormularioItem(Model model, ItemEstoqueRequest request) {
        model.addAttribute("active", "estoque");
        model.addAttribute("itemForm", request);
        model.addAttribute("categorias", catalogoService.listarCategoriasAtivas());
        model.addAttribute("unidades", catalogoService.listarUnidadesAtivas());
    }

    private void prepararFormularioMovimento(Model model, MovimentoEstoqueRequest request) {
        model.addAttribute("active", "estoque");
        model.addAttribute("movimentoForm", request);
        model.addAttribute("tipos", TipoMovimentoEstoque.values());
        model.addAttribute("itens", catalogoService.listarItensAtivos());
        model.addAttribute("locais", catalogoService.listarLocaisAtivos());
    }

    private void prepararLocais(Model model, LocalEstoqueRequest request) {
        model.addAttribute("active", "estoque");
        model.addAttribute("locais", catalogoService.listarLocais());
        model.addAttribute("localForm", request);
    }

    private void prepararCategorias(Model model, CategoriaEstoqueRequest request) {
        model.addAttribute("active", "estoque");
        model.addAttribute("categorias", catalogoService.listarCategorias());
        model.addAttribute("categoriaForm", request);
    }

    private boolean usuarioPodeAjustar(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
