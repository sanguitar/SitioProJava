package com.example.sitiopro.tarefas.repository;

import com.example.sitiopro.tarefas.entity.EventoTarefaAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoTarefaAlertaRepository extends JpaRepository<EventoTarefaAlerta, Long> {

    List<EventoTarefaAlerta> findByTarefaIdOrderByOcorridoEmDescIdDesc(Long tarefaId);

    List<EventoTarefaAlerta> findByAlertaIdOrderByOcorridoEmDescIdDesc(Long alertaId);
}
