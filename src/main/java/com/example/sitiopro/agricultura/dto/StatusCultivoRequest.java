package com.example.sitiopro.agricultura.dto;
import com.example.sitiopro.agricultura.entity.StatusCultivo;
import jakarta.validation.constraints.*;
public record StatusCultivoRequest(@NotNull StatusCultivo status, @NotNull @PositiveOrZero Long versao) {}
