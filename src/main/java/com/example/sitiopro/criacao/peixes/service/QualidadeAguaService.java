package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.*;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class QualidadeAguaService {
    private static final int ESCALA=4;
    private final MedicaoQualidadeAguaRepository medicoes;
    private final ConfiguracaoQualidadeAguaRepository configuracoes;
    private final LotePeixesRepository lotes;
    private final com.example.sitiopro.criacao.core.service.CodigoCriacaoService codigos;
    private final TarefaService tarefas;
    private final AlertaService alertas;
    private final Clock clock;

    public QualidadeAguaService(MedicaoQualidadeAguaRepository medicoes,
            ConfiguracaoQualidadeAguaRepository configuracoes,LotePeixesRepository lotes,
            com.example.sitiopro.criacao.core.service.CodigoCriacaoService codigos,
            TarefaService tarefas,AlertaService alertas,Clock clock){
        this.medicoes=medicoes;this.configuracoes=configuracoes;this.lotes=lotes;this.codigos=codigos;
        this.tarefas=tarefas;this.alertas=alertas;this.clock=clock;
    }

    @Transactional(readOnly=true)
    public List<MedicaoQualidadeAguaResumo> listar(Long loteId){
        buscarLote(loteId);ConfiguracaoQualidadeAgua c=config();
        return medicoes.findByLoteIdOrderByMedidoEmDescIdDesc(loteId).stream().map(m->resumo(m,c)).toList();
    }

    @Transactional(readOnly=true)
    public MedicaoQualidadeAguaResumo atual(Long loteId){
        ConfiguracaoQualidadeAgua c=config();
        return medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(loteId).map(m->resumo(m,c)).orElse(null);
    }

    @Transactional(readOnly=true)
    public QualidadeAguaDashboardResumo dashboard(){
        ConfiguracaoQualidadeAgua c=config();
        List<LotePeixes> ativos=lotes.findByStatusOrderByCodigoAsc(StatusLotePeixes.ATIVO);
        List<MedicaoQualidadeAguaResumo> atuais=ativos.stream()
                .map(l->medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(l.getId()).map(m->resumo(m,c)).orElse(null))
                .filter(Objects::nonNull).toList();
        return new QualidadeAguaDashboardResumo(atuais.size(),atuais.stream().filter(m->!m.dentroDosLimites()).count(),
                ativos.size()-atuais.size(),atuais);
    }

    @Transactional(readOnly=true)
    public ConfiguracaoQualidadeAguaDto configuracao(){return dto(config());}

    @Transactional
    public MedicaoQualidadeAguaResumo registrar(Long loteId,MedicaoQualidadeAguaRequest r,UsuarioAtor ator){
        String chave=obrigatorio(r.getChaveIdempotencia(),"Chave de idempotência");
        codigos.bloquearIdempotencia("PEIXES_QUALIDADE_AGUA",chave);
        MedicaoQualidadeAgua existente=medicoes.findByChaveIdempotencia(chave).orElse(null);
        if(existente!=null)return resumo(existente,config());
        LotePeixes lote=buscarLote(loteId);
        if(!lote.getStatus().ativo())throw erro("LOTE_ENCERRADO","Não é possível medir um lote encerrado.");
        validar(r);
        MedicaoQualidadeAgua anterior=medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(loteId).orElse(null);
        MedicaoQualidadeAgua m=new MedicaoQualidadeAgua();
        m.setLote(lote);m.setInstalacao(lote.getInstalacaoAtual());m.setMedidoEm(r.getMedidoEm());
        m.setTemperatura(decimal(r.getTemperatura()));m.setPh(decimal(r.getPh()));
        m.setOxigenioDissolvido(decimal(r.getOxigenioDissolvido()));m.setTransparenciaCm(opcional(r.getTransparenciaCm()));
        m.setAmonia(opcional(r.getAmonia()));m.setNitrito(opcional(r.getNitrito()));
        m.setResponsavel(obrigatorio(r.getResponsavel(),"Responsável"));m.setObservacao(texto(r.getObservacao()));
        m.setChaveIdempotencia(chave);m=medicoes.saveAndFlush(m);
        if(anterior!=null)tarefas.concluirAutomatica(chaveTarefa(anterior),ator);
        ConfiguracaoQualidadeAgua c=config();agendar(m,c,ator);sincronizarAlertas(c);
        return resumo(m,c);
    }

    @Transactional
    public ConfiguracaoQualidadeAguaDto atualizarConfiguracao(ConfiguracaoQualidadeAguaDto r,UsuarioAtor ator){
        if(!ator.admin())throw new PeixesOperacaoException("ADMIN_OBRIGATORIO","Operação restrita a administradores.",HttpStatus.FORBIDDEN);
        validar(r);ConfiguracaoQualidadeAgua c=config();
        if(r.getVersao()!=null&&r.getVersao()!=c.getVersao())throw erro("CONFIGURACAO_DESATUALIZADA","A configuração foi alterada por outro usuário.");
        c.atualizar(decimal(r.getTemperaturaMin()),decimal(r.getTemperaturaMax()),decimal(r.getPhMin()),decimal(r.getPhMax()),
                decimal(r.getOxigenioMin()),decimal(r.getTransparenciaMinCm()),decimal(r.getAmoniaMax()),
                decimal(r.getNitritoMax()),r.getIntervaloMedicaoDias());
        configuracoes.saveAndFlush(c);sincronizarAlertas(c);return dto(c);
    }

    private void agendar(MedicaoQualidadeAgua m,ConfiguracaoQualidadeAgua c,UsuarioAtor ator){
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(chaveTarefa(m),
                "Medir qualidade da água de "+m.getLote().getCodigo(),
                "Registrar temperatura, pH e oxigênio dissolvido no tanque "+m.getInstalacao().getNome()+".",
                PrioridadeTarefa.NORMAL,m.getMedidoEm().plusDays(c.getIntervaloMedicaoDias()),
                ModuloOrigem.CRIACOES,PeixesService.referencia(m.getLote().getId())),ator);
    }

    private void sincronizarAlertas(ConfiguracaoQualidadeAgua c){
        List<CondicaoAlerta> condicoes=new ArrayList<>();
        for(LotePeixes lote:lotes.findByStatusOrderByCodigoAsc(StatusLotePeixes.ATIVO)){
            medicoes.findFirstByLoteIdOrderByMedidoEmDescIdDesc(lote.getId()).ifPresent(m->{
                List<String> desvios=desvios(m,c);
                if(!desvios.isEmpty())condicoes.add(new CondicaoAlerta(
                        "CRIACAO:PEIXES:LOTE:"+lote.getId()+":QUALIDADE_AGUA",
                        "Qualidade da água fora dos limites",
                        lote.getCodigo()+" em "+m.getInstalacao().getNome()+": "+String.join("; ",desvios)+".",
                        SeveridadeAlerta.ALTA,PeixesService.referencia(lote.getId()),
                        Map.of("loteId",lote.getId(),"medicaoId",m.getId(),"desvios",desvios)));
            });
        }
        alertas.sincronizar(ModuloOrigem.CRIACOES,TipoAlerta.CRIACAO_PEIXES_QUALIDADE_AGUA,condicoes);
    }

    private List<String> desvios(MedicaoQualidadeAgua m,ConfiguracaoQualidadeAgua c){
        List<String>d=new ArrayList<>();
        fora(d,"temperatura",m.getTemperatura(),c.getTemperaturaMin(),c.getTemperaturaMax());
        fora(d,"pH",m.getPh(),c.getPhMin(),c.getPhMax());
        if(m.getOxigenioDissolvido().compareTo(c.getOxigenioMin())<0)d.add("oxigênio dissolvido baixo");
        if(m.getTransparenciaCm()!=null&&m.getTransparenciaCm().compareTo(c.getTransparenciaMinCm())<0)d.add("transparência baixa");
        if(m.getAmonia()!=null&&m.getAmonia().compareTo(c.getAmoniaMax())>0)d.add("amônia alta");
        if(m.getNitrito()!=null&&m.getNitrito().compareTo(c.getNitritoMax())>0)d.add("nitrito alto");
        return List.copyOf(d);
    }

    private void fora(List<String>d,String nome,BigDecimal v,BigDecimal min,BigDecimal max){
        if(v.compareTo(min)<0||v.compareTo(max)>0)d.add(nome+" fora do limite");
    }
    private void validar(MedicaoQualidadeAguaRequest r){
        if(r.getMedidoEm()==null||r.getMedidoEm().isAfter(LocalDateTime.now(clock).plusMinutes(1)))
            throw erro("DATA_MEDICAO_INVALIDA","A data da medição é obrigatória e não pode estar no futuro.");
        faixa(r.getTemperatura(),new BigDecimal("-5"),new BigDecimal("50"),"Temperatura");
        faixa(r.getPh(),BigDecimal.ZERO,new BigDecimal("14"),"pH");
        naoNegativo(r.getOxigenioDissolvido(),"Oxigênio dissolvido");
        if(r.getTransparenciaCm()!=null)naoNegativo(r.getTransparenciaCm(),"Transparência");
        if(r.getAmonia()!=null)naoNegativo(r.getAmonia(),"Amônia");
        if(r.getNitrito()!=null)naoNegativo(r.getNitrito(),"Nitrito");
        obrigatorio(r.getResponsavel(),"Responsável");
    }
    private void validar(ConfiguracaoQualidadeAguaDto r){
        if(r.getTemperaturaMin()==null||r.getTemperaturaMax()==null||r.getTemperaturaMin().compareTo(r.getTemperaturaMax())>=0)
            throw erro("LIMITES_TEMPERATURA_INVALIDOS","A temperatura mínima deve ser menor que a máxima.");
        if(r.getPhMin()==null||r.getPhMax()==null||r.getPhMin().signum()<0||r.getPhMax().compareTo(new BigDecimal("14"))>0||r.getPhMin().compareTo(r.getPhMax())>=0)
            throw erro("LIMITES_PH_INVALIDOS","Informe limites de pH válidos entre 0 e 14.");
        naoNegativo(r.getOxigenioMin(),"Oxigênio mínimo");naoNegativo(r.getTransparenciaMinCm(),"Transparência mínima");
        naoNegativo(r.getAmoniaMax(),"Amônia máxima");naoNegativo(r.getNitritoMax(),"Nitrito máximo");
        if(r.getIntervaloMedicaoDias()==null||r.getIntervaloMedicaoDias()<1||r.getIntervaloMedicaoDias()>365)
            throw erro("INTERVALO_INVALIDO","O intervalo deve ficar entre 1 e 365 dias.");
    }
    private void faixa(BigDecimal v,BigDecimal min,BigDecimal max,String campo){if(v==null||v.compareTo(min)<0||v.compareTo(max)>0)throw erro("VALOR_INVALIDO",campo+" está fora da faixa permitida.");}
    private void naoNegativo(BigDecimal v,String campo){if(v==null||v.signum()<0)throw erro("VALOR_INVALIDO",campo+" não pode ser negativo.");}
    private ConfiguracaoQualidadeAgua config(){return configuracoes.findById(ConfiguracaoQualidadeAgua.ID_UNICO).orElseThrow(()->new IllegalStateException("Configuração de qualidade da água não inicializada."));}
    private LotePeixes buscarLote(Long id){return lotes.findById(id).orElseThrow(()->new PeixesOperacaoException("LOTE_NAO_ENCONTRADO","Lote de peixes não encontrado: "+id,HttpStatus.NOT_FOUND));}
    private MedicaoQualidadeAguaResumo resumo(MedicaoQualidadeAgua m,ConfiguracaoQualidadeAgua c){List<String>d=desvios(m,c);return new MedicaoQualidadeAguaResumo(m.getId(),m.getLote().getId(),m.getLote().getCodigo(),m.getInstalacao().getId(),m.getInstalacao().getNome(),m.getMedidoEm(),m.getTemperatura(),m.getPh(),m.getOxigenioDissolvido(),m.getTransparenciaCm(),m.getAmonia(),m.getNitrito(),m.getResponsavel(),m.getObservacao(),d,d.isEmpty(),m.getVersao());}
    private ConfiguracaoQualidadeAguaDto dto(ConfiguracaoQualidadeAgua c){ConfiguracaoQualidadeAguaDto d=new ConfiguracaoQualidadeAguaDto();d.setTemperaturaMin(c.getTemperaturaMin());d.setTemperaturaMax(c.getTemperaturaMax());d.setPhMin(c.getPhMin());d.setPhMax(c.getPhMax());d.setOxigenioMin(c.getOxigenioMin());d.setTransparenciaMinCm(c.getTransparenciaMinCm());d.setAmoniaMax(c.getAmoniaMax());d.setNitritoMax(c.getNitritoMax());d.setIntervaloMedicaoDias(c.getIntervaloMedicaoDias());d.setVersao(c.getVersao());return d;}
    public static String chaveTarefa(MedicaoQualidadeAgua m){return "CRIACAO:PEIXES:LOTE:"+m.getLote().getId()+":QUALIDADE_AGUA:"+m.getId();}
    private BigDecimal decimal(BigDecimal v){return v.setScale(ESCALA,RoundingMode.HALF_UP);}private BigDecimal opcional(BigDecimal v){return v==null?null:decimal(v);}private String texto(String v){return StringUtils.hasText(v)?v.trim():null;}private String obrigatorio(String v,String campo){if(!StringUtils.hasText(v))throw erro("CAMPO_OBRIGATORIO",campo+" é obrigatório.");return v.trim();}private PeixesOperacaoException erro(String c,String m){return new PeixesOperacaoException(c,m);}
}
