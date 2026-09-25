package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.CicloReprodutivoSuinosRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class SuinosAlertasService {
    private final CicloReprodutivoSuinosRepository ciclos;
    private final AlertaService alertas;
    private final Clock clock;

    public SuinosAlertasService(CicloReprodutivoSuinosRepository ciclos, AlertaService alertas, Clock clock) {
        this.ciclos = ciclos; this.alertas = alertas; this.clock = clock;
    }

    public void avaliar() {
        LocalDate hoje = LocalDate.now(clock);
        List<CondicaoAlerta> checagens = ciclos.findByStatusAndDataPrevistaChecagemBefore(
                        StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM, hoje.plusDays(1)).stream()
                .map(c -> condicao(c, "CHECAGEM_PENDENTE", "Checagem de gestação pendente",
                        "A checagem de " + c.getMatriz().getCodigo() + " estava prevista para "
                                + c.getDataPrevistaChecagem() + ".", SeveridadeAlerta.ALTA))
                .toList();
        alertas.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_SUINOS_CHECAGEM_PENDENTE, checagens);

        List<CondicaoAlerta> partos = ciclos.findByStatusAndDataPrevistaPartoBefore(
                        StatusCicloReprodutivoSuinos.GESTANTE, hoje).stream()
                .map(c -> condicao(c, "PARTO_ATRASADO", "Parto previsto em atraso",
                        "O parto de " + c.getMatriz().getCodigo() + " estava previsto para "
                                + c.getDataPrevistaParto() + ".", SeveridadeAlerta.CRITICA))
                .toList();
        alertas.sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_SUINOS_PARTO_ATRASADO, partos);
    }

    private CondicaoAlerta condicao(CicloReprodutivoSuinos ciclo, String sufixo, String titulo,
            String descricao, SeveridadeAlerta severidade) {
        return new CondicaoAlerta("CRIACAO:SUINOS:REPRODUCAO:" + ciclo.getId() + ":" + sufixo,
                titulo, descricao, severidade, SuinosReproducaoOperacionalService.referencia(ciclo.getId()),
                Map.of("cicloId", ciclo.getId(), "matrizId", ciclo.getMatriz().getId()));
    }
}
