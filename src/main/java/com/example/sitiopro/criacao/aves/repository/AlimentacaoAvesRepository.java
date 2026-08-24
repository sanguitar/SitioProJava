package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.AlimentacaoAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlimentacaoAvesRepository extends JpaRepository<AlimentacaoAves, Long> {
    Optional<AlimentacaoAves> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = {"itemEstoque", "itemEstoque.unidadeMedida", "localEstoque", "movimentoEstoque"})
    List<AlimentacaoAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
}
