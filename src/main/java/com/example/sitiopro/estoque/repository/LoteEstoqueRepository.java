package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.LoteEstoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteEstoqueRepository extends JpaRepository<LoteEstoque, Long> {

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida"})
    List<LoteEstoque> findByValidadeBeforeOrderByValidadeAsc(LocalDate data);

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida"})
    List<LoteEstoque> findByValidadeBetweenOrderByValidadeAsc(LocalDate inicio, LocalDate fim);

    Optional<LoteEstoque> findByItemIdAndCodigoIgnoreCase(Long itemId, String codigo);
}
