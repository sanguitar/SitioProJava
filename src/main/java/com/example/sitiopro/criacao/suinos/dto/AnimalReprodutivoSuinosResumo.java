package com.example.sitiopro.criacao.suinos.dto;

import com.example.sitiopro.criacao.suinos.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalReprodutivoSuinosResumo(Long id, String codigo, String identificacao,
        TipoAnimalReprodutivo tipo, StatusAnimalReprodutivo status, Long loteId, String loteCodigo,
        LocalDate dataNascimento, BigDecimal pesoAtual, long versao) {}
