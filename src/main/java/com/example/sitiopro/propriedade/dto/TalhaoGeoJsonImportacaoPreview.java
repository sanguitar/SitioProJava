package com.example.sitiopro.propriedade.dto;

import java.math.BigDecimal;
import java.util.List;

public record TalhaoGeoJsonImportacaoPreview(List<Item> itens, int total, int alterados, boolean valido) {
    public TalhaoGeoJsonImportacaoPreview {
        itens = itens == null ? List.of() : List.copyOf(itens);
    }

    public boolean possuiErros() {
        return !valido;
    }

    public record Item(String codigo, Long talhaoId, String nome, BigDecimal areaCadastralHa,
            BigDecimal areaGisAtualM2, BigDecimal areaGisImportadaM2, int verticesAtuais, int verticesImportados,
            boolean alterado, List<String> alteracoes, List<String> erros) {
        public Item {
            alteracoes = alteracoes == null ? List.of() : List.copyOf(alteracoes);
            erros = erros == null ? List.of() : List.copyOf(erros);
        }

        public boolean valido() {
            return erros.isEmpty();
        }
    }
}
