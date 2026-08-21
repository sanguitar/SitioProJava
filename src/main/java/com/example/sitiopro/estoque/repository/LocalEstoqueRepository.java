package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.LocalEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalEstoqueRepository extends JpaRepository<LocalEstoque, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<LocalEstoque> findAllByOrderByNomeAsc();

    List<LocalEstoque> findByAtivoTrueOrderByNomeAsc();
}
