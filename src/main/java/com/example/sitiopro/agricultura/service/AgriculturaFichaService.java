package com.example.sitiopro.agricultura.service;

import com.example.sitiopro.agricultura.dto.CultivoDetalhe;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgriculturaFichaService {
    private static final Logger log = LoggerFactory.getLogger(AgriculturaFichaService.class);
    private final AgriculturaService agricultura;
    private final ClimaConsultaService clima;
    public AgriculturaFichaService(AgriculturaService agricultura, ClimaConsultaService clima) {
        this.agricultura = agricultura; this.clima = clima;
    }
    public CultivoDetalhe detalhar(Long id) {
        var local = agricultura.detalheLocal(id);
        ClimaResumo resumo;
        // Climate has its own read transaction; its failure cannot roll back the crop read.
        try { resumo = clima.resumo(); }
        catch (RuntimeException ex) {
            log.warn("Resumo climatico local indisponivel na ficha agricola.");
            resumo = ClimaResumo.naoSincronizado();
        }
        return new CultivoDetalhe(local.resumo(), local.plantios(), local.acompanhamentos(),
                local.colheitas(), local.adubacoes(), local.irrigacoes(), local.tratamentos(), local.ocorrencias(),
                local.tarefas(), resumo == null ? ClimaResumo.naoSincronizado() : resumo);
    }
}
