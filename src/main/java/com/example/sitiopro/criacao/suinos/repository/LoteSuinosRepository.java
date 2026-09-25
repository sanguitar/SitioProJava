package com.example.sitiopro.criacao.suinos.repository;
import com.example.sitiopro.criacao.suinos.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface LoteSuinosRepository extends JpaRepository<LoteSuinos,Long> {
    Optional<LoteSuinos> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths="instalacaoAtual") @Override Optional<LoteSuinos> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @EntityGraph(attributePaths="instalacaoAtual")
    @Query("select l from LoteSuinos l where l.id=:id") Optional<LoteSuinos> buscarParaAtualizacao(@Param("id") Long id);
    @EntityGraph(attributePaths="instalacaoAtual")
    @Query("select l from LoteSuinos l where (:status is null or l.status=:status) and (:termo is null or lower(l.codigo) like lower(concat('%',:termo,'%')) or lower(l.origem) like lower(concat('%',:termo,'%'))) order by case when l.status=com.example.sitiopro.criacao.suinos.entity.StatusLoteSuinos.ATIVO then 0 else 1 end, l.dataEntrada desc, l.id desc")
    Page<LoteSuinos> buscar(@Param("status") StatusLoteSuinos status,@Param("termo") String termo,Pageable pageable);
    @EntityGraph(attributePaths="instalacaoAtual") List<LoteSuinos> findByStatusOrderByCodigoAsc(StatusLoteSuinos status);
    long countByStatus(StatusLoteSuinos status);
    @Query("select coalesce(sum(l.quantidadeAtual),0) from LoteSuinos l where l.status=:status") Long somarQuantidade(@Param("status") StatusLoteSuinos status);
    @Query("select avg(l.pesoMedio) from LoteSuinos l where l.status=:status and l.pesoMedio is not null") java.math.BigDecimal pesoMedio(@Param("status") StatusLoteSuinos status);
    @Query("select coalesce(sum(l.quantidadeAtual),0) from LoteSuinos l where l.instalacaoAtual.id=:instalacaoId and l.status=:status and (:ignorarLoteId is null or l.id<>:ignorarLoteId)")
    Long somarOcupacao(@Param("instalacaoId") Long instalacaoId,@Param("status") StatusLoteSuinos status,@Param("ignorarLoteId") Long ignorarLoteId);
}
