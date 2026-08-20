package com.example.sitiopro.planejamento;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public final class PlanejamentoView {

    private PlanejamentoView() {
    }

    public static String renderizar(HttpServletRequest request, Model model) {
        String rota = normalizarRota(request);
        PaginaPlanejada pagina = PlanejamentoCatalogo.buscarPagina(rota);

        if (pagina == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ModuloPlanejado modulo = pagina.modulo();
        AcaoModulo acao = pagina.acao();

        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        model.addAttribute("active", modulo.active());
        model.addAttribute("modulo", modulo);
        model.addAttribute("acaoAtual", acao);
        model.addAttribute("breadcrumbs", criarBreadcrumbs(modulo, acao));
        model.addAttribute("tituloPagina", acao.titulo() + " - " + modulo.titulo()
                + " | " + PlanejamentoCatalogo.APP_NAME);
        model.addAttribute("cssPath", "/css/" + modulo.cssFile());
        model.addAttribute("statusPagina", StatusPlanejamento.PLANEJADO);
        return "planejamento/modulo";
    }

    public static String normalizarRota(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (!contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private static List<Breadcrumb> criarBreadcrumbs(ModuloPlanejado modulo, AcaoModulo acao) {
        return List.of(
                new Breadcrumb("Início", "/sitio/painel", false),
                new Breadcrumb(modulo.grupo(), modulo.basePath(), false),
                new Breadcrumb(acao.titulo(), acao.rota(), true)
        );
    }
}
