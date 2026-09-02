package com.example.sitiopro.usuario.security;

import com.example.sitiopro.shared.api.ApiErrorResponse;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.shared.observability.RequestCorrelation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
            SessionRegistry sessionRegistry) throws Exception {
        RequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");
        AccessDeniedHandlerImpl pageAccessDeniedHandler = new AccessDeniedHandlerImpl();
        pageAccessDeniedHandler.setErrorPage("/403");
        AccessDeniedHandler apiAccessDeniedHandler = (request, response, accessDeniedException) -> {
            String requestId = RequestCorrelation.currentRequestId();
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "ACCESS_DENIED",
                    "module", "usuario",
                    "user.name", request.getUserPrincipal() == null ? "anonymous" : request.getUserPrincipal().getName()))) {
                log.warn("Acesso negado a {} {}", request.getMethod(), request.getRequestURI());
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    new ApiErrorResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "ACESSO_NEGADO",
                            "Acesso negado.", request.getRequestURI(), requestId));
        };
        AccessDeniedHandler delegatingAccessDeniedHandler = (request, response, accessDeniedException) -> {
            if (apiMatcher.matches(request)) {
                apiAccessDeniedHandler.handle(request, response, accessDeniedException);
                return;
            }
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "ACCESS_DENIED",
                    "module", "usuario",
                    "user.name", request.getUserPrincipal() == null ? "anonymous" : request.getUserPrincipal().getName()))) {
                log.warn("Acesso negado a {} {}", request.getMethod(), request.getRequestURI());
            }
            pageAccessDeniedHandler.handle(request, response, accessDeniedException);
        };

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/403", "/error", "/health").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/brand/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/liveness",
                                "/actuator/health/readiness").permitAll()
                        .requestMatchers("/actuator/info", "/actuator/metrics", "/actuator/metrics/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/estoque/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/estoque/itens").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/estoque/movimentos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/compras/**", "/api/v1/fornecedores").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/clima/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tarefas/**", "/api/v1/alertas/**",
                                "/api/v1/painel/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/criacoes/aves/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/criacoes/aves/instalacoes",
                                "/api/v1/criacoes/aves/lotes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/criacoes/aves/incubacoes/*/cancelar",
                                "/api/v1/criacoes/aves/incubacoes/*/ajustar-previsao").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/criacoes/aves/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/integracoes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/compras", "/api/v1/compras/*/itens",
                                "/api/v1/compras/*/confirmar", "/api/v1/fornecedores").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tarefas", "/api/v1/tarefas/*/iniciar",
                                "/api/v1/tarefas/*/concluir", "/api/v1/alertas/*/reconhecer",
                                "/api/v1/alertas/*/criar-tarefa").authenticated()
                        .requestMatchers("/api/v1/**").denyAll()
                        .requestMatchers("/administracao/**", "/configuracoes/roadmap").hasRole("ADMIN")
                        .requestMatchers("/sitio/admin", "/sitio/admin/**", "/sitio/configuracoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sitio/compras/fornecedores/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sitio/compras/*/cancelar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sitio/tarefas/*/cancelar",
                                "/sitio/alertas/*/resolver").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sitio/tarefas/**", "/sitio/alertas/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sitio/tarefas/**", "/sitio/alertas/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/sitio/compras/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sitio/compras/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/sitio/estoque/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sitio/estoque/itens",
                                "/sitio/estoque/categorias", "/sitio/estoque/locais").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sitio/estoque/movimentacoes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sitio/criacoes/aves/instalacoes",
                                "/sitio/criacoes/aves/instalacoes/*",
                                "/sitio/criacoes/aves/lotes",
                                "/sitio/criacoes/aves/lotes/*",
                                "/sitio/criacoes/aves/lotes/*/encerrar",
                                "/sitio/criacoes/aves/incubacoes/*/cancelar",
                                "/sitio/criacoes/aves/incubacoes/*/ajustar-previsao").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/sitio/criacoes/aves/instalacoes/nova",
                                "/sitio/criacoes/aves/instalacoes/*/editar",
                                "/sitio/criacoes/aves/lotes/novo",
                                "/sitio/criacoes/aves/lotes/*/editar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sitio/criacoes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/sitio/criacoes/aves/**").authenticated()
                        .requestMatchers("/gestao/**", "/criacoes/**", "/agricultura/**", "/agua/**",
                                "/propriedade/**", "/ola", "/saudacao").authenticated()
                        .requestMatchers("/sitio/**", "/api/fipe/**").authenticated()
                        .anyRequest().denyAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/sitio/painel", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                        .sessionRegistry(sessionRegistry))
                .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(delegatingAccessDeniedHandler));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
}
