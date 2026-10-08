package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class ManutencaoAlertasService {
    private final RegistroManutencaoRepository registros;
    private final PlanoManutencaoPreventivaRepository planos;
    private final LeituraMedidorAtivoRepository leituras;
    private final AlertaService alertas;
    private final Clock clock;

    public ManutencaoAlertasService(RegistroManutencaoRepository registros,
            PlanoManutencaoPreventivaRepository planos, LeituraMedidorAtivoRepository leituras,
            AlertaService alertas, Clock clock) {
        this.registros = registros;
        this.planos = planos;
        this.leituras = leituras;
        this.alertas = alertas;
        this.clock = clock;
    }

    public void avaliar() {
        List<CondicaoAlerta> condicoes = new ArrayList<>(registros.findByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(LocalDateTime.now(clock))
                .stream().map(r -> new CondicaoAlerta(
                        "MANUTENCAO:REGISTRO:" + r.getId() + ":VENCIDA",
                        "Manutenção vencida: " + r.getAtivo().getCodigo(),
                        "A manutenção de " + r.getAtivo().getNome() + " estava prevista para " + r.getProximaManutencao() + ".",
                        SeveridadeAlerta.ALTA, ManutencaoService.referencia(r.getAtivo().getId()),
                        Map.of("registroManutencaoId", r.getId(), "ativoId", r.getAtivo().getId(),
                                "dataPrevista", r.getProximaManutencao())))
                .toList());
        planos.findByAtivoTrueOrderByProximaDataAscNomeAsc().stream()
                .filter(this::vencido)
                .map(this::condicaoPlano)
                .forEach(condicoes::add);
        alertas.sincronizar(ModuloOrigem.MANUTENCAO, TipoAlerta.MANUTENCAO_VENCIDA, condicoes);
    }

    private boolean vencido(PlanoManutencaoPreventiva plano) {
        return switch (plano.getTipoPeriodicidade()) {
            case DIAS -> plano.getProximaData().isBefore(LocalDateTime.now(clock));
            case HORIMETRO -> leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(
                    plano.getAtivoPatrimonial().getId()).map(LeituraMedidorAtivo::getHorimetro)
                    .filter(v -> v.compareTo(plano.getProximoValor()) >= 0).isPresent();
            case QUILOMETRAGEM -> leituras.findTopByAtivoPatrimonialIdAndQuilometragemIsNotNullOrderByDataLeituraDescIdDesc(
                    plano.getAtivoPatrimonial().getId()).map(LeituraMedidorAtivo::getQuilometragem)
                    .filter(v -> v.compareTo(plano.getProximoValor()) >= 0).isPresent();
        };
    }

    private CondicaoAlerta condicaoPlano(PlanoManutencaoPreventiva plano) {
        String limite = plano.getTipoPeriodicidade() == TipoPeriodicidadeManutencao.DIAS
                ? plano.getProximaData().toString()
                : plano.getProximoValor().toPlainString();
        return new CondicaoAlerta(ManutencaoPreventivaService.chaveAlerta(plano),
                "Preventiva vencida: " + plano.getAtivoPatrimonial().getCodigo(),
                plano.getNome() + " atingiu o limite previsto (" + limite + ").",
                SeveridadeAlerta.ALTA, ManutencaoService.referencia(plano.getAtivoPatrimonial().getId()),
                Map.of("planoPreventivoId", plano.getId(), "ativoId", plano.getAtivoPatrimonial().getId(),
                        "tipoPeriodicidade", plano.getTipoPeriodicidade().name(), "limite", limite));
    }
}
