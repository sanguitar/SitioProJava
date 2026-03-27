package com.example.demo.Service;

import com.example.demo.Model.Abastecimento;
import com.example.demo.Model.Veiculo;
import com.example.demo.Repository.AbastecimentoRepository;
import com.example.demo.Repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AbastecimentoService {

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Transactional
    public Abastecimento registrarAbastecimento(Abastecimento abastecimento) {
        // Validação Sênior: Verifica se o veículo existe
        Veiculo veiculo = veiculoRepository.findById(abastecimento.getVeiculo().getId())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado para o abastecimento"));

        // Atualiza o KM do veículo se o abastecimento trouxer um valor maior
        if (abastecimento.getKmNoAto() != null && abastecimento.getKmNoAto() > veiculo.getKmAtual()) {
            veiculo.setKmAtual(abastecimento.getKmNoAto());
            veiculoRepository.save(veiculo);
        }

        return abastecimentoRepository.save(abastecimento);
    }

    public List<Abastecimento> listarTodos() {
        return abastecimentoRepository.findAll();
    }
}