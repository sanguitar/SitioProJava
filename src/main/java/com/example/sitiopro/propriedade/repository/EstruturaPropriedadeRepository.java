package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.EstruturaPropriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface EstruturaPropriedadeRepository extends JpaRepository<EstruturaPropriedade, Long> {
    Page<EstruturaPropriedade> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId, Pageable pageable);
    Optional<EstruturaPropriedade> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    long countByPropriedadeId(Long propriedadeId);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
    List<EstruturaPropriedade> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId);
}
