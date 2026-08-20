package com.example.sitiopro.frota.service;

import com.example.sitiopro.frota.model.Veiculo;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public Veiculo salvar(Veiculo veiculo) {
        if (veiculo.getNome() != null) {
            veiculo.setNome(veiculo.getNome().toUpperCase());
        }
        if (!StringUtils.hasText(veiculo.getSituacao())) {
            veiculo.setSituacao("DISPONIVEL");
        }
        if (!StringUtils.hasText(veiculo.getIcone())) {
            veiculo.setIcone(iconePorTipo(veiculo.getTipo()));
        }
        return veiculoRepository.save(veiculo);
    }

    public Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado: " + id));
    }

    private String iconePorTipo(String tipo) {
        if ("2".equals(tipo)) {
            return "fa-motorcycle";
        }
        if ("3".equals(tipo)) {
            return "fa-truck";
        }
        return "fa-car";
    }
}
