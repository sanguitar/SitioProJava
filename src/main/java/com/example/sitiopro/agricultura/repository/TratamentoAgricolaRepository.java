package com.example.sitiopro.agricultura.repository;
import com.example.sitiopro.agricultura.entity.TratamentoAgricola;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TratamentoAgricolaRepository extends JpaRepository<TratamentoAgricola, Long> {
    List<TratamentoAgricola> findByCultivoIdOrderByDataDescIdDesc(Long cultivoId);
    Page<TratamentoAgricola> findByCultivoPropriedadeIdOrderByDataDescIdDesc(Long propriedadeId, Pageable pagina);
    Optional<TratamentoAgricola> findByCultivoIdAndChaveIdempotencia(Long cultivoId, String chaveIdempotencia);
}
