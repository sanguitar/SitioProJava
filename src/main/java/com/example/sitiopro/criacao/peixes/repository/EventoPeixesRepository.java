package com.example.sitiopro.criacao.peixes.repository;
import com.example.sitiopro.criacao.peixes.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
public interface EventoPeixesRepository extends JpaRepository<EventoPeixes,Long> {
    Optional<EventoPeixes> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths={"instalacaoOrigem","instalacaoDestino","itemEstoque","localEstoque","movimentoEstoque"}) List<EventoPeixes> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
    @Query("select coalesce(sum(e.valorDecimal),0) from EventoPeixes e where e.tipo=:tipo and e.dataEvento>=:inicio") BigDecimal somarValorDesde(@Param("tipo") TipoEventoPeixes tipo,@Param("inicio") LocalDateTime inicio);
    @Query("select coalesce(sum(e.quantidade),0) from EventoPeixes e where e.tipo in :tipos and e.dataEvento>=:inicio") Long somarQuantidadeDesde(@Param("tipos") Collection<TipoEventoPeixes> tipos,@Param("inicio") LocalDateTime inicio);
}
