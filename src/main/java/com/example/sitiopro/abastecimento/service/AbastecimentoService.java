package com.example.sitiopro.abastecimento.service;

import com.example.sitiopro.abastecimento.model.Abastecimento;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.frota.model.Veiculo;
import com.example.sitiopro.frota.service.VeiculoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AbastecimentoService {

    private final AbastecimentoRepository abastecimentoRepository;
    private final VeiculoService veiculoService;

    public AbastecimentoService(AbastecimentoRepository abastecimentoRepository, VeiculoService veiculoService) {
        this.abastecimentoRepository = abastecimentoRepository;
        this.veiculoService = veiculoService;
    }

    @Transactional
    public Abastecimento registrarAbastecimento(Abastecimento abastecimento) {
        Veiculo veiculo = veiculoService.buscarPorId(abastecimento.getVeiculo().getId());
        Double kmAtual = veiculo.getKmAtual() == null ? 0D : veiculo.getKmAtual();

        if (abastecimento.getKmNoAto() != null && abastecimento.getKmNoAto() > kmAtual) {
            veiculo.setKmAtual(abastecimento.getKmNoAto());
            veiculoService.salvar(veiculo);
        }

        abastecimento.setVeiculo(veiculo);
        return abastecimentoRepository.save(abastecimento);
    }

    public List<Abastecimento> listarTodos() {
        return abastecimentoRepository.findAll();
    }
}
