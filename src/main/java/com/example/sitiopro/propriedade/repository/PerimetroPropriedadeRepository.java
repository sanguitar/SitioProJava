package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.PerimetroPropriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerimetroPropriedadeRepository extends JpaRepository<PerimetroPropriedade, Long> {
    Optional<PerimetroPropriedade> findByPropriedadeId(Long propriedadeId);
}
