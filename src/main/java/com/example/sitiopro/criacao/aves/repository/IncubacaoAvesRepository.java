package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncubacaoAvesRepository extends JpaRepository<IncubacaoAves, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    Optional<IncubacaoAves> findByChaveIdempotencia(String chave);

    @EntityGraph(attributePaths = {"instalacao", "loteReprodutor", "posturaOrigem", "posturaOrigem.lote", "loteResultante"})
    Page<IncubacaoAves> findAllByOrderByDataInicioDescIdDesc(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"instalacao", "loteReprodutor", "posturaOrigem", "posturaOrigem.lote", "loteResultante"})
    Optional<IncubacaoAves> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"instalacao", "loteReprodutor", "posturaOrigem", "posturaOrigem.lote", "loteResultante"})
    @Query("select i from IncubacaoAves i where i.id = :id")
    Optional<IncubacaoAves> buscarParaAtualizacao(@Param("id") Long id);

    long countByStatus(StatusIncubacaoAves status);
    long countByStatusAndDataPrevistaEclosaoBetween(StatusIncubacaoAves status, LocalDate inicio, LocalDate fim);
    List<IncubacaoAves> findByStatusOrderByDataPrevistaEclosaoAsc(StatusIncubacaoAves status);

    @Query("select coalesce(sum(i.quantidadeOvos), 0) from IncubacaoAves i where i.status = :status")
    Long somarOvosPorStatus(@Param("status") StatusIncubacaoAves status);

    Optional<IncubacaoAves> findFirstByStatusOrderByDataPrevistaEclosaoAscIdAsc(StatusIncubacaoAves status);

    @Query("select coalesce(sum(i.pintinhosEclodidos), 0) from IncubacaoAves i where i.status = :status and i.dataEclosao >= :desde")
    Long somarPintinhosDesde(@Param("status") StatusIncubacaoAves status, @Param("desde") LocalDate desde);
}
