package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.OvoIncubacaoAves;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OvoIncubacaoAvesRepository extends JpaRepository<OvoIncubacaoAves, Long> {
    List<OvoIncubacaoAves> findByIncubacaoIdOrderByNumeroAsc(Long incubacaoId);
    long countByIncubacaoId(Long incubacaoId);
}

