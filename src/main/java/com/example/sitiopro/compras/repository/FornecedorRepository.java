package com.example.sitiopro.compras.repository;

import com.example.sitiopro.compras.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    List<Fornecedor> findAllByOrderByNomeAsc();

    List<Fornecedor> findByAtivoTrueOrderByNomeAsc();

    long countByAtivoTrue();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
