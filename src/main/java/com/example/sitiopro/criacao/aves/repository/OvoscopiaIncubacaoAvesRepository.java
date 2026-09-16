package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.OvoscopiaIncubacaoAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OvoscopiaIncubacaoAvesRepository extends JpaRepository<OvoscopiaIncubacaoAves, Long> {
    @EntityGraph(attributePaths = {"itens", "itens.ovo"})
    List<OvoscopiaIncubacaoAves> findByIncubacaoIdOrderByDataOvoscopiaDescIdDesc(Long incubacaoId);

    @EntityGraph(attributePaths = {"itens", "itens.ovo", "incubacao"})
    Optional<OvoscopiaIncubacaoAves> findByChaveIdempotencia(String chaveIdempotencia);
}

