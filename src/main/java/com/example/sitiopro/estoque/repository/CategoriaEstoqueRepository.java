package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaEstoqueRepository extends JpaRepository<CategoriaEstoque, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<CategoriaEstoque> findAllByOrderByNomeAsc();

    List<CategoriaEstoque> findByAtivaTrueOrderByNomeAsc();
}
