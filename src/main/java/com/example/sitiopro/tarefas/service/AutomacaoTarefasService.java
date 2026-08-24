package com.example.sitiopro.tarefas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomacaoTarefasService {

    private final RegrasAlertasService regrasAlertasService;
    private final RecorrenciaTarefaService recorrenciaService;

    public AutomacaoTarefasService(RegrasAlertasService regrasAlertasService,
            RecorrenciaTarefaService recorrenciaService) {
        this.regrasAlertasService = regrasAlertasService;
        this.recorrenciaService = recorrenciaService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean avaliarAlertas() {
        regrasAlertasService.avaliar();
        return true;
    }

    @Transactional
    public int gerarRecorrencias() {
        return recorrenciaService.gerarOcorrenciasVencidas();
    }
}
