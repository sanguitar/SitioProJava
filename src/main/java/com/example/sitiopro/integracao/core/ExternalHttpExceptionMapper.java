package com.example.sitiopro.integracao.core;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class ExternalHttpExceptionMapper {

    private ExternalHttpExceptionMapper() {
    }

    public static IntegracaoHttpException mapear(RuntimeException exception) {
        if (exception instanceof IntegracaoHttpException integracaoHttpException) {
            return integracaoHttpException;
        }
        if (exception instanceof HttpStatusCodeException statusException) {
            int status = statusException.getStatusCode().value();
            if (status == 429) {
                Long retryAfter = retryAfterSeconds(statusException.getResponseHeaders());
                return new IntegracaoHttpException(
                        "API_RATE_LIMIT", "O provedor limitou temporariamente as chamadas.", status,
                        IntegracaoHttpException.Tipo.RATE_LIMIT, retryAfter, statusException);
            }
            if (status >= 500) {
                return new IntegracaoHttpException(
                        "API_HTTP_5XX", "O provedor retornou uma falha temporária.", status,
                        IntegracaoHttpException.Tipo.TRANSIENTE, null, statusException);
            }
            String codigo = switch (status) {
                case 400 -> "API_REQUISICAO_INVALIDA";
                case 401, 403 -> "API_AUTENTICACAO_RECUSADA";
                case 404 -> "API_RECURSO_NAO_ENCONTRADO";
                default -> "API_HTTP_4XX";
            };
            return new IntegracaoHttpException(
                    codigo, "O provedor recusou a requisição.", status,
                    IntegracaoHttpException.Tipo.PERMANENTE, null, statusException);
        }
        if (exception instanceof ResourceAccessException) {
            boolean timeout = possuiCausa(exception, HttpTimeoutException.class)
                    || possuiCausa(exception, java.net.SocketTimeoutException.class);
            return new IntegracaoHttpException(
                    timeout ? "API_TIMEOUT" : "API_CONEXAO",
                    timeout ? "Tempo limite da API externa excedido." : "Não foi possível conectar à API externa.",
                    null,
                    timeout ? IntegracaoHttpException.Tipo.TIMEOUT : IntegracaoHttpException.Tipo.TRANSIENTE,
                    null,
                    exception);
        }
        if (exception instanceof RestClientException) {
            return new IntegracaoHttpException(
                    "API_PAYLOAD_INVALIDO", "A resposta do provedor não pôde ser interpretada.", null,
                    IntegracaoHttpException.Tipo.PERMANENTE, null, exception);
        }
        return new IntegracaoHttpException(
                "API_FALHA_INESPERADA", "Falha inesperada ao consultar a API externa.", null,
                IntegracaoHttpException.Tipo.PERMANENTE, null, exception);
    }

    private static Long retryAfterSeconds(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String valor = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Math.max(0, Long.parseLong(valor.trim()));
        } catch (NumberFormatException ignored) {
            try {
                ZonedDateTime data = ZonedDateTime.parse(valor.trim(), DateTimeFormatter.RFC_1123_DATE_TIME);
                return Math.max(0, Duration.between(ZonedDateTime.now(data.getZone()), data).toSeconds());
            } catch (RuntimeException invalidDate) {
                return null;
            }
        }
    }

    private static boolean possuiCausa(Throwable throwable, Class<? extends Throwable> tipo) {
        Throwable atual = throwable;
        while (atual != null) {
            if (tipo.isInstance(atual)) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }
}
