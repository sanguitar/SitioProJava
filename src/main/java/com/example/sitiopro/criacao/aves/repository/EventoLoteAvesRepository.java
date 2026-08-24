package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.EventoLoteAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoLoteAvesRepository extends JpaRepository<EventoLoteAves, Long> {
    @EntityGraph(attributePaths = {"instalacaoOrigem", "instalacaoDestino"})
    List<EventoLoteAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
}
