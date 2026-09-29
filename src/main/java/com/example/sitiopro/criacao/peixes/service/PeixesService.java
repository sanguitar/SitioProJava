package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class PeixesService {
    private static final int ESCALA = 4;
    private final LotePeixesRepository lotes;
    private final EventoPeixesRepository eventos;
    private final InstalacaoCriacaoService instalacoes;
    private final CodigoCriacaoService codigos;
    private final EstoqueCatalogoService catalogo;
    private final EstoqueMovimentoService estoque;
    private final TarefaService tarefas;
    private final AlertaService alertas;
    private final Clock clock;

    public PeixesService(LotePeixesRepository lotes, EventoPeixesRepository eventos,
            InstalacaoCriacaoService instalacoes, CodigoCriacaoService codigos,
            EstoqueCatalogoService catalogo, EstoqueMovimentoService estoque,
            TarefaService tarefas, AlertaService alertas, Clock clock) {
        this.lotes=lotes; this.eventos=eventos; this.instalacoes=instalacoes; this.codigos=codigos;
        this.catalogo=catalogo; this.estoque=estoque; this.tarefas=tarefas; this.alertas=alertas; this.clock=clock;
    }

    @Transactional(readOnly=true)
    public PaginaResponse<LotePeixesResumo> listar(StatusLotePeixes status,String termo,int pagina,int tamanho) {
        String busca=StringUtils.hasText(termo)?termo.trim():null;
        return PaginaResponse.de(lotes.buscar(status,busca,PageRequest.of(Math.max(0,pagina),Math.min(100,Math.max(1,tamanho)))).map(this::resumo));
    }

    @Transactional(readOnly=true)
    public LotePeixesDetalhe detalhar(Long id) {
        LotePeixes lote=buscar(id);
        List<EventoPeixes> historico=eventos.findByLoteIdOrderByDataEventoDescIdDesc(id);
        BigDecimal consumo=historico.stream().filter(e->e.getTipo()==TipoEventoPeixes.ALIMENTACAO)
                .map(EventoPeixes::getValorDecimal).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add);
        long mortalidade=historico.stream().filter(e->e.getTipo()==TipoEventoPeixes.MORTALIDADE||e.getTipo()==TipoEventoPeixes.PERDA)
                .map(EventoPeixes::getQuantidade).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
        String referencia=referencia(id);
        return new LotePeixesDetalhe(lote.getId(),lote.getCodigo(),lote.getEspecie(),lote.getQuantidadeInicial(),lote.getQuantidadeAtual(),
                lote.getDataEntrada(),lote.getOrigem(),lote.getPesoMedio(),biomassa(lote),lote.getInstalacaoAtual().getId(),
                lote.getInstalacaoAtual().getNome(),lote.getStatus(),lote.getObservacao(),escala(consumo),mortalidade,
                historico.stream().map(this::evento).toList(),alertas.listarRelacionados(ModuloOrigem.CRIACOES,referencia),
                tarefas.listarRelacionadas(ModuloOrigem.CRIACOES,referencia),lote.getVersao(),lote.getCriadoEm(),
                lote.getCriadoPor(),lote.getAlteradoEm(),lote.getAlteradoPor());
    }

    @Transactional(readOnly=true)
    public PeixesDashboardResumo dashboard() {
        List<LotePeixes> ativos=lotes.findByStatusOrderByCodigoAsc(StatusLotePeixes.ATIVO);
        long tarefasAbertas=ativos.stream().flatMap(l->tarefas.listarRelacionadas(ModuloOrigem.CRIACOES,referencia(l.getId())).stream())
                .filter(t->!t.status().finalizado()).count();
        long alertasAbertos=ativos.stream().mapToLong(l->alertas.listarRelacionados(ModuloOrigem.CRIACOES,referencia(l.getId())).size()).sum();
        LocalDateTime inicio=LocalDateTime.now(clock).minusDays(30);
        return new PeixesDashboardResumo(ativos.size(),Optional.ofNullable(lotes.somarQuantidade(StatusLotePeixes.ATIVO)).orElse(0L),
                escala(lotes.somarBiomassa(StatusLotePeixes.ATIVO)),escala(lotes.pesoMedio(StatusLotePeixes.ATIVO)),
                escala(eventos.somarValorDesde(TipoEventoPeixes.ALIMENTACAO,inicio)),
                Optional.ofNullable(eventos.somarQuantidadeDesde(List.of(TipoEventoPeixes.MORTALIDADE,TipoEventoPeixes.PERDA),inicio)).orElse(0L),
                tarefasAbertas,alertasAbertos);
    }

    @Transactional
    public LotePeixesDetalhe criar(CriarLotePeixesRequest r,UsuarioAtor ator) {
        exigirAdmin(ator); String chave=chave(r.getChaveIdempotencia()); codigos.bloquearIdempotencia("LOTE_PEIXES",chave);
        LotePeixes existente=lotes.findByChaveIdempotencia(chave).orElse(null); if(existente!=null)return detalhar(existente.getId());
        validarCriacao(r); InstalacaoCriacao tanque=instalacoes.reservarCapacidadePeixes(r.getInstalacaoId(),r.getQuantidadeInicial(),null);
        LotePeixes lote=new LotePeixes(); lote.setCodigo(codigos.proximoLotePeixes()); lote.setEspecie(textoObrigatorio(r.getEspecie(),"Espécie"));
        lote.setQuantidadeInicial(r.getQuantidadeInicial()); lote.setQuantidadeAtual(r.getQuantidadeInicial()); lote.setDataEntrada(r.getDataEntrada());
        lote.setOrigem(textoObrigatorio(r.getOrigem(),"Origem")); lote.setPesoMedio(r.getPesoMedio()==null?null:positivo(r.getPesoMedio(),"Peso médio"));
        lote.setInstalacaoAtual(tanque); lote.setStatus(StatusLotePeixes.ATIVO); lote.setObservacao(texto(r.getObservacao())); lote.setChaveIdempotencia(chave);
        lote=lotes.save(lote); novoEvento(lote,TipoEventoPeixes.ENTRADA_INICIAL,r.getQuantidadeInicial(),null,r.getDataEntrada().atStartOfDay(),ator.ator(),r.getObservacao(),chave,null,tanque,null,null);
        return detalhar(lote.getId());
    }

    @Transactional public LotePeixesDetalhe registrarEntrada(Long id,EntradaPeixesRequest r,UsuarioAtor ator) {
        return operar(id,r,ator,(l,e)->{instalacoes.reservarCapacidadePeixes(l.getInstalacaoAtual().getId(),r.getQuantidade(),l.getId());l.setQuantidadeAtual(Math.addExact(l.getQuantidadeAtual(),r.getQuantidade()));e.setTipo(TipoEventoPeixes.ENTRADA_PEIXES);e.setQuantidade(r.getQuantidade());});
    }
    @Transactional public LotePeixesDetalhe registrarPerda(Long id,PerdaPeixesRequest r,UsuarioAtor ator) {
        if(r.getTipo()!=TipoEventoPeixes.MORTALIDADE&&r.getTipo()!=TipoEventoPeixes.PERDA)throw erro("TIPO_PERDA_INVALIDO","Use mortalidade ou perda.");
        return operar(id,r,ator,(l,e)->{if(r.getQuantidade()>l.getQuantidadeAtual())throw erro("QUANTIDADE_INSUFICIENTE","A perda não pode exceder a quantidade atual.");l.setQuantidadeAtual(l.getQuantidadeAtual()-r.getQuantidade());if(l.getQuantidadeAtual()==0)l.setStatus(StatusLotePeixes.ENCERRADO);e.setTipo(r.getTipo());e.setQuantidade(r.getQuantidade());});
    }
    @Transactional public LotePeixesDetalhe registrarBiometria(Long id,BiometriaPeixesRequest r,UsuarioAtor ator) {
        return operar(id,r,ator,(l,e)->{BigDecimal peso=positivo(r.getPesoMedio(),"Peso médio");l.setPesoMedio(peso);e.setTipo(TipoEventoPeixes.BIOMETRIA);e.setValorDecimal(peso);});
    }
    @Transactional public LotePeixesDetalhe transferir(Long id,TransferenciaPeixesRequest r,UsuarioAtor ator) {
        return operar(id,r,ator,(l,e)->{InstalacaoCriacao destino=instalacoes.reservarCapacidadePeixes(r.getInstalacaoDestinoId(),l.getQuantidadeAtual(),l.getId());if(destino.getId().equals(l.getInstalacaoAtual().getId()))throw erro("DESTINO_IGUAL_ORIGEM","Selecione outro tanque.");e.setTipo(TipoEventoPeixes.TRANSFERENCIA);e.setInstalacaoOrigem(l.getInstalacaoAtual());e.setInstalacaoDestino(destino);l.setInstalacaoAtual(destino);});
    }
    @Transactional public LotePeixesDetalhe registrarAlimentacao(Long id,AlimentacaoPeixesRequest r,UsuarioAtor ator) {
        String chave=chave(r.getChaveIdempotencia()); codigos.bloquearIdempotencia("PEIXES_EVENTO",chave);
        EventoPeixes existente=eventos.findByChaveIdempotencia(chave).orElse(null);if(existente!=null)return detalhar(existente.getLote().getId());
        LotePeixes lote=ativo(id);validarData(r.getDataEvento());BigDecimal quantidade=positivo(r.getQuantidade(),"Quantidade");
        ItemEstoque item=catalogo.buscarItem(r.getItemEstoqueId());LocalEstoque local=catalogo.buscarLocalAtivo(r.getLocalEstoqueId());
        EventoPeixes evento=novoEvento(lote,TipoEventoPeixes.ALIMENTACAO,null,quantidade,r.getDataEvento(),ator.ator(),r.getObservacao(),chave,null,null,item,local);
        eventos.flush();MovimentoEstoqueRequest movimento=new MovimentoEstoqueRequest();movimento.setItemId(item.getId());movimento.setQuantidade(quantidade);
        movimento.setLocalOrigemId(local.getId());movimento.setLoteCodigo(texto(r.getLoteEstoqueCodigo()));movimento.setObservacao(texto(r.getObservacao()));movimento.setDataMovimento(r.getDataEvento());
        evento.setMovimentoEstoque(estoque.registrarConsumoCriacaoPeixes(movimento,evento.getId(),id));return detalhar(id);
    }

    private LotePeixesDetalhe operar(Long id,OperacaoPeixesBase r,UsuarioAtor ator,Aplicador aplicador) {
        String chave=chave(r.getChaveIdempotencia());codigos.bloquearIdempotencia("PEIXES_EVENTO",chave);
        EventoPeixes existente=eventos.findByChaveIdempotencia(chave).orElse(null);if(existente!=null)return detalhar(existente.getLote().getId());
        LotePeixes lote=ativo(id);validarData(r.getDataEvento());EventoPeixes evento=baseEvento(lote,r.getDataEvento(),ator.ator(),r.getObservacao(),chave);aplicador.aplicar(lote,evento);eventos.save(evento);return detalhar(id);
    }
    private EventoPeixes novoEvento(LotePeixes lote,TipoEventoPeixes tipo,Integer qtd,BigDecimal valor,LocalDateTime data,String usuario,String obs,String chave,InstalacaoCriacao origem,InstalacaoCriacao destino,ItemEstoque item,LocalEstoque local){EventoPeixes e=baseEvento(lote,data,usuario,obs,chave);e.setTipo(tipo);e.setQuantidade(qtd);e.setValorDecimal(valor);e.setInstalacaoOrigem(origem);e.setInstalacaoDestino(destino);e.setItemEstoque(item);e.setLocalEstoque(local);return eventos.save(e);}
    private EventoPeixes baseEvento(LotePeixes lote,LocalDateTime data,String usuario,String obs,String chave){EventoPeixes e=new EventoPeixes();e.setLote(lote);e.setDataEvento(data);e.setUsuario(usuario);e.setObservacao(texto(obs));e.setChaveIdempotencia(chave);return e;}
    private void validarCriacao(CriarLotePeixesRequest r){if(r.getQuantidadeInicial()==null||r.getQuantidadeInicial()<1)throw erro("QUANTIDADE_INVALIDA","Quantidade inicial deve ser maior que zero.");if(r.getDataEntrada()==null||r.getDataEntrada().isAfter(LocalDate.now(clock)))throw erro("DATA_ENTRADA_INVALIDA","Informe uma data de entrada válida.");if(r.getPesoMedio()!=null)positivo(r.getPesoMedio(),"Peso médio");}
    private void validarData(LocalDateTime data){if(data==null)throw erro("DATA_OBRIGATORIA","Informe a data da operação.");if(data.isAfter(LocalDateTime.now(clock).plusMinutes(1)))throw erro("DATA_FUTURA","A operação não pode estar no futuro.");}
    private LotePeixes ativo(Long id){LotePeixes l=lotes.buscarParaAtualizacao(id).orElseThrow(()->naoEncontrado(id));if(!l.getStatus().ativo())throw erro("LOTE_ENCERRADO","O lote está encerrado.");return l;}
    private LotePeixes buscar(Long id){return lotes.findById(id).orElseThrow(()->naoEncontrado(id));}
    private LotePeixesResumo resumo(LotePeixes l){return new LotePeixesResumo(l.getId(),l.getCodigo(),l.getEspecie(),l.getQuantidadeAtual(),l.getPesoMedio(),biomassa(l),l.getInstalacaoAtual().getId(),l.getInstalacaoAtual().getNome(),l.getStatus(),l.getDataEntrada());}
    private EventoPeixesResumo evento(EventoPeixes e){return new EventoPeixesResumo(e.getId(),e.getTipo(),e.getTipo().getRotulo(),e.getQuantidade(),e.getValorDecimal(),e.getDataEvento(),e.getUsuario(),e.getObservacao(),e.getInstalacaoOrigem()==null?null:e.getInstalacaoOrigem().getNome(),e.getInstalacaoDestino()==null?null:e.getInstalacaoDestino().getNome(),e.getItemEstoque()==null?null:e.getItemEstoque().getNome(),e.getLocalEstoque()==null?null:e.getLocalEstoque().getNome(),e.getMovimentoEstoque()==null?null:e.getMovimentoEstoque().getId());}
    private BigDecimal biomassa(LotePeixes l){return l.getPesoMedio()==null?null:escala(l.getPesoMedio().multiply(BigDecimal.valueOf(l.getQuantidadeAtual())));}
    public static String referencia(Long id){return "PEIXES:LOTE:"+id;} private String chave(String v){return textoObrigatorio(v,"Chave de idempotência");}
    private String texto(String v){return StringUtils.hasText(v)?v.trim():null;} private String textoObrigatorio(String v,String campo){if(!StringUtils.hasText(v))throw erro("CAMPO_OBRIGATORIO",campo+" é obrigatório.");return v.trim();}
    private BigDecimal positivo(BigDecimal v,String campo){if(v==null||v.signum()<=0)throw erro("VALOR_INVALIDO",campo+" deve ser maior que zero.");return escala(v);}
    private BigDecimal escala(BigDecimal v){return v==null?BigDecimal.ZERO.setScale(ESCALA):v.setScale(ESCALA,RoundingMode.HALF_UP);}
    private void exigirAdmin(UsuarioAtor ator){if(!ator.admin())throw new PeixesOperacaoException("ADMIN_OBRIGATORIO","Operação restrita a administradores.",HttpStatus.FORBIDDEN);}
    private PeixesOperacaoException erro(String c,String m){return new PeixesOperacaoException(c,m);} private PeixesOperacaoException naoEncontrado(Long id){return new PeixesOperacaoException("LOTE_NAO_ENCONTRADO","Lote de peixes não encontrado: "+id,HttpStatus.NOT_FOUND);}
    @FunctionalInterface private interface Aplicador{void aplicar(LotePeixes lote,EventoPeixes evento);}
}
