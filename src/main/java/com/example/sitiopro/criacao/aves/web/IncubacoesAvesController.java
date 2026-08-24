package com.example.sitiopro.criacao.aves.web;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import com.example.sitiopro.criacao.aves.service.*;
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
import java.util.UUID;

@Controller
@RequestMapping("/sitio/criacoes/aves/incubacoes")
public class IncubacoesAvesController {
    private final IncubacaoAvesService service; private final InstalacaoCriacaoService instalacaoService;
    private final LoteAvesService loteService; private final Clock clock;
    public IncubacoesAvesController(IncubacaoAvesService service, InstalacaoCriacaoService instalacaoService,
            LoteAvesService loteService, Clock clock) { this.service = service; this.instalacaoService = instalacaoService; this.loteService = loteService; this.clock = clock; }

    @GetMapping public String listar(@RequestParam(defaultValue = "0") int pagina, Model m) { base(m); m.addAttribute("pagina", service.listar(pagina, 20)); return "criacoes/aves/incubacoes/lista"; }
    @GetMapping("/nova") public String nova(Model m) { CriarIncubacaoAvesRequest r = new CriarIncubacaoAvesRequest(); LocalDate hoje = LocalDate.now(clock); r.setDataInicio(hoje); r.setDataPrevistaEclosao(hoje.plusDays(21)); r.setChaveIdempotencia(UUID.randomUUID().toString()); formulario(m, r); return "criacoes/aves/incubacoes/form"; }
    @PostMapping public String criar(@Valid @ModelAttribute("incubacaoForm") CriarIncubacaoAvesRequest r, BindingResult br, Model m, RedirectAttributes ra, Authentication a) { if (br.hasErrors()) { formulario(m, r); return "criacoes/aves/incubacoes/form"; } try { var criada = service.criar(r, UsuarioAtor.de(a)); ra.addFlashAttribute("mensagem", "Incubação iniciada."); return "redirect:/sitio/criacoes/aves/incubacoes/" + criada.id(); } catch (AvesOperacaoException ex) { br.addError(new ObjectError("incubacaoForm", ex.getMessage())); formulario(m, r); return "criacoes/aves/incubacoes/form"; } }
    @GetMapping("/{id}") public String detalhe(@PathVariable Long id, Model m) { base(m); m.addAttribute("incubacao", service.detalhar(id)); m.addAttribute("instalacoes", instalacaoService.listarAtivas()); FinalizarIncubacaoAvesRequest r = new FinalizarIncubacaoAvesRequest(); r.setDataEclosao(LocalDate.now(clock)); r.setChaveIdempotenciaLote(UUID.randomUUID().toString()); m.addAttribute("finalizacaoForm", r); m.addAttribute("finalidades", FinalidadeLoteAves.values()); m.addAttribute("sexos", SexoLoteAves.values()); return "criacoes/aves/incubacoes/detalhe"; }
    @PostMapping("/{id}/finalizar") public String finalizar(@PathVariable Long id, @Valid @ModelAttribute FinalizarIncubacaoAvesRequest r, BindingResult br, RedirectAttributes ra, Authentication a) { if (br.hasErrors()) ra.addFlashAttribute("erro", erro(br)); else try { service.finalizar(id, r, UsuarioAtor.de(a)); ra.addFlashAttribute("mensagem", "Incubação finalizada."); } catch (AvesOperacaoException ex) { ra.addFlashAttribute("erro", ex.getMessage()); } return redirect(id); }
    @PostMapping("/{id}/cancelar") public String cancelar(@PathVariable Long id, RedirectAttributes ra, Authentication a) { try { service.cancelar(id, UsuarioAtor.de(a)); ra.addFlashAttribute("mensagem", "Incubação cancelada."); } catch (AvesOperacaoException ex) { ra.addFlashAttribute("erro", ex.getMessage()); } return redirect(id); }
    private void formulario(Model m, CriarIncubacaoAvesRequest r) { base(m); m.addAttribute("incubacaoForm", r); m.addAttribute("incubadoras", instalacaoService.listarIncubadorasAtivas()); m.addAttribute("lotes", loteService.listarAtivos()); }
    private void base(Model m) { m.addAttribute("active", "aves"); }
    private String redirect(Long id) { return "redirect:/sitio/criacoes/aves/incubacoes/" + id; }
    private String erro(BindingResult br) { return br.getAllErrors().stream().map(ObjectError::getDefaultMessage).findFirst().orElse("Dados inválidos."); }
}
