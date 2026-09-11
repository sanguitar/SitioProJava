package com.example.sitiopro.agricultura.repository;
import com.example.sitiopro.agricultura.entity.OcorrenciaCultivo;
import com.example.sitiopro.agricultura.entity.SeveridadeOcorrencia;
import com.example.sitiopro.agricultura.entity.StatusOcorrenciaCultivo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface OcorrenciaCultivoRepository extends JpaRepository<OcorrenciaCultivo, Long> {
    @EntityGraph(attributePaths = "referenciasAgrofit")
    List<OcorrenciaCultivo> findByCultivoIdOrderByDataHoraDescIdDesc(Long cultivoId);
    @EntityGraph(attributePaths = "referenciasAgrofit")
    Page<OcorrenciaCultivo> findByCultivoPropriedadeIdOrderByDataHoraDescIdDesc(Long propriedadeId, Pageable pagina);
    @EntityGraph(attributePaths = "referenciasAgrofit")
    Optional<OcorrenciaCultivo> findByCultivoIdAndChaveIdempotencia(Long cultivoId, String chaveIdempotencia);
    @EntityGraph(attributePaths = "referenciasAgrofit")
    Optional<OcorrenciaCultivo> findByIdAndCultivoPropriedadeId(Long id, Long propriedadeId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "referenciasAgrofit")
    @Query("select distinct o from OcorrenciaCultivo o where o.id = :id and o.cultivo.propriedade.id = :propriedadeId")
    Optional<OcorrenciaCultivo> buscarParaAtualizacao(@Param("id") Long id,
            @Param("propriedadeId") Long propriedadeId);
    @EntityGraph(attributePaths = "referenciasAgrofit")
    List<OcorrenciaCultivo> findByStatusInAndSeveridadeInOrderById(
            Collection<StatusOcorrenciaCultivo> status, Collection<SeveridadeOcorrencia> severidades);
    long countByCultivoPropriedadeIdAndStatusIn(Long propriedadeId,
            Collection<StatusOcorrenciaCultivo> status);
    long countByCultivoPropriedadeIdAndStatusInAndSeveridadeIn(Long propriedadeId,
            Collection<StatusOcorrenciaCultivo> status, Collection<SeveridadeOcorrencia> severidades);
    @Query("select count(distinct o.cultivo.id) from OcorrenciaCultivo o where o.cultivo.propriedade.id = :propriedadeId and o.status in :status")
    long contarCultivosAfetados(@Param("propriedadeId") Long propriedadeId,
            @Param("status") Collection<StatusOcorrenciaCultivo> status);
}
