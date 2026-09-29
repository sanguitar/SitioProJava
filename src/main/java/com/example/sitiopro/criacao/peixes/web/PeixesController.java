package com.example.sitiopro.criacao.peixes.web;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.service.PeixesService;
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
@RequestMapping("/sitio/criacoes/peixes")
public class PeixesController {
    private final PeixesService service; private final InstalacaoCriacaoService instalacoes;
    private final EstoqueCatalogoService estoque; private final TarefaService tarefas; private final Clock clock;
    public PeixesController(PeixesService service,InstalacaoCriacaoService instalacoes,EstoqueCatalogoService estoque,TarefaService tarefas,Clock clock){this.service=service;this.instalacoes=instalacoes;this.estoque=estoque;this.tarefas=tarefas;this.clock=clock;}
    @GetMapping public String dashboard(Model m){base(m);m.addAttribute("resumo",service.dashboard());m.addAttribute("lotes",service.listar(StatusLotePeixes.ATIVO,null,0,8).conteudo());return "criacoes/peixes/dashboard";}
    @GetMapping("/lotes") public String lotes(@RequestParam(required=false) StatusLotePeixes status,@RequestParam(required=false) String termo,@RequestParam(defaultValue="0") int pagina,Model m){base(m);m.addAttribute("pagina",service.listar(status,termo,pagina,20));m.addAttribute("statusSelecionado",status);m.addAttribute("termo",termo);m.addAttribute("statusLotes",StatusLotePeixes.values());return "criacoes/peixes/lotes/lista";}
    @GetMapping("/lotes/novo") public String novo(Model m){CriarLotePeixesRequest r=new CriarLotePeixesRequest();r.setDataEntrada(LocalDate.now(clock));r.setChaveIdempotencia(chave());form(m,r);return "criacoes/peixes/lotes/form";}
    @PostMapping("/lotes") public String criar(@Valid @ModelAttribute("loteForm") CriarLotePeixesRequest r,BindingResult br,Model m,RedirectAttributes ra,Authentication a){if(br.hasErrors()){form(m,r);return "criacoes/peixes/lotes/form";}try{var lote=service.criar(r,UsuarioAtor.de(a));ra.addFlashAttribute("mensagem","Lote de peixes cadastrado.");return redirect(lote.id());}catch(RuntimeException ex){br.addError(new ObjectError("loteForm",ex.getMessage()));form(m,r);return "criacoes/peixes/lotes/form";}}
    @GetMapping("/lotes/{id}") public String detalhe(@PathVariable Long id,Model m){preparar(id,m);return "criacoes/peixes/lotes/detalhe";}
    @PostMapping("/lotes/{id}/entradas") public String entrada(@PathVariable Long id,@Valid @ModelAttribute EntradaPeixesRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarEntrada(id,r,UsuarioAtor.de(a)),"Entrada registrada.");}
    @PostMapping("/lotes/{id}/perdas") public String perda(@PathVariable Long id,@Valid @ModelAttribute PerdaPeixesRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarPerda(id,r,UsuarioAtor.de(a)),"Perda registrada.");}
    @PostMapping("/lotes/{id}/transferencias") public String transferencia(@PathVariable Long id,@Valid @ModelAttribute TransferenciaPeixesRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.transferir(id,r,UsuarioAtor.de(a)),"Lote transferido.");}
    @PostMapping("/lotes/{id}/biometrias") public String biometria(@PathVariable Long id,@Valid @ModelAttribute BiometriaPeixesRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarBiometria(id,r,UsuarioAtor.de(a)),"Biometria registrada.");}
    @PostMapping("/lotes/{id}/alimentacoes") public String alimentacao(@PathVariable Long id,@Valid @ModelAttribute AlimentacaoPeixesRequest r,BindingResult br,RedirectAttributes ra,Authentication a){return operar(id,br,ra,()->service.registrarAlimentacao(id,r,UsuarioAtor.de(a)),"Alimentação registrada e estoque atualizado.");}
    @PostMapping("/lotes/{id}/tarefas") public String tarefa(@PathVariable Long id,@Valid @ModelAttribute TarefaRequest r,BindingResult br,RedirectAttributes ra,Authentication a){if(br.hasErrors()){ra.addFlashAttribute("erro",erro(br));return redirect(id);}try{service.detalhar(id);tarefas.criarVinculada(r,UsuarioAtor.de(a),ModuloOrigem.CRIACOES,PeixesService.referencia(id));ra.addFlashAttribute("mensagem","Tarefa vinculada ao lote.");}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return redirect(id);}
    private void preparar(Long id,Model m){var lote=service.detalhar(id);base(m);m.addAttribute("lote",lote);m.addAttribute("instalacoes",instalacoes.listarTanquesAtivos());m.addAttribute("itensEstoque",estoque.listarItensAtivos());m.addAttribute("locaisEstoque",estoque.listarLocaisAtivos());m.addAttribute("tiposPerda",new TipoEventoPeixes[]{TipoEventoPeixes.MORTALIDADE,TipoEventoPeixes.PERDA});EntradaPeixesRequest entrada=new EntradaPeixesRequest();init(entrada);PerdaPeixesRequest perda=new PerdaPeixesRequest();init(perda);perda.setTipo(TipoEventoPeixes.MORTALIDADE);TransferenciaPeixesRequest transferencia=new TransferenciaPeixesRequest();init(transferencia);BiometriaPeixesRequest biometria=new BiometriaPeixesRequest();init(biometria);AlimentacaoPeixesRequest alimentacao=new AlimentacaoPeixesRequest();init(alimentacao);m.addAttribute("entradaForm",entrada);m.addAttribute("perdaForm",perda);m.addAttribute("transferenciaForm",transferencia);m.addAttribute("biometriaForm",biometria);m.addAttribute("alimentacaoForm",alimentacao);TarefaRequest tarefa=new TarefaRequest();tarefa.setTitulo("Manejo do lote "+lote.codigo());m.addAttribute("tarefaForm",tarefa);m.addAttribute("prioridades",PrioridadeTarefa.values());}
    private void form(Model m,CriarLotePeixesRequest r){base(m);m.addAttribute("loteForm",r);m.addAttribute("instalacoes",instalacoes.listarTanquesAtivos());}
    private void init(OperacaoPeixesBase r){r.setChaveIdempotencia(chave());r.setDataEvento(LocalDateTime.now(clock).withSecond(0).withNano(0));}
    private String operar(Long id,BindingResult br,RedirectAttributes ra,Acao acao,String ok){if(br.hasErrors()){ra.addFlashAttribute("erro",erro(br));return redirect(id);}try{acao.executar();ra.addFlashAttribute("mensagem",ok);}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return redirect(id);}
    private String erro(BindingResult br){return br.getAllErrors().stream().map(ObjectError::getDefaultMessage).findFirst().orElse("Dados inválidos.");}private String redirect(Long id){return "redirect:/sitio/criacoes/peixes/lotes/"+id;}private String chave(){return UUID.randomUUID().toString();}private void base(Model m){m.addAttribute("active","piscicultura");}@FunctionalInterface private interface Acao{Object executar();}
}
