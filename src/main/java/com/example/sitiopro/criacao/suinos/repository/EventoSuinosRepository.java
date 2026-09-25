package com.example.sitiopro.criacao.suinos.repository;
import com.example.sitiopro.criacao.suinos.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
public interface EventoSuinosRepository extends JpaRepository<EventoSuinos,Long> {
    Optional<EventoSuinos> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths={"instalacaoOrigem","instalacaoDestino","itemEstoque","localEstoque","movimentoEstoque"})
    List<EventoSuinos> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);
    @Query("select coalesce(sum(e.valorDecimal),0) from EventoSuinos e where e.tipo=:tipo and e.dataEvento>=:inicio")
    BigDecimal somarValorDesde(@Param("tipo") TipoEventoSuinos tipo,@Param("inicio") LocalDateTime inicio);
    @Query("select coalesce(sum(e.quantidade),0) from EventoSuinos e where e.tipo in :tipos and e.dataEvento>=:inicio")
    Long somarQuantidadeDesde(@Param("tipos") Collection<TipoEventoSuinos> tipos,@Param("inicio") LocalDateTime inicio);
}
