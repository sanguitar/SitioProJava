package com.example.sitiopro.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestObservabilityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestObservabilityFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/brand/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico")
                || path.equals("/health")
                || path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long started = System.nanoTime();
        String requestId = RequestCorrelation.normalize(request.getHeader(RequestCorrelation.HEADER_NAME));
        response.setHeader(RequestCorrelation.HEADER_NAME, requestId);

        MDC.put(RequestCorrelation.MDC_REQUEST_ID, requestId);
        MDC.put("http.request.method", request.getMethod());
        MDC.put("url.path", request.getRequestURI());
        MDC.put("module", moduleForPath(request.getRequestURI()));

        Exception failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationNanos = System.nanoTime() - started;
            int status = failure == null ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            String userName = currentUserName(request);
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "http_request",
                    "event.duration", durationNanos,
                    "http.response.status_code", status,
                    "user.name", userName))) {
                logRequest(request, status, durationNanos);
            } finally {
                MDC.remove(RequestCorrelation.MDC_REQUEST_ID);
                MDC.remove("http.request.method");
                MDC.remove("url.path");
                MDC.remove("module");
            }
        }
    }

    private void logRequest(HttpServletRequest request, int status, long durationNanos) {
        String message = "HTTP {} {} -> {} em {} ms";
        long durationMillis = durationNanos / 1_000_000;
        if (status >= 500) {
            log.error(message, request.getMethod(), request.getRequestURI(), status, durationMillis);
        } else if (status >= 400) {
            log.warn(message, request.getMethod(), request.getRequestURI(), status, durationMillis);
        } else {
            log.info(message, request.getMethod(), request.getRequestURI(), status, durationMillis);
        }
    }

    private String currentUserName(HttpServletRequest request) {
        if (request.getUserPrincipal() != null && request.getUserPrincipal().getName() != null
                && !request.getUserPrincipal().getName().isBlank()) {
            return request.getUserPrincipal().getName();
        }
        Authentication authentication = resolveAuthentication(request);
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        String name = authentication.getName();
        return name == null || name.isBlank() ? "anonymous" : name;
    }

    private Authentication resolveAuthentication(HttpServletRequest request) {
        Object requestContext = request.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (requestContext instanceof SecurityContext securityContext) {
            return securityContext.getAuthentication();
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (sessionContext instanceof SecurityContext securityContext) {
                return securityContext.getAuthentication();
            }
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String moduleForPath(String path) {
        if (path.startsWith("/api/v1/estoque") || path.startsWith("/sitio/estoque")) {
            return "estoque";
        }
        if (path.startsWith("/api/v1/compras") || path.startsWith("/api/v1/fornecedores")
                || path.startsWith("/sitio/compras")) {
            return "compras";
        }
        if (path.startsWith("/api/v1/clima") || path.startsWith("/api/v1/admin/integracoes")
                || path.startsWith("/sitio/admin/integracoes")) {
            return "integracao";
        }
        if (path.startsWith("/sitio/admin/usuarios") || path.startsWith("/sitio/perfil") || path.startsWith("/login")
                || path.startsWith("/logout")) {
            return "usuario";
        }
        if (path.startsWith("/sitio/frota")) {
            return "frota";
        }
        if (path.startsWith("/sitio/abastecimento") || path.startsWith("/sitio/abastecimentos")) {
            return "abastecimento";
        }
        if (path.startsWith("/sitio/configuracoes")) {
            return "categoria";
        }
        if (path.startsWith("/sitio/cadastro") || path.startsWith("/sitio/editar")) {
            return "producao";
        }
        if (path.startsWith("/sitio/painel")) {
            return "dashboard";
        }
        if (path.startsWith("/sitio/tarefas")) {
            return "tarefas";
        }
        if (path.startsWith("/sitio/aves")) {
            return "aves";
        }
        if (path.startsWith("/sitio/suinos")) {
            return "suinos";
        }
        if (path.startsWith("/sitio/piscicultura")) {
            return "piscicultura";
        }
        if (path.startsWith("/sitio/agricultura")) {
            return "agricultura";
        }
        if (path.startsWith("/sitio/agua")) {
            return "agua";
        }
        if (path.startsWith("/sitio/casa") || path.startsWith("/sitio/despensa")) {
            return "casa";
        }
        if (path.startsWith("/sitio/manutencao") || path.startsWith("/sitio/ar-condicionado")
                || path.startsWith("/sitio/dedetizacao") || path.startsWith("/sitio/reformas")
                || path.startsWith("/sitio/deterioracoes")) {
            return "manutencao";
        }
        if (path.startsWith("/sitio/patrimonio")) {
            return "patrimonio";
        }
        if (path.startsWith("/sitio/seguranca")) {
            return "seguranca";
        }
        return "sistema";
    }
}
