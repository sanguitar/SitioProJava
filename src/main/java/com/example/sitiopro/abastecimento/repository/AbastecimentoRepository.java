package com.example.sitiopro.abastecimento.repository;

import com.example.sitiopro.abastecimento.model.Abastecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {
}
