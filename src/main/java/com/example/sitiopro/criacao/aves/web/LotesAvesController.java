package com.example.sitiopro.criacao.aves.web;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.*;
import com.example.sitiopro.criacao.aves.service.*;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/sitio/criacoes/aves/lotes")
public class LotesAvesController {
    private final LoteAvesService loteService; private final ManejoAvesService manejoService;
    private final InstalacaoCriacaoService instalacaoService; private final EstoqueCatalogoService estoqueService;
    private final TarefaService tarefaService; private final Clock clock;

    public LotesAvesController(LoteAvesService loteService, ManejoAvesService manejoService,
            InstalacaoCriacaoService instalacaoService, EstoqueCatalogoService estoqueService,
            TarefaService tarefaService, Clock clock) {
        this.loteService = loteService; this.manejoService = manejoService; this.instalacaoService = instalacaoService;
        this.estoqueService = estoqueService; this.tarefaService = tarefaService; this.clock = clock;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusLoteAves status, @RequestParam(required = false) String termo,
            @RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model); model.addAttribute("pagina", loteService.listar(status, termo, pagina, 20));
        model.addAttribute("statusSelecionado", status); model.addAttribute("termo", termo); model.addAttribute("statusLotes", StatusLoteAves.values());
        return "criacoes/aves/lotes/lista";
    }

    @GetMapping("/novo") public String novo(Model model) { CriarLoteAvesRequest r = new CriarLoteAvesRequest(); r.setDataEntrada(LocalDate.now(clock)); r.setChaveIdempotencia(chave()); formularioCriacao(model, r); return "criacoes/aves/lotes/form"; }

    @PostMapping
    public String criar(@Valid @ModelAttribute("loteForm") CriarLoteAvesRequest request, BindingResult result,
            Model model, RedirectAttributes redirect, Authentication auth) {
        if (result.hasErrors()) { formularioCriacao(model, request); return "criacoes/aves/lotes/form"; }
        try { var lote = loteService.criar(request, UsuarioAtor.de(auth)); redirect.addFlashAttribute("mensagem", "Lote cadastrado."); return "redirect:/sitio/criacoes/aves/lotes/" + lote.id(); }
        catch (AvesOperacaoException ex) { result.addError(new ObjectError("loteForm", ex.getMessage())); formularioCriacao(model, request); return "criacoes/aves/lotes/form"; }
    }

    @GetMapping("/{id}") public String detalhe(@PathVariable Long id, Model model) { prepararDetalhe(id, model); return "criacoes/aves/lotes/detalhe"; }
    @GetMapping("/{id}/editar") public String editar(@PathVariable Long id, Model model) { base(model); model.addAttribute("loteId", id); model.addAttribute("loteForm", loteService.formularioEdicao(id)); enums(model); return "criacoes/aves/lotes/editar"; }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("loteForm") AtualizarLoteAvesRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication auth) {
        if (result.hasErrors()) { base(model); model.addAttribute("loteId", id); enums(model); return "criacoes/aves/lotes/editar"; }
        return executar(id, redirect, () -> loteService.atualizar(id, request, UsuarioAtor.de(auth)), "Lote atualizado.");
    }

    @PostMapping("/{id}/mortalidade") public String mortalidade(@PathVariable Long id, @Valid @ModelAttribute RegistrarMortalidadeAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> manejoService.registrarMortalidade(id, r, UsuarioAtor.de(a)), "Mortalidade registrada."); }
    @PostMapping("/{id}/alimentacao") public String alimentacao(@PathVariable Long id, @Valid @ModelAttribute RegistrarAlimentacaoAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> manejoService.registrarAlimentacao(id, r, UsuarioAtor.de(a)), "Alimentação registrada e estoque atualizado."); }
    @PostMapping("/{id}/pesagem") public String pesagem(@PathVariable Long id, @Valid @ModelAttribute RegistrarPesagemAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> manejoService.registrarPesagem(id, r, UsuarioAtor.de(a)), "Pesagem registrada."); }
    @PostMapping("/{id}/postura") public String postura(@PathVariable Long id, @Valid @ModelAttribute RegistrarPosturaAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> manejoService.registrarPostura(id, r, UsuarioAtor.de(a)), "Postura registrada."); }
    @PostMapping("/{id}/transferir") public String transferir(@PathVariable Long id, @Valid @ModelAttribute TransferirLoteAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> manejoService.transferir(id, r, UsuarioAtor.de(a)), "Lote transferido."); }
    @PostMapping("/{id}/encerrar") public String encerrar(@PathVariable Long id, @Valid @ModelAttribute EncerrarLoteAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { return operacao(id, br, ra, () -> loteService.encerrar(id, r, UsuarioAtor.de(a)), "Lote encerrado."); }

    @PostMapping("/{id}/tarefas")
    public String tarefa(@PathVariable Long id, @Valid @ModelAttribute TarefaRequest r, BindingResult br,
            RedirectAttributes ra, Authentication a) {
        if (br.hasErrors()) { ra.addFlashAttribute("erro", erro(br)); return redirect(id); }
        try { loteService.detalhar(id); tarefaService.criarVinculada(r, UsuarioAtor.de(a), ModuloOrigem.CRIACOES, "LOTE:" + id); ra.addFlashAttribute("mensagem", "Tarefa vinculada ao lote."); }
        catch (RuntimeException ex) { ra.addFlashAttribute("erro", ex.getMessage()); }
        return redirect(id);
    }

    private void prepararDetalhe(Long id, Model m) {
        LoteAvesDetalhe lote = loteService.detalhar(id);
        base(m); m.addAttribute("lote", lote); m.addAttribute("instalacoes", instalacaoService.listarAtivas());
        m.addAttribute("itensEstoque", estoqueService.listarItensAtivos()); m.addAttribute("locaisEstoque", estoqueService.listarLocaisAtivos());
        RegistrarMortalidadeAvesRequest mortalidade = new RegistrarMortalidadeAvesRequest(); inicializar(mortalidade);
        RegistrarAlimentacaoAvesRequest alimentacao = new RegistrarAlimentacaoAvesRequest(); inicializar(alimentacao);
        RegistrarPesagemAvesRequest pesagem = new RegistrarPesagemAvesRequest(); inicializar(pesagem);
        RegistrarPosturaAvesRequest postura = new RegistrarPosturaAvesRequest(); inicializar(postura); postura.setDataColeta(LocalDate.now(clock));
        TransferirLoteAvesRequest transferencia = new TransferirLoteAvesRequest(); inicializar(transferencia);
        m.addAttribute("mortalidadeForm", mortalidade); m.addAttribute("alimentacaoForm", alimentacao);
        m.addAttribute("pesagemForm", pesagem); m.addAttribute("posturaForm", postura); m.addAttribute("transferenciaForm", transferencia);
        m.addAttribute("encerramentoForm", new EncerrarLoteAvesRequest()); m.addAttribute("statusFinais", new StatusLoteAves[]{StatusLoteAves.ENCERRADO, StatusLoteAves.VENDIDO, StatusLoteAves.ABATIDO});
        TarefaRequest tarefa = new TarefaRequest(); tarefa.setTitulo("Manejo do lote " + lote.codigo());
        m.addAttribute("tarefaForm", tarefa); m.addAttribute("prioridades", PrioridadeTarefa.values()); m.addAttribute("recorrencias", TipoRecorrencia.values());
    }

    private void formularioCriacao(Model m, CriarLoteAvesRequest r) { base(m); m.addAttribute("loteForm", r); m.addAttribute("instalacoes", instalacaoService.listarAtivas()); enums(m); }
    private void enums(Model m) { m.addAttribute("especies", EspecieAves.values()); m.addAttribute("finalidades", FinalidadeLoteAves.values()); m.addAttribute("sexos", SexoLoteAves.values()); }
    private void base(Model m) { m.addAttribute("active", "aves"); }
    private void inicializar(OperacaoLoteAvesRequest r) { r.setChaveIdempotencia(chave()); r.setDataEvento(LocalDateTime.now(clock).withSecond(0).withNano(0)); }
    private String chave() { return UUID.randomUUID().toString(); }
    private String operacao(Long id, BindingResult br, RedirectAttributes ra, Acao acao, String sucesso) { if (br.hasErrors()) { ra.addFlashAttribute("erro", erro(br)); return redirect(id); } return executar(id, ra, acao, sucesso); }
    private String executar(Long id, RedirectAttributes ra, Acao acao, String sucesso) { try { acao.executar(); ra.addFlashAttribute("mensagem", sucesso); } catch (RuntimeException ex) { ra.addFlashAttribute("erro", ex.getMessage()); } return redirect(id); }
    private String erro(BindingResult br) { return br.getAllErrors().stream().map(ObjectError::getDefaultMessage).findFirst().orElse("Dados inválidos."); }
    private String redirect(Long id) { return "redirect:/sitio/criacoes/aves/lotes/" + id; }
    @FunctionalInterface private interface Acao { Object executar(); }
}
