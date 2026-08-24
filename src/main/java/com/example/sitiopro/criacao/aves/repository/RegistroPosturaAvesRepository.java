package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.RegistroPosturaAves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPosturaAvesRepository extends JpaRepository<RegistroPosturaAves, Long> {
    Optional<RegistroPosturaAves> findByChaveIdempotencia(String chave);
    List<RegistroPosturaAves> findByLoteIdOrderByDataColetaDescIdDesc(Long loteId);
    @Query("select coalesce(sum(p.ovosInteiros), 0) from RegistroPosturaAves p where p.lote.id = :loteId and p.dataColeta between :inicio and :fim")
    Long somarInteiros(@Param("loteId") Long loteId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    @Query("select coalesce(sum(p.ovosInteiros), 0) from RegistroPosturaAves p where p.dataColeta = :data")
    Long somarInteirosNaData(@Param("data") LocalDate data);
}
