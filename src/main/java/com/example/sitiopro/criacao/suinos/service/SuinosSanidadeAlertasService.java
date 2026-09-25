package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.suinos.repository.RegistroSanitarioSuinosRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class SuinosSanidadeAlertasService {
    private final RegistroSanitarioSuinosRepository registros;
    private final AlertaService alertas;
    private final Clock clock;
    public SuinosSanidadeAlertasService(RegistroSanitarioSuinosRepository registros,
            AlertaService alertas, Clock clock) {
        this.registros = registros; this.alertas = alertas; this.clock = clock;
    }
    public void avaliar() {
        LocalDate hoje = LocalDate.now(clock);
        List<CondicaoAlerta> vencidos = registros
                .findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(hoje).stream()
                .map(r -> new CondicaoAlerta("CRIACAO:SUINOS:SANIDADE:" + r.getId() + ":VENCIDO",
                        "Procedimento sanitário vencido", r.getProximaAcao() + " estava previsto para "
                                + r.getProximaAcaoData() + " (" + alvo(r) + ").",
                        SeveridadeAlerta.ALTA, SuinosSanidadeService.referencia(r.getId()),
                        Map.of("registroSanitarioId", r.getId(), "dataPrevista", r.getProximaAcaoData())))
                .toList();
        alertas.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO,
                vencidos);
    }
    private String alvo(com.example.sitiopro.criacao.suinos.entity.RegistroSanitarioSuinos r) {
        return r.getLote() != null ? r.getLote().getCodigo() : r.getAnimalReprodutivo().getCodigo();
    }
}
