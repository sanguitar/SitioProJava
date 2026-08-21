package com.example.sitiopro.compras.controller;

import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.CompraFiltro;
import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.compras.service.ComprasOperacaoException;
import com.example.sitiopro.compras.service.FornecedorService;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/compras")
public class ComprasController {

    private final CompraService compraService;
    private final FornecedorService fornecedorService;
    private final EstoqueCatalogoService estoqueCatalogoService;

    public ComprasController(CompraService compraService,
            FornecedorService fornecedorService,
            EstoqueCatalogoService estoqueCatalogoService) {
        this.compraService = compraService;
        this.fornecedorService = fornecedorService;
        this.estoqueCatalogoService = estoqueCatalogoService;
    }

    @GetMapping
    public String dashboard(@ModelAttribute("filtro") CompraFiltro filtro, Model model) {
        model.addAttribute("active", "compras");
        model.addAttribute("resumo", compraService.montarResumo());
        model.addAttribute("compras", compraService.listar(filtro));
        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
        model.addAttribute("statusCompras", StatusCompra.values());
        return "compras/dashboard";
    }

    @GetMapping("/novo")
    public String aliasNovo() {
        return "redirect:/sitio/compras/nova";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararFormularioCompra(model, new CompraRequest());
        return "compras/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("compraForm") CompraRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormularioCompra(model, request);
            return "compras/form";
        }
        try {
            CompraDetalhe compra = compraService.criarCompra(request);
            attributes.addFlashAttribute("mensagem", "Compra criada como rascunho.");
            return "redirect:/sitio/compras/" + compra.id();
        } catch (ComprasOperacaoException ex) {
            bindingResult.addError(new ObjectError("compraForm", ex.getMessage()));
            prepararFormularioCompra(model, request);
            return "compras/form";
        }
    }

    @GetMapping("/detalhe")
    public String aliasDetalhe() {
        return "redirect:/sitio/compras";
    }

    @GetMapping("/historico")
    public String aliasHistorico() {
        return "redirect:/sitio/compras?status=CONFIRMADA";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        prepararDetalhe(model, compraService.detalhar(id), null, new ItemCompraRequest());
        return "compras/detalhe";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("compraForm") CompraRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        CompraDetalhe detalheAtual = compraService.detalhar(id);
        if (bindingResult.hasErrors()) {
            prepararDetalhe(model, detalheAtual, request, new ItemCompraRequest());
            return "compras/detalhe";
        }
        try {
            compraService.atualizarRascunho(id, request);
            attributes.addFlashAttribute("mensagem", "Dados da compra atualizados.");
        } catch (ComprasOperacaoException | EstoqueOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Long id,
            @Valid @ModelAttribute("itemForm") ItemCompraRequest request,
            BindingResult bindingResult,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("erro", primeiroErro(bindingResult));
            return "redirect:/sitio/compras/" + id;
        }
        try {
            compraService.adicionarItem(id, request);
            attributes.addFlashAttribute("mensagem", "Item incluído na compra.");
        } catch (ComprasOperacaoException | EstoqueOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @PostMapping("/{id}/itens/{itemId}/remover")
    public String removerItem(@PathVariable Long id,
            @PathVariable Long itemId,
            RedirectAttributes attributes) {
        try {
            compraService.removerItem(id, itemId);
            attributes.addFlashAttribute("mensagem", "Item removido da compra.");
        } catch (ComprasOperacaoException | EstoqueOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @PostMapping("/{id}/itens/{itemId}")
    public String atualizarItem(@PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @ModelAttribute("itemForm") ItemCompraRequest request,
            BindingResult bindingResult,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("erro", primeiroErro(bindingResult));
            return "redirect:/sitio/compras/" + id;
        }
        try {
            compraService.atualizarItem(id, itemId, request);
            attributes.addFlashAttribute("mensagem", "Item atualizado.");
        } catch (ComprasOperacaoException | EstoqueOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            compraService.confirmarCompra(id);
            attributes.addFlashAttribute("mensagem", "Compra confirmada e estoque atualizado.");
        } catch (ComprasOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            compraService.cancelarRascunho(id);
            attributes.addFlashAttribute("mensagem", "Compra cancelada.");
        } catch (ComprasOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/" + id;
    }

    @GetMapping("/fornecedores")
    public String fornecedores(Model model) {
        model.addAttribute("active", "compras");
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        return "compras/fornecedores";
    }

    @GetMapping("/fornecedores/novo")
    public String novoFornecedor(Model model) {
        prepararFormularioFornecedor(model, new FornecedorRequest());
        return "compras/fornecedor-form";
    }

    @PostMapping("/fornecedores")
    public String criarFornecedor(@Valid @ModelAttribute("fornecedorForm") FornecedorRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormularioFornecedor(model, request);
            return "compras/fornecedor-form";
        }
        try {
            fornecedorService.criar(request);
            attributes.addFlashAttribute("mensagem", "Fornecedor cadastrado com sucesso.");
            return "redirect:/sitio/compras/fornecedores";
        } catch (ComprasOperacaoException ex) {
            bindingResult.addError(new ObjectError("fornecedorForm", ex.getMessage()));
            prepararFormularioFornecedor(model, request);
            return "compras/fornecedor-form";
        }
    }

    @GetMapping("/fornecedores/{id}")
    public String detalheFornecedor(@PathVariable Long id, Model model) {
        model.addAttribute("active", "compras");
        model.addAttribute("fornecedor", fornecedorService.detalhar(id));
        model.addAttribute("fornecedorForm", fornecedorService.formulario(id));
        return "compras/fornecedor-detalhe";
    }

    @PostMapping("/fornecedores/{id}")
    public String atualizarFornecedor(@PathVariable Long id,
            @Valid @ModelAttribute("fornecedorForm") FornecedorRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("active", "compras");
            model.addAttribute("fornecedor", fornecedorService.detalhar(id));
            return "compras/fornecedor-detalhe";
        }
        try {
            fornecedorService.atualizar(id, request);
            attributes.addFlashAttribute("mensagem", "Fornecedor atualizado.");
        } catch (ComprasOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/compras/fornecedores/" + id;
    }

    private void prepararFormularioCompra(Model model, CompraRequest request) {
        model.addAttribute("active", "compras");
        model.addAttribute("compraForm", request);
        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
    }

    private void prepararFormularioFornecedor(Model model, FornecedorRequest request) {
        model.addAttribute("active", "compras");
        model.addAttribute("fornecedorForm", request);
    }

    private void prepararDetalhe(Model model, CompraDetalhe detalhe, CompraRequest compraForm,
            ItemCompraRequest itemForm) {
        model.addAttribute("active", "compras");
        model.addAttribute("detalhe", detalhe);
        model.addAttribute("compraForm", compraForm == null ? paraForm(detalhe) : compraForm);
        model.addAttribute("itemForm", itemForm);
        model.addAttribute("fornecedores", fornecedorService.listarAtivos());
        model.addAttribute("itensEstoque", estoqueCatalogoService.listarItensAtivos());
        model.addAttribute("locaisEstoque", estoqueCatalogoService.listarLocaisAtivos());
    }

    private CompraRequest paraForm(CompraDetalhe detalhe) {
        CompraRequest request = new CompraRequest();
        request.setFornecedorId(detalhe.fornecedor().id());
        request.setDataCompra(detalhe.dataCompra());
        request.setNumeroDocumento(detalhe.numeroDocumento());
        request.setObservacao(detalhe.observacao());
        request.setFrete(detalhe.frete());
        request.setDesconto(detalhe.desconto());
        return request;
    }

    private String primeiroErro(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .findFirst()
                .orElse("Dados inválidos.");
    }
}
