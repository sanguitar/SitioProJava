package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.MortalidadeAves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MortalidadeAvesRepository extends JpaRepository<MortalidadeAves, Long> {
    Optional<MortalidadeAves> findByChaveIdempotencia(String chave);
    List<MortalidadeAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
    @Query("select coalesce(sum(m.quantidade), 0) from MortalidadeAves m where m.lote.id = :loteId and m.dataEvento >= :desde")
    Long somarDesde(@Param("loteId") Long loteId, @Param("desde") LocalDateTime desde);
    @Query("select coalesce(sum(m.quantidade), 0) from MortalidadeAves m where m.dataEvento >= :desde")
    Long somarTotalDesde(@Param("desde") LocalDateTime desde);
}
