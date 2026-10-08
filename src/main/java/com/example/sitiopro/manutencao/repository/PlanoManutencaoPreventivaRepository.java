package com.example.sitiopro.manutencao.repository;

import com.example.sitiopro.manutencao.entity.PlanoManutencaoPreventiva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PlanoManutencaoPreventivaRepository extends JpaRepository<PlanoManutencaoPreventiva, Long> {
    Optional<PlanoManutencaoPreventiva> findByChaveIdempotencia(String chave);
    List<PlanoManutencaoPreventiva> findByAtivoPatrimonialIdOrderByAtivoDescNomeAsc(Long ativoId);
    List<PlanoManutencaoPreventiva> findByAtivoTrueOrderByProximaDataAscNomeAsc();
    long countByAtivoTrue();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlanoManutencaoPreventiva p where p.id=:id")
    Optional<PlanoManutencaoPreventiva> buscarParaAtualizacao(@Param("id") Long id);
}
