package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.LoteAves;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoteAvesRepository extends JpaRepository<LoteAves, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    Optional<LoteAves> findByChaveIdempotencia(String chave);

    @EntityGraph(attributePaths = "instalacaoAtual")
    @Query("""
            select l from LoteAves l
            where (:status is null or l.status = :status)
              and (:termo is null or lower(l.codigo) like lower(concat('%', :termo, '%'))
                   or lower(coalesce(l.nome, '')) like lower(concat('%', :termo, '%')))
            order by case when l.status = com.example.sitiopro.criacao.aves.entity.StatusLoteAves.ATIVO then 0 else 1 end,
                     l.dataEntrada desc, l.id desc
            """)
    Page<LoteAves> buscar(@Param("status") StatusLoteAves status, @Param("termo") String termo, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "instalacaoAtual")
    Optional<LoteAves> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "instalacaoAtual")
    @Query("select l from LoteAves l where l.id = :id")
    Optional<LoteAves> buscarParaAtualizacao(@Param("id") Long id);

    @EntityGraph(attributePaths = "instalacaoAtual")
    List<LoteAves> findByStatusOrderByCodigoAsc(StatusLoteAves status);

    long countByStatus(StatusLoteAves status);

    @Query("select coalesce(sum(l.quantidadeAtual), 0) from LoteAves l where l.status = :status")
    Long somarQuantidadePorStatus(@Param("status") StatusLoteAves status);

    @Query("select coalesce(sum(l.quantidadeAtual), 0) from LoteAves l where l.instalacaoAtual.id = :instalacaoId and l.status = :status and (:ignorarLoteId is null or l.id <> :ignorarLoteId)")
    Long somarOcupacao(@Param("instalacaoId") Long instalacaoId, @Param("status") StatusLoteAves status,
            @Param("ignorarLoteId") Long ignorarLoteId);

    boolean existsByInstalacaoAtualIdAndStatus(Long instalacaoId, StatusLoteAves status);
}
