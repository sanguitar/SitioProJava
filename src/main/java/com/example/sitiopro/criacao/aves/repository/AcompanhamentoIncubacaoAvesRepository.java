package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.AcompanhamentoIncubacaoAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcompanhamentoIncubacaoAvesRepository
        extends JpaRepository<AcompanhamentoIncubacaoAves, Long> {

    Optional<AcompanhamentoIncubacaoAves> findByChaveIdempotencia(String chave);

    @EntityGraph(attributePaths = "incubacao")
    List<AcompanhamentoIncubacaoAves> findByIncubacaoIdOrderByDataHoraDescIdDesc(Long incubacaoId);
}
