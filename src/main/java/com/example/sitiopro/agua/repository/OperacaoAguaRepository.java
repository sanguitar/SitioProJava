package com.example.sitiopro.agua.repository;

import com.example.sitiopro.agua.entity.OperacaoAgua;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OperacaoAguaRepository extends JpaRepository<OperacaoAgua, Long> {
    Optional<OperacaoAgua> findByChaveIdempotencia(String chaveIdempotencia);
    @EntityGraph(attributePaths = {"recursoOrigem", "recursoDestino"})
    Optional<OperacaoAgua> findByPublicId(UUID publicId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"recursoOrigem", "recursoDestino"})
    @Query("select o from OperacaoAgua o where o.publicId = :publicId")
    Optional<OperacaoAgua> buscarPorPublicIdParaAtualizacao(@Param("publicId") UUID publicId);

    @EntityGraph(attributePaths = {"recursoOrigem", "recursoDestino"})
    List<OperacaoAgua> findByTipoOrderByIdAsc(com.example.sitiopro.agua.entity.TipoOperacaoAgua tipo);
    Page<OperacaoAgua> findByRecursoOrigemPropriedadeIdOrderByDataOperacaoDescIdDesc(
            Long propriedadeId, Pageable pageable);

    @Query("""
            select o from OperacaoAgua o
            where o.recursoOrigem.id = :recursoId or o.recursoDestino.id = :recursoId
            order by o.dataOperacao desc, o.id desc
            """)
    List<OperacaoAgua> listarDoRecurso(@Param("recursoId") Long recursoId, Pageable pageable);
}
