package com.example.sitiopro.agricultura.repository;
import com.example.sitiopro.agricultura.entity.AdubacaoCultivo;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AdubacaoCultivoRepository extends JpaRepository<AdubacaoCultivo, Long> {
    List<AdubacaoCultivo> findByCultivoIdOrderByDataDescIdDesc(Long cultivoId);
    Page<AdubacaoCultivo> findByCultivoPropriedadeIdOrderByDataDescIdDesc(Long propriedadeId, Pageable pagina);
    Optional<AdubacaoCultivo> findByCultivoIdAndChaveIdempotencia(Long cultivoId, String chaveIdempotencia);
}
