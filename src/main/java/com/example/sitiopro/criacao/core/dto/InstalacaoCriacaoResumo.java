package com.example.sitiopro.criacao.core.dto;

import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;

import java.time.LocalDateTime;

public record InstalacaoCriacaoResumo(Long id, String nome, TipoInstalacaoCriacao tipo, String tipoRotulo,
        String descricao, Integer capacidade, long ocupacao, boolean ativo, long versao,
        LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor,
        Long estruturaId, String estruturaNome) {
    public InstalacaoCriacaoResumo(Long id, String nome, TipoInstalacaoCriacao tipo, String tipoRotulo,
            String descricao, Integer capacidade, long ocupacao, boolean ativo, long versao,
            LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor) {
        this(id, nome, tipo, tipoRotulo, descricao, capacidade, ocupacao, ativo, versao,
                criadoEm, criadoPor, alteradoEm, alteradoPor, null, null);
    }
}
