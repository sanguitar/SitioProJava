package com.example.sitiopro.administracao.service;

import com.example.sitiopro.administracao.dto.AdministracaoResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.shared.observability.ObservabilityProperties;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdministracaoResumoService {

    private static final Logger log = LoggerFactory.getLogger(AdministracaoResumoService.class);

    private final UsuarioService usuarioService;
    private final SistemaSaudeService sistemaSaudeService;
    private final IntegracaoPainelService integracaoPainelService;
    private final ObservabilityProperties observabilityProperties;

    public AdministracaoResumoService(UsuarioService usuarioService,
            SistemaSaudeService sistemaSaudeService,
            IntegracaoPainelService integracaoPainelService,
            ObservabilityProperties observabilityProperties) {
        this.usuarioService = usuarioService;
        this.sistemaSaudeService = sistemaSaudeService;
        this.integracaoPainelService = integracaoPainelService;
        this.observabilityProperties = observabilityProperties;
    }

    public AdministracaoResumo resumo() {
        AdministracaoResumo.Usuarios usuarios = carregarUsuarios();
        SistemaSaudeResumo saudeSistema = carregarSaude();
        AdministracaoResumo.Saude saude = saudeSistema == null
                ? new AdministracaoResumo.Saude(false, "INDISPONIVEL", "INDISPONIVEL")
                : new AdministracaoResumo.Saude(true, saudeSistema.aplicacao(), saudeSistema.banco());
        AdministracaoResumo.Observabilidade observabilidade = new AdministracaoResumo.Observabilidade(
                observabilityProperties.isEnabled(),
                saudeSistema == null ? "INDISPONIVEL" : saudeSistema.observabilidade());

        return new AdministracaoResumo(usuarios, saude, carregarIntegracoes(), observabilidade);
    }

    private AdministracaoResumo.Usuarios carregarUsuarios() {
        try {
            return new AdministracaoResumo.Usuarios(true,
                    usuarioService.contarAtivos(), usuarioService.contarAdministradoresAtivos());
        } catch (RuntimeException ex) {
            registrarDegradacao("usuarios", ex);
            return new AdministracaoResumo.Usuarios(false, 0, 0);
        }
    }

    private SistemaSaudeResumo carregarSaude() {
        try {
            return sistemaSaudeService.resumo();
        } catch (RuntimeException ex) {
            registrarDegradacao("saude", ex);
            return null;
        }
    }

    private AdministracaoResumo.Integracoes carregarIntegracoes() {
        try {
            IntegracaoPainelResumo resumo = integracaoPainelService.resumo();
            return new AdministracaoResumo.Integracoes(true, resumo.operacionais(), resumo.falhas());
        } catch (RuntimeException ex) {
            registrarDegradacao("integracoes", ex);
            return new AdministracaoResumo.Integracoes(false, 0, 0);
        }
    }

    private void registrarDegradacao(String secao, RuntimeException ex) {
        log.warn("Seção indisponível no resumo administrativo: {} ({}).",
                secao, ex.getClass().getSimpleName());
    }
}
