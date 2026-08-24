package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.TransferenciaLoteAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferenciaLoteAvesRepository extends JpaRepository<TransferenciaLoteAves, Long> {
    Optional<TransferenciaLoteAves> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = {"instalacaoOrigem", "instalacaoDestino"})
    List<TransferenciaLoteAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
}
