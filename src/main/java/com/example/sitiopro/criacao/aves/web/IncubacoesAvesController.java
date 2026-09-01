package com.example.sitiopro.criacao.aves.web;

import com.example.sitiopro.criacao.aves.dto.AjustarPrevisaoIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.CriarIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.FinalizarIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.dto.RegistrarAcompanhamentoIncubacaoAvesRequest;
import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import com.example.sitiopro.criacao.aves.entity.TipoAcompanhamentoIncubacaoAves;
import com.example.sitiopro.criacao.aves.service.AvesOperacaoException;
import com.example.sitiopro.criacao.aves.service.IncubacaoAcompanhamentoService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAvesService;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.aves.service.LoteAvesService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/sitio/criacoes/aves/incubacoes")
public class IncubacoesAvesController {

    private final IncubacaoAvesService service;
    private final IncubacaoAcompanhamentoService acompanhamentoService;
    private final InstalacaoCriacaoService instalacaoService;
    private final LoteAvesService loteService;
    private final Clock clock;

    public IncubacoesAvesController(IncubacaoAvesService service,
            IncubacaoAcompanhamentoService acompanhamentoService,
            InstalacaoCriacaoService instalacaoService, LoteAvesService loteService, Clock clock) {
        this.service = service;
        this.acompanhamentoService = acompanhamentoService;
        this.instalacaoService = instalacaoService;
        this.loteService = loteService;
        this.clock = clock;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model);
        model.addAttribute("pagina", service.listar(pagina, 20));
        return "criacoes/aves/incubacoes/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        CriarIncubacaoAvesRequest request = new CriarIncubacaoAvesRequest();
        LocalDate hoje = LocalDate.now(clock);
        request.setMetodo(MetodoIncubacaoAves.CHOCADEIRA);
        request.setEspecie(EspecieAves.GALINHA);
        request.setDataInicio(hoje);
        request.setDataPrevistaEclosao(service.previsaoPadrao(request.getEspecie(), hoje));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        formulario(model, request);
        return "criacoes/aves/incubacoes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("incubacaoForm") CriarIncubacaoAvesRequest request,
            BindingResult bindingResult, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            formulario(model, request);
            return "criacoes/aves/incubacoes/form";
        }
        try {
            var criada = service.criar(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Incubação iniciada e marcos preparados.");
            return redirect(criada.id());
        } catch (AvesOperacaoException ex) {
            bindingResult.addError(new ObjectError("incubacaoForm", ex.getMessage()));
            formulario(model, request);
            return "criacoes/aves/incubacoes/form";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        base(model);
        var incubacao = service.detalhar(id);
        model.addAttribute("incubacao", incubacao);
        model.addAttribute("instalacoes", instalacaoService.listarAtivas());

        FinalizarIncubacaoAvesRequest finalizacao = new FinalizarIncubacaoAvesRequest();
        finalizacao.setDataEclosao(LocalDate.now(clock));
        finalizacao.setCriarLote(true);
        finalizacao.setChaveIdempotenciaLote(UUID.randomUUID().toString());
        model.addAttribute("finalizacaoForm", finalizacao);
        model.addAttribute("finalidades", FinalidadeLoteAves.values());
        model.addAttribute("sexos", SexoLoteAves.values());

        model.addAttribute("verificacaoForm",
                acompanhamentoService.novo(TipoAcompanhamentoIncubacaoAves.VERIFICACAO_GERAL));
        model.addAttribute("ovoscopiaForm",
                acompanhamentoService.novo(TipoAcompanhamentoIncubacaoAves.OVOSCOPIA));
        model.addAttribute("medicaoForm",
                acompanhamentoService.novo(TipoAcompanhamentoIncubacaoAves.TEMPERATURA_UMIDADE));

        AjustarPrevisaoIncubacaoAvesRequest ajuste = new AjustarPrevisaoIncubacaoAvesRequest();
        ajuste.setDataPrevistaEclosao(incubacao.dataPrevistaEclosao());
        model.addAttribute("ajusteForm", ajuste);
        return "criacoes/aves/incubacoes/detalhe";
    }

    @PostMapping("/{id}/acompanhamentos")
    public String registrarAcompanhamento(@PathVariable Long id,
            @Valid @ModelAttribute RegistrarAcompanhamentoIncubacaoAvesRequest request,
            BindingResult bindingResult, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("erro", erro(bindingResult));
        } else {
            try {
                acompanhamentoService.registrar(id, request);
                redirect.addFlashAttribute("mensagem", "Registro adicionado ao histórico.");
            } catch (AvesOperacaoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
            }
        }
        return redirect(id);
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id,
            @Valid @ModelAttribute FinalizarIncubacaoAvesRequest request, BindingResult bindingResult,
            RedirectAttributes redirect, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("erro", erro(bindingResult));
        } else {
            try {
                service.finalizar(id, request, UsuarioAtor.de(authentication));
                redirect.addFlashAttribute("mensagem", "Incubação finalizada.");
            } catch (AvesOperacaoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
            }
        }
        return redirect(id);
    }

    @PostMapping("/{id}/ajustar-previsao")
    public String ajustarPrevisao(@PathVariable Long id,
            @Valid @ModelAttribute AjustarPrevisaoIncubacaoAvesRequest request, BindingResult bindingResult,
            RedirectAttributes redirect, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("erro", erro(bindingResult));
        } else {
            try {
                service.ajustarPrevisao(id, request, UsuarioAtor.de(authentication));
                redirect.addFlashAttribute("mensagem", "Previsão de eclosão ajustada.");
            } catch (AvesOperacaoException ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
            }
        }
        return redirect(id);
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect, Authentication authentication) {
        try {
            service.cancelar(id, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Incubação cancelada.");
        } catch (AvesOperacaoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return redirect(id);
    }

    private void formulario(Model model, CriarIncubacaoAvesRequest request) {
        base(model);
        model.addAttribute("incubacaoForm", request);
        model.addAttribute("metodos", MetodoIncubacaoAves.values());
        model.addAttribute("especies", EspecieAves.values());
        model.addAttribute("periodosIncubacao", service.periodosIncubacao());
        model.addAttribute("instalacoes", instalacaoService.listarAtivas());
        model.addAttribute("lotes", loteService.listarAtivos());
        model.addAttribute("posturas", service.listarPosturasRecentes());
    }

    private void base(Model model) { model.addAttribute("active", "aves"); }
    private String redirect(Long id) { return "redirect:/sitio/criacoes/aves/incubacoes/" + id; }
    private String erro(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage)
                .findFirst().orElse("Dados inválidos.");
    }
}
