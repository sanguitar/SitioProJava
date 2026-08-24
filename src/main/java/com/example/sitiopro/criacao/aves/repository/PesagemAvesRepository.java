package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.PesagemAves;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesagemAvesRepository extends JpaRepository<PesagemAves, Long> {
    Optional<PesagemAves> findByChaveIdempotencia(String chave);
    List<PesagemAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
}
