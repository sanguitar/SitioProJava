package com.example.sitiopro.agricultura.api;
import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.service.*;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/agricultura")
public class AgriculturaApiController {
    private final AgriculturaService service;
    private final AgriculturaFichaService fichas;
    public AgriculturaApiController(AgriculturaService service, AgriculturaFichaService fichas) {
        this.service = service; this.fichas = fichas;
    }
    @GetMapping("/resumo") public AgriculturaResumo resumo() { return service.painel(); }
    @GetMapping("/cultivos/{id}") public CultivoDetalhe detalhe(@PathVariable Long id) { return fichas.detalhar(id); }
    @GetMapping("/safras")
    public PaginaResponse<SafraResumo> listarSafras(@RequestParam(defaultValue = "0") int pagina) { return service.listarSafras(pagina); }
    @PostMapping("/safras") @ResponseStatus(HttpStatus.CREATED)
    public SafraResumo criarSafra(@Valid @RequestBody SafraRequest request) { return service.salvarSafra(null, request); }
    @PutMapping("/safras/{id}")
    public SafraResumo atualizarSafra(@PathVariable Long id, @Valid @RequestBody SafraRequest request) { return service.salvarSafra(id, request); }
    @GetMapping("/culturas")
    public PaginaResponse<CulturaResumo> listarCulturas(@RequestParam(defaultValue = "0") int pagina) { return service.listarCulturas(pagina); }
    @PostMapping("/culturas") @ResponseStatus(HttpStatus.CREATED)
    public CulturaResumo criarCultura(@Valid @RequestBody CulturaRequest request) { return service.salvarCultura(null, request); }
    @PutMapping("/culturas/{id}")
    public CulturaResumo atualizarCultura(@PathVariable Long id, @Valid @RequestBody CulturaRequest request) { return service.salvarCultura(id, request); }
    @GetMapping("/cultivos")
    public PaginaResponse<CultivoResumo> listarCultivos(@RequestParam(defaultValue = "0") int pagina) { return service.listarCultivos(pagina); }
    @GetMapping("/adubacoes")
    public PaginaResponse<AdubacaoResumo> listarAdubacoes(@RequestParam(defaultValue = "0") int pagina) { return service.listarAdubacoes(pagina); }
    @GetMapping("/irrigacoes")
    public PaginaResponse<IrrigacaoResumo> listarIrrigacoes(@RequestParam(defaultValue = "0") int pagina) { return service.listarIrrigacoes(pagina); }
    @GetMapping("/tratamentos")
    public PaginaResponse<TratamentoResumo> listarTratamentos(@RequestParam(defaultValue = "0") int pagina) { return service.listarTratamentos(pagina); }
    @GetMapping("/ocorrencias")
    public PaginaResponse<OcorrenciaResumo> listarOcorrencias(@RequestParam(defaultValue = "0") int pagina) { return service.listarOcorrencias(pagina); }
    @GetMapping("/ocorrencias/{id}")
    public OcorrenciaDetalhe detalharOcorrencia(@PathVariable Long id) { return service.detalharOcorrencia(id); }
    @PostMapping("/cultivos") @ResponseStatus(HttpStatus.CREATED)
    public CultivoResumo criarCultivo(@Valid @RequestBody CultivoRequest request) { return service.salvarCultivo(null, request); }
    @PutMapping("/cultivos/{id}")
    public CultivoResumo atualizarCultivo(@PathVariable Long id, @Valid @RequestBody CultivoRequest request) { return service.salvarCultivo(id, request); }
    @PostMapping("/cultivos/{id}/plantios") @ResponseStatus(HttpStatus.CREATED)
    public PlantioResumo registrarPlantio(@PathVariable Long id, @Valid @RequestBody PlantioRequest request) { return service.registrarPlantio(id, request); }
    @PostMapping("/cultivos/{id}/acompanhamentos") @ResponseStatus(HttpStatus.CREATED)
    public AcompanhamentoResumo registrarAcompanhamento(@PathVariable Long id, @Valid @RequestBody AcompanhamentoRequest request) { return service.registrarAcompanhamento(id, request); }
    @PostMapping("/cultivos/{id}/colheitas") @ResponseStatus(HttpStatus.CREATED)
    public ColheitaResumo registrarColheita(@PathVariable Long id, @Valid @RequestBody ColheitaRequest request) { return service.registrarColheita(id, request); }
    @PostMapping("/cultivos/{id}/adubacoes") @ResponseStatus(HttpStatus.CREATED)
    public AdubacaoResumo registrarAdubacao(@PathVariable Long id, @Valid @RequestBody AdubacaoRequest request) { return service.registrarAdubacao(id, request); }
    @PostMapping("/cultivos/{id}/irrigacoes") @ResponseStatus(HttpStatus.CREATED)
    public IrrigacaoResumo registrarIrrigacao(@PathVariable Long id, @Valid @RequestBody IrrigacaoRequest request) { return service.registrarIrrigacao(id, request); }
    @PostMapping("/cultivos/{id}/tratamentos") @ResponseStatus(HttpStatus.CREATED)
    public TratamentoResumo registrarTratamento(@PathVariable Long id, @Valid @RequestBody TratamentoRequest request) { return service.registrarTratamento(id, request); }
    @PostMapping("/cultivos/{id}/ocorrencias") @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaResumo registrarOcorrencia(@PathVariable Long id, @Valid @RequestBody OcorrenciaRequest request) { return service.registrarOcorrencia(id, request); }
    @PutMapping("/ocorrencias/{id}")
    public OcorrenciaResumo atualizarOcorrencia(@PathVariable Long id,
            @Valid @RequestBody OcorrenciaAtualizacaoRequest request) {
        return service.atualizarOcorrencia(id, request);
    }
    @PostMapping("/ocorrencias/{id}/encerrar")
    public OcorrenciaResumo encerrarOcorrencia(@PathVariable Long id,
            @Valid @RequestBody EncerramentoOcorrenciaRequest request) {
        return service.encerrarOcorrencia(id, request);
    }
    @PostMapping("/ocorrencias/{id}/tarefa-inspecao") @ResponseStatus(HttpStatus.CREATED)
    public TarefaResumo criarTarefaInspecao(@PathVariable Long id, Authentication authentication) {
        return service.criarTarefaInspecao(id, UsuarioAtor.de(authentication));
    }
    @PostMapping("/cultivos/{id}/tarefas") @ResponseStatus(HttpStatus.CREATED)
    public TarefaDetalhe tarefa(@PathVariable Long id, @Valid @RequestBody TarefaRequest request, Authentication authentication) {
        return service.criarTarefa(id, request, UsuarioAtor.de(authentication));
    }
    @PostMapping("/cultivos/{id}/status")
    public CultivoResumo status(@PathVariable Long id, @Valid @RequestBody StatusCultivoRequest request) {
        return service.alterarStatus(id, request);
    }
}
