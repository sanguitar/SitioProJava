package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.entity.TipoEventoLoteAves;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.TarefaResumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LoteAvesDetalhe(
        Long id, String codigo, String nome, EspecieAves especie, FinalidadeLoteAves finalidade,
        String linhagem, String origem, LocalDate dataEntrada, LocalDate dataNascimento, Long idadeDias,
        int quantidadeInicial, int quantidadeAtual, SexoLoteAves sexo,
        Long instalacaoId, String instalacaoNome, StatusLoteAves status, String observacoes,
        BigDecimal custoInicial, BigDecimal custoAlimentacao, BigDecimal custoConhecido,
        IndicadoresAlimentacao indicadoresAlimentacao, IndicadoresPostura indicadoresPostura,
        PesagemResumo ultimaPesagem, List<AlimentacaoResumo> alimentacoes,
        List<MortalidadeResumo> mortalidades, List<PesagemResumo> pesagens,
        List<PosturaResumo> posturas, List<TransferenciaResumo> transferencias,
        List<EventoResumo> historico, List<AlertaResumo> alertas, List<TarefaResumo> tarefas,
        long versao, LocalDateTime criadoEm, String criadoPor, LocalDateTime alteradoEm, String alteradoPor) {

    public record IndicadoresAlimentacao(List<ConsumoItemResumo> itens, BigDecimal custoAcumulado) {}
    public record ConsumoItemResumo(Long itemId, String itemNome, String unidade, BigDecimal consumoAcumulado,
            BigDecimal consumoMedioDiario, BigDecimal consumoPorAve, BigDecimal saldoAtual,
            BigDecimal diasAutonomiaEstimada) {}
    public record IndicadoresPostura(long hoje, long ultimos7Dias, long ultimos30Dias,
            BigDecimal mediaDiaria30Dias, BigDecimal ovosPorAveHoje) {}
    public record AlimentacaoResumo(Long id, Long itemId, String itemNome, BigDecimal quantidade, String unidade,
            Long localId, String localNome, LocalDateTime dataEvento, Long movimentoEstoqueId,
            BigDecimal custoConhecido, String observacao) {}
    public record MortalidadeResumo(Long id, int quantidade, LocalDateTime dataEvento, String causa, String observacao) {}
    public record PesagemResumo(Long id, LocalDateTime dataEvento, Integer quantidadeAmostrada,
            BigDecimal pesoMedio, BigDecimal pesoMinimo, BigDecimal pesoMaximo, String observacao) {}
    public record PosturaResumo(Long id, LocalDate dataColeta, int ovosInteiros, int ovosQuebrados,
            int ovosDescartados, String observacao) {}
    public record TransferenciaResumo(Long id, Long origemId, String origemNome, Long destinoId,
            String destinoNome, LocalDateTime dataEvento, String usuario, String observacao) {}
    public record EventoResumo(Long id, TipoEventoLoteAves tipo, String tipoRotulo, Integer quantidade,
            LocalDateTime dataEvento, String origem, String usuario, String observacao,
            String referenciaExterna, String instalacaoOrigem, String instalacaoDestino) {}
}
