package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.peixes.repository.RegistroSanitarioPeixesRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class PeixesSanidadeAlertasService {
    private final RegistroSanitarioPeixesRepository registros;
    private final AlertaService alertas;
    private final Clock clock;

    public PeixesSanidadeAlertasService(RegistroSanitarioPeixesRepository registros,
            AlertaService alertas, Clock clock) {
        this.registros = registros;
        this.alertas = alertas;
        this.clock = clock;
    }

    public void avaliar() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<CondicaoAlerta> pendentes = registros
                .findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(agora).stream()
                .map(r -> new CondicaoAlerta(
                        "CRIACAO:PEIXES:SANIDADE:" + r.getId() + ":PENDENTE",
                        "Ação sanitária de peixes pendente",
                        r.getProximaAcao() + " estava prevista para " + r.getProximaAcaoData()
                                + " no lote " + r.getLote().getCodigo() + ".",
                        SeveridadeAlerta.ALTA, PeixesService.referencia(r.getLote().getId()),
                        Map.of("registroSanitarioId", r.getId(), "loteId", r.getLote().getId(),
                                "dataPrevista", r.getProximaAcaoData())))
                .toList();
        alertas.sincronizar(ModuloOrigem.CRIACOES,
                TipoAlerta.CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE, pendentes);
    }
}
