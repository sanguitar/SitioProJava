package com.example.sitiopro.tarefas.entity;

public enum TipoAlerta {
    ESTOQUE_ABAIXO_MINIMO("Estoque abaixo do mínimo"),
    LOTE_PROXIMO_VENCIMENTO("Lote próximo do vencimento"),
    LOTE_VENCIDO("Lote vencido"),
    INTEGRACAO_DESATUALIZADA("Integração desatualizada"),
    INTEGRACAO_COM_FALHA("Integração com falha"),
    CHUVA_INTENSA_24H("Chuva intensa nas próximas 24h"),
    CRIACAO_MORTALIDADE_ALTA("Mortalidade elevada no lote"),
    CRIACAO_INCUBACAO_ECLOSAO_PROXIMA("Eclosão próxima"),
    CRIACAO_INCUBACAO_ATRASADA("Incubação atrasada"),
    CRIACAO_SUINOS_CHECAGEM_PENDENTE("Checagem de gestação suína pendente"),
    CRIACAO_SUINOS_PARTO_ATRASADO("Parto suíno previsto em atraso"),
    CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO("Procedimento sanitário suíno vencido"),
    CRIACAO_PEIXES_QUALIDADE_AGUA("Qualidade da água fora dos limites"),
    CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE("Ação sanitária de peixes pendente"),
    AGRICULTURA_OCORRENCIA_FITOSSANITARIA("Ocorrência fitossanitária relevante");

    private final String rotulo;

    TipoAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
