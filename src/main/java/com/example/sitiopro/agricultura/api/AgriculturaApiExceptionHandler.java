package com.example.sitiopro.agricultura.api;
import com.example.sitiopro.shared.api.ApiErrorResponse;
import com.example.sitiopro.shared.observability.RequestCorrelation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AgriculturaApiController.class)
public class AgriculturaApiExceptionHandler {
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorResponse> conflito(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(409).body(new ApiErrorResponse(Instant.now(), 409, "CONFLITO_DE_DADOS",
                "Registro alterado ou vinculo invalido. Recarregue os dados.",
                request.getRequestURI(), RequestCorrelation.currentRequestId()));
    }
}
