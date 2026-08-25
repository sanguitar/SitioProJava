package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.ItemEstoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"categoria", "unidadeMedida"})
    @Query("select i from ItemEstoque i where i.id = :id")
    Optional<ItemEstoque> buscarParaMovimentacao(@Param("id") Long id);

    boolean existsByNomeIgnoreCase(String nome);
}
