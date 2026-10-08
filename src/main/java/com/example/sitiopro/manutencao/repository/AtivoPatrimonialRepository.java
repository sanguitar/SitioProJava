package com.example.sitiopro.manutencao.repository;

import com.example.sitiopro.manutencao.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AtivoPatrimonialRepository extends JpaRepository<AtivoPatrimonial, Long> {
    Optional<AtivoPatrimonial> findByChaveIdempotencia(String chave);
    List<AtivoPatrimonial> findAllByOrderByNomeAsc();
    long countByStatusIn(Collection<StatusAtivoPatrimonial> status);
    long countByStatus(StatusAtivoPatrimonial status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AtivoPatrimonial a where a.id=:id")
    Optional<AtivoPatrimonial> buscarParaAtualizacao(@Param("id") Long id);
}
