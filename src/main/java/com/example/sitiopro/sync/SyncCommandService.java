package com.example.sitiopro.sync;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.agua.dto.OperacaoAguaRequest;
import com.example.sitiopro.agua.entity.OperacaoAgua;
import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import com.example.sitiopro.agua.repository.OperacaoAguaRepository;
import com.example.sitiopro.agua.service.AguaService;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class SyncCommandService {
    private static final String ROTA = "/api/v1/sync";

    private final ApiIdempotencyRepository idempotencias;
    private final UsuarioRepository usuarios;
    private final TarefaRepository tarefas;
    private final OperacaoAguaRepository operacoesAgua;
    private final TarefaService tarefaService;
    private final AguaService aguaService;
    private final CodigoCriacaoService locks;
    private final SyncSnapshotMapper snapshots;
    private final ConfiguracaoOperacionalService configuracao;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Clock clock;

    public SyncCommandService(ApiIdempotencyRepository idempotencias, UsuarioRepository usuarios,
            TarefaRepository tarefas, OperacaoAguaRepository operacoesAgua,
            TarefaService tarefaService, AguaService aguaService, CodigoCriacaoService locks,
            SyncSnapshotMapper snapshots, ConfiguracaoOperacionalService configuracao,
            ObjectMapper objectMapper, Validator validator, Clock clock) {
        this.idempotencias = idempotencias;
        this.usuarios = usuarios;
        this.tarefas = tarefas;
        this.operacoesAgua = operacoesAgua;
        this.tarefaService = tarefaService;
        this.aguaService = aguaService;
        this.locks = locks;
        this.snapshots = snapshots;
        this.configuracao = configuracao;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncDtos.StoredOutcome processar(UUID deviceId, SyncDtos.SyncCommand command, UsuarioAtor ator) {
        exigirAtor(ator);
        if (command.operation() != SyncDtos.Operation.UPSERT) {
            throw erro("OPERACAO_SYNC_NAO_SUPORTADA", "O piloto aceita somente UPSERT.", HttpStatus.BAD_REQUEST);
        }
        locks.bloquearIdempotencia("MOBILE_SYNC_OPERATION", ator.id() + ":" + deviceId + ":" + command.operationId());
        String requestHash = hash(command);
        ApiIdempotency existente = idempotencias
                .findByUsuarioIdAndDeviceIdAndOperationId(ator.id(), deviceId, command.operationId())
                .orElse(null);
        if (existente != null) {
            if (!existente.getRequestHash().equals(requestHash)) {
                throw erro("OPERATION_ID_REUTILIZADA",
                        "A operationId já foi utilizada com outro conteúdo.", HttpStatus.CONFLICT);
            }
            return lerOutcome(existente.getResponseJson());
        }

        locks.bloquearIdempotencia("MOBILE_SYNC_ENTITY", command.entityType() + ":" + command.entityId());
        SyncDtos.StoredOutcome outcome = switch (command.entityType()) {
            case TAREFA -> processarTarefa(command, ator);
            case AGUA_MEDICAO -> processarMedicao(command, ator);
        };
        Usuario usuario = usuarios.findById(ator.id())
                .orElseThrow(() -> erro("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.", HttpStatus.UNAUTHORIZED));
        Instant agora = clock.instant();
        int status = outcome.conflict() == null ? HttpStatus.OK.value() : HttpStatus.CONFLICT.value();
        idempotencias.save(new ApiIdempotency(command.operationId(), deviceId, usuario,
                command.entityType().name() + ":" + command.operation().name(), ROTA,
                requestHash, status, escrever(outcome), agora, agora.plusSeconds(30L * 24 * 60 * 60)));
        return outcome;
    }

    private SyncDtos.StoredOutcome processarTarefa(SyncDtos.SyncCommand command, UsuarioAtor ator) {
        SyncDtos.TarefaPayload payload = converter(command.payload(), SyncDtos.TarefaPayload.class);
        validar(payload);
        Tarefa existente = tarefas.buscarPorPublicIdParaAtualizacao(command.entityId()).orElse(null);
        if (existente != null && !versaoConfere(command.baseVersion(), existente.getVersao())) {
            return conflito(command, existente.getVersao());
        }
        if (existente == null && command.baseVersion() != null && command.baseVersion() != 0) {
            throw erro("VERSAO_CRIACAO_INVALIDA", "Uma criação offline não deve informar versão existente.",
                    HttpStatus.CONFLICT);
        }

        TarefaRequest request = tarefaRequest(payload);
        Long id;
        if (existente == null) {
            id = tarefaService.criarOffline(request, command.entityId(), ator).id();
        } else {
            id = existente.getId();
            tarefaService.atualizar(id, request, ator);
        }
        aplicarStatus(id, payload.status(), existente == null ? StatusTarefa.PENDENTE : existente.getStatus(), ator);
        tarefas.flush();
        SyncSnapshot snapshot = snapshots.snapshot(SyncDtos.EntityType.TAREFA, command.entityId());
        return SyncDtos.StoredOutcome.result(new SyncDtos.CommandResult(command.operationId(), command.entityType(),
                command.entityId(), SyncDtos.ResultStatus.APPLIED, snapshot.version(), snapshot.payload()));
    }

    private SyncDtos.StoredOutcome processarMedicao(SyncDtos.SyncCommand command, UsuarioAtor ator) {
        SyncDtos.AguaMedicaoPayload payload = converter(command.payload(), SyncDtos.AguaMedicaoPayload.class);
        validar(payload);
        OperacaoAgua existente = operacoesAgua.buscarPorPublicIdParaAtualizacao(command.entityId()).orElse(null);
        if (existente != null) {
            return conflito(command, existente.getVersao());
        }
        if (command.baseVersion() != null && command.baseVersion() != 0) {
            throw erro("VERSAO_CRIACAO_INVALIDA", "Uma medição nova não deve informar versão existente.",
                    HttpStatus.CONFLICT);
        }
        OperacaoAguaRequest request = new OperacaoAguaRequest();
        request.setRecursoOrigemId(payload.recursoOrigemId());
        request.setTipo(TipoOperacaoAgua.MEDICAO_NIVEL);
        request.setDataOperacao(LocalDateTime.ofInstant(payload.dataOperacao(), configuracao.obter().zoneId()));
        request.setQuantidadeLitros(payload.quantidadeLitros());
        request.setObservacao(payload.observacao());
        request.setChaveIdempotencia(command.operationId().toString());
        aguaService.registrarOffline(request, command.entityId(), ator);
        operacoesAgua.flush();
        SyncSnapshot snapshot = snapshots.snapshot(SyncDtos.EntityType.AGUA_MEDICAO, command.entityId());
        return SyncDtos.StoredOutcome.result(new SyncDtos.CommandResult(command.operationId(), command.entityType(),
                command.entityId(), SyncDtos.ResultStatus.APPLIED, snapshot.version(), snapshot.payload()));
    }

    private SyncDtos.StoredOutcome conflito(SyncDtos.SyncCommand command, long versaoAtual) {
        SyncSnapshot snapshot = snapshots.snapshot(command.entityType(), command.entityId());
        return SyncDtos.StoredOutcome.conflict(new SyncDtos.Conflict(command.operationId(), command.entityType(),
                command.entityId(), command.baseVersion(), versaoAtual, "SERVER_WINS", snapshot.payload()));
    }

    private boolean versaoConfere(Long enviada, long atual) {
        return enviada != null && enviada == atual;
    }

    private TarefaRequest tarefaRequest(SyncDtos.TarefaPayload payload) {
        TarefaRequest request = new TarefaRequest();
        request.setTitulo(payload.titulo());
        request.setDescricao(payload.descricao());
        request.setPrioridade(payload.prioridade());
        request.setDataVencimento(payload.dataVencimento() == null ? null
                : LocalDateTime.ofInstant(payload.dataVencimento(), configuracao.obter().zoneId()));
        request.setResponsavelId(payload.responsavelId());
        request.setRecorrencia(payload.recorrencia() == null ? TipoRecorrencia.NENHUMA : payload.recorrencia());
        request.setIntervaloDias(payload.intervaloDias());
        return request;
    }

    private void aplicarStatus(Long id, StatusTarefa desejado, StatusTarefa atual, UsuarioAtor ator) {
        StatusTarefa alvo = desejado == null ? atual : desejado;
        if (alvo == atual) return;
        switch (alvo) {
            case EM_ANDAMENTO -> tarefaService.iniciar(id, ator);
            case CONCLUIDA -> tarefaService.concluir(id, ator);
            case CANCELADA -> tarefaService.cancelar(id, ator);
            case PENDENTE -> throw erro("TRANSICAO_TAREFA_INVALIDA",
                    "O piloto não reabre tarefas para PENDENTE.", HttpStatus.CONFLICT);
        }
    }

    private <T> T converter(JsonNode payload, Class<T> type) {
        try {
            return objectMapper.treeToValue(payload, type);
        } catch (JsonProcessingException ex) {
            throw erro("PAYLOAD_SYNC_INVALIDO", "O payload não corresponde ao tipo informado.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validar(Object value) {
        Set<ConstraintViolation<Object>> violacoes = validator.validate(value);
        if (!violacoes.isEmpty()) {
            String mensagem = violacoes.stream().sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage()).findFirst().orElse("Payload inválido.");
            throw erro("PAYLOAD_SYNC_INVALIDO", mensagem, HttpStatus.BAD_REQUEST);
        }
    }

    private String hash(SyncDtos.SyncCommand command) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("entityType", command.entityType().name());
        node.put("entityId", command.entityId().toString());
        if (command.baseVersion() == null) node.putNull("baseVersion"); else node.put("baseVersion", command.baseVersion());
        node.put("operation", command.operation().name());
        node.set("payload", ordenar(command.payload()));
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(node);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Não foi possível calcular o hash idempotente.", ex);
        }
    }

    private JsonNode ordenar(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = objectMapper.createObjectNode();
            java.util.stream.StreamSupport.stream(((Iterable<String>) node::fieldNames).spliterator(), false)
                    .sorted().forEach(nome -> sorted.set(nome, ordenar(node.get(nome))));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            node.forEach(item -> array.add(ordenar(item)));
            return array;
        }
        return node;
    }

    private String escrever(SyncDtos.StoredOutcome outcome) {
        try { return objectMapper.writeValueAsString(outcome); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Falha ao persistir resposta idempotente.", ex); }
    }

    private SyncDtos.StoredOutcome lerOutcome(String json) {
        try { return objectMapper.readValue(json, SyncDtos.StoredOutcome.class); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Resposta idempotente inválida no banco.", ex); }
    }

    private void exigirAtor(UsuarioAtor ator) {
        if (ator.id() == null) throw erro("USUARIO_TOKEN_INVALIDO", "Token sem usuário válido.", HttpStatus.UNAUTHORIZED);
    }

    private SyncException erro(String code, String message, HttpStatus status) {
        return new SyncException(code, message, status);
    }
}
