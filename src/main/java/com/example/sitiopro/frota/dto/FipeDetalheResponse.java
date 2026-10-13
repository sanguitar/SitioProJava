package com.example.sitiopro.frota.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record FipeDetalheResponse(Integer id, String anoModelo, BigDecimal valor, JsonNode historico) {
}
