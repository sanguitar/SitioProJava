package com.example.sitiopro.criacao.peixes.web;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.service.*;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
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
public class QualidadeAguaController {
    private final QualidadeAguaService qualidade; private final PeixesService peixes; private final Clock clock;
    public QualidadeAguaController(QualidadeAguaService qualidade,PeixesService peixes,Clock clock){this.qualidade=qualidade;this.peixes=peixes;this.clock=clock;}
    @GetMapping("/lotes/{id}/qualidade-agua") public String historico(@PathVariable Long id,Model m){var lote=peixes.detalhar(id);MedicaoQualidadeAguaRequest form=new MedicaoQualidadeAguaRequest();form.setMedidoEm(LocalDateTime.now(clock).withSecond(0).withNano(0));form.setChaveIdempotencia(UUID.randomUUID().toString());m.addAttribute("active","piscicultura");m.addAttribute("lote",lote);m.addAttribute("medicoes",qualidade.listar(id));m.addAttribute("limites",qualidade.configuracao());m.addAttribute("medicaoForm",form);return "criacoes/peixes/qualidade-agua/historico";}
    @PostMapping("/lotes/{id}/qualidade-agua") public String registrar(@PathVariable Long id,@Valid @ModelAttribute("medicaoForm") MedicaoQualidadeAguaRequest r,BindingResult br,RedirectAttributes ra,Authentication a){if(br.hasErrors()){ra.addFlashAttribute("erro",br.getAllErrors().get(0).getDefaultMessage());return redirect(id);}try{qualidade.registrar(id,r,UsuarioAtor.de(a));ra.addFlashAttribute("mensagem","Medição de qualidade da água registrada.");}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return redirect(id);}
    @GetMapping("/qualidade-agua/configuracao") public String configuracao(Model m){m.addAttribute("active","piscicultura");m.addAttribute("configForm",qualidade.configuracao());return "criacoes/peixes/qualidade-agua/configuracao";}
    @PostMapping("/qualidade-agua/configuracao") public String atualizar(@Valid @ModelAttribute("configForm") ConfiguracaoQualidadeAguaDto r,BindingResult br,RedirectAttributes ra,Authentication a){if(br.hasErrors()){ra.addFlashAttribute("erro",br.getAllErrors().get(0).getDefaultMessage());return "redirect:/sitio/criacoes/peixes/qualidade-agua/configuracao";}try{qualidade.atualizarConfiguracao(r,UsuarioAtor.de(a));ra.addFlashAttribute("mensagem","Limites de qualidade da água atualizados.");}catch(RuntimeException ex){ra.addFlashAttribute("erro",ex.getMessage());}return "redirect:/sitio/criacoes/peixes/qualidade-agua/configuracao";}
    private String redirect(Long id){return "redirect:/sitio/criacoes/peixes/lotes/"+id+"/qualidade-agua";}
}
