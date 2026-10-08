package com.example.sitiopro.manutencao.web;

import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.manutencao.service.ManutencaoPreventivaService;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Controller
public class ManutencaoController {
    private final ManutencaoService service;
    private final ManutencaoPreventivaService preventiva;
    private final PropriedadeService propriedade;
    private final EstoqueCatalogoService estoque;
    private final Clock clock;

    public ManutencaoController(ManutencaoService service, ManutencaoPreventivaService preventiva, PropriedadeService propriedade,
            EstoqueCatalogoService estoque, Clock clock) {
        this.service = service; this.preventiva = preventiva; this.propriedade = propriedade; this.estoque = estoque; this.clock = clock;
    }

    @GetMapping("/sitio/manutencao")
    public String dashboard(Model model) {
        base(model, "manutencao"); model.addAttribute("resumo", service.dashboard());
        return "manutencao/dashboard";
    }

    @GetMapping("/sitio/patrimonio")
    public String patrimonio() { return "redirect:/sitio/manutencao/ativos"; }

    @GetMapping("/sitio/manutencao/ativos")
    public String ativos(Model model) {
        base(model, "patrimonio"); model.addAttribute("ativos", service.listarAtivos());
        return "manutencao/ativos/lista";
    }

    @GetMapping("/sitio/manutencao/ativos/novo")
    public String novoAtivo(Model model) {
        AtivoPatrimonialRequest request = new AtivoPatrimonialRequest();
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararAtivo(model, request, null); return "manutencao/ativos/form";
    }

    @PostMapping("/sitio/manutencao/ativos")
    public String criarAtivo(@Valid @ModelAttribute("ativoForm") AtivoPatrimonialRequest request,
            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepararAtivo(model, request, null); return "manutencao/ativos/form"; }
        try {
            var criado = service.criarAtivo(request);
            redirect.addFlashAttribute("mensagem", "Ativo patrimonial cadastrado.");
            return "redirect:/sitio/manutencao/ativos/" + criado.id();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("ativoForm", ex.getMessage()));
            prepararAtivo(model, request, null); return "manutencao/ativos/form";
        }
    }

    @GetMapping("/sitio/manutencao/ativos/{id}")
    public String detalheAtivo(@PathVariable Long id, Model model) {
        base(model, "patrimonio"); model.addAttribute("ativo", service.detalharAtivo(id));
        model.addAttribute("manutencoes", service.listarManutencoes(id));
        model.addAttribute("planos", preventiva.listarPlanos(id));
        model.addAttribute("leituras", preventiva.listarLeituras(id));
        return "manutencao/ativos/detalhe";
    }

    @GetMapping("/sitio/manutencao/ativos/{id}/editar")
    public String editarAtivo(@PathVariable Long id, Model model) {
        prepararAtivo(model, service.formularioAtivo(id), id); return "manutencao/ativos/form";
    }

    @PostMapping("/sitio/manutencao/ativos/{id}")
    public String atualizarAtivo(@PathVariable Long id,
            @Valid @ModelAttribute("ativoForm") AtivoPatrimonialRequest request,
            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { prepararAtivo(model, request, id); return "manutencao/ativos/form"; }
        try {
            service.atualizarAtivo(id, request); redirect.addFlashAttribute("mensagem", "Ativo atualizado.");
            return "redirect:/sitio/manutencao/ativos/" + id;
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("ativoForm", ex.getMessage()));
            prepararAtivo(model, request, id); return "manutencao/ativos/form";
        }
    }

    @GetMapping("/sitio/manutencao/registros/novo")
    public String novaManutencao(@RequestParam(required = false) Long ativoId,
            @RequestParam(required = false) Long planoId, Model model) {
        RegistroManutencaoRequest request = new RegistroManutencaoRequest();
        request.setAtivoId(ativoId); request.setPlanoPreventivoId(planoId);
        request.setDataManutencao(LocalDateTime.now(clock).withSecond(0).withNano(0));
        request.setCusto(BigDecimal.ZERO); request.setChaveIdempotencia(UUID.randomUUID().toString());
        for (int i = 0; i < 3; i++) request.getConsumos().add(new ConsumoManutencaoRequest());
        prepararManutencao(model, request); return "manutencao/registros/form";
    }

    @PostMapping("/sitio/manutencao/registros")
    public String registrarManutencao(@Valid @ModelAttribute("manutencaoForm") RegistroManutencaoRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { completarLinhas(request); prepararManutencao(model, request); return "manutencao/registros/form"; }
        try {
            var criado = service.registrarManutencao(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Manutenção registrada.");
            return "redirect:/sitio/manutencao/ativos/" + criado.ativoId();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("manutencaoForm", ex.getMessage()));
            completarLinhas(request); prepararManutencao(model, request); return "manutencao/registros/form";
        }
    }

    @PostMapping("/sitio/manutencao/registros/{id}/concluir-proxima")
    public String concluirProxima(@PathVariable Long id, RedirectAttributes redirect, Authentication authentication) {
        try {
            var registro = service.concluirProximaManutencao(id, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Próxima manutenção concluída.");
            return "redirect:/sitio/manutencao/ativos/" + registro.ativoId();
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage()); return "redirect:/sitio/manutencao";
        }
    }

    @GetMapping("/sitio/manutencao/ativos/{ativoId}/planos/novo")
    public String novoPlano(@PathVariable Long ativoId, Model model) {
        PlanoManutencaoPreventivaRequest request = new PlanoManutencaoPreventivaRequest();
        request.setAtivoId(ativoId); request.setDataReferencia(LocalDateTime.now(clock).withSecond(0).withNano(0));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararPlano(model, request, null); return "manutencao/planos/form";
    }

    @GetMapping("/sitio/manutencao/planos/{id}/editar")
    public String editarPlano(@PathVariable Long id, Model model) {
        prepararPlano(model, preventiva.formularioPlano(id), id); return "manutencao/planos/form";
    }

    @PostMapping("/sitio/manutencao/planos")
    public String criarPlano(@Valid @ModelAttribute("planoForm") PlanoManutencaoPreventivaRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { prepararPlano(model, request, null); return "manutencao/planos/form"; }
        try {
            var criado = preventiva.criarPlano(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Plano preventivo criado.");
            return "redirect:/sitio/manutencao/ativos/" + criado.ativoId();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("planoForm", ex.getMessage()));
            prepararPlano(model, request, null); return "manutencao/planos/form";
        }
    }

    @PostMapping("/sitio/manutencao/planos/{id}")
    public String atualizarPlano(@PathVariable Long id,
            @Valid @ModelAttribute("planoForm") PlanoManutencaoPreventivaRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { prepararPlano(model, request, id); return "manutencao/planos/form"; }
        try {
            var atualizado = preventiva.atualizarPlano(id, request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Plano preventivo atualizado.");
            return "redirect:/sitio/manutencao/ativos/" + atualizado.ativoId();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("planoForm", ex.getMessage()));
            prepararPlano(model, request, id); return "manutencao/planos/form";
        }
    }

    @PostMapping("/sitio/manutencao/planos/{id}/desativar")
    public String desativarPlano(@PathVariable Long id, @RequestParam long versao,
            RedirectAttributes redirect, Authentication authentication) {
        var plano = preventiva.desativarPlano(id, versao, UsuarioAtor.de(authentication));
        redirect.addFlashAttribute("mensagem", "Plano preventivo desativado.");
        return "redirect:/sitio/manutencao/ativos/" + plano.ativoId();
    }

    @GetMapping("/sitio/manutencao/ativos/{ativoId}/leituras/nova")
    public String novaLeitura(@PathVariable Long ativoId, Model model) {
        prepararLeitura(model, novaLeituraForm(ativoId), false); return "manutencao/leituras/form";
    }

    @GetMapping("/sitio/manutencao/ativos/{ativoId}/leituras/ajuste")
    public String novoAjuste(@PathVariable Long ativoId, Model model) {
        prepararLeitura(model, novaLeituraForm(ativoId), true); return "manutencao/leituras/form";
    }

    @PostMapping("/sitio/manutencao/leituras")
    public String registrarLeitura(@Valid @ModelAttribute("leituraForm") LeituraMedidorRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        return salvarLeitura(request, result, model, redirect, authentication, false);
    }

    @PostMapping("/sitio/manutencao/leituras/ajuste")
    public String registrarAjuste(@Valid @ModelAttribute("leituraForm") LeituraMedidorRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        return salvarLeitura(request, result, model, redirect, authentication, true);
    }

    private void prepararAtivo(Model model, AtivoPatrimonialRequest request, Long id) {
        base(model, "patrimonio"); model.addAttribute("ativoForm", request); model.addAttribute("ativoId", id);
        model.addAttribute("tipos", TipoAtivoPatrimonial.values()); model.addAttribute("statusAtivo", StatusAtivoPatrimonial.values());
        model.addAttribute("estruturas", propriedade.listarEstruturaPropriedade(0, 100).conteudo());
    }
    private void prepararManutencao(Model model, RegistroManutencaoRequest request) {
        base(model, "manutencao"); model.addAttribute("manutencaoForm", request);
        model.addAttribute("tipos", TipoManutencao.values()); model.addAttribute("ativos", service.listarAtivos());
        model.addAttribute("planosPreventivos", request.getAtivoId() == null ? java.util.List.of() : preventiva.listarPlanosAtivos(request.getAtivoId()));
        model.addAttribute("itensEstoque", estoque.listarItensAtivos()); model.addAttribute("locaisEstoque", estoque.listarLocaisAtivos());
    }
    private void prepararPlano(Model model, PlanoManutencaoPreventivaRequest request, Long id) {
        base(model, "manutencao"); model.addAttribute("planoForm", request); model.addAttribute("planoId", id);
        model.addAttribute("ativo", service.detalharAtivo(request.getAtivoId()));
        model.addAttribute("periodicidades", TipoPeriodicidadeManutencao.values());
    }
    private LeituraMedidorRequest novaLeituraForm(Long ativoId) {
        LeituraMedidorRequest request = new LeituraMedidorRequest(); request.setAtivoId(ativoId);
        request.setDataLeitura(LocalDateTime.now(clock).withSecond(0).withNano(0)); request.setChaveIdempotencia(UUID.randomUUID().toString());
        return request;
    }
    private void prepararLeitura(Model model, LeituraMedidorRequest request, boolean ajuste) {
        base(model, "manutencao"); model.addAttribute("leituraForm", request); model.addAttribute("ajuste", ajuste);
        model.addAttribute("ativo", service.detalharAtivo(request.getAtivoId()));
    }
    private String salvarLeitura(LeituraMedidorRequest request, BindingResult result, Model model,
            RedirectAttributes redirect, Authentication authentication, boolean ajuste) {
        if (result.hasErrors()) { prepararLeitura(model, request, ajuste); return "manutencao/leituras/form"; }
        try {
            if (ajuste) preventiva.registrarAjusteAdministrativo(request, UsuarioAtor.de(authentication));
            else preventiva.registrarLeitura(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", ajuste ? "Leitura corrigida com registro de auditoria." : "Leitura registrada.");
            return "redirect:/sitio/manutencao/ativos/" + request.getAtivoId();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("leituraForm", ex.getMessage()));
            prepararLeitura(model, request, ajuste); return "manutencao/leituras/form";
        }
    }
    private void completarLinhas(RegistroManutencaoRequest request) {
        while (request.getConsumos().size() < 3) request.getConsumos().add(new ConsumoManutencaoRequest());
    }
    private void base(Model model, String active) { model.addAttribute("active", active); }
}
