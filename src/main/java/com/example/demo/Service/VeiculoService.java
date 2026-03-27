package com.example.demo.Service;

import com.example.demo.Model.Veiculo;
import com.example.demo.Repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    // Resolve a "zica" do listarTodos
    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    // Resolve a "zica" do salvar com tratamento de erro explícito
    public Veiculo salvar(Veiculo veiculo) {
        try {
            // Lógica Sênior: Garante que o nome esteja em Uppercase para o banco
            if (veiculo.getNome() != null) {
                veiculo.setNome(veiculo.getNome().toUpperCase());
            }
            return veiculoRepository.save(veiculo);
        } catch (Exception e) {
            throw new RuntimeException("Erro de persistência: " + e.getMessage());
        }
    }
}