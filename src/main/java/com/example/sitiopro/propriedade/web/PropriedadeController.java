package com.example.sitiopro.propriedade.web;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.propriedade.service.PropriedadeOperacaoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
@RequestMapping("/sitio/propriedade")
public class PropriedadeController {
    private final PropriedadeService service;
    public PropriedadeController(PropriedadeService service) { this.service = service; }

    @InitBinder("form")
    void campos(WebDataBinder binder) {
        if (binder.getTarget() instanceof PropriedadeRequest) binder.setAllowedFields("nome", "observacao", "versao", "municipio", "uf", "areaTotalHa", "latitudeCentral", "longitudeCentral", "ativo");
        if (binder.getTarget() instanceof AreaPropriedadeRequest) binder.setAllowedFields("nome", "observacao", "versao", "tipo", "areaHa", "ativo");
        if (binder.getTarget() instanceof TalhaoRequest) binder.setAllowedFields("nome", "observacao", "versao", "areaId", "areaHa", "status");
        if (binder.getTarget() instanceof PiqueteRequest) binder.setAllowedFields("nome", "observacao", "versao", "areaId", "areaHa", "status");
        if (binder.getTarget() instanceof EstruturaPropriedadeRequest) binder.setAllowedFields("nome", "observacao", "versao", "areaId", "tipo", "capacidade", "unidadeCapacidade", "ativo");
        if (binder.getTarget() instanceof RecursoHidricoRequest) binder.setAllowedFields("nome", "observacao", "versao", "areaId", "tipo", "capacidadeLitros", "ativo");
    }

    @GetMapping
    public String inicio(Model model) {
        base(model, "propriedade");
        model.addAttribute("propriedade", service.resumo());
        return "propriedade/index";
    }

    @GetMapping("/editar")
    public String editar(Model model) {
        formulario(model, "propriedade", null, service.formulario());
        return "propriedade/form";
    }

    @PostMapping
    public String atualizar(@Valid @ModelAttribute("form") PropriedadeRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> { service.atualizar(form); return null; }, form, result, model, redirect, "propriedade", null);
    }

    @GetMapping("/areas")
    public String listarAreaPropriedade(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "areas");
        model.addAttribute("pagina", service.listarAreaPropriedade(pagina, 20));
        return "propriedade/lista";
    }

    @GetMapping("/areas/novo")
    public String novoAreaPropriedade(Model model) {
        formulario(model, "areas", null, new AreaPropriedadeRequest());
        return "propriedade/form";
    }

    @GetMapping("/areas/{id}")
    public String detalheAreaPropriedade(@PathVariable Long id, Model model) {
        base(model, "areas");
        model.addAttribute("registro", service.detalharAreaPropriedade(id));
        return "propriedade/detalhe";
    }

    @GetMapping("/areas/{id}/editar")
    public String editarAreaPropriedade(@PathVariable Long id, Model model) {
        formulario(model, "areas", id, service.formularioAreaPropriedade(id));
        return "propriedade/form";
    }

    @PostMapping("/areas")
    public String criarAreaPropriedade(@Valid @ModelAttribute("form") AreaPropriedadeRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarAreaPropriedade(null, form), form, result, model, redirect, "areas", null);
    }

    @PostMapping("/areas/{id}")
    public String atualizarAreaPropriedade(@PathVariable Long id, @Valid @ModelAttribute("form") AreaPropriedadeRequest form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarAreaPropriedade(id, form), form, result, model, redirect, "areas", id);
    }

    @PostMapping("/areas/{id}/desativar")
    public String desativarAreaPropriedade(@PathVariable Long id, @RequestParam Long versao, RedirectAttributes redirect) {
        service.desativarAreaPropriedade(id, versao);
        redirect.addFlashAttribute("mensagem", "Registro desativado.");
        return "redirect:/sitio/propriedade/areas/" + id;
    }

    @GetMapping("/talhoes")
    public String listarTalhao(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "talhoes");
        model.addAttribute("pagina", service.listarTalhao(pagina, 20));
        return "propriedade/lista";
    }

    @GetMapping("/talhoes/novo")
    public String novoTalhao(Model model) {
        formulario(model, "talhoes", null, new TalhaoRequest());
        return "propriedade/form";
    }

    @GetMapping("/talhoes/{id}")
    public String detalheTalhao(@PathVariable Long id, Model model) {
        base(model, "talhoes");
        model.addAttribute("registro", service.detalharTalhao(id));
        return "propriedade/detalhe";
    }

    @GetMapping("/talhoes/{id}/editar")
    public String editarTalhao(@PathVariable Long id, Model model) {
        formulario(model, "talhoes", id, service.formularioTalhao(id));
        return "propriedade/form";
    }

    @PostMapping("/talhoes")
    public String criarTalhao(@Valid @ModelAttribute("form") TalhaoRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarTalhao(null, form), form, result, model, redirect, "talhoes", null);
    }

    @PostMapping("/talhoes/{id}")
    public String atualizarTalhao(@PathVariable Long id, @Valid @ModelAttribute("form") TalhaoRequest form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarTalhao(id, form), form, result, model, redirect, "talhoes", id);
    }

    @PostMapping("/talhoes/{id}/desativar")
    public String desativarTalhao(@PathVariable Long id, @RequestParam Long versao, RedirectAttributes redirect) {
        service.desativarTalhao(id, versao);
        redirect.addFlashAttribute("mensagem", "Registro desativado.");
        return "redirect:/sitio/propriedade/talhoes/" + id;
    }

    @GetMapping("/piquetes")
    public String listarPiquete(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "piquetes");
        model.addAttribute("pagina", service.listarPiquete(pagina, 20));
        return "propriedade/lista";
    }

    @GetMapping("/piquetes/novo")
    public String novoPiquete(Model model) {
        formulario(model, "piquetes", null, new PiqueteRequest());
        return "propriedade/form";
    }

    @GetMapping("/piquetes/{id}")
    public String detalhePiquete(@PathVariable Long id, Model model) {
        base(model, "piquetes");
        model.addAttribute("registro", service.detalharPiquete(id));
        return "propriedade/detalhe";
    }

    @GetMapping("/piquetes/{id}/editar")
    public String editarPiquete(@PathVariable Long id, Model model) {
        formulario(model, "piquetes", id, service.formularioPiquete(id));
        return "propriedade/form";
    }

    @PostMapping("/piquetes")
    public String criarPiquete(@Valid @ModelAttribute("form") PiqueteRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarPiquete(null, form), form, result, model, redirect, "piquetes", null);
    }

    @PostMapping("/piquetes/{id}")
    public String atualizarPiquete(@PathVariable Long id, @Valid @ModelAttribute("form") PiqueteRequest form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarPiquete(id, form), form, result, model, redirect, "piquetes", id);
    }

    @PostMapping("/piquetes/{id}/desativar")
    public String desativarPiquete(@PathVariable Long id, @RequestParam Long versao, RedirectAttributes redirect) {
        service.desativarPiquete(id, versao);
        redirect.addFlashAttribute("mensagem", "Registro desativado.");
        return "redirect:/sitio/propriedade/piquetes/" + id;
    }

    @GetMapping("/estruturas")
    public String listarEstruturaPropriedade(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "estruturas");
        model.addAttribute("pagina", service.listarEstruturaPropriedade(pagina, 20));
        return "propriedade/lista";
    }

    @GetMapping("/estruturas/novo")
    public String novoEstruturaPropriedade(Model model) {
        formulario(model, "estruturas", null, new EstruturaPropriedadeRequest());
        return "propriedade/form";
    }

    @GetMapping("/estruturas/{id}")
    public String detalheEstruturaPropriedade(@PathVariable Long id, Model model) {
        base(model, "estruturas");
        model.addAttribute("registro", service.detalharEstruturaPropriedade(id));
        return "propriedade/detalhe";
    }

    @GetMapping("/estruturas/{id}/editar")
    public String editarEstruturaPropriedade(@PathVariable Long id, Model model) {
        formulario(model, "estruturas", id, service.formularioEstruturaPropriedade(id));
        return "propriedade/form";
    }

    @PostMapping("/estruturas")
    public String criarEstruturaPropriedade(@Valid @ModelAttribute("form") EstruturaPropriedadeRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarEstruturaPropriedade(null, form), form, result, model, redirect, "estruturas", null);
    }

    @PostMapping("/estruturas/{id}")
    public String atualizarEstruturaPropriedade(@PathVariable Long id, @Valid @ModelAttribute("form") EstruturaPropriedadeRequest form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarEstruturaPropriedade(id, form), form, result, model, redirect, "estruturas", id);
    }

    @PostMapping("/estruturas/{id}/desativar")
    public String desativarEstruturaPropriedade(@PathVariable Long id, @RequestParam Long versao, RedirectAttributes redirect) {
        service.desativarEstruturaPropriedade(id, versao);
        redirect.addFlashAttribute("mensagem", "Registro desativado.");
        return "redirect:/sitio/propriedade/estruturas/" + id;
    }

    @GetMapping("/recursos-hidricos")
    public String listarRecursoHidrico(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "recursos-hidricos");
        model.addAttribute("pagina", service.listarRecursoHidrico(pagina, 20));
        return "propriedade/lista";
    }

    @GetMapping("/recursos-hidricos/novo")
    public String novoRecursoHidrico(Model model) {
        formulario(model, "recursos-hidricos", null, new RecursoHidricoRequest());
        return "propriedade/form";
    }

    @GetMapping("/recursos-hidricos/{id}")
    public String detalheRecursoHidrico(@PathVariable Long id, Model model) {
        base(model, "recursos-hidricos");
        model.addAttribute("registro", service.detalharRecursoHidrico(id));
        return "propriedade/detalhe";
    }

    @GetMapping("/recursos-hidricos/{id}/editar")
    public String editarRecursoHidrico(@PathVariable Long id, Model model) {
        formulario(model, "recursos-hidricos", id, service.formularioRecursoHidrico(id));
        return "propriedade/form";
    }

    @PostMapping("/recursos-hidricos")
    public String criarRecursoHidrico(@Valid @ModelAttribute("form") RecursoHidricoRequest form, BindingResult result,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarRecursoHidrico(null, form), form, result, model, redirect, "recursos-hidricos", null);
    }

    @PostMapping("/recursos-hidricos/{id}")
    public String atualizarRecursoHidrico(@PathVariable Long id, @Valid @ModelAttribute("form") RecursoHidricoRequest form,
            BindingResult result, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarRecursoHidrico(id, form), form, result, model, redirect, "recursos-hidricos", id);
    }

    @PostMapping("/recursos-hidricos/{id}/desativar")
    public String desativarRecursoHidrico(@PathVariable Long id, @RequestParam Long versao, RedirectAttributes redirect) {
        service.desativarRecursoHidrico(id, versao);
        redirect.addFlashAttribute("mensagem", "Registro desativado.");
        return "redirect:/sitio/propriedade/recursos-hidricos/" + id;
    }

    private String salvar(java.util.function.Supplier<CadastroFisicoResumo> operacao, Object form,
            BindingResult result, Model model, RedirectAttributes redirect, String cadastro, Long id) {
        if (!result.hasErrors()) {
            try {
                var salvo = operacao.get();
                redirect.addFlashAttribute("mensagem", "Dados salvos.");
                return "redirect:" + caminho(cadastro) + (salvo == null ? "" : "/" + salvo.id());
            } catch (PropriedadeOperacaoException ex) {
                if (ex.getCampo() == null) result.reject("propriedade.invalida", ex.getMessage());
                else result.rejectValue(ex.getCampo(), "propriedade.invalida", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException ex) {
                result.reject("propriedade.conflito", "Registro alterado. Recarregue a página antes de salvar.");
            } catch (DataIntegrityViolationException ex) {
                result.reject("propriedade.conflito", "Não foi possível salvar. Confira os vínculos e nomes duplicados.");
            }
        }
        formulario(model, cadastro, id, form);
        return "propriedade/form";
    }

    private String caminho(String cadastro) {
        return "/sitio/propriedade" + ("propriedade".equals(cadastro) ? "" : "/" + cadastro);
    }

    private void base(Model model, String cadastro) {
        model.addAttribute("active", "propriedade");
        model.addAttribute("cadastro", cadastro);
        model.addAttribute("caminho", caminho(cadastro));
        model.addAttribute("titulo", switch (cadastro) {
            case "areas" -> "Áreas";
            case "talhoes" -> "Talhões";
            case "piquetes" -> "Piquetes";
            case "estruturas" -> "Estruturas";
            case "recursos-hidricos" -> "Recursos hídricos";
            default -> "Propriedade";
        });
    }

    private void formulario(Model model, String cadastro, Long id, Object form) {
        base(model, cadastro);
        model.addAttribute("form", form);
        model.addAttribute("registroId", id);
        model.addAttribute("acao", caminho(cadastro) + (id == null ? "" : "/" + id));
        model.addAttribute("areas", service.areasDisponiveis());
        model.addAttribute("tipos", switch (cadastro) {
            case "areas" -> TipoAreaPropriedade.values();
            case "estruturas" -> TipoEstruturaPropriedade.values();
            case "recursos-hidricos" -> TipoRecursoHidrico.values();
            default -> new Object[0];
        });
        model.addAttribute("estados", StatusDivisaoFisica.values());
    }
}
