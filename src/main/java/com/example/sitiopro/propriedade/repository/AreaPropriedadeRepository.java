package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.AreaPropriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface AreaPropriedadeRepository extends JpaRepository<AreaPropriedade, Long> {
    Page<AreaPropriedade> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId, Pageable pageable);
    Optional<AreaPropriedade> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    long countByPropriedadeId(Long propriedadeId);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
    List<AreaPropriedade> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId);
}
