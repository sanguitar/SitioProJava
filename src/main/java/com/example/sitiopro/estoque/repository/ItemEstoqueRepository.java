package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.ItemEstoque;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    long countByAtivoTrue();

    @EntityGraph(attributePaths = {"categoria", "unidadeMedida"})
    List<ItemEstoque> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = {"categoria", "unidadeMedida"})
    List<ItemEstoque> findByAtivoTrueOrderByNomeAsc();

    @Override
    @EntityGraph(attributePaths = {"categoria", "unidadeMedida"})
    Optional<ItemEstoque> findById(Long id);

    boolean existsByNomeIgnoreCase(String nome);
}
