package com.example.demo.Repository;

import com.example.demo.Model.Abastecimento;
import com.example.demo.Model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    // Busca todos os abastecimentos de um veículo específico
    List<Abastecimento> findByVeiculoOrderByDataDesc(Veiculo veiculo);

    // Soma total gasta com combustível (Mágica do JPQL para o seu relatório)
    @Query("SELECT SUM(a.valorTotal) FROM Abastecimento a")
    Double somarTotalGasto();
}