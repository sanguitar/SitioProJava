package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.Propriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PropriedadeRepository extends JpaRepository<Propriedade, Long> {
    Optional<Propriedade> findByPrincipalTrue();

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Propriedade p where p.principal = true")
    Optional<Propriedade> bloquearPrincipal();
}
