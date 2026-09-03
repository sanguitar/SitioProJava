package com.example.sitiopro.propriedade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VersaoRequest(@NotNull @PositiveOrZero Long versao) {
}
