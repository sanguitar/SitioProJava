package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findTop10ByOrderByDataMovimentoDescIdDesc();

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findAllByOrderByDataMovimentoDescIdDesc();

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findByItemIdOrderByDataMovimentoDescIdDesc(Long itemId);

    List<MovimentoEstoque> findByItemId(Long itemId);

    List<MovimentoEstoque> findByItemIdAndLoteId(Long itemId, Long loteId);

    @Override
    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    Optional<MovimentoEstoque> findById(Long id);
}
