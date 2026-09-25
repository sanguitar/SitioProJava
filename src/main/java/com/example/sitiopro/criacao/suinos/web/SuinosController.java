package com.example.sitiopro.criacao.suinos.web;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.service.*;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.UUID;

@Controller
@RequestMapping("/sitio/criacoes/suinos")
public class SuinosController {
    private final SuinosService service;private final SuinosSanidadeService sanidade;private final InstalacaoCriacaoService instalacoes;private final EstoqueCatalogoService estoque;private final TarefaService tarefas;private final Clock clock;
    public SuinosController(SuinosService service,SuinosSanidadeService sanidade,InstalacaoCriacaoService instalacoes,EstoqueCatalogoService estoque,TarefaService tarefas,Clock clock){this.service=service;this.sanidade=sanidade;this.instalacoes=instalacoes;this.estoque=estoque;this.tarefas=tarefas;this.clock=clock;}
    @GetMapping public String dashboard(Model m){base(m);m.addAttribute("resumo",service.dashboard());m.addAttribute("sanidade",sanidade.resumoOperacional());m.addAttribute("lotes",service.listar(StatusLoteSuinos.ATIVO,null,0,8).conteudo());return "criacoes/suinos/dashboard";}
    @GetMapping("/lotes") public String lotes(@RequestParam(required=false) StatusLoteSuinos status,@RequestParam(required=false) String termo,@RequestParam(defaultValue="0") int pagina,Model m){base(m);m.addAttribute("pagina",service.listar(status,termo,pagina,20));m.addAttribute("statusSelecionado",status);m.addAttribute("termo",termo);m.addAttribute("statusLotes",StatusLoteSuinos.values());return "criacoes/suinos/lotes/lista";}
    @GetMapping("/lotes/novo") public String novo(Model m){CriarLoteSuinosRequest r=new CriarLoteSuinosRequest();r.setDataEntrada(LocalDate.now(clock));r.setChaveIdempotencia(chave());form(m,r);return "criacoes/suinos/lotes/form";}
    @PostMapping("/lotes") public String criar(@Valid @ModelAttribute("loteForm") CriarLoteSuinosRequest r,BindingResult br,Model m,RedirectAttributes ra,Authentication a){if(br.hasErrors()){form(m,r);return "criacoes/suinos/lotes/form";}try{var lote=service.criar(r,UsuarioAtor.de(a));ra.addFlashAttribute("mensagem","Lote de suínos cadastrado.");return redirect(lote.id());}catch(RuntimeException ex){br.addError(new ObjectError("loteForm",ex.getMessage()));form(m,r);return "criacoes/suinos/lotes/form";}}
    @GetMapping("/lotes/{id}") public String detalhe(@PathVariable Long id,Model m){preparar(id,m);return "criacoes/suinos/lotes/detalhe";}
    @PostMapping("/lotes/{id}/entradas") public String entrada(@PathVariable Long id,@Valid @ModelAttribute EntradaSuinosRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarEntrada(id,r,UsuarioAtor.de(a)),"Entrada registrada.");}
    @PostMapping("/lotes/{id}/perdas") public String perda(@PathVariable Long id,@Valid @ModelAttribute PerdaSuinosRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarPerda(id,r,UsuarioAtor.de(a)),"Perda registrada.");}
    @PostMapping("/lotes/{id}/transferencias") public String transferencia(@PathVariable Long id,@Valid @ModelAttribute TransferenciaSuinosRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.transferir(id,r,UsuarioAtor.de(a)),"Lote transferido.");}
    @PostMapping("/lotes/{id}/pesagens") public String pesagem(@PathVariable Long id,@Valid @ModelAttribute PesagemSuinosRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarPesagem(id,r,UsuarioAtor.de(a)),"Pesagem registrada.");}
    @PostMapping("/lotes/{id}/alimentacoes") public String alimentacao(@PathVariable Long id,@Valid @ModelAttribute AlimentacaoSuinosRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarAlimentacao(id,r,UsuarioAtor.de(a)),"Alimentação registrada e estoque atualizado.");}
    @PostMapping("/lotes/{id}/tarefas") public String tarefa(@PathVariable Long id,@Valid @ModelAttribute TarefaRequest r,BindingResult br,RedirectAttributes ra,Authentication a){if(br.hasErrors()){ra.addFlashAttribute("erro",erro(br));return redirect(id);}try{service.detalhar(id);tarefas.criarVinculada(r,UsuarioAtor.de(a),ModuloOrigem.CRIACOES,"SUINOS:LOTE:"+id);ra.addFlashAttribute("mensagem","Tarefa vinculada ao lote.");}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return redirect(id);}
    private void preparar(Long id,Model m){var lote=service.detalhar(id);base(m);m.addAttribute("lote",lote);m.addAttribute("instalacoes",instalacoes.listarAtivas());m.addAttribute("itensEstoque",estoque.listarItensAtivos());m.addAttribute("locaisEstoque",estoque.listarLocaisAtivos());m.addAttribute("tiposPerda",new TipoEventoSuinos[]{TipoEventoSuinos.MORTALIDADE,TipoEventoSuinos.PERDA});EntradaSuinosRequest entrada=new EntradaSuinosRequest();init(entrada);PerdaSuinosRequest perda=new PerdaSuinosRequest();init(perda);perda.setTipo(TipoEventoSuinos.MORTALIDADE);TransferenciaSuinosRequest transferencia=new TransferenciaSuinosRequest();init(transferencia);PesagemSuinosRequest pesagem=new PesagemSuinosRequest();init(pesagem);AlimentacaoSuinosRequest alimentacao=new AlimentacaoSuinosRequest();init(alimentacao);m.addAttribute("entradaForm",entrada);m.addAttribute("perdaForm",perda);m.addAttribute("transferenciaForm",transferencia);m.addAttribute("pesagemForm",pesagem);m.addAttribute("alimentacaoForm",alimentacao);TarefaRequest tarefa=new TarefaRequest();tarefa.setTitulo("Manejo do lote "+lote.codigo());m.addAttribute("tarefaForm",tarefa);m.addAttribute("prioridades",PrioridadeTarefa.values());}
    private void form(Model m,CriarLoteSuinosRequest r){base(m);m.addAttribute("loteForm",r);m.addAttribute("categorias",CategoriaSuino.values());m.addAttribute("instalacoes",instalacoes.listarAtivas());}
    private void init(OperacaoSuinosBase r){r.setChaveIdempotencia(chave());r.setDataEvento(LocalDateTime.now(clock).withSecond(0).withNano(0));}
    private String operar(Long id,BindingResult br,RedirectAttributes ra,Acao acao,String ok){if(br.hasErrors()){ra.addFlashAttribute("erro",erro(br));return redirect(id);}try{acao.executar();ra.addFlashAttribute("mensagem",ok);}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return redirect(id);}
    private String erro(BindingResult br){return br.getAllErrors().stream().map(ObjectError::getDefaultMessage).findFirst().orElse("Dados inválidos.");}private String redirect(Long id){return "redirect:/sitio/criacoes/suinos/lotes/"+id;}private String chave(){return UUID.randomUUID().toString();}private void base(Model m){m.addAttribute("active","suinos");}@FunctionalInterface private interface Acao{Object executar();}
}
