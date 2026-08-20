package com.example.sitiopro.planejamento.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PlanejamentoController {

    private static final Map<String, ModuloPlanejado> MODULOS = criarModulos();
    private static final Map<String, PaginaPlanejada> PAGINAS = criarPaginas(MODULOS.values());
    private static final List<RoadmapItem> ROADMAP = criarRoadmap();
    private static final Map<String, List<RoadmapItem>> ROADMAP_GRUPOS = agruparRoadmap(ROADMAP);

    @GetMapping("/configuracoes/roadmap")
    public String roadmap(Model model) {
        model.addAttribute("usuario", "Systems Analyst");
        model.addAttribute("active", "roadmap");
        model.addAttribute("roadmapGrupos", ROADMAP_GRUPOS);
        model.addAttribute("statusDisponiveis", Status.values());
        return "admin/roadmap";
    }

    @GetMapping({
            "/gestao/{modulo}",
            "/gestao/{modulo}/{acao}",
            "/criacoes/{modulo}",
            "/criacoes/{modulo}/{segmento}",
            "/criacoes/{modulo}/{segmento}/{acao}",
            "/agricultura/{modulo}",
            "/agricultura/{modulo}/{acao}",
            "/agua/{modulo}",
            "/agua/{modulo}/{acao}",
            "/propriedade/{modulo}",
            "/propriedade/{modulo}/{acao}",
            "/administracao/{modulo}",
            "/administracao/{modulo}/{acao}",
            "/sitio/frota/detalhe",
            "/sitio/frota/historico",
            "/sitio/abastecimento",
            "/sitio/abastecimento/detalhe",
            "/sitio/abastecimento/historico"
    })
    public String paginaPlanejada(HttpServletRequest request, Model model) {
        String rota = normalizarRota(request);
        PaginaPlanejada pagina = PAGINAS.get(rota);

        if (pagina == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ModuloPlanejado modulo = pagina.modulo();
        AcaoModulo acao = pagina.acao();

        model.addAttribute("usuario", "Systems Analyst");
        model.addAttribute("active", modulo.active());
        model.addAttribute("modulo", modulo);
        model.addAttribute("acaoAtual", acao);
        model.addAttribute("breadcrumbs", criarBreadcrumbs(modulo, acao));
        model.addAttribute("tituloPagina", acao.titulo() + " - " + modulo.titulo());
        model.addAttribute("cssPath", "/css/" + modulo.cssFile());
        model.addAttribute("statusPagina", Status.PLANEJADO);
        return "planejamento/modulo";
    }

    private static String normalizarRota(HttpServletRequest request) {
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

    private static Map<String, PaginaPlanejada> criarPaginas(Collection<ModuloPlanejado> modulos) {
        Map<String, PaginaPlanejada> paginas = new LinkedHashMap<>();
        for (ModuloPlanejado modulo : modulos) {
            for (AcaoModulo acao : modulo.acoes()) {
                paginas.put(acao.rota(), new PaginaPlanejada(modulo, acao));
            }
        }
        return paginas;
    }

    private static Map<String, List<RoadmapItem>> agruparRoadmap(List<RoadmapItem> roadmap) {
        Map<String, List<RoadmapItem>> grupos = new LinkedHashMap<>();
        for (RoadmapItem item : roadmap) {
            grupos.computeIfAbsent(item.grupo(), chave -> new ArrayList<>()).add(item);
        }
        return grupos;
    }

    private static Map<String, ModuloPlanejado> criarModulos() {
        Map<String, ModuloPlanejado> modulos = new LinkedHashMap<>();

        adicionar(modulos, modulo("Gestão", "Estoque", "/gestao/estoque", "estoque", "estoque.css",
                "domain-estoque", "fa-boxes-stacked", Status.PRECISA_REVISAO,
                "Controle visual de insumos, materiais, alimentos, itens produtivos e alertas de reposição.",
                links(
                        link("Painel atual", "/sitio/painel", "fa-chart-line"),
                        link("Cadastro atual", "/sitio/cadastro", "fa-plus"),
                        link("Categorias atuais", "/sitio/configuracoes", "fa-tags")
                ),
                "Listagem por categoria, local, quantidade e status",
                "Cadastro de itens com unidade, estoque mínimo e custo estimado",
                "Detalhe com posição atual, movimentações e vínculo com centros de custo",
                "Histórico de entradas, saídas, perdas e ajustes"));

        adicionar(modulos, modulo("Gestão", "Compras", "/gestao/compras", "compras", "compras.css",
                "domain-compras", "fa-cart-shopping", Status.PLANEJADO,
                "Planejamento de compras, cotações, pedidos e recebimentos da propriedade.",
                "Solicitações de compra por área do sítio",
                "Comparação de fornecedores e valores cotados",
                "Pedidos em aberto, recebidos e cancelados",
                "Histórico de compras por centro de custo"));

        adicionar(modulos, modulo("Gestão", "Tarefas", "/gestao/tarefas", "tarefas", "tarefas.css",
                "domain-tarefas", "fa-list-check", Status.PLANEJADO,
                "Agenda operacional para atividades recorrentes, responsáveis, prazos e pendências.",
                "Quadro de tarefas por prioridade e responsável",
                "Cadastro de tarefas avulsas e recorrentes",
                "Detalhe com checklist, anexos e observações",
                "Histórico de execução por módulo"));

        adicionar(modulos, modulo("Criações", "Aves", "/criacoes/aves", "aves", "aves.css",
                "domain-aves", "fa-egg", Status.PLANEJADO,
                "Visão consolidada do ciclo de aves, do ovo ao galinheiro produtivo.",
                "Resumo de lotes por fase de criação",
                "Cadastro de lotes, linhagens e origem",
                "Detalhe com mortalidade, alimentação e transferências",
                "Histórico produtivo por lote"));

        adicionar(modulos, modulo("Criações", "Chocadeira", "/criacoes/aves/chocadeira", "aves", "aves.css",
                "domain-aves", "fa-temperature-half", Status.PLANEJADO,
                "Acompanhamento de chocagens, ovos incubados, viragens, temperatura e eclosão.",
                "Mapa de ciclos ativos na chocadeira",
                "Cadastro de ciclo de incubação",
                "Detalhe com lote, datas, perdas e nascimentos",
                "Histórico de taxa de eclosão"));

        adicionar(modulos, modulo("Criações", "Pinteiro", "/criacoes/aves/pinteiro", "aves", "aves.css",
                "domain-aves", "fa-feather", Status.PLANEJADO,
                "Controle dos pintinhos em recria inicial, aquecimento, ração e sanidade.",
                "Listagem de lotes no pinteiro",
                "Cadastro de entrada de pintinhos",
                "Detalhe de evolução, perdas e consumo",
                "Histórico de manejo por lote"));

        adicionar(modulos, modulo("Criações", "Galinheiro", "/criacoes/aves/galinheiro", "aves", "aves.css",
                "domain-aves", "fa-house-chimney", Status.PLANEJADO,
                "Gestão de aves adultas, postura, alimentação, coleta de ovos e produtividade.",
                "Mapa de galinheiros e lotes alojados",
                "Cadastro de baias, aves e transferências",
                "Detalhe de postura, ração e sanidade",
                "Histórico de produção de ovos"));

        adicionar(modulos, modulo("Criações", "Suínos", "/criacoes/suinos", "suinos", "suinos.css",
                "domain-suinos", "fa-bacon", Status.PLANEJADO,
                "Controle de lotes suínos, reprodução, alimentação, pesagem e saída.",
                "Listagem por lote, fase e baia",
                "Cadastro de animais, matrizes e lotes",
                "Detalhe com pesagens, consumo e sanidade",
                "Histórico reprodutivo e produtivo"));

        adicionar(modulos, modulo("Criações", "Piscicultura", "/criacoes/piscicultura", "piscicultura",
                "piscicultura.css", "domain-piscicultura", "fa-fish", Status.PLANEJADO,
                "Gestão dos tanques, biometria, arraçoamento, qualidade da água e despescas.",
                "Listagem de tanques e lotes",
                "Cadastro de povoamento e manejo",
                "Detalhe com biometria, mortalidade e alimentação",
                "Histórico de despesca e produtividade"));

        adicionar(modulos, modulo("Agricultura", "Áreas/Talhões", "/agricultura/areas-talhoes", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-map-location-dot", Status.PLANEJADO,
                "Cadastro visual das áreas produtivas, talhões, uso atual e características do solo.",
                "Listagem de áreas e talhões",
                "Cadastro de área produtiva",
                "Detalhe com tamanho, localização e uso",
                "Histórico de ocupação e manejo"));

        adicionar(modulos, modulo("Agricultura", "Culturas", "/agricultura/culturas", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-seedling", Status.PLANEJADO,
                "Catálogo de culturas, variedades, ciclos esperados e parâmetros de manejo.",
                "Listagem de culturas cadastradas",
                "Cadastro de cultura e variedade",
                "Detalhe de ciclo, espaçamento e exigências",
                "Histórico de uso por talhão"));

        adicionar(modulos, modulo("Agricultura", "Plantios", "/agricultura/plantios", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-wheat-awn", Status.PLANEJADO,
                "Planejamento e acompanhamento dos plantios por talhão, cultura, data e estágio.",
                "Listagem de plantios ativos",
                "Cadastro de plantio",
                "Detalhe com área, cultura, estágio e custos",
                "Histórico de safras"));

        adicionar(modulos, modulo("Agricultura", "Adubação", "/agricultura/adubacao", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-flask", Status.PLANEJADO,
                "Registro de adubações, insumos aplicados, dose, área e custo por aplicação.",
                "Listagem de aplicações",
                "Cadastro de adubação",
                "Detalhe com insumos e dose por área",
                "Histórico por cultura e talhão"));

        adicionar(modulos, modulo("Agricultura", "Irrigação", "/agricultura/irrigacao", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-droplet", Status.PLANEJADO,
                "Planejamento agrícola das irrigações por cultura, talhão e janela operacional.",
                "Agenda de irrigações por plantio",
                "Cadastro de programação",
                "Detalhe de lâmina, duração e setor",
                "Histórico hídrico por cultura"));

        adicionar(modulos, modulo("Agricultura", "Tratamentos", "/agricultura/tratamentos", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-spray-can-sparkles", Status.PLANEJADO,
                "Aplicação de defensivos, controle fitossanitário e acompanhamento de carências.",
                "Listagem de tratamentos",
                "Cadastro de tratamento",
                "Detalhe com produto, alvo e carência",
                "Histórico fitossanitário"));

        adicionar(modulos, modulo("Agricultura", "Colheitas", "/agricultura/colheitas", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-basket-shopping", Status.PLANEJADO,
                "Controle de colheitas, rendimento, destino, perdas e vínculo com estoque.",
                "Listagem de colheitas",
                "Cadastro de colheita",
                "Detalhe com produção, perdas e destino",
                "Histórico de produtividade"));

        adicionar(modulos, modulo("Água", "Reservatórios", "/agua/reservatorios", "agua", "agua.css",
                "domain-agua", "fa-water", Status.PLANEJADO,
                "Controle de caixas, tanques, reservatórios, níveis e capacidade disponível.",
                "Listagem de reservatórios",
                "Cadastro de reservatório",
                "Detalhe com capacidade, nível e origem",
                "Histórico de medições"));

        adicionar(modulos, modulo("Água", "Bombas", "/agua/bombas", "agua", "agua.css",
                "domain-agua", "fa-gears", Status.PLANEJADO,
                "Cadastro e acompanhamento de bombas, vazão, energia, manutenções e operação.",
                "Listagem de bombas e status",
                "Cadastro de bomba",
                "Detalhe com potência, vazão e local",
                "Histórico de operação e manutenção"));

        adicionar(modulos, modulo("Água", "Irrigação", "/agua/irrigacao", "agua", "agua.css",
                "domain-agua", "fa-droplet", Status.PLANEJADO,
                "Operação hidráulica dos setores de irrigação, turnos, bombas e registros.",
                "Painel de setores e turnos",
                "Cadastro de setor irrigado",
                "Detalhe com bomba, vazão e reservatório",
                "Histórico de irrigações executadas"));

        adicionar(modulos, modulo("Água", "Registros", "/agua/registros", "agua", "agua.css",
                "domain-agua", "fa-clipboard-list", Status.PLANEJADO,
                "Leituras operacionais de água: níveis, consumo, pressão e observações de campo.",
                "Listagem de leituras",
                "Cadastro de registro",
                "Detalhe da medição",
                "Histórico por reservatório e setor"));

        adicionar(modulos, modulo("Água", "Manutenções", "/agua/manutencoes", "agua", "agua.css",
                "domain-agua", "fa-screwdriver-wrench", Status.PLANEJADO,
                "Planejamento de reparos em bombas, tubulações, registros, filtros e reservatórios.",
                "Listagem de manutenções",
                "Cadastro de manutenção",
                "Detalhe com peças, custo e responsável",
                "Histórico técnico por equipamento"));

        adicionar(modulos, modulo("Propriedade", "Casa", "/propriedade/casa", "casa", "casa.css",
                "domain-casa", "fa-house", Status.PLANEJADO,
                "Mapa de ambientes da casa, rotinas, itens essenciais e ocorrências domésticas.",
                "Listagem de ambientes e cuidados",
                "Cadastro de ambiente ou item da casa",
                "Detalhe com responsáveis, periodicidade e observações",
                "Histórico de ocorrências"));

        adicionar(modulos, modulo("Propriedade", "Despensa", "/propriedade/despensa", "casa", "casa.css",
                "domain-casa", "fa-jar", Status.PLANEJADO,
                "Controle visual de alimentos, produtos de limpeza e itens de consumo doméstico.",
                "Listagem de itens da despensa",
                "Cadastro de item doméstico",
                "Detalhe com validade, quantidade e local",
                "Histórico de consumo e reposição"));

        adicionar(modulos, modulo("Propriedade", "Manutenção", "/propriedade/manutencao", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-screwdriver-wrench", Status.PLANEJADO,
                "Gestão das manutenções prediais, chamados, prioridades, custos e responsáveis.",
                "Listagem de demandas abertas",
                "Cadastro de manutenção",
                "Detalhe com orçamento, peças e fotos",
                "Histórico de reparos"));

        adicionar(modulos, modulo("Propriedade", "Ar-condicionado", "/propriedade/ar-condicionado", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-wind", Status.PLANEJADO,
                "Controle de equipamentos de climatização, limpeza, gás, filtros e revisões.",
                "Listagem de aparelhos",
                "Cadastro de ar-condicionado",
                "Detalhe com BTUs, ambiente e próxima revisão",
                "Histórico de limpezas e reparos"));

        adicionar(modulos, modulo("Propriedade", "Dedetização", "/propriedade/dedetizacao", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-shield-halved", Status.PLANEJADO,
                "Agenda de dedetizações, áreas atendidas, produtos aplicados e validade.",
                "Listagem de aplicações",
                "Cadastro de dedetização",
                "Detalhe com área, produto e validade",
                "Histórico preventivo"));

        adicionar(modulos, modulo("Propriedade", "Reformas", "/propriedade/reformas", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-person-digging", Status.PLANEJADO,
                "Acompanhamento de pequenas obras, materiais, orçamento, etapas e conclusão.",
                "Listagem de reformas",
                "Cadastro de reforma",
                "Detalhe com etapas, custos e responsáveis",
                "Histórico de obras"));

        adicionar(modulos, modulo("Propriedade", "Deteriorações", "/propriedade/deterioracoes", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-triangle-exclamation", Status.PLANEJADO,
                "Registro de problemas estruturais, infiltrações, desgaste, avarias e riscos.",
                "Listagem de deteriorações",
                "Cadastro de ocorrência",
                "Detalhe com gravidade, fotos e plano de ação",
                "Histórico de correções"));

        adicionar(modulos, modulo("Propriedade", "Patrimônio", "/propriedade/patrimonio", "patrimonio",
                "patrimonio.css", "domain-patrimonio", "fa-box-archive", Status.PLANEJADO,
                "Inventário de bens, equipamentos, localização, valor, estado e manutenção.",
                "Listagem patrimonial",
                "Cadastro de bem",
                "Detalhe com valor, localização e condição",
                "Histórico de movimentações"));

        adicionar(modulos, modulo("Propriedade", "Segurança/Câmeras", "/propriedade/seguranca-cameras",
                "seguranca", "seguranca.css", "domain-seguranca", "fa-video", Status.PLANEJADO,
                "Mapa de câmeras, sensores, pontos críticos, ocorrências e revisões de segurança.",
                "Listagem de pontos de segurança",
                "Cadastro de câmera ou equipamento",
                "Detalhe com local, cobertura e status",
                "Histórico de ocorrências e revisões"));

        adicionar(modulos, moduloComAcoes("Veículos", "Frota", "/sitio/frota", "frota", "frota.css",
                "domain-frota", "fa-truck-pickup", Status.FUNCIONAL,
                "Gestão dos veículos cadastrados, consulta FIPE e documentação operacional.",
                links(
                        link("Listagem funcional", "/sitio/frota", "fa-list"),
                        link("Novo veículo funcional", "/sitio/frota/novo", "fa-plus")
                ),
                List.of(
                        acao("detalhe", "Detalhe planejado", "/sitio/frota/detalhe",
                                "Tela futura para consultar documento, FIPE, manutenção e custos do veículo."),
                        acao("historico", "Histórico planejado", "/sitio/frota/historico",
                                "Tela futura para reunir abastecimentos, consultas FIPE e manutenções.")
                ),
                "Detalhe operacional de cada veículo",
                "Histórico de FIPE, abastecimentos e custos",
                "Alertas de documento, quilometragem e manutenção",
                "Resumo de custo por veículo"));

        adicionar(modulos, moduloComAcoes("Veículos", "Abastecimentos", "/sitio/abastecimento", "abastecimento",
                "abastecimento.css", "domain-abastecimento", "fa-gas-pump", Status.EM_DESENVOLVIMENTO,
                "Registro e acompanhamento dos abastecimentos vinculados à frota.",
                links(
                        link("Novo abastecimento funcional", "/sitio/abastecimento/novo", "fa-plus"),
                        link("Frota funcional", "/sitio/frota", "fa-truck-pickup")
                ),
                List.of(
                        acao("listagem", "Listagem planejada", "/sitio/abastecimento",
                                "Tela futura para consultar abastecimentos por veículo, período e local."),
                        acao("detalhe", "Detalhe planejado", "/sitio/abastecimento/detalhe",
                                "Tela futura para revisar litros, valor, hodômetro e média."),
                        acao("historico", "Histórico planejado", "/sitio/abastecimento/historico",
                                "Tela futura para analisar consumo e custos recorrentes.")
                ),
                "Listagem dos abastecimentos registrados",
                "Detalhe de abastecimento com médias e custo",
                "Histórico por veículo e período",
                "Indicadores de consumo"));

        adicionar(modulos, modulo("Administração", "Usuários", "/administracao/usuarios", "usuarios", "global.css",
                "domain-admin", "fa-users", Status.PLANEJADO,
                "Controle de usuários, perfis, permissões e responsáveis operacionais.",
                "Listagem de usuários",
                "Cadastro de usuário",
                "Detalhe com perfil e permissões",
                "Histórico de acessos e alterações"));

        adicionar(modulos, modulo("Administração", "Configurações", "/administracao/configuracoes",
                "configuracoes", "global.css", "domain-admin", "fa-gears", Status.PRECISA_REVISAO,
                "Central futura de parametrizações gerais do sistema e módulos administrativos.",
                links(
                        link("Categorias atuais", "/sitio/configuracoes", "fa-tags"),
                        link("Roadmap", "/configuracoes/roadmap", "fa-map")
                ),
                "Parâmetros gerais do sistema",
                "Organização das configurações existentes",
                "Ativação visual de módulos",
                "Revisão das categorias atuais"));

        adicionar(modulos, modulo("Administração", "Centros de custo", "/administracao/centros-custo",
                "centros-custo", "global.css", "domain-admin", "fa-coins", Status.PLANEJADO,
                "Estrutura de centros de custo para compras, produção, frota e propriedade.",
                "Listagem de centros de custo",
                "Cadastro de centro de custo",
                "Detalhe com vínculos por módulo",
                "Histórico de custos associados"));

        adicionar(modulos, modulo("Administração", "Unidades de medida", "/administracao/unidades-medida",
                "unidades-medida", "global.css", "domain-admin", "fa-ruler-combined", Status.PLANEJADO,
                "Padronização de unidades usadas em estoque, agricultura, água e criações.",
                "Listagem de unidades",
                "Cadastro de unidade de medida",
                "Detalhe com abreviação e tipo",
                "Histórico de uso"));

        adicionar(modulos, modulo("Administração", "Dados da propriedade", "/administracao/dados-propriedade",
                "dados-propriedade", "global.css", "domain-admin", "fa-location-dot", Status.PLANEJADO,
                "Dados cadastrais do sítio, localização, áreas, documentos e responsáveis.",
                "Resumo cadastral da propriedade",
                "Cadastro de dados gerais",
                "Detalhe com endereço, documentos e contatos",
                "Histórico de alterações cadastrais"));

        return modulos;
    }

    private static void adicionar(Map<String, ModuloPlanejado> modulos, ModuloPlanejado modulo) {
        modulos.put(modulo.basePath(), modulo);
    }

    private static ModuloPlanejado modulo(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, Status roadmapStatus, String descricao,
            String... funcionalidades) {
        return modulo(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus, descricao,
                List.of(), funcionalidades);
    }

    private static ModuloPlanejado modulo(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, Status roadmapStatus, String descricao,
            List<LinkAtalho> atalhos, String... funcionalidades) {
        return moduloComAcoes(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus, descricao,
                atalhos, acoesPadrao(basePath, titulo), funcionalidades);
    }

    private static ModuloPlanejado moduloComAcoes(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, Status roadmapStatus, String descricao,
            List<LinkAtalho> atalhos, List<AcaoModulo> acoes, String... funcionalidades) {
        return new ModuloPlanejado(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus,
                descricao, List.of(funcionalidades), atalhos, acoes);
    }

    private static List<AcaoModulo> acoesPadrao(String basePath, String titulo) {
        return List.of(
                acao("listagem", "Visão geral", basePath,
                        "Tela inicial planejada para consultar e filtrar " + titulo.toLowerCase() + "."),
                acao("cadastro", "Cadastro planejado", basePath + "/novo",
                        "Formulário visual planejado para cadastrar registros de " + titulo.toLowerCase() + "."),
                acao("detalhe", "Detalhe planejado", basePath + "/detalhe",
                        "Tela planejada para visualizar um registro de " + titulo.toLowerCase() + " em profundidade."),
                acao("historico", "Histórico planejado", basePath + "/historico",
                        "Linha do tempo planejada para acompanhar mudanças e eventos de " + titulo.toLowerCase() + ".")
        );
    }

    private static AcaoModulo acao(String chave, String titulo, String rota, String descricao) {
        String icon = switch (chave) {
            case "cadastro" -> "fa-plus";
            case "detalhe" -> "fa-circle-info";
            case "historico" -> "fa-clock-rotate-left";
            default -> "fa-table-list";
        };
        return new AcaoModulo(chave, titulo, rota, descricao, icon);
    }

    private static List<LinkAtalho> links(LinkAtalho... links) {
        return List.of(links);
    }

    private static LinkAtalho link(String rotulo, String rota, String icon) {
        return new LinkAtalho(rotulo, rota, icon);
    }

    private static List<RoadmapItem> criarRoadmap() {
        return List.of(
                new RoadmapItem("Início", "Dashboard", "/sitio/painel", Status.FUNCIONAL,
                        "Painel atual de produção, categorias e alertas."),
                item("/gestao/estoque"),
                item("/gestao/compras"),
                item("/gestao/tarefas"),
                item("/criacoes/aves"),
                item("/criacoes/aves/chocadeira"),
                item("/criacoes/aves/pinteiro"),
                item("/criacoes/aves/galinheiro"),
                item("/criacoes/suinos"),
                item("/criacoes/piscicultura"),
                item("/agricultura/areas-talhoes"),
                item("/agricultura/culturas"),
                item("/agricultura/plantios"),
                item("/agricultura/adubacao"),
                item("/agricultura/irrigacao"),
                item("/agricultura/tratamentos"),
                item("/agricultura/colheitas"),
                item("/agua/reservatorios"),
                item("/agua/bombas"),
                item("/agua/irrigacao"),
                item("/agua/registros"),
                item("/agua/manutencoes"),
                item("/propriedade/casa"),
                item("/propriedade/despensa"),
                item("/propriedade/manutencao"),
                item("/propriedade/ar-condicionado"),
                item("/propriedade/dedetizacao"),
                item("/propriedade/reformas"),
                item("/propriedade/deterioracoes"),
                item("/propriedade/patrimonio"),
                item("/propriedade/seguranca-cameras"),
                item("/sitio/frota"),
                item("/sitio/abastecimento"),
                item("/administracao/usuarios"),
                new RoadmapItem("Administração", "Configurações atuais", "/sitio/configuracoes", Status.FUNCIONAL,
                        "Categorias de domínio já existentes."),
                item("/administracao/configuracoes"),
                item("/administracao/centros-custo"),
                item("/administracao/unidades-medida"),
                item("/administracao/dados-propriedade")
        );
    }

    private static RoadmapItem item(String basePath) {
        ModuloPlanejado modulo = MODULOS.get(basePath);
        if (modulo == null) {
            throw new IllegalArgumentException("Módulo não encontrado no roadmap: " + basePath);
        }
        return new RoadmapItem(modulo.grupo(), modulo.titulo(), modulo.basePath(), modulo.roadmapStatus(),
                modulo.descricao());
    }

    public enum Status {
        FUNCIONAL("FUNCIONAL", "status-funcional"),
        EM_DESENVOLVIMENTO("EM DESENVOLVIMENTO", "status-em-desenvolvimento"),
        PLANEJADO("PLANEJADO", "status-planejado"),
        PRECISA_REVISAO("PRECISA REVISÃO", "status-precisa-revisao");

        private final String rotulo;
        private final String classe;

        Status(String rotulo, String classe) {
            this.rotulo = rotulo;
            this.classe = classe;
        }

        public String getRotulo() {
            return rotulo;
        }

        public String getClasse() {
            return classe;
        }
    }

    public record ModuloPlanejado(
            String grupo,
            String titulo,
            String basePath,
            String active,
            String cssFile,
            String domainClass,
            String icon,
            Status roadmapStatus,
            String descricao,
            List<String> funcionalidades,
            List<LinkAtalho> atalhos,
            List<AcaoModulo> acoes) {
    }

    public record PaginaPlanejada(ModuloPlanejado modulo, AcaoModulo acao) {
    }

    public record AcaoModulo(String chave, String titulo, String rota, String descricao, String icon) {
    }

    public record LinkAtalho(String rotulo, String rota, String icon) {
    }

    public record Breadcrumb(String rotulo, String rota, boolean ativo) {
    }

    public record RoadmapItem(String grupo, String titulo, String rota, Status status, String descricao) {
    }
}
