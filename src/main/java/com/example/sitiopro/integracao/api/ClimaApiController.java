package com.example.sitiopro.integracao.api;

import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.dto.PrevisaoClimaticaResponse;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clima")
@Tag(name = "Clima", description = "Previsões locais persistidas pelo Sítio Guaratinguetá")
public class ClimaApiController {

    private final ClimaConsultaService climaConsultaService;

    public ClimaApiController(ClimaConsultaService climaConsultaService) {
        this.climaConsultaService = climaConsultaService;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Consulta o resumo climático local")
    public ClimaResumo resumo() {
        return climaConsultaService.resumo();
    }

    @GetMapping("/previsao")
    @Operation(summary = "Consulta a previsão horária persistida")
    public List<PrevisaoClimaticaResponse> previsao(@RequestParam(defaultValue = "168") int horas) {
        return climaConsultaService.previsao(horas);
    }
}
