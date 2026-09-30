package com.example.sitiopro.criacao.peixes.repository;
import com.example.sitiopro.criacao.peixes.entity.MedicaoQualidadeAgua;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MedicaoQualidadeAguaRepository extends JpaRepository<MedicaoQualidadeAgua,Long>{
    Optional<MedicaoQualidadeAgua> findByChaveIdempotencia(String chave);
    Optional<MedicaoQualidadeAgua> findFirstByLoteIdOrderByMedidoEmDescIdDesc(Long loteId);
    List<MedicaoQualidadeAgua> findByLoteIdOrderByMedidoEmDescIdDesc(Long loteId);
}
