package com.example.sitiopro.frota.dto;

import com.example.sitiopro.frota.model.TipoMedidorVeiculo;
import java.math.BigDecimal;

public record VeiculoResumo(
        Long id, String nome, String placa, String tipo, String marcaFipe, String modeloFipe,
        Integer anoModelo, TipoMedidorVeiculo tipoMedidor, BigDecimal kmAtual,
        BigDecimal horimetroAtual, String situacao, BigDecimal valorFipe,
        String ultimaConsultaFipe, String icone, Long ativoPatrimonialId,
        String ativoPatrimonialCodigo, String ativoPatrimonialNome, long versao) {

    public BigDecimal leituraAtual() {
        return tipoMedidor == TipoMedidorVeiculo.HORIMETRO ? horimetroAtual : kmAtual;
    }
}
