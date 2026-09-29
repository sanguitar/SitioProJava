package com.example.sitiopro.criacao.peixes.repository;
import com.example.sitiopro.criacao.peixes.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;
public interface LotePeixesRepository extends JpaRepository<LotePeixes,Long> {
    Optional<LotePeixes> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths="instalacaoAtual") @Override Optional<LotePeixes> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @EntityGraph(attributePaths="instalacaoAtual") @Query("select l from LotePeixes l where l.id=:id") Optional<LotePeixes> buscarParaAtualizacao(@Param("id") Long id);
    @EntityGraph(attributePaths="instalacaoAtual") @Query("select l from LotePeixes l where (:status is null or l.status=:status) and (:termo is null or lower(l.codigo) like lower(concat('%',:termo,'%')) or lower(l.especie) like lower(concat('%',:termo,'%')) or lower(l.origem) like lower(concat('%',:termo,'%'))) order by case when l.status=com.example.sitiopro.criacao.peixes.entity.StatusLotePeixes.ATIVO then 0 else 1 end,l.dataEntrada desc,l.id desc") Page<LotePeixes> buscar(@Param("status") StatusLotePeixes status,@Param("termo") String termo,Pageable pageable);
    @EntityGraph(attributePaths="instalacaoAtual") List<LotePeixes> findByStatusOrderByCodigoAsc(StatusLotePeixes status);
    @Query("select coalesce(sum(l.quantidadeAtual),0) from LotePeixes l where l.status=:status") Long somarQuantidade(@Param("status") StatusLotePeixes status);
    @Query("select avg(l.pesoMedio) from LotePeixes l where l.status=:status and l.pesoMedio is not null") BigDecimal pesoMedio(@Param("status") StatusLotePeixes status);
    @Query("select coalesce(sum(l.quantidadeAtual*l.pesoMedio),0) from LotePeixes l where l.status=:status and l.pesoMedio is not null") BigDecimal somarBiomassa(@Param("status") StatusLotePeixes status);
    @Query("select coalesce(sum(l.quantidadeAtual),0) from LotePeixes l where l.instalacaoAtual.id=:instalacaoId and l.status=:status and (:ignorarLoteId is null or l.id<>:ignorarLoteId)") Long somarOcupacao(@Param("instalacaoId") Long instalacaoId,@Param("status") StatusLotePeixes status,@Param("ignorarLoteId") Long ignorarLoteId);
}
