package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.tarefas.dto.HistoricoOperacionalResumo;
import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.EventoTarefaAlerta;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TipoEventoOperacional;
import com.example.sitiopro.tarefas.repository.EventoTarefaAlertaRepository;
import com.example.sitiopro.usuario.entity.Usuario;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricoOperacionalService {

    private final EventoTarefaAlertaRepository repository;
    private final Clock clock;

    public HistoricoOperacionalService(EventoTarefaAlertaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void registrarTarefa(Tarefa tarefa, TipoEventoOperacional tipo, Usuario usuario, String ator,
            String detalhe) {
        EventoTarefaAlerta evento = novoEvento(tipo, usuario, ator, detalhe);
        evento.setTarefa(tarefa);
        repository.save(evento);
    }

    public void registrarAlerta(Alerta alerta, TipoEventoOperacional tipo, Usuario usuario, String ator,
            String detalhe) {
        EventoTarefaAlerta evento = novoEvento(tipo, usuario, ator, detalhe);
        evento.setAlerta(alerta);
        repository.save(evento);
    }

    public List<HistoricoOperacionalResumo> listarTarefa(Long tarefaId) {
        return repository.findByTarefaIdOrderByOcorridoEmDescIdDesc(tarefaId).stream()
                .map(this::resumo)
                .toList();
    }

    public List<HistoricoOperacionalResumo> listarAlerta(Long alertaId) {
        return repository.findByAlertaIdOrderByOcorridoEmDescIdDesc(alertaId).stream()
                .map(this::resumo)
                .toList();
    }

    private EventoTarefaAlerta novoEvento(TipoEventoOperacional tipo, Usuario usuario, String ator,
            String detalhe) {
        EventoTarefaAlerta evento = new EventoTarefaAlerta();
        evento.setTipo(tipo);
        evento.setOcorridoEm(LocalDateTime.now(clock));
        evento.setUsuario(usuario);
        evento.setAtor(ator == null || ator.isBlank() ? "sistema" : ator);
        evento.setDetalhe(limitar(detalhe, 500));
        return evento;
    }

    private HistoricoOperacionalResumo resumo(EventoTarefaAlerta evento) {
        return new HistoricoOperacionalResumo(evento.getId(), evento.getTipo(), evento.getOcorridoEm(),
                evento.getAtor(), evento.getDetalhe());
    }

    private String limitar(String texto, int limite) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String normalizado = texto.trim();
        return normalizado.length() <= limite ? normalizado : normalizado.substring(0, limite);
    }
}
