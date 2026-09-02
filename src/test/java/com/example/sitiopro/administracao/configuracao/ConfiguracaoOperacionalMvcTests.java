package com.example.sitiopro.administracao.configuracao;

import com.example.sitiopro.administracao.configuracao.controller.ConfiguracaoOperacionalController;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalInvalidaException;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.servico;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConfiguracaoOperacionalMvcTests {
    private ConfiguracaoOperacionalService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = servico();
        mvc = MockMvcBuilders.standaloneSetup(new ConfiguracaoOperacionalController(service)).build();
    }

    @Test
    void getApresentaFormularioExplicitoSemMutacao() throws Exception {
        mvc.perform(get("/sitio/admin/configuracoes")).andExpect(status().isOk())
                .andExpect(view().name("admin/configuracoes"))
                .andExpect(model().attributeExists("configuracaoForm", "configuracao"));
        verify(service, never()).atualizar(any());
    }

    @Test
    void postValidoAceitaSomenteCamposOperacionais() throws Exception {
        mvc.perform(postValido().param("id", "2").param("revisaoLocalizacao", "100")
                        .param("alteradoPor", "invasor").param("senha", "sentinela-nao-persistir"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/sitio/admin/configuracoes"));
        verify(service).atualizar(argThat(f -> f.getNomePropriedade().equals("Sítio teste")
                && f.getDiasPadraoIncubacao() == 21));
    }

    @Test
    void coordenadaForaDoLimiteNaoChegaAoService() throws Exception {
        mvc.perform(postValido().param("latitude", "91").param("longitude", "10"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("configuracaoForm", "latitude"));
        verify(service, never()).atualizar(any());
    }

    @Test
    void erroDeTimezoneDoServiceRetornaNoCampo() throws Exception {
        when(service.atualizar(any())).thenThrow(new ConfiguracaoOperacionalInvalidaException("timezone", "Fuso inválido."));
        mvc.perform(postValido()).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("configuracaoForm", "timezone"));
    }

    @Test
    void camposObrigatoriosNaoAceitamPostVazio() throws Exception {
        mvc.perform(post("/sitio/admin/configuracoes")).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("configuracaoForm", "nomePropriedade", "timezone",
                        "diasPadraoIncubacao", "antecedenciaAlertaEclosaoDias"));
        verify(service, never()).atualizar(any());
    }

    private MockHttpServletRequestBuilder postValido() {
        return post("/sitio/admin/configuracoes").param("nomePropriedade", "Sítio teste")
                .param("timezone", "UTC").param("diasPadraoIncubacao", "21")
                .param("antecedenciaAlertaEclosaoDias", "2");
    }
}
