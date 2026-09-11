package com.example.sitiopro.usuario.controller;

import jakarta.servlet.DispatcherType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AutenticacaoControllerTests {
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void acessoNegadoAceitaMetodoOriginalDoEncaminhamento(String metodo) throws Exception {
        MockMvcBuilders.standaloneSetup(new AutenticacaoController()).build()
                .perform(request(HttpMethod.valueOf(metodo), "/403").with(req -> {
                    req.setDispatcherType(DispatcherType.FORWARD);
                    return req;
                }))
                .andExpect(handler().methodName("acessoNegado"))
                .andExpect(view().name("security/403"));
    }
}
