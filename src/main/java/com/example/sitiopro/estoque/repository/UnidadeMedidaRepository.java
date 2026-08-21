package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {

    List<UnidadeMedida> findByAtivaTrueOrderByNomeAsc();
}
