package com.example.sitiopro.agricultura.repository;
import com.example.sitiopro.agricultura.entity.IrrigacaoCultivo;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IrrigacaoCultivoRepository extends JpaRepository<IrrigacaoCultivo, Long> {
    List<IrrigacaoCultivo> findByCultivoIdOrderByDataHoraDescIdDesc(Long cultivoId);
    Page<IrrigacaoCultivo> findByCultivoPropriedadeIdOrderByDataHoraDescIdDesc(Long propriedadeId, Pageable pagina);
    Optional<IrrigacaoCultivo> findByCultivoIdAndChaveIdempotencia(Long cultivoId, String chaveIdempotencia);
}
