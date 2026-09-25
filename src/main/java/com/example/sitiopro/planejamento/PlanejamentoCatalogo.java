package com.example.sitiopro.planejamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PlanejamentoCatalogo {

    public static final String APP_NAME = "Sítio Guaratinguetá";
    public static final String USUARIO_VISUAL = "Sítio Guaratinguetá";

    private static final Map<String, ModuloPlanejado> MODULOS = criarModulos();
    private static final Map<String, PaginaPlanejada> PAGINAS = criarPaginas(MODULOS.values());
    private static final List<RoadmapItem> ROADMAP = criarRoadmap();
    private static final Map<String, List<RoadmapItem>> ROADMAP_GRUPOS = agruparRoadmap(ROADMAP);
    private static final Map<String, String> REDIRECIONAMENTOS = criarRedirecionamentos();

    private PlanejamentoCatalogo() {
    }

    public static PaginaPlanejada buscarPagina(String rota) {
        return PAGINAS.get(rota);
    }

    public static String buscarRedirecionamento(String rotaAntiga) {
        return REDIRECIONAMENTOS.get(rotaAntiga);
    }

    public static Map<String, List<RoadmapItem>> roadmapPorGrupo() {
        return ROADMAP_GRUPOS;
    }

    public static StatusPlanejamento[] statusDisponiveis() {
        return StatusPlanejamento.values();
    }

    private static Map<String, PaginaPlanejada> criarPaginas(Collection<ModuloPlanejado> modulos) {
        Map<String, PaginaPlanejada> paginas = new LinkedHashMap<>();
        for (ModuloPlanejado modulo : modulos) {
            for (AcaoModulo acao : modulo.acoes()) {
                paginas.put(acao.rota(), new PaginaPlanejada(modulo, acao));
            }
        }
        return Collections.unmodifiableMap(paginas);
    }

    private static Map<String, List<RoadmapItem>> agruparRoadmap(List<RoadmapItem> roadmap) {
        Map<String, List<RoadmapItem>> grupos = new LinkedHashMap<>();
        for (RoadmapItem item : roadmap) {
            grupos.computeIfAbsent(item.grupo(), chave -> new ArrayList<>()).add(item);
        }
        grupos.replaceAll((grupo, itens) -> List.copyOf(itens));
        return Collections.unmodifiableMap(grupos);
    }

    private static Map<String, ModuloPlanejado> criarModulos() {
        Map<String, ModuloPlanejado> modulos = new LinkedHashMap<>();

        adicionar(modulos, modulo("Gestão", "Estoque", "/sitio/estoque", "estoque", "estoque.css",
                "domain-estoque", "fa-boxes-stacked", StatusPlanejamento.FUNCIONAL,
                "Controle funcional de insumos, materiais, alimentos, itens produtivos e alertas de reposição.",
                links(
                        link("Resumo do estoque", "/sitio/estoque", "fa-chart-line"),
                        link("Itens", "/sitio/estoque/itens", "fa-box"),
                        link("Nova movimentação", "/sitio/estoque/movimentacoes/nova", "fa-right-left")
                ),
                "Resumo operacional com itens críticos, vencimentos e valor estimado",
                "Cadastro de itens com categoria, unidade, estoque mínimo, lote e validade",
                "Movimentações de entrada, consumo, perda, descarte, ajuste e transferência",
                "Histórico como fonte confiável para cálculo de saldo"));

        adicionar(modulos, moduloComAcoes("Gestão", "Compras", "/sitio/compras", "compras", "compras.css",
                "domain-compras", "fa-cart-shopping", StatusPlanejamento.FUNCIONAL,
                "Compras funcionais com fornecedores, rascunhos, confirmação e entrada automática no estoque.",
                links(
                        link("Resumo de compras", "/sitio/compras", "fa-chart-line"),
                        link("Nova compra", "/sitio/compras/nova", "fa-plus"),
                        link("Fornecedores", "/sitio/compras/fornecedores", "fa-truck-field")
                ),
                List.of(
                        acao("listagem", "Listagem funcional", "/sitio/compras",
                                "Tela funcional para consultar compras por status, fornecedor e período."),
                        acao("cadastro", "Cadastro funcional", "/sitio/compras/nova",
                                "Tela funcional para criar compras em rascunho."),
                        acao("detalhe", "Detalhe funcional", "/sitio/compras/detalhe",
                                "Tela real disponível pelo identificador da compra, com itens, totais e auditoria."),
                        acao("historico", "Histórico funcional", "/sitio/compras/historico",
                                "Atalho funcional para compras confirmadas.")
                ),
                "Cadastro de fornecedores para compras",
                "Compras em rascunho com inclusão e remoção de itens",
                "Cálculo autoritativo de subtotal, frete, desconto e total",
                "Confirmação transacional com entradas reais no estoque"));

        adicionar(modulos, moduloComAcoes("Gestão", "Tarefas", "/sitio/tarefas", "tarefas", "tarefas.css",
                "domain-tarefas", "fa-list-check", StatusPlanejamento.FUNCIONAL,
                "Agenda operacional para atividades manuais e recorrentes, responsáveis, prazos e pendências.",
                links(
                        link("Nova tarefa", "/sitio/tarefas/nova", "fa-plus"),
                        link("Alertas", "/sitio/alertas", "fa-triangle-exclamation")
                ),
                List.of(
                        acao("listagem", "Listagem funcional", "/sitio/tarefas",
                                "Consulta paginada por status, prioridade, responsável e prazo."),
                        acao("cadastro", "Cadastro funcional", "/sitio/tarefas/nova",
                                "Criação de tarefas manuais e recorrentes."),
                        acao("detalhe", "Detalhe funcional", "/sitio/tarefas/detalhe",
                                "Detalhe por identificador com ações e histórico imutável."),
                        acao("historico", "Histórico funcional", "/sitio/tarefas/historico",
                                "Atalho para tarefas concluídas.")
                ),
                "Fluxo de início, conclusão e cancelamento",
                "Recorrências diárias, semanais, mensais ou por intervalo",
                "Controle por responsável e perfil",
                "Histórico operacional persistido"));

        adicionar(modulos, moduloComAcoes("Gestão", "Alertas", "/sitio/alertas", "alertas", "tarefas.css",
                "domain-tarefas", "fa-triangle-exclamation", StatusPlanejamento.FUNCIONAL,
                "Condições automáticas detectadas no estoque, nas integrações e na previsão climática.",
                links(link("Tarefas", "/sitio/tarefas", "fa-list-check")),
                List.of(
                        acao("listagem", "Listagem funcional", "/sitio/alertas",
                                "Consulta paginada por status, severidade e módulo."),
                        acao("detalhe", "Detalhe funcional", "/sitio/alertas",
                                "Detalhe por identificador com reconhecimento, resolução e vínculo com tarefa.")
                ),
                "Deduplicação determinística de condições abertas",
                "Reconhecimento e resolução auditáveis",
                "Conversão opcional de alerta em tarefa",
                "Regras para Estoque, Integrações e Clima"));

        adicionar(modulos, modulo("Criações", "Aves", "/sitio/aves", "aves", "aves.css",
                "domain-aves", "fa-egg", StatusPlanejamento.PLANEJADO,
                "Visão consolidada do ciclo de aves, do ovo ao galinheiro produtivo.",
                "Resumo de lotes por fase de criação",
                "Cadastro de lotes, linhagens e origem",
                "Detalhe com mortalidade, alimentação e transferências",
                "Histórico produtivo por lote"));

        adicionar(modulos, modulo("Criações", "Chocadeira", "/sitio/aves/chocadeira", "aves", "aves.css",
                "domain-aves", "fa-temperature-half", StatusPlanejamento.PLANEJADO,
                "Acompanhamento de chocagens, ovos incubados, viragens, temperatura e eclosão.",
                "Mapa de ciclos ativos na chocadeira",
                "Cadastro de ciclo de incubação",
                "Detalhe com lote, datas, perdas e nascimentos",
                "Histórico de taxa de eclosão"));

        adicionar(modulos, modulo("Criações", "Pinteiro", "/sitio/aves/pinteiro", "aves", "aves.css",
                "domain-aves", "fa-feather", StatusPlanejamento.PLANEJADO,
                "Controle dos pintinhos em recria inicial, aquecimento, ração e sanidade.",
                "Listagem de lotes no pinteiro",
                "Cadastro de entrada de pintinhos",
                "Detalhe de evolução, perdas e consumo",
                "Histórico de manejo por lote"));

        adicionar(modulos, modulo("Criações", "Galinheiro", "/sitio/aves/galinheiro", "aves", "aves.css",
                "domain-aves", "fa-house-chimney", StatusPlanejamento.PLANEJADO,
                "Gestão de aves adultas, postura, alimentação, coleta de ovos e produtividade.",
                "Mapa de galinheiros e lotes alojados",
                "Cadastro de baias, aves e transferências",
                "Detalhe de postura, ração e sanidade",
                "Histórico de produção de ovos"));

        adicionar(modulos, modulo("Criações", "Suínos", "/sitio/criacoes/suinos", "suinos", "suinos.css",
                "domain-suinos", "fa-bacon", StatusPlanejamento.FUNCIONAL,
                "Controle operacional de lotes, alimentação, pesagem e movimentações.",
                "Listagem por lote, fase e instalação",
                "Cadastro de lotes com código automático",
                "Detalhe com quantidade, peso e consumo",
                "Histórico de entradas, perdas e transferências"));

        adicionar(modulos, modulo("Criações", "Piscicultura", "/sitio/piscicultura", "piscicultura",
                "piscicultura.css", "domain-piscicultura", "fa-fish", StatusPlanejamento.PLANEJADO,
                "Gestão dos tanques, biometria, arraçoamento, qualidade da água e despescas.",
                "Listagem de tanques e lotes",
                "Cadastro de povoamento e manejo",
                "Detalhe com biometria, mortalidade e alimentação",
                "Histórico de despesca e produtividade"));

        adicionar(modulos, modulo("Agricultura", "Áreas/Talhões", "/sitio/agricultura/areas", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-map-location-dot", StatusPlanejamento.FUNCIONAL,
                "Talhões oficiais mantidos no cadastro físico da Propriedade.",
                "Listagem de áreas e talhões",
                "Cadastro de área produtiva",
                "Detalhe com tamanho, localização e uso",
                "Histórico de ocupação e manejo"));

        adicionar(modulos, modulo("Agricultura", "Culturas", "/sitio/agricultura/culturas", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-seedling", StatusPlanejamento.FUNCIONAL,
                "Catálogo interno de culturas e ciclos, com referência Agrofit opcional.",
                "Listagem de culturas cadastradas",
                "Cadastro e edição de culturas",
                "Ciclo estimado e nome científico",
                "Ativação e desativação"));

        adicionar(modulos, modulo("Agricultura", "Plantios", "/sitio/agricultura/plantios", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-wheat-awn", StatusPlanejamento.FUNCIONAL,
                "Planejamento e acompanhamento dos plantios por talhão, cultura, data e estágio.",
                "Listagem de plantios ativos",
                "Cadastro de plantio",
                "Origem externa ou consumo pelo serviço oficial de Estoque",
                "Histórico de safras"));

        adicionar(modulos, modulo("Agricultura", "Safras e cultivos", "/sitio/agricultura", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-seedling", StatusPlanejamento.FUNCIONAL,
                "Ciclo agrícola vinculado aos Talhões oficiais da Propriedade.",
                "Safras e cultivos com área reservada",
                "Plantios, acompanhamentos e colheitas",
                "Tarefas relacionadas e clima local",
                "Histórico auditado de campo"));

        adicionar(modulos, modulo("Agricultura", "Adubação", "/sitio/agricultura/adubacao", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-flask", StatusPlanejamento.FUNCIONAL,
                "Registro auditável de adubações executadas e consumo opcional do Estoque.",
                "Listagem de aplicações",
                "Cadastro de adubação",
                "Detalhe com insumos e área aplicada",
                "Histórico por cultura e talhão"));

        adicionar(modulos, modulo("Agricultura", "Irrigação", "/sitio/agricultura/irrigacao", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-droplet", StatusPlanejamento.FUNCIONAL,
                "Registro das irrigações executadas por cultivo, duração, volume e método.",
                "Irrigações por cultivo",
                "Cadastro de execução",
                "Detalhe de duração, volume e método",
                "Histórico hídrico por cultura"));

        adicionar(modulos, modulo("Agricultura", "Tratamentos", "/sitio/agricultura/tratamentos", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-spray-can-sparkles", StatusPlanejamento.FUNCIONAL,
                "Registro operacional de tratamentos executados, sem receituário agronômico.",
                "Listagem de tratamentos",
                "Cadastro de tratamento",
                "Detalhe com produto, finalidade observada e área",
                "Histórico fitossanitário"));

        adicionar(modulos, modulo("Agricultura", "Perdas e ocorrências", "/sitio/agricultura/ocorrencias", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-triangle-exclamation", StatusPlanejamento.FUNCIONAL,
                "Registro auditável de ocorrências operacionais e perdas parciais ou totais.",
                "Listagem por cultivo",
                "Cadastro de ocorrência",
                "Severidade e área afetada",
                "Encerramento controlado por perda total"));

        adicionar(modulos, modulo("Agricultura", "Colheitas", "/sitio/agricultura/colheitas", "agricultura",
                "agricultura.css", "domain-agricultura", "fa-basket-shopping", StatusPlanejamento.FUNCIONAL,
                "Colheitas parciais ou finais, com quantidade, unidade, classificação e perdas.",
                "Listagem de colheitas",
                "Cadastro de colheita",
                "Detalhe com produção e perdas",
                "Histórico por cultivo, com entrada opcional e idempotente no Estoque"));

        adicionar(modulos, modulo("Água", "Água / Irrigação", "/sitio/agua", "agua", "agua.css",
                "domain-agua", "fa-droplet", StatusPlanejamento.PLANEJADO,
                "Visão operacional de reservatórios, bombas, setores irrigados e registros de água.",
                "Painel de disponibilidade hídrica",
                "Cadastro operacional de estrutura hídrica",
                "Detalhe consolidado por ponto de água",
                "Histórico de operação e manutenção"));

        adicionar(modulos, modulo("Água", "Reservatórios", "/sitio/agua/reservatorios", "agua", "agua.css",
                "domain-agua", "fa-water", StatusPlanejamento.PLANEJADO,
                "Controle de caixas, tanques, reservatórios, níveis e capacidade disponível.",
                "Listagem de reservatórios",
                "Cadastro de reservatório",
                "Detalhe com capacidade, nível e origem",
                "Histórico de medições"));

        adicionar(modulos, modulo("Água", "Bombas", "/sitio/agua/bombas", "agua", "agua.css",
                "domain-agua", "fa-gears", StatusPlanejamento.PLANEJADO,
                "Cadastro e acompanhamento de bombas, vazão, energia, manutenções e operação.",
                "Listagem de bombas e status",
                "Cadastro de bomba",
                "Detalhe com potência, vazão e local",
                "Histórico de operação e manutenção"));

        adicionar(modulos, modulo("Água", "Irrigação", "/sitio/agua/irrigacao", "agua", "agua.css",
                "domain-agua", "fa-droplet", StatusPlanejamento.PLANEJADO,
                "Operação hidráulica dos setores de irrigação, turnos, bombas e registros.",
                "Painel de setores e turnos",
                "Cadastro de setor irrigado",
                "Detalhe com bomba, vazão e reservatório",
                "Histórico de irrigações executadas"));

        adicionar(modulos, modulo("Água", "Registros", "/sitio/agua/registros", "agua", "agua.css",
                "domain-agua", "fa-clipboard-list", StatusPlanejamento.PLANEJADO,
                "Leituras operacionais de água: níveis, consumo, pressão e observações de campo.",
                "Listagem de leituras",
                "Cadastro de registro",
                "Detalhe da medição",
                "Histórico por reservatório e setor"));

        adicionar(modulos, modulo("Água", "Manutenções", "/sitio/agua/manutencoes", "agua", "agua.css",
                "domain-agua", "fa-screwdriver-wrench", StatusPlanejamento.PLANEJADO,
                "Planejamento de reparos em bombas, tubulações, registros, filtros e reservatórios.",
                "Listagem de manutenções",
                "Cadastro de manutenção",
                "Detalhe com peças, custo e responsável",
                "Histórico técnico por equipamento"));

        adicionar(modulos, modulo("Propriedade", "Casa", "/sitio/casa", "casa", "casa.css",
                "domain-casa", "fa-house", StatusPlanejamento.PLANEJADO,
                "Mapa de ambientes da casa, rotinas, itens essenciais e ocorrências domésticas.",
                "Listagem de ambientes e cuidados",
                "Cadastro de ambiente ou item da casa",
                "Detalhe com responsáveis, periodicidade e observações",
                "Histórico de ocorrências"));

        adicionar(modulos, modulo("Propriedade", "Despensa", "/sitio/despensa", "casa", "casa.css",
                "domain-casa", "fa-jar", StatusPlanejamento.PLANEJADO,
                "Controle visual de alimentos, produtos de limpeza e itens de consumo doméstico.",
                "Listagem de itens da despensa",
                "Cadastro de item doméstico",
                "Detalhe com validade, quantidade e local",
                "Histórico de consumo e reposição"));

        adicionar(modulos, modulo("Propriedade", "Manutenção", "/sitio/manutencao", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-screwdriver-wrench", StatusPlanejamento.PLANEJADO,
                "Gestão das manutenções prediais, chamados, prioridades, custos e responsáveis.",
                "Listagem de demandas abertas",
                "Cadastro de manutenção",
                "Detalhe com orçamento, peças e fotos",
                "Histórico de reparos"));

        adicionar(modulos, modulo("Propriedade", "Ar-condicionado", "/sitio/ar-condicionado", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-wind", StatusPlanejamento.PLANEJADO,
                "Controle de equipamentos de climatização, limpeza, gás, filtros e revisões.",
                "Listagem de aparelhos",
                "Cadastro de ar-condicionado",
                "Detalhe com BTUs, ambiente e próxima revisão",
                "Histórico de limpezas e reparos"));

        adicionar(modulos, modulo("Propriedade", "Dedetização", "/sitio/dedetizacao", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-shield-halved", StatusPlanejamento.PLANEJADO,
                "Agenda de dedetizações, áreas atendidas, produtos aplicados e validade.",
                "Listagem de aplicações",
                "Cadastro de dedetização",
                "Detalhe com área, produto e validade",
                "Histórico preventivo"));

        adicionar(modulos, modulo("Propriedade", "Reformas", "/sitio/reformas", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-person-digging", StatusPlanejamento.PLANEJADO,
                "Acompanhamento de pequenas obras, materiais, orçamento, etapas e conclusão.",
                "Listagem de reformas",
                "Cadastro de reforma",
                "Detalhe com etapas, custos e responsáveis",
                "Histórico de obras"));

        adicionar(modulos, modulo("Propriedade", "Deteriorações", "/sitio/deterioracoes", "manutencao",
                "manutencao.css", "domain-manutencao", "fa-triangle-exclamation", StatusPlanejamento.PLANEJADO,
                "Registro de problemas estruturais, infiltrações, desgaste, avarias e riscos.",
                "Listagem de deteriorações",
                "Cadastro de ocorrência",
                "Detalhe com gravidade, fotos e plano de ação",
                "Histórico de correções"));

        adicionar(modulos, modulo("Propriedade", "Patrimônio", "/sitio/patrimonio", "patrimonio",
                "patrimonio.css", "domain-patrimonio", "fa-box-archive", StatusPlanejamento.PLANEJADO,
                "Inventário de bens, equipamentos, localização, valor, estado e manutenção.",
                "Listagem patrimonial",
                "Cadastro de bem",
                "Detalhe com valor, localização e condição",
                "Histórico de movimentações"));

        adicionar(modulos, modulo("Propriedade", "Segurança/Câmeras", "/sitio/seguranca",
                "seguranca", "seguranca.css", "domain-seguranca", "fa-video", StatusPlanejamento.PLANEJADO,
                "Mapa de câmeras, sensores, pontos críticos, ocorrências e revisões de segurança.",
                "Listagem de pontos de segurança",
                "Cadastro de câmera ou equipamento",
                "Detalhe com local, cobertura e status",
                "Histórico de ocorrências e revisões"));

        adicionar(modulos, moduloComAcoes("Veículos", "Frota", "/sitio/frota", "frota", "frota.css",
                "domain-frota", "fa-truck-pickup", StatusPlanejamento.FUNCIONAL,
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

        adicionar(modulos, moduloComAcoes("Veículos", "Abastecimentos", "/sitio/abastecimentos", "abastecimento",
                "abastecimento.css", "domain-abastecimento", "fa-gas-pump", StatusPlanejamento.EM_DESENVOLVIMENTO,
                "Registro e acompanhamento dos abastecimentos vinculados à frota.",
                links(
                        link("Novo abastecimento funcional", "/sitio/abastecimento/novo", "fa-plus"),
                        link("Frota funcional", "/sitio/frota", "fa-truck-pickup")
                ),
                List.of(
                        acao("listagem", "Listagem planejada", "/sitio/abastecimentos",
                                "Tela futura para consultar abastecimentos por veículo, período e local."),
                        acao("cadastro", "Cadastro funcional", "/sitio/abastecimentos/novo",
                                "Atalho para o formulário funcional de novo abastecimento."),
                        acao("detalhe", "Detalhe planejado", "/sitio/abastecimentos/detalhe",
                                "Tela futura para revisar litros, valor, hodômetro e média."),
                        acao("historico", "Histórico planejado", "/sitio/abastecimentos/historico",
                                "Tela futura para analisar consumo e custos recorrentes.")
                ),
                "Listagem dos abastecimentos registrados",
                "Detalhe de abastecimento com médias e custo",
                "Histórico por veículo e período",
                "Indicadores de consumo"));

        adicionar(modulos, moduloComAcoes("Administração", "Usuários", "/sitio/admin/usuarios", "usuarios", "global.css",
                "domain-admin", "fa-users", StatusPlanejamento.FUNCIONAL,
                "Controle de usuários, perfis, permissões e responsáveis operacionais.",
                links(link("Listagem funcional", "/sitio/admin/usuarios", "fa-users")),
                List.of(
                        acao("listagem", "Listagem funcional", "/sitio/admin/usuarios",
                                "Tela funcional para listar usuários, perfis e status."),
                        acao("cadastro", "Cadastro funcional", "/sitio/admin/usuarios/novo",
                                "Tela funcional para cadastrar usuários internos.")
                ),
                "Listagem de usuários",
                "Cadastro de usuário",
                "Edição de perfil e status",
                "Reset administrativo de senha"));

        adicionar(modulos, moduloComAcoes("Integrações", "Integration Foundation", "/sitio/admin/integracoes",
                "integracoes", "integracoes.css", "domain-admin", "fa-plug-circle-bolt",
                StatusPlanejamento.FUNCIONAL,
                "Fundação persistente para sincronização, histórico, concorrência, resiliência e operação de fontes externas.",
                links(link("Painel funcional", "/sitio/admin/integracoes", "fa-plug-circle-bolt")),
                List.of(acao("listagem", "Painel funcional", "/sitio/admin/integracoes",
                        "Visão operacional das fontes implementadas e planejadas.")),
                "Estado e histórico persistidos no SQL Server",
                "Prevenção de sincronização concorrente por fonte",
                "Sincronização manual protegida por ADMIN e CSRF",
                "Execução agendada e observabilidade estruturada"));

        adicionar(modulos, moduloComAcoes("Integrações", "Open-Meteo", "/sitio/admin/integracoes/open-meteo",
                "integracoes", "integracoes.css", "domain-admin", "fa-cloud-sun-rain",
                StatusPlanejamento.EM_DESENVOLVIMENTO,
                "Previsão climática horária persistida para o dashboard e APIs internas.",
                links(link("Detalhe operacional", "/sitio/admin/integracoes/open-meteo", "fa-cloud-sun-rain")),
                List.of(acao("detalhe", "Detalhe operacional", "/sitio/admin/integracoes/open-meteo",
                        "Configuração segura, estado e histórico do Open-Meteo.")),
                "Temperatura, umidade e precipitação",
                "Vento, rajadas, ET0 e umidade do solo",
                "Resumo climático local no dashboard",
                "API interna /api/v1/clima"));

        adicionar(modulos, moduloComAcoes("Integrações", "Embrapa Agrofit piloto",
                "/sitio/admin/integracoes/embrapa-agrofit", "integracoes", "integracoes.css",
                "domain-admin", "fa-seedling", StatusPlanejamento.EM_DESENVOLVIMENTO,
                "Piloto controlado do catálogo de culturas da API Agrofit v1, dependente de token oficial.",
                links(link("Detalhe operacional", "/sitio/admin/integracoes/embrapa-agrofit", "fa-seedling")),
                List.of(acao("detalhe", "Detalhe operacional", "/sitio/admin/integracoes/embrapa-agrofit",
                        "Configuração segura, estado e histórico do piloto Agrofit.")),
                "Adapter isolado para Agrofit v1",
                "Token somente por variável externa",
                "Recorte inicial do catálogo de culturas",
                "Expansão futura condicionada aos termos oficiais"));

        adicionar(modulos, modulo("Administração", "Configurações", "/sitio/admin/configuracoes",
                "configuracoes", "admin.css", "domain-admin", "fa-gears", StatusPlanejamento.FUNCIONAL,
                "Configurações operacionais tipadas e auditadas no SQL Server.",
                links(
                        link("Central Admin", "/sitio/admin", "fa-gears"),
                        link("Roadmap", "/sitio/admin/roadmap", "fa-map")
                ),
                "Nome da propriedade",
                "Localização e timezone",
                "Dias padrão de incubação de galinha",
                "Antecedência do alerta de eclosão"));

        adicionar(modulos, modulo("Administração", "Centros de custo", "/sitio/admin/centros-custo",
                "centros-custo", "global.css", "domain-admin", "fa-coins", StatusPlanejamento.PLANEJADO,
                "Estrutura de centros de custo para compras, produção, frota e propriedade.",
                "Listagem de centros de custo",
                "Cadastro de centro de custo",
                "Detalhe com vínculos por módulo",
                "Histórico de custos associados"));

        adicionar(modulos, modulo("Administração", "Unidades de medida", "/sitio/admin/unidades-medida",
                "unidades-medida", "global.css", "domain-admin", "fa-ruler-combined", StatusPlanejamento.PLANEJADO,
                "Padronização de unidades usadas em estoque, agricultura, água e criações.",
                "Listagem de unidades",
                "Cadastro de unidade de medida",
                "Detalhe com abreviação e tipo",
                "Histórico de uso"));

        adicionar(modulos, modulo("Propriedade", "Cadastro físico", "/sitio/admin/propriedade",
                "propriedade", "propriedade.css", "property-page", "fa-location-dot", StatusPlanejamento.FUNCIONAL,
                "Imóvel, áreas, talhões, piquetes, estruturas e recursos hídricos.",
                "Resumo e dados físicos do imóvel",
                "Cadastros físicos com controle de versão",
                "Vínculo opcional das instalações de Criações",
                "Auditoria de alterações"));

        return Collections.unmodifiableMap(modulos);
    }

    private static void adicionar(Map<String, ModuloPlanejado> modulos, ModuloPlanejado modulo) {
        modulos.put(modulo.basePath(), modulo);
    }

    private static ModuloPlanejado modulo(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, StatusPlanejamento roadmapStatus, String descricao,
            String... funcionalidades) {
        return modulo(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus, descricao,
                List.of(), funcionalidades);
    }

    private static ModuloPlanejado modulo(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, StatusPlanejamento roadmapStatus, String descricao,
            List<LinkAtalho> atalhos, String... funcionalidades) {
        return moduloComAcoes(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus, descricao,
                atalhos, acoesPadrao(basePath, titulo), funcionalidades);
    }

    private static ModuloPlanejado moduloComAcoes(String grupo, String titulo, String basePath, String active,
            String cssFile, String domainClass, String icon, StatusPlanejamento roadmapStatus, String descricao,
            List<LinkAtalho> atalhos, List<AcaoModulo> acoes, String... funcionalidades) {
        return new ModuloPlanejado(grupo, titulo, basePath, active, cssFile, domainClass, icon, roadmapStatus,
                descricao, List.of(funcionalidades), atalhos, acoes);
    }

    private static List<AcaoModulo> acoesPadrao(String basePath, String titulo) {
        String tituloNormalizado = titulo.toLowerCase(Locale.ROOT);
        return List.of(
                acao("listagem", "Visão geral", basePath,
                        "Tela inicial planejada para consultar e filtrar " + tituloNormalizado + "."),
                acao("cadastro", "Cadastro planejado", basePath + "/novo",
                        "Formulário visual planejado para cadastrar registros de " + tituloNormalizado + "."),
                acao("detalhe", "Detalhe planejado", basePath + "/detalhe",
                        "Tela planejada para visualizar um registro de " + tituloNormalizado + " em profundidade."),
                acao("historico", "Histórico planejado", basePath + "/historico",
                        "Linha do tempo planejada para acompanhar mudanças e eventos de " + tituloNormalizado + ".")
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
                new RoadmapItem("Início", "Dashboard", "/sitio/painel", StatusPlanejamento.FUNCIONAL,
                        "Painel atual de produção, categorias e alertas."),
                item("/sitio/estoque"),
                item("/sitio/compras"),
                item("/sitio/tarefas"),
                item("/sitio/alertas"),
                item("/sitio/aves"),
                item("/sitio/aves/chocadeira"),
                item("/sitio/aves/pinteiro"),
                item("/sitio/aves/galinheiro"),
                item("/sitio/criacoes/suinos"),
                item("/sitio/piscicultura"),
                item("/sitio/agricultura/areas"),
                item("/sitio/agricultura/culturas"),
                item("/sitio/agricultura/plantios"),
                item("/sitio/agricultura/adubacao"),
                item("/sitio/agricultura/irrigacao"),
                item("/sitio/agricultura/tratamentos"),
                item("/sitio/agricultura/ocorrencias"),
                item("/sitio/agricultura/colheitas"),
                item("/sitio/agua"),
                item("/sitio/agua/reservatorios"),
                item("/sitio/agua/bombas"),
                item("/sitio/agua/irrigacao"),
                item("/sitio/agua/registros"),
                item("/sitio/agua/manutencoes"),
                item("/sitio/casa"),
                item("/sitio/despensa"),
                item("/sitio/manutencao"),
                item("/sitio/ar-condicionado"),
                item("/sitio/dedetizacao"),
                item("/sitio/reformas"),
                item("/sitio/deterioracoes"),
                item("/sitio/patrimonio"),
                item("/sitio/seguranca"),
                item("/sitio/frota"),
                item("/sitio/abastecimentos"),
                item("/sitio/admin/usuarios"),
                new RoadmapItem("Administração", "Configurações atuais", "/sitio/configuracoes",
                        StatusPlanejamento.FUNCIONAL, "Categorias de domínio já existentes."),
                item("/sitio/admin/configuracoes"),
                item("/sitio/admin/centros-custo"),
                item("/sitio/admin/unidades-medida"),
                item("/sitio/admin/propriedade")
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

    private static Map<String, String> criarRedirecionamentos() {
        Map<String, String> redirecionamentos = new LinkedHashMap<>();

        redirecionamentos.put("/configuracoes/roadmap", "/sitio/admin/roadmap");
        redirecionamentos.put("/sitio/abastecimento", "/sitio/abastecimentos");
        redirecionamentos.put("/sitio/abastecimento/detalhe", "/sitio/abastecimentos/detalhe");
        redirecionamentos.put("/sitio/abastecimento/historico", "/sitio/abastecimentos/historico");

        redirecionarFluxo(redirecionamentos, "/gestao/estoque", "/sitio/estoque");
        redirecionarFluxo(redirecionamentos, "/gestao/compras", "/sitio/compras");
        redirecionarFluxo(redirecionamentos, "/gestao/tarefas", "/sitio/tarefas");

        redirecionarFluxo(redirecionamentos, "/criacoes/aves", "/sitio/aves");
        redirecionarFluxo(redirecionamentos, "/criacoes/aves/chocadeira", "/sitio/aves/chocadeira");
        redirecionarFluxo(redirecionamentos, "/criacoes/aves/pinteiro", "/sitio/aves/pinteiro");
        redirecionarFluxo(redirecionamentos, "/criacoes/aves/galinheiro", "/sitio/aves/galinheiro");
        redirecionarFluxo(redirecionamentos, "/criacoes/suinos", "/sitio/criacoes/suinos");
        redirecionarFluxo(redirecionamentos, "/criacoes/piscicultura", "/sitio/piscicultura");

        redirecionarFluxo(redirecionamentos, "/agricultura/areas-talhoes", "/sitio/agricultura/areas");
        redirecionarFluxo(redirecionamentos, "/agricultura/culturas", "/sitio/agricultura/culturas");
        redirecionarFluxo(redirecionamentos, "/agricultura/plantios", "/sitio/agricultura/plantios");
        redirecionarFluxo(redirecionamentos, "/agricultura/adubacao", "/sitio/agricultura/adubacao");
        redirecionarFluxo(redirecionamentos, "/agricultura/irrigacao", "/sitio/agricultura/irrigacao");
        redirecionarFluxo(redirecionamentos, "/agricultura/tratamentos", "/sitio/agricultura/tratamentos");
        redirecionarFluxo(redirecionamentos, "/agricultura/colheitas", "/sitio/agricultura/colheitas");

        redirecionarFluxo(redirecionamentos, "/agua/reservatorios", "/sitio/agua/reservatorios");
        redirecionarFluxo(redirecionamentos, "/agua/bombas", "/sitio/agua/bombas");
        redirecionarFluxo(redirecionamentos, "/agua/irrigacao", "/sitio/agua/irrigacao");
        redirecionarFluxo(redirecionamentos, "/agua/registros", "/sitio/agua/registros");
        redirecionarFluxo(redirecionamentos, "/agua/manutencoes", "/sitio/agua/manutencoes");

        redirecionarFluxo(redirecionamentos, "/propriedade/casa", "/sitio/casa");
        redirecionarFluxo(redirecionamentos, "/propriedade/despensa", "/sitio/despensa");
        redirecionarFluxo(redirecionamentos, "/propriedade/manutencao", "/sitio/manutencao");
        redirecionarFluxo(redirecionamentos, "/propriedade/ar-condicionado", "/sitio/ar-condicionado");
        redirecionarFluxo(redirecionamentos, "/propriedade/dedetizacao", "/sitio/dedetizacao");
        redirecionarFluxo(redirecionamentos, "/propriedade/reformas", "/sitio/reformas");
        redirecionarFluxo(redirecionamentos, "/propriedade/deterioracoes", "/sitio/deterioracoes");
        redirecionarFluxo(redirecionamentos, "/propriedade/patrimonio", "/sitio/patrimonio");
        redirecionarFluxo(redirecionamentos, "/propriedade/seguranca-cameras", "/sitio/seguranca");

        redirecionarFluxo(redirecionamentos, "/administracao/usuarios", "/sitio/admin/usuarios");
        redirecionarFluxo(redirecionamentos, "/administracao/configuracoes", "/sitio/admin/configuracoes");
        redirecionarFluxo(redirecionamentos, "/administracao/centros-custo", "/sitio/admin/centros-custo");
        redirecionarFluxo(redirecionamentos, "/administracao/unidades-medida", "/sitio/admin/unidades-medida");
        redirecionarFluxo(redirecionamentos, "/administracao/dados-propriedade", "/sitio/admin/propriedade");

        return Collections.unmodifiableMap(redirecionamentos);
    }

    private static void redirecionarFluxo(Map<String, String> redirecionamentos, String antigo, String novo) {
        redirecionamentos.put(antigo, novo);
        redirecionamentos.put(antigo + "/novo", novo + "/novo");
        redirecionamentos.put(antigo + "/detalhe", novo + "/detalhe");
        redirecionamentos.put(antigo + "/historico", novo + "/historico");
    }
}
