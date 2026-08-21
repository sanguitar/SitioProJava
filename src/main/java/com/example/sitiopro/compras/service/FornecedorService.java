package com.example.sitiopro.compras.service;

import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.entity.Fornecedor;
import com.example.sitiopro.compras.repository.FornecedorRepository;
import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class FornecedorService {

    private static final Logger log = LoggerFactory.getLogger(FornecedorService.class);

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional(readOnly = true)
    public List<FornecedorResumo> listarTodos() {
        return fornecedorRepository.findAllByOrderByNomeAsc().stream()
                .map(this::paraResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FornecedorResumo> listarAtivos() {
        return fornecedorRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(this::paraResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return fornecedorRepository.countByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public FornecedorResumo detalhar(Long id) {
        return paraResumo(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public FornecedorRequest formulario(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        FornecedorRequest request = new FornecedorRequest();
        request.setNome(fornecedor.getNome());
        request.setDocumento(fornecedor.getDocumento());
        request.setTelefone(fornecedor.getTelefone());
        request.setEmail(fornecedor.getEmail());
        request.setObservacao(fornecedor.getObservacao());
        request.setAtivo(fornecedor.isAtivo());
        return request;
    }

    @Transactional
    public FornecedorResumo criar(FornecedorRequest request) {
        String nome = normalizarNome(request.getNome());
        if (fornecedorRepository.existsByNomeIgnoreCase(nome)) {
            throw new ComprasOperacaoException("FORNECEDOR_DUPLICADO",
                    "Já existe fornecedor com esse nome.");
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        fornecedor.setDocumento(normalizarTextoOpcional(request.getDocumento()));
        fornecedor.setTelefone(normalizarTextoOpcional(request.getTelefone()));
        fornecedor.setEmail(normalizarTextoOpcional(request.getEmail()));
        fornecedor.setObservacao(normalizarTextoOpcional(request.getObservacao()));
        fornecedor.setAtivo(request.isAtivo());

        Fornecedor salvo = fornecedorRepository.save(fornecedor);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "fornecedor_criado",
                "module", "compras",
                "compras.fornecedor.id", safeId(salvo.getId())))) {
            log.info("Fornecedor criado.");
        }
        return paraResumo(salvo);
    }

    @Transactional
    public FornecedorResumo atualizar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = buscarPorId(id);
        String nome = normalizarNome(request.getNome());
        if (fornecedorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ComprasOperacaoException("FORNECEDOR_DUPLICADO",
                    "Já existe fornecedor com esse nome.");
        }

        fornecedor.setNome(nome);
        fornecedor.setDocumento(normalizarTextoOpcional(request.getDocumento()));
        fornecedor.setTelefone(normalizarTextoOpcional(request.getTelefone()));
        fornecedor.setEmail(normalizarTextoOpcional(request.getEmail()));
        fornecedor.setObservacao(normalizarTextoOpcional(request.getObservacao()));
        fornecedor.setAtivo(request.isAtivo());

        Fornecedor salvo = fornecedorRepository.save(fornecedor);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "fornecedor_atualizado",
                "module", "compras",
                "compras.fornecedor.id", safeId(salvo.getId())))) {
            log.info("Fornecedor atualizado.");
        }
        return paraResumo(salvo);
    }

    @Transactional(readOnly = true)
    public Fornecedor buscarAtivoPorId(Long id) {
        return buscarPorId(id);
    }

    private Fornecedor buscarPorId(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ComprasOperacaoException("FORNECEDOR_NAO_ENCONTRADO",
                        "Fornecedor não encontrado.", HttpStatus.NOT_FOUND));
    }

    public FornecedorResumo paraResumo(Fornecedor fornecedor) {
        return new FornecedorResumo(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getDocumento(),
                fornecedor.getTelefone(),
                fornecedor.getEmail(),
                fornecedor.isAtivo());
    }

    private String normalizarNome(String valor) {
        if (!StringUtils.hasText(valor)) {
            throw new ComprasOperacaoException("FORNECEDOR_NOME_OBRIGATORIO", "Informe o nome do fornecedor.");
        }
        return valor.trim();
    }

    private String normalizarTextoOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private String safeId(Long id) {
        return id == null ? "unknown" : String.valueOf(id);
    }
}
