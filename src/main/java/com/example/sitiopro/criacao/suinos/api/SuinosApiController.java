package com.example.sitiopro.criacao.suinos.api;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.StatusLoteSuinos;
import com.example.sitiopro.criacao.suinos.service.SuinosService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/criacoes/suinos")
public class SuinosApiController {
    private final SuinosService service;
    public SuinosApiController(SuinosService service){this.service=service;}
    @GetMapping("/resumo") public SuinosDashboardResumo resumo(){return service.dashboard();}
    @GetMapping("/lotes") public PaginaResponse<LoteSuinosResumo> lotes(@RequestParam(required=false) StatusLoteSuinos status,@RequestParam(required=false) String termo,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho){return service.listar(status,termo,pagina,tamanho);}
    @GetMapping("/lotes/{id}") public LoteSuinosDetalhe lote(@PathVariable Long id){return service.detalhar(id);}
    @PostMapping("/lotes") public ResponseEntity<LoteSuinosDetalhe> criar(@Valid @RequestBody CriarLoteSuinosRequest r,Authentication a){var criado=service.criar(r,UsuarioAtor.de(a));var uri=ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/criacoes/suinos/lotes/{id}").buildAndExpand(criado.id()).toUri();return ResponseEntity.created(uri).body(criado);}
    @PostMapping("/lotes/{id}/entradas") public LoteSuinosDetalhe entrada(@PathVariable Long id,@Valid @RequestBody EntradaSuinosRequest r,Authentication a){return service.registrarEntrada(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/perdas") public LoteSuinosDetalhe perda(@PathVariable Long id,@Valid @RequestBody PerdaSuinosRequest r,Authentication a){return service.registrarPerda(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/transferencias") public LoteSuinosDetalhe transferencia(@PathVariable Long id,@Valid @RequestBody TransferenciaSuinosRequest r,Authentication a){return service.transferir(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/pesagens") public LoteSuinosDetalhe pesagem(@PathVariable Long id,@Valid @RequestBody PesagemSuinosRequest r,Authentication a){return service.registrarPesagem(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/alimentacoes") public LoteSuinosDetalhe alimentacao(@PathVariable Long id,@Valid @RequestBody AlimentacaoSuinosRequest r,Authentication a){return service.registrarAlimentacao(id,r,UsuarioAtor.de(a));}
}
