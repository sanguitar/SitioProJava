package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.Piquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PiqueteRepository extends JpaRepository<Piquete, Long> {
    Page<Piquete> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId, Pageable pageable);
    Optional<Piquete> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    long countByPropriedadeId(Long propriedadeId);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
    List<Piquete> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId);
}
