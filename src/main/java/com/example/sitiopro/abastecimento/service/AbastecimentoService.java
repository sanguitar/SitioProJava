package com.example.sitiopro.abastecimento.service;

import com.example.sitiopro.abastecimento.dto.*;
import com.example.sitiopro.abastecimento.model.Abastecimento;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.frota.dto.*;
import com.example.sitiopro.frota.model.*;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.manutencao.dto.LeituraMedidorRequest;
import com.example.sitiopro.manutencao.service.ManutencaoPreventivaService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class AbastecimentoService {
    private final AbastecimentoRepository abastecimentos;
    private final VeiculoService veiculos;
    private final CodigoCriacaoService codigos;
    private final ManutencaoPreventivaService manutencao;
    private final Clock clock;

    public AbastecimentoService(AbastecimentoRepository abastecimentos, VeiculoService veiculos,
            CodigoCriacaoService codigos, ManutencaoPreventivaService manutencao, Clock clock) {
        this.abastecimentos = abastecimentos; this.veiculos = veiculos; this.codigos = codigos;
        this.manutencao = manutencao; this.clock = clock;
    }

    @Transactional
    public AbastecimentoResumo registrar(AbastecimentoRequest request, UsuarioAtor ator) {
        validarRequest(request);
        String chave = request.getChaveIdempotencia().trim();
        codigos.bloquearIdempotencia("FROTA_ABASTECIMENTO", chave);
        Abastecimento existente = abastecimentos.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            validarRetry(existente, request);
            return resumo(existente);
        }

        Veiculo veiculo = veiculos.buscarParaAtualizacao(request.getVeiculoId());
        validarMedidor(veiculo, request);
        Abastecimento abastecimento = new Abastecimento();
        abastecimento.setVeiculo(veiculo); abastecimento.setData(request.getData());
        abastecimento.setLitros(escala(request.getLitros(), 3));
        abastecimento.setPrecoPorLitro(escala(request.getPrecoPorLitro(), 4));
        abastecimento.setKmNoAto(escala(request.getKmNoAto(), 2));
        abastecimento.setHorimetroNoAto(escala(request.getHorimetroNoAto(), 2));
        abastecimento.setLocal(request.getLocal().trim()); abastecimento.setChaveIdempotencia(chave);
        abastecimento.calcularTotal();
        abastecimento = abastecimentos.saveAndFlush(abastecimento);

        atualizarMedidor(veiculo, request);
        sincronizarManutencao(veiculo, request, ator, chave);
        abastecimentos.flush();
        return resumo(abastecimento);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AbastecimentoResumo> listar(Long veiculoId, int pagina, int tamanho) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        var pageable = PageRequest.of(paginaSegura, tamanhoSeguro);
        var page = veiculoId == null
                ? abastecimentos.findAllByOrderByDataDescIdDesc(pageable)
                : abastecimentos.findByVeiculoIdOrderByDataDescIdDesc(veiculoId, pageable);
        return PaginaResponse.de(page.map(this::resumo));
    }

    @Transactional(readOnly = true)
    public AbastecimentoResumo detalhar(Long id) {
        return resumo(abastecimentos.findById(id).orElseThrow(() -> ausente(id)));
    }

    @Transactional(readOnly = true)
    public List<VeiculoOperacionalResumo> resumoFrota() {
        return veiculos.listarTodos().stream().map(this::resumoOperacional).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoOperacionalResumo resumoOperacional(Long veiculoId) {
        return resumoOperacional(veiculos.detalhar(veiculoId));
    }

    private VeiculoOperacionalResumo resumoOperacional(VeiculoResumo veiculo) {
        List<Abastecimento> historico = abastecimentos.findByVeiculoIdOrderByDataAscIdAsc(veiculo.id());
        BigDecimal litros = historico.stream().map(Abastecimento::getLitros).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(3, RoundingMode.HALF_UP);
        BigDecimal custo = historico.stream().map(Abastecimento::getValorTotal).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal consumo = calcularConsumoMedio(historico, veiculo.tipoMedidor());
        return new VeiculoOperacionalResumo(veiculo, historico.size(), litros, custo, consumo,
                veiculo.tipoMedidor().getUnidade() + "/l");
    }

    private BigDecimal calcularConsumoMedio(List<Abastecimento> historico, TipoMedidorVeiculo tipo) {
        BigDecimal leituraAnterior = null;
        BigDecimal deslocamento = BigDecimal.ZERO;
        BigDecimal litrosIntervalos = BigDecimal.ZERO;
        for (Abastecimento atual : historico) {
            BigDecimal leitura = tipo == TipoMedidorVeiculo.HORIMETRO
                    ? atual.getHorimetroNoAto() : atual.getKmNoAto();
            if (leitura == null) continue;
            if (leituraAnterior != null && leitura.compareTo(leituraAnterior) >= 0) {
                deslocamento = deslocamento.add(leitura.subtract(leituraAnterior));
                litrosIntervalos = litrosIntervalos.add(atual.getLitros());
            }
            leituraAnterior = leitura;
        }
        if (litrosIntervalos.signum() == 0) return null;
        return deslocamento.divide(litrosIntervalos, 2, RoundingMode.HALF_UP);
    }

    private void validarRequest(AbastecimentoRequest r) {
        if (r.getVeiculoId() == null) throw erro("VEICULO_OBRIGATORIO", "Informe o veículo.");
        if (r.getData() == null || r.getData().isAfter(LocalDate.now(clock)))
            throw erro("DATA_INVALIDA", "A data é obrigatória e não pode estar no futuro.");
        if (r.getLitros() == null || r.getLitros().signum() <= 0) throw erro("LITROS_INVALIDOS", "Informe uma quantidade de litros maior que zero.");
        if (r.getPrecoPorLitro() == null || r.getPrecoPorLitro().signum() <= 0) throw erro("PRECO_INVALIDO", "Informe um preço por litro maior que zero.");
        if (!StringUtils.hasText(r.getLocal())) throw erro("LOCAL_OBRIGATORIO", "Informe o local do abastecimento.");
        if (!StringUtils.hasText(r.getChaveIdempotencia())) throw erro("IDEMPOTENCIA_OBRIGATORIA", "Informe a chave de idempotência.");
    }

    private void validarMedidor(Veiculo v, AbastecimentoRequest r) {
        boolean quilometragem = v.getTipoMedidor() == TipoMedidorVeiculo.QUILOMETRAGEM;
        BigDecimal leitura = quilometragem ? r.getKmNoAto() : r.getHorimetroNoAto();
        BigDecimal leituraIncorreta = quilometragem ? r.getHorimetroNoAto() : r.getKmNoAto();
        BigDecimal atual = quilometragem ? v.getKmAtual() : v.getHorimetroAtual();
        if (leitura == null || leituraIncorreta != null)
            throw erro("MEDIDOR_INCOMPATIVEL", "Informe somente a leitura de " + v.getTipoMedidor().getRotulo().toLowerCase(Locale.ROOT) + ".");
        if (leitura.signum() < 0) throw erro("LEITURA_INVALIDA", "A leitura do medidor não pode ser negativa.");
        if (atual != null && leitura.compareTo(atual) < 0)
            throw erro("LEITURA_REGRESSIVA", "A leitura não pode regredir. Use o ajuste administrativo em Manutenção para correções.", HttpStatus.CONFLICT);
    }

    private void atualizarMedidor(Veiculo v, AbastecimentoRequest r) {
        if (v.getTipoMedidor() == TipoMedidorVeiculo.HORIMETRO) v.setHorimetroAtual(escala(r.getHorimetroNoAto(), 2));
        else v.setKmAtual(escala(r.getKmNoAto(), 2));
    }

    private void sincronizarManutencao(Veiculo v, AbastecimentoRequest r, UsuarioAtor ator, String chave) {
        if (v.getAtivoPatrimonial() == null) return;
        LeituraMedidorRequest leitura = new LeituraMedidorRequest();
        leitura.setAtivoId(v.getAtivoPatrimonial().getId());
        leitura.setDataLeitura(dataLeitura(r.getData()));
        leitura.setQuilometragem(escala(r.getKmNoAto(), 2));
        leitura.setHorimetro(escala(r.getHorimetroNoAto(), 2));
        leitura.setChaveIdempotencia("ABASTECIMENTO:" + chave);
        manutencao.registrarLeitura(leitura, ator);
    }

    private LocalDateTime dataLeitura(LocalDate data) {
        LocalDate hoje = LocalDate.now(clock);
        return data.equals(hoje) ? LocalDateTime.now(clock).withNano(0) : data.atTime(12, 0);
    }

    private void validarRetry(Abastecimento a, AbastecimentoRequest r) {
        if (!Objects.equals(a.getVeiculo().getId(), r.getVeiculoId()) || !Objects.equals(a.getData(), r.getData())
                || diferente(a.getLitros(), escala(r.getLitros(), 3))
                || diferente(a.getPrecoPorLitro(), escala(r.getPrecoPorLitro(), 4))
                || diferente(a.getKmNoAto(), escala(r.getKmNoAto(), 2))
                || diferente(a.getHorimetroNoAto(), escala(r.getHorimetroNoAto(), 2))
                || !Objects.equals(a.getLocal(), r.getLocal().trim()))
            throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já foi usada por outro abastecimento.", HttpStatus.CONFLICT);
    }

    private boolean diferente(BigDecimal a, BigDecimal b) { return a == null ? b != null : b == null || a.compareTo(b) != 0; }
    private BigDecimal escala(BigDecimal v, int escala) { return v == null ? null : v.setScale(escala, RoundingMode.HALF_UP); }
    private AbastecimentoResumo resumo(Abastecimento a) {
        return new AbastecimentoResumo(a.getId(), a.getVeiculo().getId(), a.getVeiculo().getNome(), a.getData(),
                a.getLitros(), a.getPrecoPorLitro(), a.getValorTotal(), a.getKmNoAto(), a.getHorimetroNoAto(),
                a.getVeiculo().getTipoMedidor(), a.getLocal(), a.getVersao(), a.getCriadoEm(), a.getCriadoPor());
    }
    private AbastecimentoOperacaoException ausente(Long id) { return erro("ABASTECIMENTO_NAO_ENCONTRADO", "Abastecimento não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private AbastecimentoOperacaoException erro(String c, String m) { return new AbastecimentoOperacaoException(c, m); }
    private AbastecimentoOperacaoException erro(String c, String m, HttpStatus s) { return new AbastecimentoOperacaoException(c, m, s); }
}
