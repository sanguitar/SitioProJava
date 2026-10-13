package com.example.sitiopro.agua.repository;

import com.example.sitiopro.agua.entity.OperacaoAgua;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OperacaoAguaRepository extends JpaRepository<OperacaoAgua, Long> {
    Optional<OperacaoAgua> findByChaveIdempotencia(String chaveIdempotencia);
    Page<OperacaoAgua> findByRecursoOrigemPropriedadeIdOrderByDataOperacaoDescIdDesc(
            Long propriedadeId, Pageable pageable);

    @Query("""
            select o from OperacaoAgua o
            where o.recursoOrigem.id = :recursoId or o.recursoDestino.id = :recursoId
            order by o.dataOperacao desc, o.id desc
            """)
    List<OperacaoAgua> listarDoRecurso(@Param("recursoId") Long recursoId, Pageable pageable);
}
