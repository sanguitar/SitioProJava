package com.example.sitiopro.agricultura.web;

import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.entity.*;
import com.example.sitiopro.agricultura.service.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.TarefaAlertaOperacaoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.function.Supplier;
import java.util.UUID;

@Controller
@RequestMapping("/sitio/agricultura")
public class AgriculturaController {
    private final AgriculturaService service;
    private final AgriculturaFichaService fichas;
    private final EstoqueCatalogoService catalogo;
    private final TarefaService tarefas;
    public AgriculturaController(AgriculturaService service, AgriculturaFichaService fichas,
            EstoqueCatalogoService catalogo, TarefaService tarefas) {
        this.service = service; this.fichas = fichas; this.catalogo = catalogo; this.tarefas = tarefas;
    }
    @InitBinder({"form", "encerramento"})
    void campos(WebDataBinder binder) {
        if (binder.getTarget() instanceof SafraRequest) binder.setAllowedFields("nome", "anoInicio", "anoFim", "dataInicio", "dataFim", "status", "observacao", "versao");
        if (binder.getTarget() instanceof CulturaRequest) binder.setAllowedFields("nomeComum", "nomeCientifico", "cicloDiasEstimado", "agrofitCulturaId", "ativo", "observacao", "versao");
        if (binder.getTarget() instanceof CultivoRequest) binder.setAllowedFields("safraId", "talhaoId", "culturaId", "areaCultivadaHa", "dataPlantio", "previsaoColheita", "observacao", "versao");
        if (binder.getTarget() instanceof PlantioRequest) binder.setAllowedFields("data", "metodo", "quantidade", "unidade", "espacamento", "origem", "descricaoOrigem", "itemEstoqueId", "localEstoqueId", "loteCodigo", "observacao", "versao");
        if (binder.getTarget() instanceof AcompanhamentoRequest) binder.setAllowedFields("dataHora", "tipo", "descricao", "observacao", "versao");
        if (binder.getTarget() instanceof ColheitaRequest) binder.setAllowedFields("data", "quantidade", "unidade", "classificacao", "perdas", "finalizaCultivo", "destino", "itemEstoqueId", "localEstoqueId", "loteCodigo", "validade", "chaveIdempotencia", "observacao", "versao");
        if (binder.getTarget() instanceof AdubacaoRequest) binder.setAllowedFields("data", "produto", "quantidade", "unidade", "areaAplicadaHa", "metodo", "origem", "descricaoOrigem", "itemEstoqueId", "localEstoqueId", "loteCodigo", "chaveIdempotencia", "observacao", "versao");
        if (binder.getTarget() instanceof IrrigacaoRequest) binder.setAllowedFields("dataHora", "duracaoMinutos", "volumeLitros", "metodo", "chaveIdempotencia", "observacao", "versao");
        if (binder.getTarget() instanceof TratamentoRequest) binder.setAllowedFields("data", "finalidade", "produtoAplicado", "quantidade", "unidade", "areaTratadaHa", "metodo", "origem", "descricaoOrigem", "itemEstoqueId", "localEstoqueId", "loteCodigo", "chaveIdempotencia", "observacao", "versao");
        if (binder.getTarget() instanceof OcorrenciaRequest) binder.setAllowedFields("dataHora", "tipo", "severidade", "titulo", "descricao", "areaAfetadaHa", "quantidadePerdida", "unidadePerda", "perdaTotal", "agrofitCulturaIds", "chaveIdempotencia", "observacao", "versao");
        if (binder.getTarget() instanceof OcorrenciaAtualizacaoRequest) binder.setAllowedFields("tipo", "severidade", "titulo", "descricao", "areaAfetadaHa", "observacao", "agrofitCulturaIds", "acompanhamento", "chaveIdempotencia", "versao");
        if (binder.getTarget() instanceof EncerramentoOcorrenciaRequest) binder.setAllowedFields("dataHora", "resolucao", "chaveIdempotencia", "versao");
        if (binder.getTarget() instanceof TarefaRequest) binder.setAllowedFields("titulo", "descricao", "prioridade", "dataVencimento", "responsavelId");
    }
    @GetMapping
    public String inicio(Model model) {
        base(model, "inicio", "Agricultura"); model.addAttribute("painel", service.painel()); return "agricultura/index";
    }

    @GetMapping("/safras")
    public String listarSafra(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "safras", "Safras"); model.addAttribute("pagina", service.listarSafras(pagina)); return "agricultura/lista";
    }
    @GetMapping("/safras/novo")
    public String novoSafra(Model model) { return formulario(model, "safras", null, new SafraRequest()); }
    @GetMapping("/safras/{id}/editar")
    public String editarSafra(@PathVariable Long id, Model model) { return formulario(model, "safras", id, service.formularioSafra(id)); }
    @PostMapping("/safras")
    public String criarSafra(@Valid @ModelAttribute("form") SafraRequest form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarSafra(null, form).id(), "safras", null, form, errors, model, redirect);
    }
    @PostMapping("/safras/{id}")
    public String atualizarSafra(@PathVariable Long id, @Valid @ModelAttribute("form") SafraRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarSafra(id, form).id(), "safras", id, form, errors, model, redirect);
    }
    @GetMapping("/safras/{id}")
    public String detalheSafra(@PathVariable Long id, Model model) {
        base(model, "safras", "Safras"); model.addAttribute("registro", service.detalharSafra(id)); return "agricultura/cadastro-detalhe";
    }

    @GetMapping("/culturas")
    public String listarCultura(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "culturas", "Culturas"); model.addAttribute("pagina", service.listarCulturas(pagina)); return "agricultura/lista";
    }
    @GetMapping("/culturas/novo")
    public String novoCultura(Model model) { return formulario(model, "culturas", null, new CulturaRequest()); }
    @GetMapping("/culturas/{id}/editar")
    public String editarCultura(@PathVariable Long id, Model model) { return formulario(model, "culturas", id, service.formularioCultura(id)); }
    @PostMapping("/culturas")
    public String criarCultura(@Valid @ModelAttribute("form") CulturaRequest form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarCultura(null, form).id(), "culturas", null, form, errors, model, redirect);
    }
    @PostMapping("/culturas/{id}")
    public String atualizarCultura(@PathVariable Long id, @Valid @ModelAttribute("form") CulturaRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarCultura(id, form).id(), "culturas", id, form, errors, model, redirect);
    }
    @GetMapping("/culturas/{id}")
    public String detalheCultura(@PathVariable Long id, Model model) {
        base(model, "culturas", "Culturas"); model.addAttribute("registro", service.detalharCultura(id)); return "agricultura/cadastro-detalhe";
    }

    @GetMapping("/cultivos")
    public String listarCultivo(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "cultivos", "Cultivos"); model.addAttribute("pagina", service.listarCultivos(pagina)); return "agricultura/lista";
    }
    @GetMapping("/cultivos/novo")
    public String novoCultivo(Model model) { return formulario(model, "cultivos", null, new CultivoRequest()); }
    @GetMapping("/cultivos/{id}/editar")
    public String editarCultivo(@PathVariable Long id, Model model) { return formulario(model, "cultivos", id, service.formularioCultivo(id)); }
    @PostMapping("/cultivos")
    public String criarCultivo(@Valid @ModelAttribute("form") CultivoRequest form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarCultivo(null, form).id(), "cultivos", null, form, errors, model, redirect);
    }
    @PostMapping("/cultivos/{id}")
    public String atualizarCultivo(@PathVariable Long id, @Valid @ModelAttribute("form") CultivoRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return salvar(() -> service.salvarCultivo(id, form).id(), "cultivos", id, form, errors, model, redirect);
    }

    @GetMapping("/cultivos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        base(model, "cultivos", "Ficha do cultivo"); model.addAttribute("ficha", fichas.detalhar(id)); return "agricultura/cultivo";
    }
    @GetMapping("/colheitas")
    public String colheitas(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "colheitas", "Colheitas"); model.addAttribute("pagina", service.listarColheitas(pagina)); return "agricultura/lista";
    }
    @GetMapping("/adubacao")
    public String adubacoes(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "adubacao", "Adubacoes"); model.addAttribute("pagina", service.listarAdubacoes(pagina)); return "agricultura/lista";
    }
    @GetMapping("/irrigacao")
    public String irrigacoes(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "irrigacao", "Irrigacoes"); model.addAttribute("pagina", service.listarIrrigacoes(pagina)); return "agricultura/lista";
    }
    @GetMapping("/tratamentos")
    public String tratamentos(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "tratamentos", "Tratamentos agricolas"); model.addAttribute("pagina", service.listarTratamentos(pagina)); return "agricultura/lista";
    }
    @GetMapping("/ocorrencias")
    public String ocorrencias(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model, "ocorrencias", "Ocorrencias fitossanitarias"); model.addAttribute("pagina", service.listarOcorrencias(pagina)); return "agricultura/lista";
    }
    @GetMapping("/ocorrencias/{id}")
    public String detalheOcorrencia(@PathVariable Long id, Model model) {
        base(model, "ocorrencias", "Ocorrencia fitossanitaria");
        OcorrenciaDetalhe registro = service.detalharOcorrencia(id);
        model.addAttribute("registro", registro);
        if (!model.containsAttribute("encerramento")) {
            EncerramentoOcorrenciaRequest form = service.novoEncerramentoOcorrencia(registro.resumo().versao());
            form.setChaveIdempotencia(UUID.randomUUID().toString());
            model.addAttribute("encerramento", form);
        }
        return "agricultura/ocorrencia";
    }
    @GetMapping("/ocorrencias/{id}/editar")
    public String editarOcorrencia(@PathVariable Long id, Model model) {
        OcorrenciaAtualizacaoRequest form = service.formularioOcorrencia(id);
        form.setChaveIdempotencia(UUID.randomUUID().toString());
        return formularioOcorrencia(model, id, form);
    }
    @PostMapping("/ocorrencias/{id}")
    public String atualizarOcorrencia(@PathVariable Long id,
            @Valid @ModelAttribute("form") OcorrenciaAtualizacaoRequest form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.atualizarOcorrencia(id, form); redirect.addFlashAttribute("mensagem", "Ocorrencia atualizada.");
                return "redirect:/sitio/agricultura/ocorrencias/" + id;
            } catch (AgriculturaOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou referencia invalida. Recarregue os dados.");
            }
        }
        return formularioOcorrencia(model, id, form);
    }
    @PostMapping("/ocorrencias/{id}/encerrar")
    public String encerrarOcorrencia(@PathVariable Long id,
            @Valid @ModelAttribute("encerramento") EncerramentoOcorrenciaRequest form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.encerrarOcorrencia(id, form); redirect.addFlashAttribute("mensagem", "Ocorrencia encerrada.");
                return "redirect:/sitio/agricultura/ocorrencias/" + id;
            } catch (AgriculturaOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado. Recarregue os dados.");
            }
        }
        base(model, "ocorrencias", "Ocorrencia fitossanitaria");
        model.addAttribute("registro", service.detalharOcorrencia(id));
        return "agricultura/ocorrencia";
    }
    @PostMapping("/ocorrencias/{id}/tarefa-inspecao")
    public String criarTarefaInspecao(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirect) {
        try {
            service.criarTarefaInspecao(id, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Tarefa de inspecao disponivel no acompanhamento.");
        } catch (AgriculturaOperacaoException | TarefaAlertaOperacaoException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/agricultura/ocorrencias/" + id;
    }
    @PostMapping("/cultivos/{id}/status")
    public String status(@PathVariable Long id, @RequestParam StatusCultivo status, @RequestParam Long versao,
            RedirectAttributes redirect) {
        service.alterarStatus(id, new StatusCultivoRequest(status, versao));
        redirect.addFlashAttribute("mensagem", "Status atualizado."); return "redirect:/sitio/agricultura/cultivos/" + id;
    }
    @GetMapping("/cultivos/{id}/plantios/novo")
    public String novoPlantio(@PathVariable Long id, Model model) {
        var resumo = service.detalharCultivo(id);
        var form = new PlantioRequest();
        form.setVersao(resumo.versao());
        return operacao(model, id, "plantios", form);
    }
    @PostMapping("/cultivos/{id}/plantios")
    public String registrarPlantio(@PathVariable Long id, @Valid @ModelAttribute("form") PlantioRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.registrarPlantio(id, form);
                redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/cultivos/" + id;
            } catch (AgriculturaOperacaoException | EstoqueOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou vinculo invalido. Recarregue a ficha.");
            }
        }
        return operacao(model, id, "plantios", form);
    }
    @GetMapping("/cultivos/{id}/acompanhamentos/novo")
    public String novoAcompanhamento(@PathVariable Long id, Model model) {
        var resumo = service.detalharCultivo(id);
        var form = new AcompanhamentoRequest();
        form.setVersao(resumo.versao());
        return operacao(model, id, "acompanhamentos", form);
    }
    @PostMapping("/cultivos/{id}/acompanhamentos")
    public String registrarAcompanhamento(@PathVariable Long id, @Valid @ModelAttribute("form") AcompanhamentoRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.registrarAcompanhamento(id, form);
                redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/cultivos/" + id;
            } catch (AgriculturaOperacaoException | EstoqueOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou vinculo invalido. Recarregue a ficha.");
            }
        }
        return operacao(model, id, "acompanhamentos", form);
    }
    @GetMapping("/cultivos/{id}/colheitas/novo")
    public String novoColheita(@PathVariable Long id, Model model) {
        var resumo = service.detalharCultivo(id);
        var form = new ColheitaRequest();
        form.setVersao(resumo.versao()); form.setChaveIdempotencia(UUID.randomUUID().toString());
        return operacao(model, id, "colheitas", form);
    }
    @GetMapping("/cultivos/{id}/adubacoes/novo")
    public String novaAdubacao(@PathVariable Long id, Model model) {
        var form = new AdubacaoRequest(); preparar(form, service.detalharCultivo(id).versao());
        return operacao(model, id, "adubacoes", form);
    }
    @PostMapping("/cultivos/{id}/adubacoes")
    public String registrarAdubacao(@PathVariable Long id, @Valid @ModelAttribute("form") AdubacaoRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return registrarOperacao(() -> service.registrarAdubacao(id, form), id, "adubacoes", form, errors, model, redirect);
    }
    @GetMapping("/cultivos/{id}/irrigacoes/novo")
    public String novaIrrigacao(@PathVariable Long id, Model model) {
        var form = new IrrigacaoRequest(); preparar(form, service.detalharCultivo(id).versao());
        return operacao(model, id, "irrigacoes", form);
    }
    @PostMapping("/cultivos/{id}/irrigacoes")
    public String registrarIrrigacao(@PathVariable Long id, @Valid @ModelAttribute("form") IrrigacaoRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return registrarOperacao(() -> service.registrarIrrigacao(id, form), id, "irrigacoes", form, errors, model, redirect);
    }
    @GetMapping("/cultivos/{id}/tratamentos/novo")
    public String novoTratamento(@PathVariable Long id, Model model) {
        var form = new TratamentoRequest(); preparar(form, service.detalharCultivo(id).versao());
        return operacao(model, id, "tratamentos", form);
    }
    @PostMapping("/cultivos/{id}/tratamentos")
    public String registrarTratamento(@PathVariable Long id, @Valid @ModelAttribute("form") TratamentoRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return registrarOperacao(() -> service.registrarTratamento(id, form), id, "tratamentos", form, errors, model, redirect);
    }
    @GetMapping("/cultivos/{id}/ocorrencias/novo")
    public String novaOcorrencia(@PathVariable Long id, Model model) {
        var form = new OcorrenciaRequest(); preparar(form, service.detalharCultivo(id).versao());
        return operacao(model, id, "ocorrencias", form);
    }
    @PostMapping("/cultivos/{id}/ocorrencias")
    public String registrarOcorrencia(@PathVariable Long id, @Valid @ModelAttribute("form") OcorrenciaRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        return registrarOperacao(() -> service.registrarOcorrencia(id, form), id, "ocorrencias", form, errors, model, redirect);
    }
    @PostMapping("/cultivos/{id}/colheitas")
    public String registrarColheita(@PathVariable Long id, @Valid @ModelAttribute("form") ColheitaRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.registrarColheita(id, form);
                redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/cultivos/" + id;
            } catch (AgriculturaOperacaoException | EstoqueOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou vinculo invalido. Recarregue a ficha.");
            }
        }
        return operacao(model, id, "colheitas", form);
    }
    @GetMapping("/cultivos/{id}/tarefas/novo")
    public String novoTarefa(@PathVariable Long id, Model model) {
        var resumo = service.detalharCultivo(id);
        var form = new TarefaRequest();
        return operacao(model, id, "tarefas", form);
    }
    @PostMapping("/cultivos/{id}/tarefas")
    public String registrarTarefa(@PathVariable Long id, @Valid @ModelAttribute("form") TarefaRequest form,
            BindingResult errors, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (!errors.hasErrors()) {
            try {
                service.criarTarefa(id, form, UsuarioAtor.de(authentication));
                redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/cultivos/" + id;
            } catch (AgriculturaOperacaoException | EstoqueOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou vinculo invalido. Recarregue a ficha.");
            }
        }
        return operacao(model, id, "tarefas", form);
    }

    private String salvar(Supplier<Long> acao, String secao, Long id, Object form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                Long salvo = acao.get(); redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/" + secao + "/" + salvo;
            } catch (AgriculturaOperacaoException ex) { errors.reject("operacao", ex.getMessage()); }
            catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou nome duplicado. Recarregue os dados.");
            }
        }
        return formulario(model, secao, id, form);
    }
    private String registrarOperacao(Supplier<?> acao, Long id, String operacao, Object form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                acao.get(); redirect.addFlashAttribute("mensagem", "Registro salvo.");
                return "redirect:/sitio/agricultura/cultivos/" + id;
            } catch (AgriculturaOperacaoException | EstoqueOperacaoException | TarefaAlertaOperacaoException ex) {
                errors.reject("operacao", ex.getMessage());
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
                errors.reject("conflito", "Registro alterado ou vinculo invalido. Recarregue a ficha.");
            }
        }
        return operacao(model, id, operacao, form);
    }
    private void preparar(AdubacaoRequest form, long versao) { form.setVersao(versao); form.setChaveIdempotencia(UUID.randomUUID().toString()); }
    private void preparar(IrrigacaoRequest form, long versao) { form.setVersao(versao); form.setChaveIdempotencia(UUID.randomUUID().toString()); }
    private void preparar(TratamentoRequest form, long versao) { form.setVersao(versao); form.setChaveIdempotencia(UUID.randomUUID().toString()); }
    private void preparar(OcorrenciaRequest form, long versao) { form.setVersao(versao); form.setChaveIdempotencia(UUID.randomUUID().toString()); }
    private String formulario(Model model, String secao, Long id, Object form) {
        base(model, secao, id == null ? "Novo registro" : "Editar registro");
        model.addAttribute("form", form); model.addAttribute("id", id);
        model.addAttribute("acao", "/sitio/agricultura/" + secao + (id == null ? "" : "/" + id));
        if (secao.equals("safras")) model.addAttribute("statuses", StatusSafra.values());
        if (secao.equals("culturas")) model.addAttribute("agrofit", service.opcoesAgrofit());
        if (secao.equals("cultivos")) {
            model.addAttribute("safras", service.opcoesSafras()); model.addAttribute("talhoes", service.opcoesTalhoes());
            model.addAttribute("culturas", service.opcoesCulturas());
        }
        return "agricultura/form";
    }
    private String operacao(Model model, Long id, String operacao, Object form) {
        base(model, "cultivos", "Registrar " + switch (operacao) {
            case "plantios" -> "plantio"; case "acompanhamentos" -> "acompanhamento";
            case "colheitas" -> "colheita"; case "adubacoes" -> "adubacao";
            case "irrigacoes" -> "irrigacao"; case "tratamentos" -> "tratamento";
            case "ocorrencias" -> "ocorrencia"; default -> "tarefa";
        });
        model.addAttribute("cultivo", service.detalharCultivo(id)); model.addAttribute("form", form);
        model.addAttribute("operacao", operacao); model.addAttribute("acao", "/sitio/agricultura/cultivos/" + id + "/" + operacao);
        if (operacao.equals("plantios") || operacao.equals("adubacoes") || operacao.equals("tratamentos") || operacao.equals("colheitas")) {
            model.addAttribute("itens", catalogo.listarItensAtivos().stream()
                    .map(i -> new OpcaoAgricola(i.getId(), i.getNome() + " (" + i.getUnidadeMedida().getSigla() + ")")).toList());
            model.addAttribute("locais", catalogo.listarLocaisAtivos().stream()
                    .map(l -> new OpcaoAgricola(l.getId(), l.getNome())).toList());
        }
        if (operacao.equals("acompanhamentos")) model.addAttribute("tipos", TipoAcompanhamentoCultivo.values());
        if (operacao.equals("adubacoes") || operacao.equals("tratamentos")) model.addAttribute("origens", OrigemInsumo.values());
        if (operacao.equals("colheitas")) model.addAttribute("destinos", DestinoColheita.values());
        if (operacao.equals("ocorrencias")) {
            model.addAttribute("tipos", TipoOcorrenciaCultivo.values());
            model.addAttribute("severidades", SeveridadeOcorrencia.values());
            model.addAttribute("agrofit", service.opcoesAgrofit());
        }
        if (operacao.equals("tarefas")) {
            model.addAttribute("prioridades", PrioridadeTarefa.values());
            model.addAttribute("responsaveis", tarefas.listarResponsaveisAtivos());
        }
        return "agricultura/operacao";
    }
    private String formularioOcorrencia(Model model, Long id, OcorrenciaAtualizacaoRequest form) {
        base(model, "ocorrencias", "Atualizar ocorrencia");
        model.addAttribute("registro", service.detalharOcorrencia(id)); model.addAttribute("form", form);
        model.addAttribute("acao", "/sitio/agricultura/ocorrencias/" + id);
        model.addAttribute("tipos", TipoOcorrenciaCultivo.values());
        model.addAttribute("severidades", SeveridadeOcorrencia.values());
        model.addAttribute("agrofit", service.opcoesAgrofit());
        return "agricultura/ocorrencia-form";
    }
    private void base(Model model, String secao, String titulo) {
        model.addAttribute("active", "agricultura"); model.addAttribute("secao", secao); model.addAttribute("titulo", titulo);
        model.addAttribute("caminho", "/sitio/agricultura/" + secao);
    }
}
