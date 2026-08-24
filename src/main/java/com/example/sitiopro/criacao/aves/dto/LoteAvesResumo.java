package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;

import java.time.LocalDate;

public record LoteAvesResumo(Long id, String codigo, String nome, EspecieAves especie,
        FinalidadeLoteAves finalidade, int quantidadeAtual, Long instalacaoId, String instalacaoNome,
        StatusLoteAves status, LocalDate dataEntrada) {
}
