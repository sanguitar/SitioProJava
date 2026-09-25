package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.*;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.entity.*;
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
public class SuinosService {
    private static final int ESCALA=4;
    private final LoteSuinosRepository lotes; private final EventoSuinosRepository eventos;
    private final InstalacaoCriacaoService instalacoes; private final CodigoCriacaoService codigos;
    private final EstoqueCatalogoService catalogo; private final EstoqueMovimentoService estoque;
    private final TarefaService tarefas; private final AlertaService alertas; private final Clock clock;

    public SuinosService(LoteSuinosRepository lotes,EventoSuinosRepository eventos,
            InstalacaoCriacaoService instalacoes,CodigoCriacaoService codigos,EstoqueCatalogoService catalogo,
            EstoqueMovimentoService estoque,TarefaService tarefas,AlertaService alertas,Clock clock){
        this.lotes=lotes;this.eventos=eventos;this.instalacoes=instalacoes;this.codigos=codigos;
        this.catalogo=catalogo;this.estoque=estoque;this.tarefas=tarefas;this.alertas=alertas;this.clock=clock;
    }

    @Transactional(readOnly=true)
    public PaginaResponse<LoteSuinosResumo> listar(StatusLoteSuinos status,String termo,int pagina,int tamanho){
        String busca=StringUtils.hasText(termo)?termo.trim():null;
        return PaginaResponse.de(lotes.buscar(status,busca,PageRequest.of(Math.max(0,pagina),Math.min(100,Math.max(1,tamanho)))).map(this::resumo));
    }

    @Transactional(readOnly=true)
    public LoteSuinosDetalhe detalhar(Long id){
        LoteSuinos lote=buscar(id); List<EventoSuinos> historico=eventos.findByLoteIdOrderByDataEventoDescIdDesc(id);
        String referencia=referencia(id);
        BigDecimal consumo=historico.stream().filter(e->e.getTipo()==TipoEventoSuinos.ALIMENTACAO)
                .map(EventoSuinos::getValorDecimal).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add);
        int mortalidade=historico.stream().filter(e->e.getTipo()==TipoEventoSuinos.MORTALIDADE||e.getTipo()==TipoEventoSuinos.PERDA)
                .map(EventoSuinos::getQuantidade).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        return new LoteSuinosDetalhe(lote.getId(),lote.getCodigo(),lote.getCategoria(),lote.getDataEntrada(),lote.getDataNascimento(),
                lote.getOrigem(),lote.getQuantidadeInicial(),lote.getQuantidadeAtual(),lote.getPesoMedio(),
                lote.getInstalacaoAtual().getId(),lote.getInstalacaoAtual().getNome(),lote.getStatus(),lote.getObservacao(),
                escala(consumo),mortalidade,historico.stream().map(this::evento).toList(),
                alertas.listarRelacionados(ModuloOrigem.CRIACOES,referencia),tarefas.listarRelacionadas(ModuloOrigem.CRIACOES,referencia),
                lote.getVersao(),lote.getCriadoEm(),lote.getCriadoPor(),lote.getAlteradoEm(),lote.getAlteradoPor());
    }

    @Transactional(readOnly=true)
    public SuinosDashboardResumo dashboard(){
        List<LoteSuinos> ativos=lotes.findByStatusOrderByCodigoAsc(StatusLoteSuinos.ATIVO);
        long tarefasAbertas=ativos.stream().flatMap(l->tarefas.listarRelacionadas(ModuloOrigem.CRIACOES,referencia(l.getId())).stream())
                .filter(t->!t.status().finalizado()).count();
        long alertasAbertos=ativos.stream().mapToLong(l->alertas.listarRelacionados(ModuloOrigem.CRIACOES,referencia(l.getId())).size()).sum();
        LocalDateTime inicio=LocalDateTime.now(clock).minusDays(30);
        return new SuinosDashboardResumo(ativos.size(),Optional.ofNullable(lotes.somarQuantidade(StatusLoteSuinos.ATIVO)).orElse(0L),
                escala(lotes.pesoMedio(StatusLoteSuinos.ATIVO)),escala(eventos.somarValorDesde(TipoEventoSuinos.ALIMENTACAO,inicio)),
                Optional.ofNullable(eventos.somarQuantidadeDesde(List.of(TipoEventoSuinos.MORTALIDADE,TipoEventoSuinos.PERDA),inicio)).orElse(0L),
                tarefasAbertas,alertasAbertos);
    }

    @Transactional
    public LoteSuinosDetalhe criar(CriarLoteSuinosRequest r,UsuarioAtor ator){
        exigirAdmin(ator); String chave=chave(r.getChaveIdempotencia()); codigos.bloquearIdempotencia("LOTE_SUINOS",chave);
        LoteSuinos existente=lotes.findByChaveIdempotencia(chave).orElse(null); if(existente!=null)return detalhar(existente.getId());
        validarCriacao(r); InstalacaoCriacao instalacao=instalacoes.reservarCapacidadeSuinos(r.getInstalacaoId(),r.getQuantidadeInicial(),null);
        LoteSuinos lote=new LoteSuinos(); lote.setCodigo(codigos.proximoLoteSuinos()); lote.setCategoria(r.getCategoria());
        lote.setDataEntrada(r.getDataEntrada());lote.setDataNascimento(r.getDataNascimento());lote.setOrigem(textoObrigatorio(r.getOrigem(),"Origem"));
        lote.setQuantidadeInicial(r.getQuantidadeInicial());lote.setQuantidadeAtual(r.getQuantidadeInicial());lote.setPesoMedio(escala(r.getPesoMedio()));
        lote.setInstalacaoAtual(instalacao);lote.setStatus(StatusLoteSuinos.ATIVO);lote.setObservacao(texto(r.getObservacao()));lote.setChaveIdempotencia(chave);
        lote=lotes.save(lote); novoEvento(lote,TipoEventoSuinos.ENTRADA_INICIAL,r.getQuantidadeInicial(),null,r.getDataEntrada().atStartOfDay(),ator.ator(),r.getObservacao(),chave,null,instalacao,null,null);
        return detalhar(lote.getId());
    }

    @Transactional
    public LoteSuinosDetalhe criarLoteDoParto(Long instalacaoId, int quantidade, LocalDate dataNascimento,
            BigDecimal pesoMedio, String origem, String observacao, String chaveIdempotencia, UsuarioAtor ator) {
        String chave = chave(chaveIdempotencia);
        codigos.bloquearIdempotencia("LOTE_SUINOS", chave);
        LoteSuinos existente = lotes.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return detalhar(existente.getId());
        if (quantidade < 1) throw erro("QUANTIDADE_INVALIDA", "O lote do parto exige ao menos um leitão vivo.");
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now(clock)))
            throw erro("DATA_PARTO_INVALIDA", "A data do parto é inválida.");
        InstalacaoCriacao instalacao = instalacoes.reservarCapacidadeSuinos(instalacaoId, quantidade, null);
        LoteSuinos lote = new LoteSuinos();
        lote.setCodigo(codigos.proximoLoteSuinos());
        lote.setCategoria(CategoriaSuino.LEITAO);
        lote.setDataEntrada(dataNascimento);
        lote.setDataNascimento(dataNascimento);
        lote.setOrigem(textoObrigatorio(origem, "Origem"));
        lote.setQuantidadeInicial(quantidade);
        lote.setQuantidadeAtual(quantidade);
        lote.setPesoMedio(pesoMedio == null ? null : positivo(pesoMedio, "Peso médio"));
        lote.setInstalacaoAtual(instalacao);
        lote.setStatus(StatusLoteSuinos.ATIVO);
        lote.setObservacao(texto(observacao));
        lote.setChaveIdempotencia(chave);
        lote = lotes.save(lote);
        novoEvento(lote, TipoEventoSuinos.ENTRADA_INICIAL, quantidade, null,
                dataNascimento.atStartOfDay(), ator.ator(), observacao, chave, null, instalacao, null, null);
        return detalhar(lote.getId());
    }

    @Transactional public LoteSuinosDetalhe registrarEntrada(Long id,EntradaSuinosRequest r,UsuarioAtor ator){
        return operar(id,r,ator,(l,e)->{l.setQuantidadeAtual(Math.addExact(l.getQuantidadeAtual(),r.getQuantidade()));e.setTipo(TipoEventoSuinos.ENTRADA_ANIMAIS);e.setQuantidade(r.getQuantidade());});
    }
    @Transactional public LoteSuinosDetalhe registrarPerda(Long id,PerdaSuinosRequest r,UsuarioAtor ator){
        if(r.getTipo()!=TipoEventoSuinos.MORTALIDADE&&r.getTipo()!=TipoEventoSuinos.PERDA)throw erro("TIPO_PERDA_INVALIDO","Use mortalidade ou perda.");
        return operar(id,r,ator,(l,e)->{if(r.getQuantidade()>l.getQuantidadeAtual())throw erro("QUANTIDADE_INSUFICIENTE","A perda não pode exceder a quantidade atual.");l.setQuantidadeAtual(l.getQuantidadeAtual()-r.getQuantidade());if(l.getQuantidadeAtual()==0)l.setStatus(StatusLoteSuinos.ENCERRADO);e.setTipo(r.getTipo());e.setQuantidade(r.getQuantidade());});
    }
    @Transactional public LoteSuinosDetalhe registrarPesagem(Long id,PesagemSuinosRequest r,UsuarioAtor ator){
        return operar(id,r,ator,(l,e)->{BigDecimal peso=positivo(r.getPesoMedio(),"Peso médio");l.setPesoMedio(peso);e.setTipo(TipoEventoSuinos.PESAGEM);e.setValorDecimal(peso);});
    }
    @Transactional public LoteSuinosDetalhe transferir(Long id,TransferenciaSuinosRequest r,UsuarioAtor ator){
        return operar(id,r,ator,(l,e)->{InstalacaoCriacao destino=instalacoes.reservarCapacidadeSuinos(r.getInstalacaoDestinoId(),l.getQuantidadeAtual(),l.getId());if(destino.getId().equals(l.getInstalacaoAtual().getId()))throw erro("DESTINO_IGUAL_ORIGEM","Selecione outra instalação.");e.setTipo(TipoEventoSuinos.TRANSFERENCIA);e.setInstalacaoOrigem(l.getInstalacaoAtual());e.setInstalacaoDestino(destino);l.setInstalacaoAtual(destino);});
    }
    @Transactional public LoteSuinosDetalhe registrarAlimentacao(Long id,AlimentacaoSuinosRequest r,UsuarioAtor ator){
        String chave=chave(r.getChaveIdempotencia());codigos.bloquearIdempotencia("SUINOS_EVENTO",chave);
        EventoSuinos existente=eventos.findByChaveIdempotencia(chave).orElse(null);if(existente!=null)return detalhar(existente.getLote().getId());
        LoteSuinos lote=ativo(id);validarData(r.getDataEvento());BigDecimal quantidade=positivo(r.getQuantidade(),"Quantidade");
        ItemEstoque item=catalogo.buscarItem(r.getItemEstoqueId());LocalEstoque local=catalogo.buscarLocalAtivo(r.getLocalEstoqueId());
        EventoSuinos evento=novoEvento(lote,TipoEventoSuinos.ALIMENTACAO,null,quantidade,r.getDataEvento(),ator.ator(),r.getObservacao(),chave,null,null,item,local);
        eventos.flush();MovimentoEstoqueRequest movimento=new MovimentoEstoqueRequest();movimento.setItemId(item.getId());movimento.setQuantidade(quantidade);
        movimento.setLocalOrigemId(local.getId());movimento.setLoteCodigo(texto(r.getLoteEstoqueCodigo()));movimento.setObservacao(texto(r.getObservacao()));movimento.setDataMovimento(r.getDataEvento());
        evento.setMovimentoEstoque(estoque.registrarConsumoCriacaoSuinos(movimento,evento.getId(),id));
        return detalhar(id);
    }

    private LoteSuinosDetalhe operar(Long id,OperacaoSuinosBase r,UsuarioAtor ator,Aplicador aplicador){
        String chave=chave(r.getChaveIdempotencia());codigos.bloquearIdempotencia("SUINOS_EVENTO",chave);
        EventoSuinos existente=eventos.findByChaveIdempotencia(chave).orElse(null);if(existente!=null)return detalhar(existente.getLote().getId());
        LoteSuinos lote=ativo(id);validarData(r.getDataEvento());EventoSuinos evento=baseEvento(lote,r.getDataEvento(),ator.ator(),r.getObservacao(),chave);aplicador.aplicar(lote,evento);eventos.save(evento);return detalhar(id);
    }
    private EventoSuinos novoEvento(LoteSuinos lote,TipoEventoSuinos tipo,Integer qtd,BigDecimal valor,LocalDateTime data,String usuario,String obs,String chave,InstalacaoCriacao origem,InstalacaoCriacao destino,ItemEstoque item,LocalEstoque local){EventoSuinos e=baseEvento(lote,data,usuario,obs,chave);e.setTipo(tipo);e.setQuantidade(qtd);e.setValorDecimal(valor);e.setInstalacaoOrigem(origem);e.setInstalacaoDestino(destino);e.setItemEstoque(item);e.setLocalEstoque(local);return eventos.save(e);}
    private EventoSuinos baseEvento(LoteSuinos lote,LocalDateTime data,String usuario,String obs,String chave){EventoSuinos e=new EventoSuinos();e.setLote(lote);e.setDataEvento(data);e.setUsuario(usuario);e.setObservacao(texto(obs));e.setChaveIdempotencia(chave);return e;}
    private void validarCriacao(CriarLoteSuinosRequest r){if(r.getCategoria()==null)throw erro("CATEGORIA_OBRIGATORIA","Informe a categoria.");if(r.getQuantidadeInicial()==null||r.getQuantidadeInicial()<1)throw erro("QUANTIDADE_INVALIDA","Quantidade inicial deve ser maior que zero.");if(r.getDataEntrada()==null)throw erro("DATA_OBRIGATORIA","Informe a data de entrada.");if(r.getDataNascimento()!=null&&r.getDataNascimento().isAfter(r.getDataEntrada()))throw erro("DATA_NASCIMENTO_INVALIDA","Nascimento não pode ser posterior à entrada.");if(r.getPesoMedio()!=null)positivo(r.getPesoMedio(),"Peso médio");}
    private void validarData(LocalDateTime data){if(data==null)throw erro("DATA_OBRIGATORIA","Informe a data da operação.");if(data.isAfter(LocalDateTime.now(clock).plusMinutes(1)))throw erro("DATA_FUTURA","A operação não pode estar no futuro.");}
    private LoteSuinos ativo(Long id){LoteSuinos l=lotes.buscarParaAtualizacao(id).orElseThrow(()->naoEncontrado(id));if(!l.getStatus().ativo())throw erro("LOTE_ENCERRADO","O lote está encerrado.");return l;}
    private LoteSuinos buscar(Long id){return lotes.findById(id).orElseThrow(()->naoEncontrado(id));}
    private LoteSuinosResumo resumo(LoteSuinos l){return new LoteSuinosResumo(l.getId(),l.getCodigo(),l.getCategoria(),l.getQuantidadeAtual(),l.getPesoMedio(),l.getInstalacaoAtual().getId(),l.getInstalacaoAtual().getNome(),l.getStatus(),l.getDataEntrada());}
    private EventoSuinosResumo evento(EventoSuinos e){return new EventoSuinosResumo(e.getId(),e.getTipo(),e.getTipo().getRotulo(),e.getQuantidade(),e.getValorDecimal(),e.getDataEvento(),e.getUsuario(),e.getObservacao(),e.getInstalacaoOrigem()==null?null:e.getInstalacaoOrigem().getNome(),e.getInstalacaoDestino()==null?null:e.getInstalacaoDestino().getNome(),e.getItemEstoque()==null?null:e.getItemEstoque().getNome(),e.getLocalEstoque()==null?null:e.getLocalEstoque().getNome(),e.getMovimentoEstoque()==null?null:e.getMovimentoEstoque().getId());}
    private String referencia(Long id){return "SUINOS:LOTE:"+id;} private String chave(String v){return textoObrigatorio(v,"Chave de idempotência");}
    private String texto(String v){return StringUtils.hasText(v)?v.trim():null;} private String textoObrigatorio(String v,String campo){if(!StringUtils.hasText(v))throw erro("CAMPO_OBRIGATORIO",campo+" é obrigatório.");return v.trim();}
    private BigDecimal positivo(BigDecimal v,String campo){if(v==null||v.signum()<=0)throw erro("VALOR_INVALIDO",campo+" deve ser maior que zero.");return escala(v);}
    private BigDecimal escala(BigDecimal v){return v==null?null:v.setScale(ESCALA,RoundingMode.HALF_UP);}
    private void exigirAdmin(UsuarioAtor ator){if(!ator.admin())throw new SuinosOperacaoException("ADMIN_OBRIGATORIO","Operação restrita a administradores.",HttpStatus.FORBIDDEN);}
    private SuinosOperacaoException erro(String c,String m){return new SuinosOperacaoException(c,m);} private SuinosOperacaoException naoEncontrado(Long id){return new SuinosOperacaoException("LOTE_NAO_ENCONTRADO","Lote de suínos não encontrado: "+id,HttpStatus.NOT_FOUND);}
    @FunctionalInterface private interface Aplicador{void aplicar(LoteSuinos lote,EventoSuinos evento);}
}
