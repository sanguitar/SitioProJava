package com.example.sitiopro.abastecimento.repository;

import com.example.sitiopro.abastecimento.model.Abastecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {
    Optional<Abastecimento> findByChaveIdempotencia(String chaveIdempotencia);
    List<Abastecimento> findByVeiculoIdOrderByDataAscIdAsc(Long veiculoId);
    Page<Abastecimento> findAllByOrderByDataDescIdDesc(Pageable pageable);
    Page<Abastecimento> findByVeiculoIdOrderByDataDescIdDesc(Long veiculoId, Pageable pageable);
}
