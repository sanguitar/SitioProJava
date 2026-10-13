package com.example.sitiopro.agua.service;

import com.example.sitiopro.propriedade.entity.RecursoHidrico;
import com.example.sitiopro.propriedade.repository.PropriedadeRepository;
import com.example.sitiopro.propriedade.repository.RecursoHidricoRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AguaAlertasService {
    private final RecursoHidricoRepository recursos;
    private final PropriedadeRepository propriedades;
    private final AlertaService alertas;
    private final Clock clock;

    public AguaAlertasService(RecursoHidricoRepository recursos, PropriedadeRepository propriedades,
            AlertaService alertas, Clock clock) {
        this.recursos = recursos;
        this.propriedades = propriedades;
        this.alertas = alertas;
        this.clock = clock;
    }

    public void avaliar() {
        Long propriedadeId = propriedades.findByPrincipalTrue().map(p -> p.getId()).orElse(null);
        List<RecursoHidrico> ativos = propriedadeId == null
                ? List.of() : recursos.findByPropriedadeIdAndAtivoTrueOrderByNomeAsc(propriedadeId);
        alertas.sincronizar(ModuloOrigem.AGUA, TipoAlerta.AGUA_NIVEL_BAIXO,
                ativos.stream().filter(this::nivelBaixo).map(this::condicaoNivel).toList());
        LocalDateTime agora = LocalDateTime.now(clock);
        alertas.sincronizar(ModuloOrigem.AGUA, TipoAlerta.AGUA_INSPECAO_VENCIDA,
                ativos.stream().filter(r -> r.getProximaInspecao() != null && r.getProximaInspecao().isBefore(agora))
                        .map(this::condicaoInspecao).toList());
    }

    public BigDecimal limiteEfetivo(RecursoHidrico recurso) {
        if (recurso.getNivelMinimoAlertaLitros() != null) return recurso.getNivelMinimoAlertaLitros();
        return recurso.getCapacidadeLitros() == null ? null
                : recurso.getCapacidadeLitros().multiply(new BigDecimal("0.20"));
    }

    public boolean nivelBaixo(RecursoHidrico recurso) {
        BigDecimal limite = limiteEfetivo(recurso);
        return recurso.getVolumeAtualLitros() != null && limite != null
                && recurso.getVolumeAtualLitros().compareTo(limite) <= 0;
    }

    private CondicaoAlerta condicaoNivel(RecursoHidrico recurso) {
        BigDecimal limite = limiteEfetivo(recurso);
        return new CondicaoAlerta("AGUA:RECURSO:" + recurso.getId() + ":NIVEL_BAIXO",
                "Nível baixo: " + recurso.getNome(),
                "Volume atual de " + recurso.getVolumeAtualLitros().toPlainString()
                        + " L, no limite mínimo de " + limite.toPlainString() + " L.",
                SeveridadeAlerta.ALTA, AguaService.referencia(recurso.getId()),
                Map.of("recursoId", recurso.getId(), "volumeLitros", recurso.getVolumeAtualLitros(),
                        "limiteLitros", limite));
    }

    private CondicaoAlerta condicaoInspecao(RecursoHidrico recurso) {
        return new CondicaoAlerta("AGUA:RECURSO:" + recurso.getId() + ":INSPECAO_VENCIDA",
                "Inspeção vencida: " + recurso.getNome(),
                "A inspeção operacional estava prevista para " + recurso.getProximaInspecao() + ".",
                SeveridadeAlerta.ATENCAO, AguaService.referencia(recurso.getId()),
                Map.of("recursoId", recurso.getId(), "dataPrevista", recurso.getProximaInspecao()));
    }
}
