package com.example.sitiopro.criacao.suinos.repository;

import com.example.sitiopro.criacao.suinos.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface CicloReprodutivoSuinosRepository extends JpaRepository<CicloReprodutivoSuinos, Long> {
    Optional<CicloReprodutivoSuinos> findByChaveIdempotencia(String chave);
    Optional<CicloReprodutivoSuinos> findByChaveParto(String chave);
    Optional<CicloReprodutivoSuinos> findByChaveDesmame(String chave);
    @EntityGraph(attributePaths = {"matriz", "matriz.lote", "reprodutor", "reprodutor.lote", "loteLeitoes"})
    List<CicloReprodutivoSuinos> findAllByOrderByDataCoberturaDescIdDesc();
    @EntityGraph(attributePaths = {"matriz", "matriz.lote", "reprodutor", "reprodutor.lote", "loteLeitoes"})
    List<CicloReprodutivoSuinos> findByMatrizIdOrderByDataCoberturaDescIdDesc(Long matrizId);
    @EntityGraph(attributePaths = {"matriz", "matriz.lote", "reprodutor", "reprodutor.lote", "loteLeitoes"})
    @Override Optional<CicloReprodutivoSuinos> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"matriz", "matriz.lote", "reprodutor", "reprodutor.lote", "loteLeitoes", "loteLeitoes.instalacaoAtual"})
    @Query("select c from CicloReprodutivoSuinos c where c.id=:id")
    Optional<CicloReprodutivoSuinos> buscarParaAtualizacao(@Param("id") Long id);
    boolean existsByMatrizIdAndStatusIn(Long matrizId, Collection<StatusCicloReprodutivoSuinos> statuses);
    long countByStatus(StatusCicloReprodutivoSuinos status);
    long countByStatusAndDataPrevistaPartoBetween(StatusCicloReprodutivoSuinos status, LocalDate inicio, LocalDate fim);
    @EntityGraph(attributePaths = {"matriz", "matriz.lote"})
    List<CicloReprodutivoSuinos> findByStatusAndDataPrevistaChecagemBefore(
            StatusCicloReprodutivoSuinos status, LocalDate data);
    @EntityGraph(attributePaths = {"matriz", "matriz.lote"})
    List<CicloReprodutivoSuinos> findByStatusAndDataPrevistaPartoBefore(
            StatusCicloReprodutivoSuinos status, LocalDate data);
}
