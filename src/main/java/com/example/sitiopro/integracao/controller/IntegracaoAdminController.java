package com.example.sitiopro.integracao.controller;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/admin/integracoes")
public class IntegracaoAdminController {

    private final IntegracaoPainelService painelService;
    private final IntegracaoOrquestrador orquestrador;

    public IntegracaoAdminController(IntegracaoPainelService painelService,
            IntegracaoOrquestrador orquestrador) {
        this.painelService = painelService;
        this.orquestrador = orquestrador;
    }

    @GetMapping
    public String painel(Model model) {
        IntegracaoPainelResumo resumo = painelService.resumo();
        model.addAttribute("resumo", resumo);
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        return "integracao/painel";
    }

    @GetMapping("/{fonte}")
    public String detalhe(@PathVariable String fonte, Model model) {
        FonteIntegracao origem = FonteIntegracao.porSlug(fonte);
        model.addAttribute("integracao", painelService.detalhar(origem));
        model.addAttribute("historico", painelService.historico(origem));
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        return "integracao/detalhe";
    }

    @PostMapping("/{fonte}/sincronizar")
    public String sincronizar(@PathVariable String fonte, RedirectAttributes redirectAttributes) {
        FonteIntegracao origem = FonteIntegracao.porSlug(fonte);
        try {
            orquestrador.sincronizar(origem);
            redirectAttributes.addFlashAttribute("sucesso", "Sincronização concluída com sucesso.");
        } catch (IntegracaoOperacaoException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/admin/integracoes/" + origem.getSlug();
    }
}
