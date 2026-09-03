package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.Talhao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface TalhaoRepository extends JpaRepository<Talhao, Long> {
    Page<Talhao> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId, Pageable pageable);
    Optional<Talhao> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    long countByPropriedadeId(Long propriedadeId);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
    List<Talhao> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId);
}
